package com.six.dove.rpc.client;

import java.util.Collections;
import java.util.Map;

import com.six.dove.remote.AsyCallback;
import com.six.dove.remote.client.AbstractClientRemote;
import com.six.dove.remote.compiler.Compiler;
import com.six.dove.remote.protocol.RemoteSerialize;
import com.six.dove.rpc.RpcClient;

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

	public AbstractClient(String name,Compiler wrapperServiceProxyFactory, RemoteSerialize remoteSerialize, long callTimeout) {
		super(name,wrapperServiceProxyFactory, remoteSerialize, callTimeout);
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
	protected final void doStart() {
	}

}
