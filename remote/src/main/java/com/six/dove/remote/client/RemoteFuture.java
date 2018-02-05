package com.six.dove.remote.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午10:44:16
 */
public class RemoteFuture {

	private final RemoteRequest rpcRequest;
	
	private final AtomicBoolean isExecuteAsyCallback = new AtomicBoolean(false);

	private final CountDownLatch cdl = new CountDownLatch(1);
	
	private volatile long sendTime;

	private volatile long receiveTime;

	private volatile RemoteResponse rpcResponse;


	public RemoteFuture(RemoteRequest rpcRequest) {
		this.rpcRequest = rpcRequest;
	}

	public long getSendTime() {
		return sendTime;
	}

	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}

	public long getReceiveTime() {
		return receiveTime;
	}

	public RemoteRequest getRPCRequest() {
		return rpcRequest;
	}

	public void onComplete(RemoteResponse response, long receiveTime) {
		this.rpcResponse = response;
		this.receiveTime = receiveTime;
		cdl.countDown();
		if (null != rpcRequest.getAsyCallback() && isExecuteAsyCallback.compareAndSet(false, true)) {
			rpcRequest.getAsyCallback().execute(response);
		}
	}

	public RemoteResponse getResult(long timeout) {
		if (null != rpcResponse) {
			return rpcResponse;
		}
		try {
			if (timeout <= 0) {
				cdl.await();
			} else {
				cdl.await(timeout, TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException e) {
		}
		return rpcResponse;
	}
}
