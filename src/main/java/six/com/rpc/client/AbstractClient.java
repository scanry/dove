package six.com.rpc.client;

import java.util.Collections;
import java.util.Map;

import six.com.remote.client.AbstractClientRemote;
import six.com.rpc.AsyCallback;
import six.com.rpc.Compiler;
import six.com.rpc.RpcClient;

import six.com.rpc.protocol.RpcSerialize;

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
		String proxyClassName = generateProtocolProxyClassName(clz, null);
		String fullProxyClassName = packageName + "." + proxyClassName;
		// ServiceName serviceName=ServiceName.newServiceName(clz.getCanonicalName(),
		// methodName, parmaTypes, version)
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

	@Override
	public final void start() {
	}

}
