package six.com.remote.client;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import six.com.remote.AbstractRemote;
import six.com.remote.RpcConnection;
import six.com.rpc.Compiler;
import six.com.rpc.client.AbstractClient;
import six.com.rpc.client.WrapperFuture;
import six.com.rpc.exception.RpcException;
import six.com.rpc.exception.RpcInvokeException;
import six.com.rpc.exception.RpcNotFoundServiceException;
import six.com.rpc.exception.RpcRejectServiceException;
import six.com.rpc.exception.RpcTimeoutException;
import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.protocol.RpcResponse;
import six.com.rpc.protocol.RpcResponseStatus;
import six.com.rpc.protocol.RpcSerialize;
import six.com.rpc.util.ClassUtils;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe
 */
public abstract class AbstractClientRemote
		extends AbstractRemote<RpcRequest, RpcResponse, RpcRequest, WrapperFuture, ClientRpcConnection>
		implements ClientRemote {

	// 请求超时时间 6秒
	private long callTimeout = 6000;

	public AbstractClientRemote(Compiler compiler, RpcSerialize rpcSerialize, long callTimeout) {
		super(compiler, rpcSerialize);
		this.callTimeout = callTimeout;
	}

	@Override
	public RpcResponse execute(RpcRequest rpcRequest) {
		WrapperFuture wrapperFuture = null;
		ClientRpcConnection clientToServerConnection = null;
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
			RpcResponse rpcResponse = wrapperFuture.getResult(getCallTimeout());
			if (null == rpcResponse) {
				clientToServerConnection.removeWrapperFuture(rpcRequest.getId());
				throw new RpcTimeoutException(
						"execute rpcRequest[" + rpcRequest.toString() + "] timeout[" + getCallTimeout() + "]");
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

	private ClientRpcConnection findHealthyRpcConnection(RpcRequest rpcRequest) {
		String callHost = rpcRequest.getCallHost();
		int callPort = rpcRequest.getCallPort();
		String id = RpcConnection.newConnectionId(callHost, callPort);
		ClientRpcConnection connection = getConnection(id);
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
					throw new RpcTimeoutException("connected " + callHost + ":" + callPort + " timeout:" + spendTime);
				}
			}
		}
		return connection;
	}

	protected abstract ClientRpcConnection newRpcConnection(String callHost, int callPort);

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
		String importClass = AbstractClient.class.getCanonicalName();
		StringBuilder clzSb = new StringBuilder();
		clzSb.append("package ").append(packageName).append(";\n");
		clzSb.append("import ").append(importClass).append(";\n");
		clzSb.append("import six.com.rpc.protocol.RpcRequest").append(";\n");
		clzSb.append("import six.com.rpc.protocol.RpcResponse").append(";\n");
		clzSb.append("import six.com.rpc.ServiceName").append(";\n");
		clzSb.append("import six.com.rpc.AsyCallback").append(";\n\n");
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
				args.append("	       rpcRequest.setParams(args);\n");
				invokePamasSb.deleteCharAt(invokePamasSb.length() - 1);
				invokePamasStr = invokePamasSb.toString();
				parmaTypes.deleteCharAt(parmaTypes.length() - 1);
				parmaTypes.append("};\n");
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
			clzSb.append("	@Override\n");
			clzSb.append("	public " + returnTypeCanonicalName + " " + methodName + "(" + invokePamasStr + ")"
					+ throwsException + "{\n");
			clzSb.append("	       RpcRequest rpcRequest = new RpcRequest();\n");
			clzSb.append(parmaTypes);
			clzSb.append("	       ServiceName serviceName=ServiceName.newServiceName(\"" + interfaceName + "\",\""
					+ methodName + "\",parmaTypes," + DEFAULT_SERVICE_VERSION + ");\n");
			clzSb.append(
					"	       String requestId=rpcClient.createRequestId(callHost, port,serviceName.toString());\n");
			clzSb.append("	       rpcRequest.setId(requestId);\n");
			clzSb.append("	       rpcRequest.setServiceName(serviceName);\n");
			clzSb.append("	       rpcRequest.setCallHost(callHost);\n");
			clzSb.append("	       rpcRequest.setCallPort(port);\n");
			clzSb.append(args);
			clzSb.append("	       rpcRequest.setAsyCallback(asyCallback);\n");
			clzSb.append("	       RpcResponse rpcResponse = rpcClient.execute(rpcRequest);\n");
			if (ClassUtils.hasReturnType(method)) {
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
}
