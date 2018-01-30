package six.com.rpc.server;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.NettyRuntime;
import six.com.remote.server.AbstractServerRemote;
import six.com.remote.server.ServerRemote;
import six.com.rpc.Compiler;
import six.com.rpc.RpcServer;
import six.com.rpc.ServiceHook;
import six.com.rpc.protocol.RpcSerialize;

/**
 * @author sixliu
 * @date 2017年12月29日
 * @email 359852326@qq.com
 * @Description
 */
public abstract class AbstractServer extends AbstractServerRemote implements ServerRemote, RpcServer {

	final static Logger log = LoggerFactory.getLogger(AbstractServer.class);
	public static final int DEFAULT_EVENT_LOOP_THREADS = Math.max(1, NettyRuntime.availableProcessors() * 2);

	private Map<String, WrapperServiceTuple> registerMap = new ConcurrentHashMap<>();
	private ExecutorService defaultBizExecutorService;

	public AbstractServer(Compiler compiler, RpcSerialize rpcSerialize) {
		super(compiler, rpcSerialize);
		defaultBizExecutorService = Executors.newFixedThreadPool(DEFAULT_EVENT_LOOP_THREADS, new ThreadFactory() {
			private AtomicInteger threadIndex = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "NettyRpcServer-worker-biz-thread_" + this.threadIndex.incrementAndGet());
			}
		});
	}

	@Override
	public final WrapperServiceTuple getWrapperServiceTuple(String serviceName) {
		return registerMap.get(serviceName);
	}

	@Override
	public final void start() {
		doStart();
	}

	protected abstract void doStart();

	@Override
	public <T, I extends T> void register(Class<T> protocol, I instance) {
		register(defaultBizExecutorService, protocol, instance);
	}

	@Override
	public <T, I extends T> void register(Class<T> protocol, I instance, ServiceHook hook) {
		register(defaultBizExecutorService, protocol, instance, null == hook ? ServiceHook.DEFAULT_HOOK : hook);
	}

	@Override
	public <T, I extends T> void register(ExecutorService bizExecutorService, Class<T> protocol, I instance) {
		register(bizExecutorService, protocol, instance, ServiceHook.DEFAULT_HOOK);
	}

	@Override
	public <T, I extends T> void register(ExecutorService bizExecutorService, Class<T> protocol, I instance,
			ServiceHook hook) {
		Objects.requireNonNull(bizExecutorService, "bizExecutorService must not be null");
		Objects.requireNonNull(protocol, "protocol must not be null");
		Objects.requireNonNull(instance, "instance must not be null");
		Objects.requireNonNull(hook, "hook must not be null");
		if (!protocol.isAssignableFrom(instance.getClass())) {
			throw new RuntimeException("protocolClass " + protocol.getCanonicalName()
					+ " is not implemented by protocolImpl which is of class "
					+ instance.getClass().getCanonicalName());
		}
		int modifiers = instance.getClass().getModifiers();
		if (!"public".equals(Modifier.toString(modifiers))) {
			throw new RuntimeException("the instance's class[" + instance.getClass().getCanonicalName()
					+ "] is not public protocolClass ");
		}
		String protocolName = protocol.getName();
		Method[] protocolMethods = protocol.getMethods();
		String packageName = instance.getClass().getPackage().getName();
		WrapperService wrapperService = null;
		String fullClassName = null;
		for (Method protocolMethod : protocolMethods) {
			final String serviceName = getServiceName(protocolName, protocolMethod);
			String className = buildServerServiceClassName(instance, protocolMethod);
			fullClassName = packageName + "." + className;
			wrapperService = (WrapperService) getCompiler().findOrCompile(fullClassName, new Class<?>[] { protocol },
					new Object[] { instance }, () -> {
						return buildServerWrapperServiceCode(protocol, packageName, className, protocolMethod);
					});
			registerMap.put(serviceName, new WrapperServiceTuple(wrapperService, bizExecutorService, hook));
		}
	}

	@Override
	public void unregister(Class<?> protocol) {
		Objects.requireNonNull(protocol, "protocol must not be null");
		String protocolName = protocol.getName();
		Method[] protocolMethods = protocol.getMethods();
		for (Method protocolMethod : protocolMethods) {
			final String serviceName = getServiceName(protocolName, protocolMethod);
			registerMap.remove(serviceName);
		}
	}

	protected ExecutorService getDefaultBizExecutorService() {
		return defaultBizExecutorService;
	}

	public static String buildServerServiceClassName(Object instance, Method instanceMethod) {
		StringBuilder classSb = new StringBuilder();
		String instanceName = instance.getClass().getSimpleName();
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
		clz.append("import six.com.rpc.common.WrapperService;\n");
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
		if (hasReturnType(instanceMethod)) {
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
	public final void shutdown() {
		doShutdown();
	}

	protected abstract void doShutdown();
}
