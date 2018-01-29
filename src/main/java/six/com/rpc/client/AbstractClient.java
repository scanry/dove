package six.com.rpc.client;

import six.com.rpc.RpcClient;
import six.com.rpc.common.AbstractRemote;
import six.com.rpc.common.ClientRemote;
import six.com.rpc.exception.RpcException;
import six.com.rpc.exception.RpcInvokeException;
import six.com.rpc.exception.RpcNotFoundServiceException;
import six.com.rpc.exception.RpcRejectServiceException;
import six.com.rpc.exception.RpcTimeoutException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import six.com.rpc.AsyCallback;
import six.com.rpc.Compiler;
import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.protocol.RpcResponse;
import six.com.rpc.protocol.RpcResponseStatus;
import six.com.rpc.protocol.RpcSerialize;

/**
 * @author sixliu
 * @date 2017年12月29日
 * @email 359852326@qq.com
 * @Description
 */
public abstract class AbstractClient extends AbstractRemote implements ClientRemote, RpcClient {

	/**
	 * 用来存放服务，
	 */
	private Map<String, Object> serviceWeakHashMap;
	/**
	 * 链接池
	 */
	private ConnectionPool<RpcConnection> pool;

	// 请求超时时间 6秒
	private long callTimeout = 6000;

	public AbstractClient(Compiler wrapperServiceProxyFactory, RpcSerialize rpcSerialize, long callTimeout) {
		super(wrapperServiceProxyFactory, rpcSerialize);
		this.callTimeout = callTimeout;
		this.serviceWeakHashMap = Collections.synchronizedMap(new java.util.WeakHashMap<>());
		this.pool = new ConnectionPool<>();
	}

	@Override
	public RpcResponse execute(RpcRequest rpcRequest) {
		return execute(rpcRequest, getCallTimeout());
	}

	private RpcResponse execute(RpcRequest rpcRequest, long callTimeout) {
		WrapperFuture wrapperFuture = null;
		RpcConnection clientToServerConnection = null;
		try {
			clientToServerConnection = findHealthyRpcConnection(rpcRequest);
		} catch (Exception e) {
			if (null != rpcRequest.getAsyCallback()) {
				rpcRequest.getAsyCallback().execute(RpcResponse.CONNECT_FAILED);
				return RpcResponse.CONNECT_FAILED;
			} else {
				throw new RpcException(e);
			}
		}
		try {
			wrapperFuture = clientToServerConnection.send(rpcRequest);
		} catch (Exception e) {
			clientToServerConnection.removeWrapperFuture(rpcRequest.getId());
			throw new RpcException(e);
		}
		if (!wrapperFuture.hasAsyCallback()) {
			RpcResponse rpcResponse = wrapperFuture.getResult(callTimeout);
			if (null == rpcResponse) {
				clientToServerConnection.removeWrapperFuture(rpcRequest.getId());
				throw new RpcTimeoutException(
						"execute rpcRequest[" + rpcRequest.toString() + "] timeout[" + callTimeout + "]");
			} else if (rpcResponse.getStatus() == RpcResponseStatus.UNFOUND_SERVICE) {
				throw new RpcNotFoundServiceException(rpcResponse.getMsg());
			} else if (rpcResponse.getStatus() == RpcResponseStatus.REJECT) {
				throw new RpcRejectServiceException(rpcResponse.getMsg());
			} else if (rpcResponse.getStatus() == RpcResponseStatus.INVOKE_ERR) {
				throw new RpcInvokeException(rpcResponse.getMsg());
			} else {
				return rpcResponse;
			}
		} else {
			return null;
		}
	}

	private RpcConnection findHealthyRpcConnection(RpcRequest rpcRequest) {
		String callHost = rpcRequest.getCallHost();
		int callPort = rpcRequest.getCallPort();
		String findKey = ClientToServerConnection.newConnectionKey(callHost, callPort);
		RpcConnection clientToServerConnection = pool.find(findKey);
		if (null == clientToServerConnection) {
			synchronized (pool) {
				clientToServerConnection = pool.find(findKey);
				if (null == clientToServerConnection) {
					clientToServerConnection = newRpcConnection(callHost, callPort);
					pool.put(clientToServerConnection);
				}
			}
		}
		if (null != clientToServerConnection) {
			long startTime = System.currentTimeMillis();
			// 判断是否可用，如果不可用等待可用直到超时
			while (!clientToServerConnection.available()) {
				long spendTime = System.currentTimeMillis() - startTime;
				if (spendTime > getCallTimeout()) {
					try {
						clientToServerConnection.close();
					} catch (Exception e) {
					}
					throw new RpcTimeoutException("connected " + callHost + ":" + callPort + " timeout:" + spendTime);
				}
			}
		}
		return clientToServerConnection;
	}

	protected abstract RpcConnection newRpcConnection(String callHost, int callPort);

	protected static String buildClientInterfaceWrapperClassName(Class<?> clz) {
		StringBuilder classSb = new StringBuilder();
		String instanceName = clz.getSimpleName();
		classSb.append("RpcClientInterfaceProxy$");
		classSb.append(instanceName);
		return classSb.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz, final AsyCallback asyCallback) {
		checkParma(targetHost, targetPort, clz);
		String packageName = clz.getPackage().getName();
		String className = buildClientInterfaceWrapperClassName(clz);
		String fullClassName = packageName + "." + className;
		return (T) getCompiler().findOrCompile(fullClassName,
				new Class<?>[] { AbstractClient.class, String.class, int.class, AsyCallback.class },
				new Object[] { this, targetHost, targetPort, asyCallback }, () -> {
					return buildClientInterfaceWrapperCode(clz, packageName, className);
				});
	}

