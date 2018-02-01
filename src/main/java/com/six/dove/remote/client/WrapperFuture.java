package com.six.dove.remote.client;

import java.util.concurrent.atomic.AtomicBoolean;

import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午10:44:16
 */
public class WrapperFuture {

	private volatile long sendTime;

	private volatile long receiveTime;

	private volatile RemoteRequest rpcRequest;

	private volatile RemoteResponse rpcResponse;

	private AtomicBoolean executeAsyCallback = new AtomicBoolean(false);

	private volatile byte isWait = 0;

	public WrapperFuture(RemoteRequest rpcRequest) {
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

	public synchronized void onComplete(RemoteResponse response, long receiveTime) {
		this.rpcResponse = response;
		this.receiveTime = receiveTime;
		if (1 == isWait) {
			synchronized(this) {
				if (1 == isWait) {
					this.notify();
				}
			}
		}
		if (null != rpcRequest.getAsyCallback() && executeAsyCallback.compareAndSet(false, true)) {
			rpcRequest.getAsyCallback().execute(response);
		}
	}

	public boolean hasAsyCallback() {
		return null != rpcRequest.getAsyCallback();
	}

	public RemoteResponse getResult(long timeout) {
		if (null != rpcResponse) {
			return rpcResponse;
		}
		synchronized(this) {
			isWait = 1;
			try {
				if (timeout <= 0) {
					this.wait();
				} else {
					this.wait(timeout);
				}
			} catch (InterruptedException e) {
			}
			isWait = 0;
		}
		return rpcResponse;
	}
}
