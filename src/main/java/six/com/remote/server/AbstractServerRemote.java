package six.com.remote.server;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.remote.AbstractRemote;
import six.com.rpc.Compiler;
import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.protocol.RpcResponse;
import six.com.rpc.protocol.RpcResponseStatus;
import six.com.rpc.protocol.RpcSerialize;
import six.com.rpc.server.WrapperServiceTuple;
import six.com.rpc.util.ClassUtils;
import six.com.rpc.util.ExceptionUtils;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 抽象的服务调用端类
 */
public abstract class AbstractServerRemote
		extends AbstractRemote<RpcRequest, Void, RpcResponse, Void, ServerRpcConnection> implements ServerRemote {

	final static Logger log = LoggerFactory.getLogger(AbstractServerRemote.class);
	private String localHost;
	private int listenPort;

	public AbstractServerRemote(String localHost, int listenPort, Compiler compiler, RpcSerialize rpcSerialize) {
		super(compiler, rpcSerialize);
		this.localHost = localHost;
		this.listenPort = listenPort;
	}

	@Override
	public Void execute(RpcRequest rpcRequest) {
		RpcResponse rpcResponse = new RpcResponse();
		rpcResponse.setId(rpcRequest.getId());
		WrapperServiceTuple wrapperServiceTuple = getWrapperServiceTuple(rpcRequest.getServiceName());
		String address = rpcRequest.getServerRpcConnection().toString();
		if (null != wrapperServiceTuple) {
			try {
				wrapperServiceTuple.getExecutorService().submit(() -> {
					log.debug("server received coommand[" + rpcRequest.getServiceName() + "] from:" + address);
					try {
						wrapperServiceTuple.getHook().beforeHook(rpcRequest.getParams());
						Object result = wrapperServiceTuple.getWrapperService().invoke(rpcRequest.getParams());
						wrapperServiceTuple.getHook().afterHook(rpcRequest.getParams());
						rpcResponse.setStatus(RpcResponseStatus.SUCCEED);
						rpcResponse.setResult(result);
					} catch (Exception e) {
						wrapperServiceTuple.getHook().exceptionHook(rpcRequest.getParams());
						String errMsg = ExceptionUtils.getExceptionMsg(e);
						rpcResponse.setStatus(RpcResponseStatus.INVOKE_ERR);
						rpcResponse.setMsg(errMsg);
						log.error("invoke request[" + address + "] err", e);
					}
					rpcRequest.getServerRpcConnection().send(rpcResponse);
				});
			} catch (RejectedExecutionException e) {
				// 业务处理线程池满了，拒绝异常
				rpcResponse.setStatus(RpcResponseStatus.REJECT);
				String msg = "the service is too busy and reject rpcRequest[" + address + "]:"
						+ rpcRequest.getServiceName();
				rpcResponse.setMsg(msg);
				log.error(msg);
				rpcRequest.getServerRpcConnection().send(rpcResponse);
			}
		} else {
			rpcResponse.setStatus(RpcResponseStatus.UNFOUND_SERVICE);
			String msg = "unfound service by rpcRequest[" + address + "]:" + rpcRequest.getServiceName();
			rpcResponse.setMsg(msg);
			log.error(msg);
			rpcRequest.getServerRpcConnection().send(rpcResponse);
		}
		return null;
	}
	
	@Override
	protected String generateProtocolProxyClassName(Class<?> protocol, Method instanceMethod) {
		StringBuilder classSb = new StringBuilder();
		String instanceName = protocol.getSimpleName();
		String instanceMethodName = instanceMethod.getName();
		classSb.append("RpcServerServiceProxy$");
		classSb.append(instanceName).append("$");
		classSb.append(instanceMethodName);
		Parameter[] parameter = instanceMethod.getParameters();
		if (null != parameter && parameter.length > 0) {
			classSb.append("$");
			String parameterTypeName = null;
			StringBuilder invokePamasSb = new StringBuilder();
			for (int i = 0, size = parameter.length; i < size; i++) {
				parameterTypeName = parameter[i].getParameterizedType().getTypeName();
				parameterTypeName = parameterTypeName.replace(".", "_");
				invokePamasSb.append(parameterTypeName).append("$");
			}
			invokePamasSb.deleteCharAt(invokePamasSb.length() - 1);
			classSb.append(invokePamasSb);
		}
		return classSb.toString();
	}
	
	public static String buildServerWrapperServiceCode(Class<?> protocolClass, String packageName, String className,
			Method instanceMethod) {
		String method = instanceMethod.getName();
		String instanceType = protocolClass.getCanonicalName();
		Parameter[] parameter = instanceMethod.getParameters();
		StringBuilder clz = new StringBuilder();
		clz.append("package ").append(packageName).append(";\n");
		clz.append("import six.com.rpc.server.WrapperService;\n");
		clz.append("public class ").append(className).append(" implements WrapperService {\n");
		clz.append("	private ").append(instanceType).append(" instance;\n");
		clz.append("	public " + className + "(").append(instanceType).append(" instance").append("){\n");
		clz.append("		this.instance=instance;\n");
		clz.append("	}\n");
		clz.append("	@Override\n");
		clz.append("	public Object invoke(Object[] paras)throws Exception{\n");
		String invokePamasStr = "";
		if (null != parameter && parameter.length > 0) {
			String parameterTypeName = null;
			StringBuilder invokePamasSb = new StringBuilder();
			for (int i = 0, size = parameter.length; i < size; i++) {
				parameterTypeName = parameter[i].getParameterizedType().getTypeName();
				invokePamasSb.append("(").append(parameterTypeName).append(")").append("paras[").append(i).append("],");
			}
			invokePamasSb.deleteCharAt(invokePamasSb.length() - 1);
			invokePamasStr = invokePamasSb.toString();
		}
		if (ClassUtils.hasReturnType(instanceMethod)) {
			clz.append("		return this.instance.").append(method);
			clz.append("(").append(invokePamasStr).append(");\n");
		} else {
			clz.append("		this.instance.").append(method);
			clz.append("(").append(invokePamasStr).append(");\n");
			clz.append("		return null;\n");
		}
		clz.append("	}\n");
		clz.append("}\n");
		return clz.toString();
	}


	@Override
	public String getLocalHost() {
		return localHost;
	}

	@Override
	public int getListenPort() {
		return listenPort;
	}
}
