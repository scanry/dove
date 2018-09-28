package com.six.dove.remote.client;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import com.six.dove.common.utils.ClassUtils;
import com.six.dove.remote.AsyCallback;
import com.six.dove.remote.InterfaceProxyMetaBuilder;
import com.six.dove.remote.ServiceNameUtils;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;

/**
 * @author:MG01867
 * @date:2018年4月12日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class ClientProxyServiceCodeBuilderImpl implements InterfaceProxyMetaBuilder {

	@Override
	public String generateProtocolProxyClassName(Class<?> protocol, Method instanceMethod) {
		StringBuilder classSb = new StringBuilder();
		String instanceName = protocol.getSimpleName();
		classSb.append("RpcClientInterfaceProxy$");
		classSb.append(instanceName);
		return classSb.toString();
	}

	// TODO 需要设置 remoteRequest 超时时间
	@Override
	public String generateProtocolProxyClassCode(Class<?> protocolClass, String packageName, String className) {
		String interfaceName = protocolClass.getCanonicalName();
		StringBuilder clzSb = new StringBuilder();
		clzSb.append("package ").append(packageName).append(";\n");
		clzSb.append("import ").append(ClientRemote.class.getName()).append(";\n");
		clzSb.append("import ").append(RemoteRequest.class.getName()).append(";\n");
		clzSb.append("import ").append(RemoteResponse.class.getName()).append(";\n");
		clzSb.append("import ").append(ServiceNameUtils.class.getName()).append(";\n");
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
					+ methodName + "\",parmaTypes);\n");
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
}