	@SuppressWarnings("unchecked")
	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz) {
		checkParma(targetHost, targetPort, clz);
		String key = serviceKey(targetHost, targetPort, clz);
		Object service = serviceWeakHashMap.computeIfAbsent(key, mapkey -> {
			return lookupService(targetHost, targetPort, clz, null);
		});
		return (T) service;
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

	private void checkParma(String targetHost, int targetPort, Class<?> clz) {
		if (null == targetHost || targetHost.trim().length() == 0) {
			throw new IllegalArgumentException("this targetHost must be not blank");
		}
		if (1 > targetPort || 65535 < targetPort) {
			throw new IllegalArgumentException("this targetPort[" + targetPort + "] is illegal");
		}
		if (!clz.isInterface()) {
			throw new IllegalArgumentException("this clz[" + clz.getName() + "] is not tnterface");
		}
	}

	protected static String buildClientInterfaceWrapperCode(Class<?> protocolClass, String packageName,
			String className) {
		String interfaceName = protocolClass.getCanonicalName();
		String importClass = AbstractClient.class.getCanonicalName();
		StringBuilder clzSb = new StringBuilder();
		clzSb.append("package ").append(packageName).append(";\n");
		clzSb.append("import ").append(importClass).append(";\n");
		clzSb.append("import six.com.rpc.protocol.RpcRequest").append(";\n");
		clzSb.append("import six.com.rpc.protocol.RpcResponse").append(";\n");
		clzSb.append("import six.com.rpc.AsyCallback").append(";\n");
		clzSb.append("import ").append(interfaceName).append(";\n");
		clzSb.append("public class ").append(className).append(" implements " + interfaceName + " {\n");
		clzSb.append("	private ").append(importClass).append(" rpcClient;\n");
		clzSb.append("	private String callHost;\n");
		clzSb.append("	private int    port;\n");
		clzSb.append("	private six.com.rpc.AsyCallback asyCallback;\n");
		clzSb.append("	public " + className + "(").append(importClass)
				.append(" rpcClient,String callHost,int port,six.com.rpc.AsyCallback asyCallback").append("){\n");
		clzSb.append("		this.rpcClient=rpcClient;\n");
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
		String serviceName = null;
		for (Method method : methods) {
			StringBuilder throwsException = new StringBuilder();
			StringBuilder args = new StringBuilder();
			String invokePamasStr = "";
			methodName = method.getName();
			returnType = method.getReturnType();
			returnTypeCanonicalName = returnType.getCanonicalName();
			parameter = method.getParameters();
			if (null != parameter && parameter.length > 0) {
				String parameterTypeName = null;
				StringBuilder invokePamasSb = new StringBuilder();
				args.append("	       Object[] args=new Object[" + parameter.length + "];\n");
				for (int i = 0, size = parameter.length; i < size; i++) {
					parameterTypeName = parameter[i].getParameterizedType().getTypeName();
					invokePamasSb.append(parameterTypeName).append(" paras" + i).append(",");
					args.append("	       args[" + i + "]=paras" + i + ";\n");
				}
				args.append("	       rpcRequest.setParams(args);\n");
				invokePamasSb.deleteCharAt(invokePamasSb.length() - 1);
				invokePamasStr = invokePamasSb.toString();
			} else {
				args.append("	       rpcRequest.setParams(null);\n");
			}
			throwsExceptionType = method.getExceptionTypes();
			if (null != throwsExceptionType && throwsExceptionType.length > 0) {
				throwsException.append("throws ");
				for (int i = 0, size = throwsExceptionType.length; i < size; i++) {
					throwsException.append(throwsExceptionType[i].getCanonicalName() + ",");
				}
				throwsException.deleteCharAt(throwsException.length() - 1);
			}
			serviceName = getServiceName(interfaceName, method);
			clzSb.append("	@Override\n");
			clzSb.append("	public " + returnTypeCanonicalName + " " + methodName + "(" + invokePamasStr + ")"
					+ throwsException + "{\n");
			clzSb.append("	       RpcRequest rpcRequest = new RpcRequest();\n");
			clzSb.append(
					"	       String requestId=rpcClient.createRequestId(callHost, port,\"" + serviceName + "\");\n");
			clzSb.append("	       rpcRequest.setId(requestId);\n");
			clzSb.append("	       rpcRequest.setCommand(\"" + serviceName + "\");\n");
			clzSb.append("	       rpcRequest.setCallHost(callHost);\n");
			clzSb.append("	       rpcRequest.setCallPort(port);\n");
			clzSb.append(args);
			clzSb.append("	       rpcRequest.setAsyCallback(asyCallback);\n");
			clzSb.append("	       RpcResponse rpcResponse = rpcClient.execute(rpcRequest);\n");
			if (hasReturnType(method)) {
				clzSb.append("	       if (null == asyCallback) {\n");
				clzSb.append("	       		return (" + returnTypeCanonicalName + ")rpcResponse.getResult();\n");
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

	@Override
	public final void start() {
	}

	/**
	 * 从缓存中移除链接
	 * 
	 * @param connection
	 */
	public void removeConnection(ClientToServerConnection connection) {
		pool.remove(connection);
	}

	public long getCallTimeout() {
		return callTimeout;
	}

}
