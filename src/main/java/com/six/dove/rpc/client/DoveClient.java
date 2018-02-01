package com.six.dove.rpc.client;

import java.util.Objects;

import com.six.dove.common.AbstractService;
import com.six.dove.remote.AsyCallback;
import com.six.dove.remote.client.netty.NettyCilentRemote;
import com.six.dove.rpc.RpcClient;
import com.six.dove.remote.client.ClientRemote;

/**
 * @author sixliu
 * @date 2017年12月29日
 * @email 359852326@qq.com
 * @Description
 */
public class DoveClient extends AbstractService implements RpcClient {

	private ClientRemote clientRemote;

	public DoveClient() {
		this(new NettyCilentRemote());
	}

	public DoveClient(ClientRemote clientRemote) {
		super("dove-client");
		Objects.requireNonNull(clientRemote);
		this.clientRemote = clientRemote;
	}

	@Override
	public <T> T lookupService(Class<?> clz) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T lookupService(Class<?> clz, AsyCallback callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz, final AsyCallback asyCallback) {
		return clientRemote.getOrNewRemoteProtocolProxy(targetHost, targetPort, clz, asyCallback);
	}

	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz) {
		return clientRemote.getOrNewRemoteProtocolProxy(targetHost, targetPort, clz);
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
