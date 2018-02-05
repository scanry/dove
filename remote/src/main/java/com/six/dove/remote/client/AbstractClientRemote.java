package com.six.dove.remote.client;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.six.dove.common.utils.ClassUtils;
import com.six.dove.common.utils.ExceptionUtils;
import com.six.dove.remote.AbstractRemote;
import com.six.dove.remote.AsyCallback;
import com.six.dove.remote.RemoteUtils;
import com.six.dove.remote.ServiceName;
import com.six.dove.remote.client.exception.RemoteSendFailedException;
import com.six.dove.remote.client.exception.RemoteTimeoutException;
import com.six.dove.remote.client.exception.RemoteUnconnectException;
import com.six.dove.remote.client.exception.RemoteUnfoundServiceException;
import com.six.dove.remote.compiler.Compiler;
import com.six.dove.remote.connection.RemoteConnection;
import com.six.dove.remote.exception.RemoteException;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.remote.protocol.RemoteResponseState;
import com.six.dove.remote.protocol.RemoteSerialize;
import com.six.dove.remote.server.exception.RemoteInvokeException;
import com.six.dove.remote.server.exception.RemoteRejectException;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe
 */
public abstract class AbstractClientRemote
		extends AbstractRemote<RemoteRequest, RemoteResponse, RemoteRequest, RemoteFuture, ClientRemoteConnection>
		implements ClientRemote {
	private static String MAC;
	private static String PID;
	static {
		MAC = getLocalMac();
		PID = getPid();
	}
	private static AtomicInteger requestIndex = new AtomicInteger(0);
	// 请求超时时间 6秒
	private long callTimeout = 6000;
	private Map<String, Object> serviceWeakHashMap;

	public AbstractClientRemote(String name, Compiler compiler, RemoteSerialize remoteSerialize, long callTimeout) {
		super(name, compiler, remoteSerialize);
		this.callTimeout = callTimeout;
		this.serviceWeakHashMap = Collections.synchronizedMap(new java.util.WeakHashMap<>());
	}

	/**
	 * 
	 * @param targetHost
	 * @param targetPort
	 * @param serviceName
	 * @return
	 */
	@Override
	public final String createRequestId(String callHost, int callPort, ServiceName serviceName) {
		long threadId = Thread.currentThread().getId();
		StringBuilder requestId = new StringBuilder();
		requestId.append(MAC).append("/");
		requestId.append(PID).append("/");
		requestId.append(threadId).append("@");
		requestId.append(callHost).append(":");
		requestId.append(callPort).append("/");
		requestId.append(serviceName.toString()).append("/");
		requestId.append(System.currentTimeMillis()).append("/");
		requestId.append(requestIndex.incrementAndGet());
		return requestId.toString();
	}

	@SuppressWarnings("unchecked")
	public <T> T getOrNewRemoteProtocolProxy(String callHost, int callPort, Class<?> clz,String version) {
		String key = serviceKey(callHost, callPort, clz);
		Object service = serviceWeakHashMap.computeIfAbsent(key, mapkey -> {
			return getOrNewRemoteProtocolProxy(callHost, callPort, clz, version,null);
		});
		return (T) service;
	}
	
	@Override
	public <T> T getOrNewRemoteProtocolProxy(String callHost, int callPort, Class<?> clz, String version,
			final AsyCallback asyCallback) {
		RemoteUtils.checkParma(callHost, callPort, clz);
		String packageName = clz.getPackage().getName();
		String proxyClassName = generateProtocolProxyClassName(clz, null);
		String fullProxyClassName = packageName + "." + proxyClassName;
		// ServiceName serviceName=ServiceName.newServiceName(clz.getCanonicalName(),
		// methodName, parmaTypes, version)
		return getCompiler().findOrCompile(fullProxyClassName,
				new Class<?>[] { ClientRemote.class, String.class, int.class, AsyCallback.class },
				new Object[] { this, callHost, callPort, asyCallback }, () -> {
					return generateProtocolProxyClassCode(clz, packageName, proxyClassName, version);
				});
	}
	
	/**
	 * rpc service key=目标host+:+目标端口+service class name
	 * 
	 * @param targetHost
	 * @param targetPort
	 * @param clz
	 * @return
	 */
	private String serviceKey(String targetHost, int targetPort, Class<?> clz) {
		String key = targetHost + ":" + targetPort + "/" + clz.getName();
		return key;
	}

	@Override
	protected void doStart() {
	}

	@Override
	public RemoteResponse execute(RemoteRequest rpcRequest) {
		RemoteFuture remoteFuture = null;
		ClientRemoteConnection clientToServerConnection = null;
		try {
			clientToServerConnection = findHealthyRpcConnection(rpcRequest);
		} catch (RemoteException remoteException) {
			//如果是异步调用的话，那么不需要抛出异常。
			if (null != rpcRequest.getAsyCallback()) {
				RemoteResponse failedRemoteResponse=new RemoteResponse(RemoteResponseState.CONNECT_FAILED);
				failedRemoteResponse.setMsg(ExceptionUtils.getExceptionMsg(remoteException));
				rpcRequest.getAsyCallback().execute(failedRemoteResponse);
			}else {
				throw remoteException;
			}
		}
		try {
			remoteFuture = clientToServerConnection.send(rpcRequest);
		} catch (Exception e) {
			clientToServerConnection.removeRemoteFuture(rpcRequest.getId());
			throw new RemoteSendFailedException(e);
		}
		if (!rpcRequest.isAsy()) {
			RemoteResponse rpcResponse = remoteFuture.getResult(getCallTimeout());
			if (null == rpcResponse) {
				clientToServerConnection.removeRemoteFuture(rpcRequest.getId());
				throw new RemoteTimeoutException(
						"execute rpcRequest[" + rpcRequest.toString() + "] timeout[" + getCallTimeout() + "]");
			} else if (rpcResponse.getStatus() == RemoteResponseState.SEND_FAILED) {
				throw new RemoteSendFailedException(rpcResponse.getMsg());
			} else if (rpcResponse.getStatus() == RemoteResponseState.UNFOUND_SERVICE) {
				throw new RemoteUnfoundServiceException(rpcResponse.getMsg());
			} else if (rpcResponse.getStatus() == RemoteResponseState.REJECT) {
				throw new RemoteRejectException(rpcResponse.getMsg());
			} else if (rpcResponse.getStatus() == RemoteResponseState.INVOKE_ERR) {
				throw new RemoteInvokeException(rpcResponse.getMsg());
			} else {
				return rpcResponse;
			}
		} else {
			return null;
		}
	}

	private ClientRemoteConnection findHealthyRpcConnection(RemoteRequest rpcRequest) {
		String callHost = rpcRequest.getCallHost();
		int callPort = rpcRequest.getCallPort();
		String id = RemoteConnection.newConnectionId(callHost, callPort);
		ClientRemoteConnection connection = getConnection(id);
		if (null == connection) {
			connection = newRpcConnection(callHost, callPort);
			addConnection(connection);
		}
		if (null != connection) {
			long startTime = System.currentTimeMillis();
			// 判断是否可用，如果不可用等待可用直到超时
			while (!connection.available()) {
				long spendTime = System.currentTimeMillis() - startTime;
				if (spendTime > getCallTimeout()) {
					try {
						connection.close();
						removeConnection(connection.getId());
					} catch (Exception e) {
					}
					throw new RemoteUnconnectException(
							"connected " + callHost + ":" + callPort + " timeout:" + spendTime);
				}
			}
		}
		return connection;
	}

	protected abstract ClientRemoteConnection newRpcConnection(String callHost, int callPort);

	@Override
	public long getCallTimeout() {
		return callTimeout;
	}

	@Override
	public String generateProtocolProxyClassName(Class<?> protocol, Method instanceMethod) {
		StringBuilder classSb = new StringBuilder();
		String instanceName = protocol.getSimpleName();
		classSb.append("RpcClientInterfaceProxy$");
		classSb.append(instanceName);
		return classSb.toString();
	}

	@Override
	public String generateProtocolProxyClassCode(Class<?> protocolClass, String packageName, String className,
			String version) {
		String interfaceName = protocolClass.getCanonicalName();
		StringBuilder clzSb = new StringBuilder();
		clzSb.append("package ").append(packageName).append(";\n");
		clzSb.append("import ").append(ClientRemote.class.getName()).append(";\n");
		clzSb.append("import ").append(RemoteRequest.class.getName()).append(";\n");
		clzSb.append("import ").append(RemoteResponse.class.getName()).append(";\n");
		clzSb.append("import ").append(ServiceName.class.getName()).append(";\n");
		clzSb.append("import ").append(AsyCallback.class.getName()).append(";\n");
		clzSb.append("import ").append(interfaceName).append(";\n\n");

		clzSb.append("public class ").append(className).append(" implements " + interfaceName + " {\n");
		clzSb.append("	private ").append(ClientRemote.class.getSimpleName()).append(" clientRemote;\n");
		clzSb.append("	private ").append(AsyCallback.class.getSimpleName()).append("    asyCallback;\n");
		clzSb.append("	private String callHost;\n");
		clzSb.append("	private int    port;\n");
		clzSb.append("	public " + className + "(").append(ClientRemote.class.getSimpleName())
				.append(" clientRemote,String callHost,int port,").append(AsyCallback.class.getSimpleName())
				.append(" asyCallback").append("){\n");
		clzSb.append("		this.clientRemote=clientRemote;\n");
		clzSb.append("		this.callHost=callHost;\n");
		clzSb.append("		this.port=port;\n");
		clzSb.append("		this.asyCallback=asyCallback;\n");
		clzSb.append("	}\n");
		Method[] methods = protocolClass.getMethods();
		String methodName = null;
		Class<?> returnType = null;
		String returnTypeCanonicalName = null;
		Parameter[] parameter = null;
		Class<?>[] throwsExceptionType = null;
		for (Method method : methods) {
			StringBuilder throwsException = new StringBuilder();
			StringBuilder args = new StringBuilder();
			StringBuilder parmaTypes = new StringBuilder();
			String invokePamasStr = "";
			methodName = method.getName();
			returnType = method.getReturnType();
			returnTypeCanonicalName = returnType.getCanonicalName();
			parameter = method.getParameters();
			if (null != parameter && parameter.length > 0) {
				String parameterTypeName = null;
				StringBuilder invokePamasSb = new StringBuilder();
				args.append("	       Object[] args=new Object[" + parameter.length + "];\n");
				parmaTypes.append("	       String[] parmaTypes=new String[]{");
				for (int i = 0, size = parameter.length; i < size; i++) {
					parameterTypeName = parameter[i].getParameterizedType().getTypeName();
					invokePamasSb.append(parameterTypeName).append(" paras" + i).append(",");
					args.append("	       args[" + i + "]=paras" + i + ";\n");
					parmaTypes.append("\"" + parameterTypeName + "\",");
				}
				args.append("	       remoteRequest.setParams(args);\n");
				invokePamasSb.deleteCharAt(invokePamasSb.length() - 1);
				invokePamasStr = invokePamasSb.toString();
				parmaTypes.deleteCharAt(parmaTypes.length() - 1);
				parmaTypes.append("};\n");
			} else {
				args.append("	       remoteRequest.setParams(null);\n");
			}
			throwsExceptionType = method.getExceptionTypes();
			if (null != throwsExceptionType && throwsExceptionType.length > 0) {
				throwsException.append("throws ");
				for (int i = 0, size = throwsExceptionType.length; i < size; i++) {
					throwsException.append(throwsExceptionType[i].getCanonicalName() + ",");
				}
				throwsException.deleteCharAt(throwsException.length() - 1);
			}
			clzSb.append("	@Override\n");
			clzSb.append("	public " + returnTypeCanonicalName + " " + methodName + "(" + invokePamasStr + ")"
					+ throwsException + "{\n");
			clzSb.append("	       RemoteRequest remoteRequest = new RemoteRequest();\n");
			clzSb.append(parmaTypes);
			clzSb.append("	       ServiceName serviceName=ServiceName.newServiceName(\"" + interfaceName + "\",\""
					+ methodName + "\",parmaTypes,\"" + version + "\");\n");
			clzSb.append("	       String requestId=clientRemote.createRequestId(callHost, port,serviceName);\n");
			clzSb.append("	       remoteRequest.setId(requestId);\n");
			clzSb.append("	       remoteRequest.setServiceName(serviceName);\n");
			clzSb.append("	       remoteRequest.setCallHost(callHost);\n");
			clzSb.append("	       remoteRequest.setCallPort(port);\n");
			clzSb.append(args);
			clzSb.append("	       remoteRequest.setAsyCallback(asyCallback);\n");
			clzSb.append("	       RemoteResponse remoteResponse = clientRemote.execute(remoteRequest);\n");
			if (ClassUtils.hasReturnType(method)) {
				clzSb.append("	       if (null == asyCallback) {\n");
				clzSb.append("	       		return (" + returnTypeCanonicalName + ")remoteResponse.getResult();\n");
				clzSb.append("	       }else{\n");
				if (returnType.isPrimitive()) {
					clzSb.append("	       		return " + parser(returnType) + ";\n");
				} else {
					clzSb.append("	       		return null;\n");
				}
				clzSb.append("	       }\n");
			}
			clzSb.append("	}\n");
		}
		clzSb.append("}\n");
		return clzSb.toString();
	}

	static Map<Class<?>, String> returnTypeCache = new HashMap<>();
	static {
		returnTypeCache.put(byte.class, "0");
		returnTypeCache.put(char.class, "0");
		returnTypeCache.put(short.class, "0");
		returnTypeCache.put(int.class, "0");
		returnTypeCache.put(long.class, "0");
		returnTypeCache.put(float.class, "0");
		returnTypeCache.put(double.class, "0");
		returnTypeCache.put(boolean.class, "true");
	}

	private static String parser(Class<?> returnType) {
		return returnTypeCache.get(returnType);
	}

	private static String getLocalMac() {
		String mac = "";
		try {
			InetAddress ia = InetAddress.getLocalHost();
			byte[] macBytes = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
			StringBuffer sb = new StringBuffer("");
			for (int i = 0; i < macBytes.length; i++) {
				int temp = macBytes[i] & 0xff;
				String str = Integer.toHexString(temp);
				if (str.length() == 1) {
					sb.append("0" + str);
				} else {
					sb.append(str);
				}
			}
			mac = sb.toString().toUpperCase();
		} catch (Exception e) {
		}
		return mac;
	}

	private static String getPid() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		String pid = name.split("@")[0];
		return pid;
	}
}
