package com.six.dove.rpc.client;

import java.util.Objects;

import com.six.dove.common.AbstractService;
import com.six.dove.remote.AsyCallback;
import com.six.dove.remote.Remote;
import com.six.dove.remote.client.netty.NettyClientRemote;
import com.six.dove.rpc.DoveClient;
import com.six.dove.rpc.annotation.DoveService;
import com.six.dove.remote.client.ClientRemote;

/**
 * @author sixliu
 * @date 2017年12月29日
 * @email 359852326@qq.com
 * @Description
 */
public class DoveClientImpl extends AbstractService implements DoveClient {

	private ClientRemote clientRemote;

	public DoveClientImpl() {
		this(new NettyClientRemote());
	}

	public DoveClientImpl(ClientRemote clientRemote) {
		super("dove-client");
		Objects.requireNonNull(clientRemote);
		this.clientRemote = clientRemote;
	}

	@Override
	public <T> T lookupService(Class<?> clz) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T lookupService(Class<?> clz, String version, long callTimeout) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T lookupService(Class<?> clz, AsyCallback callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T lookupService(Class<?> clz, String version, long callTimeout, AsyCallback callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz) {
		DoveService doveService = clz.getAnnotation(DoveService.class);
		String version = null;
		long callTimeout = 0;
		if (null != doveService) {
			version = doveService.version();
			callTimeout = doveService.callTimeout();
		} else {
			version = Remote.REMOTE_SERVICE_VERSION;
			callTimeout = Remote.DEFAULT_CALL_TIMEOUT;
		}
		return clientRemote.getOrNewRemoteProtocolProxy(targetHost, targetPort, clz, version, callTimeout);
	}

	@Override
	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz, String version, long callTimeout) {
		return clientRemote.getOrNewRemoteProtocolProxy(targetHost, targetPort, clz, version, callTimeout);
	}

	@Override
	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz, final AsyCallback asyCallback) {
		return clientRemote.getOrNewRemoteProtocolProxy(targetHost, targetPort, clz, Remote.REMOTE_SERVICE_VERSION,
				Remote.DEFAULT_CALL_TIMEOUT, asyCallback);
	}

	@Override
	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz, String version, long callTimeout,
			AsyCallback callback) {
		return clientRemote.getOrNewRemoteProtocolProxy(targetHost, targetPort, clz, version, callTimeout);
	}

	@Override
	protected final void doStart() {
		clientRemote.start();
	}

	@Override
	protected void doStop() {
		clientRemote.stop();
	}

}
