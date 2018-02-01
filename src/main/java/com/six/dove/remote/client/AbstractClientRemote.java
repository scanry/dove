package com.six.dove.remote.client;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import com.six.dove.remote.AbstractRemote;
import com.six.dove.remote.AsyCallback;
import com.six.dove.remote.RemoteConnection;
import com.six.dove.remote.ServiceName;
import com.six.dove.remote.compiler.Compiler;
import com.six.dove.remote.exception.RemoteException;
import com.six.dove.remote.exception.RemoteInvokeException;
import com.six.dove.remote.exception.RemoteUnfoundServiceException;
import com.six.dove.remote.exception.RemoteRejectException;
import com.six.dove.remote.exception.RemoteTimeoutException;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.remote.protocol.RemoteResponseConstants;
import com.six.dove.remote.protocol.RemoteResponseState;
import com.six.dove.remote.protocol.RemoteSerialize;
import com.six.dove.rpc.client.AbstractClient;
import com.six.dove.util.ClassUtils;

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

	// 请求超时时间 6秒
	private long callTimeout = 6000;

	public AbstractClientRemote(String name,Compiler compiler, RemoteSerialize remoteSerialize, long callTimeout) {
		super(name,compiler, remoteSerialize);
		this.callTimeout = callTimeout;
	}

	@Override
	public RemoteResponse execute(RemoteRequest rpcRequest) {
		RemoteFuture remoteFuture = null;
		ClientRemoteConnection clientToServerConnection = null;
		try {
			clientToServerConnection = findHealthyRpcConnection(rpcRequest);
		} catch (Exception e) {
			if (null != rpcRequest.getAsyCallback()) {
				rpcRequest.getAsyCallback().execute(RemoteResponseConstants.CONNECT_FAILED);
				return RemoteResponseConstants.CONNECT_FAILED;
			} else {
				throw new RemoteException(e);
			}
		}
		try {
			remoteFuture = clientToServerConnection.send(rpcRequest);
		} catch (Exception e) {
			clientToServerConnection.removeRemoteFuture(rpcRequest.getId());
			throw new RemoteException(e);
		}
		if (!rpcRequest.isAsy()) {
			RemoteResponse rpcResponse = remoteFuture.getResult(getCallTimeout());
			if (null == rpcResponse) {
				clientToServerConnection.removeRemoteFuture(rpcRequest.getId());
				throw new RemoteTimeoutException(
						"execute rpcRequest[" + rpcRequest.toString() + "] timeout[" + getCallTimeout() + "]");
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
					throw new RemoteTimeoutException(
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
	protected String generateProtocolProxyClassName(Class<?> protocol, Method instanceMethod) {
		StringBuilder classSb = new StringBuilder();
		String instanceName = protocol.getSimpleName();
		classSb.append("RpcClientInterfaceProxy$");
		classSb.append(instanceName);
		return classSb.toString();
	}

	protected static String buildClientInterfaceWrapperCode(Class<?> protocolClass, String packageName,
			String className) {
		String interfaceName = protocolClass.getCanonicalName();
		StringBuilder clzSb = new StringBuilder();
		clzSb.append("package ").append(packageName).append(";\n");
		clzSb.append("import ").append(AbstractClient.class.getName()).append(";\n");
		clzSb.append("import ").append(RemoteRequest.class.getName()).append(";\n");
		clzSb.append("import ").append(RemoteResponse.class.getName()).append(";\n");
		clzSb.append("import ").append(ServiceName.class.getName()).append(";\n");
		clzSb.append("import ").append(AsyCallback.class.getName()).append(";\n");
		clzSb.append("import ").append(interfaceName).append(";\n\n");

		clzSb.append("public class ").append(className).append(" implements " + interfaceName + " {\n");
		clzSb.append("	private ").append(AbstractClient.class.getSimpleName()).append(" rpcClient;\n");
		clzSb.append("	private ").append(AsyCallback.class.getSimpleName()).append("    asyCallback;\n");
		clzSb.append("	private String callHost;\n");
		clzSb.append("	private int    port;\n");
		clzSb.append("	public " + className + "(").append(AbstractClient.class.getSimpleName())
				.append(" rpcClient,String callHost,int port,").append(AsyCallback.class.getSimpleName())
				.append(" asyCallback").append("){\n");
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
					+ methodName + "\",parmaTypes," + DEFAULT_SERVICE_VERSION + ");\n");
			clzSb.append(
					"	       String requestId=rpcClient.createRequestId(callHost, port,serviceName.toString());\n");
			clzSb.append("	       remoteRequest.setId(requestId);\n");
			clzSb.append("	       remoteRequest.setServiceName(serviceName);\n");
			clzSb.append("	       remoteRequest.setCallHost(callHost);\n");
			clzSb.append("	       remoteRequest.setCallPort(port);\n");
			clzSb.append(args);
			clzSb.append("	       remoteRequest.setAsyCallback(asyCallback);\n");
			clzSb.append("	       RemoteResponse remoteResponse = rpcClient.execute(remoteRequest);\n");
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
}
