package com.six.dove.remote.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.remote.connection.AbstractRemoteConnection;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.remote.protocol.RemoteResponseState;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午11:05:56
 */

public abstract class AbstractClientRemoteConnection extends AbstractRemoteConnection<RemoteRequest, RemoteFuture>
		implements ClientRemoteConnection {

	final static Logger log = LoggerFactory.getLogger(AbstractClientRemoteConnection.class);

	private AbstractClientRemote clientRemote;
	private Map<String, RemoteFuture> requestMap;

	protected AbstractClientRemoteConnection(AbstractClientRemote clientRemote, String host, int port) {
		super(host, port);
		this.clientRemote = clientRemote;
		this.requestMap = new ConcurrentHashMap<>();
	}

	@Override
	protected final RemoteFuture doSend(RemoteRequest request) {
		RemoteFuture remoteFuture = new RemoteFuture(request);
		remoteFuture.setSendTime(System.currentTimeMillis());
		putRemoteFuture(request.getId(), remoteFuture);
		write(request,isSuccess->{
			if (isSuccess) {
				log.debug("send rpcRequest successed");
			} else {
				removeRemoteFuture(request.getId());
				RemoteResponse failedRemoteResponse=new RemoteResponse(RemoteResponseState.SEND_FAILED);
				failedRemoteResponse.setMsg("send rpcRequest["+request+"] to ServerRemote["+toString()+"] failed");
				remoteFuture.onComplete(failedRemoteResponse);
				close();
				log.debug("send rpcRequest failed");
			}
		});
		return remoteFuture;
	}

	protected abstract void write(RemoteRequest request, SendListener sendListener);

	@Override
	public void putRemoteFuture(String rpcRequestId, RemoteFuture wrapperFuture) {
		requestMap.put(rpcRequestId, wrapperFuture);
	}

	@Override
	public RemoteFuture removeRemoteFuture(String rpcRequestId) {
		return requestMap.remove(rpcRequestId);
	}

	protected AbstractClientRemote getClientRemote() {
		return clientRemote;
	}

	@FunctionalInterface
	public interface SendListener {

		void operationComplete(boolean isSuccess);
	}
}
