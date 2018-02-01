package com.six.dove.remote.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.remote.AbstractRemoteConnection;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.rpc.client.AbstractClient;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午11:05:56
 */

public abstract class AbstractClientRemoteConnection extends AbstractRemoteConnection<RemoteRequest, RemoteFuture>
		implements ClientRemoteConnection {

	final static Logger log = LoggerFactory.getLogger(AbstractClientRemoteConnection.class);

	private AbstractClient rpcClient;
	private Map<String, RemoteFuture> requestMap;

	protected AbstractClientRemoteConnection(AbstractClient rpcClient, String host, int port) {
		super(host, port);
		this.rpcClient = rpcClient;
		this.requestMap = new ConcurrentHashMap<>();
	}

	@Override
	public void putRemoteFuture(String rpcRequestId, RemoteFuture wrapperFuture) {
		requestMap.put(rpcRequestId, wrapperFuture);
	}

	@Override
	public RemoteFuture removeRemoteFuture(String rpcRequestId) {
		return requestMap.remove(rpcRequestId);
	}

	protected AbstractClient getAbstractClient() {
		return rpcClient;
	}
}
