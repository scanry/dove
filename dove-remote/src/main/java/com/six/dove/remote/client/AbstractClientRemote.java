package com.six.dove.remote.client;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.six.dove.common.utils.ClassUtils;
import com.six.dove.remote.AbstractRemote;
import com.six.dove.remote.AsyCallback;
import com.six.dove.remote.DoveContext;
import com.six.dove.remote.compiler.Compiler;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteRequestParam;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.transport.client.ClientTransport;
import com.six.dove.transport.TransportCodec;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;

/**
 * @author:MG01867
 * @date:2018年5月9日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public abstract class AbstractClientRemote extends AbstractRemote implements ClientRemote {

	private Map<String, Object> serviceWeakHashMap = Collections.synchronizedMap(new WeakHashMap<>());
	private static ClassPool classPool = ClassPool.getDefault();
	
	public AbstractClientRemote(String name, Compiler compiler, TransportCodec transportCodec) {
		super(name, compiler, transportCodec);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getOrNewRemoteProtocolProxy(String callHost, int callPort, Class<?> clz, long callTimeout) {
		String proxyKey = ClientRemoteUtils.getClientRemoteProxyKey(callHost, callPort, clz);
		return (T) serviceWeakHashMap.computeIfAbsent(proxyKey, newKey -> {
			String packageName = null;
			String className = null;
			String fullClassName = null;
			String clientProxyServiceCode =null;
			Class<?> clientProxyServiceClass = getCompiler().compile(fullClassName, clientProxyServiceCode);
			return getCompiler().compile(fullClassName, clientProxyServiceCode);
		});
	}

	@Override
	public <T> T getOrNewRemoteProtocolProxy(String callHost, int callPort, Class<?> clz, long callTimeout,
			AsyCallback callback) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(String callHost, int callPort, String command, RemoteRequestParam requestParam,
			long callTimeout) {
		RemoteRequest request = new RemoteRequest();
		request.setCommand(command);
		request.setRemoteRequestParam(requestParam);
		SynClientInoker synClientInoker = new SynClientInoker(callHost, callPort, callTimeout, getClientTransport(),
				request);
		RemoteResponse response = synClientInoker.invoke();
		return (T) response.getResult();
	}

	@Override
	public void invoke(String callHost, int callPort, String command, RemoteRequestParam requestParam, long callTimeout,
			AsyCallback callback) {
		RemoteRequest request = new RemoteRequest();
		request.setCommand(command);
		request.setRemoteRequestParam(requestParam);
		AsyClientInoker asyClientInoker = new AsyClientInoker(callHost, callPort, callTimeout, getClientTransport(),
				request,callback);
		asyClientInoker.invoke();
	}

	protected abstract ClientTransport<?, ?> getClientTransport();

	private Class<?> t(String callHost, int callPort, Class<?> protocolClass) {
		String className = null;
		CtClass ctClass = classPool.makeClass(className);
		try {
			CtField hostField = new CtField(classPool.get("java.lang.String"), "host", ctClass);
			ctClass.addField(hostField);

			CtField portField = new CtField(CtClass.intType, "port", ctClass);
			ctClass.addField(portField);
			List<String> proxyMethodCodes = proxyMethodCodes(protocolClass);
			for (String proxyMethodCode : proxyMethodCodes) {
				CtMethod proxyMethod = CtNewMethod.make(proxyMethodCode, ctClass);
				ctClass.addMethod(proxyMethod);
			}
			ctClass.addInterface(classPool.get(protocolClass.getName()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<String> proxyMethodCodes(Class<?> protocolClass){
		Method[] methods = protocolClass.getMethods();
		List<String> proxyMethodCodes=new ArrayList<>(methods.length);
		for (Method method : methods) {
			StringBuilder clzSb = new StringBuilder();
			StringBuilder throwsException = new StringBuilder();
			StringBuilder args = new StringBuilder();
			StringBuilder parmaTypes = new StringBuilder();
			String invokePamasStr = "";
			String methodName = method.getName();
			Class<?> returnType = method.getReturnType();
			String returnTypeCanonicalName = returnType.getCanonicalName();
			Parameter[] parameter = method.getParameters();
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
				args.append("	       request.setRemoteRequestParam(args);\n");
				invokePamasSb.deleteCharAt(invokePamasSb.length() - 1);
				invokePamasStr = invokePamasSb.toString();
				parmaTypes.deleteCharAt(parmaTypes.length() - 1);
				parmaTypes.append("};\n");
			} else {
				args.append("	       request.setRemoteRequestParam(null);\n");
			}
			Class<?>[] throwsExceptionType = method.getExceptionTypes();
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
			clzSb.append("	       "+AsyCallback.class.getName()+" asyCallback="+DoveContext.class.getName()+".getCurrentThreadAsyCallback();\n");
			clzSb.append("	       "+RemoteRequest.class.getName()+" request = new "+RemoteRequest.class.getName()+"();\n");
			clzSb.append("	       request.setCommand(command);\n");
			clzSb.append("	       request.setRemoteRequestParam(requestParam);\n");
			clzSb.append(parmaTypes);
			clzSb.append("	       ClientInoker clientInoker=null;\n");
			clzSb.append("	       if(null!=asyCallback){\n");
			clzSb.append("	        	clientInoker= new AsyClientInoker(host,port,timeout,clientTransport,request,asyCallback);\n");
			clzSb.append("	                "+RemoteResponse.class.getName()+" response =clientInoker.invoke();\n");
			if (ClassUtils.hasReturnType(method)) {
				if (returnType.isPrimitive()) {
					clzSb.append("	       		return " + parser(returnType) + ";\n");
				} else {
					clzSb.append("	       		return null;\n");
				}
			}
			clzSb.append("	       }else{\n");
			clzSb.append("	        	clientInoker= new SynClientInoker(host,port,timeout,clientTransport,request);\n");
			clzSb.append("	                "+RemoteResponse.class.getName()+" response =clientInoker.invoke();\n");
			if (ClassUtils.hasReturnType(method)) {
				clzSb.append("	       		return (" + returnTypeCanonicalName + ")remoteResponse.getResult();\n");
			}else {
				clzSb.append("	       		response.getResult();\n");
			}
			clzSb.append("	       }\n");
			clzSb.append("	}\n");
			proxyMethodCodes.add(clzSb.toString());
		}
		return proxyMethodCodes;
	}
	
	public static String getParameterClassName(Parameter[] parameters,Method method){
		String parameterClassName=null;
		CtClass ctClass = classPool.makeClass(parameterClassName);
		for(int i=0;i<parameters.length;i++) {
			Parameter parameter=parameters[i];
			try {
				CtField hostField = new CtField(classPool.get(parameter.getType().getName()), "parameter"+i, ctClass);
				ctClass.addField(hostField);
				StringBuilder getParameterSb = new StringBuilder();
				getParameterSb.append("public "+parameter.getType().getName()+" getParameter(){/n"+i);
				getParameterSb.append("		return parameter"+i+";/n");
				getParameterSb.append("}/n");
				CtMethod getMethod = CtNewMethod.make(getParameterSb.toString(), ctClass);
				ctClass.addMethod(getMethod);
				
				StringBuilder setParameterSb = new StringBuilder();
				getParameterSb.append("public void setParameter("+parameter.getType().getName()+" value){/n"+i);
				getParameterSb.append("		this.parameter"+i+"=value;/n");
				getParameterSb.append("}/n");
				CtMethod setMethod = CtNewMethod.make(setParameterSb.toString(), ctClass);
				ctClass.addMethod(setMethod);
				
				ctClass.addInterface(classPool.get(RemoteRequestParam.class.getName()));
				ctClass.getClass();
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		}
		return parameterClassName;
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
