package com.six.dove.remote.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.six.dove.remote.client.exception.RemoteSendFailedException;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.remote.protocol.RemoteResponseState;
import com.six.dove.transport.connection.Connection;
import com.six.dove.transport.Request;
import com.six.dove.transport.client.ClientTransport;

/**
 * @author:MG01867
 * @date:2018年4月12日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public abstract class AbstractClientInoker implements ClientInoker {

	private String callHost;
	private int callPort;
	private long callTimeout;
	private Request request;
	private ClientTransport clientTransport;
	private static Map<String, ClientInoker> cacheRemoteFuture = new ConcurrentHashMap<>();

	private volatile long sendTime;
	private volatile long receiveTime;
	private volatile RemoteResponse remoteResponse;

	public static ClientInoker removeClientInokerFromCache(String remoteRequestId) {
		return cacheRemoteFuture.remove(remoteRequestId);
	}
	
	public ClientInoker removeSelfFromCache() {
		return removeClientInokerFromCache(request.getId());
	}

	public AbstractClientInoker(String callHost, int callPort,long callTimeout,ClientTransport clientTransport, Request request) {
		this.callHost = callHost;
		this.callPort = callPort;
		this.callTimeout = callTimeout;
		this.clientTransport = clientTransport;
		this.request = request;
		cacheRemoteFuture.put(request.getId(), this);
	}

	@Override
	public RemoteResponse invoke() {
		Connection finalConnection = clientTransport.find(callHost, callPort);
		try {
			finalConnection.send(request, sendFutrue -> {
				if (!sendFutrue.isSucceed()) {
					ClientInoker abstractClientInoker = removeClientInokerFromCache(request.getId());
					abstractClientInoker.onComplete(new RemoteResponse(RemoteResponseState.SEND_FAILED));
				} else {
					sendTime = System.currentTimeMillis();
				}
			});
		} catch (Exception e) {
			removeSelfFromCache();
			throw new RemoteSendFailedException(e);
		}
		return getResult(request);
	}

	@Override
	public void onComplete(RemoteResponse remoteResponse) {
		this.receiveTime = System.currentTimeMillis();
		this.remoteResponse =remoteResponse;
	}

	@Override
	public long getSendTime() {
		return sendTime;
	}

	@Override
	public long getReceiveTime() {
		return receiveTime;
	}
	
	@Override
	public long getCallTimeout() {
		return callTimeout;
	}

	public Request getRequest() {
		return request;
	}
	
	public RemoteResponse getRemoteResponse() {
		return remoteResponse;
	}
	
	protected abstract RemoteResponse getResult(Request request);

}
