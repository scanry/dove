package six.com.rpc.client;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import six.com.remote.RpcConnection;
import six.com.remote.client.AbstractClientRemote;
import six.com.rpc.AsyCallback;
import six.com.rpc.Compiler;
import six.com.rpc.RpcClient;

import six.com.rpc.protocol.RpcSerialize;
import six.com.rpc.util.ClassUtils;

/**
 * @author sixliu
 * @date 2017年12月29日
 * @email 359852326@qq.com
 * @Description
 */
public abstract class AbstractClient extends AbstractClientRemote implements RpcClient {

	/**
	 * 用来存放服务，
	 */
	private Map<String, Object> serviceWeakHashMap;

	public AbstractClient(Compiler wrapperServiceProxyFactory, RpcSerialize rpcSerialize, long callTimeout) {
		super(wrapperServiceProxyFactory, rpcSerialize, callTimeout);
		this.serviceWeakHashMap = Collections.synchronizedMap(new java.util.WeakHashMap<>());
	}

	@Override
	public <T> T lookupService(Class<?> clz) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T lookupService(Class<?> clz, AsyCallback callback) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz, final AsyCallback asyCallback) {
		checkParma(targetHost, targetPort, clz);
		String packageName = clz.getPackage().getName();
		String proxyClassName = buildClientInterfaceWrapperClassName(clz);
		String fullProxyClassName = packageName + "." + proxyClassName;
		//ServiceName serviceName=ServiceName.newServiceName(clz.getCanonicalName(), methodName, parmaTypes, version)
		return (T) getCompiler().findOrCompile(fullProxyClassName,
				new Class<?>[] { AbstractClient.class, String.class, int.class, AsyCallback.class },
				new Object[] { this, targetHost, targetPort, asyCallback }, () -> {
					return buildClientInterfaceWrapperCode(clz, packageName, proxyClassName);
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
		RpcConnection.checkAddress(targetHost, targetPort);
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
			StringBuilder parmaTypes=new StringBuilder();
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
					parmaTypes.append("\""+parameterTypeName+"\",");
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
			clzSb.append("	       ServiceName serviceName=ServiceName.newServiceName(\""+interfaceName+"\",\""+ methodName+"\",parmaTypes,"+DEFAULT_SERVICE_VERSION+");\n");
			clzSb.append("	       String requestId=rpcClient.createRequestId(callHost, port,serviceName.toString());\n");
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

	@Override
	public final void start() {
	}

}
