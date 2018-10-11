package com.six.dove.remote.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.six.dove.remote.client.exception.RemoteSendFailedException;
import com.six.dove.remote.client.exception.RemoteTimeoutException;
import com.six.dove.remote.client.exception.RemoteUnfoundServiceException;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.remote.protocol.RemoteResponseState;
import com.six.dove.remote.server.exception.RemoteInvokeException;
import com.six.dove.remote.server.exception.RemoteRejectException;
import com.six.dove.transport.Request;
import com.six.dove.transport.client.ClientTransport;

/**
 * @author:MG01867
 * @date:2018年4月12日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class SynClientInoker extends AbstractClientInoker {

	private final CountDownLatch cdl = new CountDownLatch(1);

	public SynClientInoker(String callHost, int callPort,long callTimeout, ClientTransport<?,?> clientTransport, Request request) {
		super(callHost, callPort,callTimeout, clientTransport, request);
	}

	@Override
	protected RemoteResponse getResult(Request request) {
		RemoteResponse rpcResponse = getOrWaitRemoteResponse();
		removeSelfFromCache();
		if (null == rpcResponse) {
			throw new RemoteTimeoutException(
					"execute rpcRequest[" + request.toString() + "] timeout[" + getCallTimeout() + "]");
		} else if (rpcResponse.getStatus() == RemoteResponseState.SEND_FAILED) {
			throw new RemoteSendFailedException(rpcResponse.getMsg());
		} else if (rpcResponse.getStatus() == RemoteResponseState.UNFOUND_SERVICE) {
			throw new RemoteUnfoundServiceException(rpcResponse.getMsg());
		} else if (rpcResponse.getStatus() == RemoteResponseState.REJECT) {
			throw new RemoteRejectException(rpcResponse.getMsg());
		} else if (rpcResponse.getStatus() == RemoteResponseState.INVOKE_ERR) {
			throw new RemoteInvokeException(rpcResponse.getMsg());
		} else {
			return rpcResponse;
		}
	}

	@Override
	public void onComplete(RemoteResponse response) {
		super.onComplete(response);
		cdl.countDown();
	}

	public RemoteResponse getOrWaitRemoteResponse() {
		if (null != getRemoteResponse()) {
			return getRemoteResponse();
		}
		try {
			if (getCallTimeout() <= 0) {
				cdl.await();
			} else {
				cdl.await(getCallTimeout(), TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException e) {
		}
		return getRemoteResponse();
	}
}
