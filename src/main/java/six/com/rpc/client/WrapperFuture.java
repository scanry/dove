package six.com.rpc.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.protocol.RpcResponse;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午10:44:16
 */
public class WrapperFuture {

	private volatile long sendTime;

	private volatile long receiveTime;

	private volatile RpcRequest rpcRequest;

	private volatile RpcResponse rpcResponse;

	private AtomicBoolean executeAsyCallback = new AtomicBoolean(false);

	private final CountDownLatch cdl = new CountDownLatch(1);

	public WrapperFuture(RpcRequest rpcRequest) {
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

	public RpcRequest getRPCRequest() {
		return rpcRequest;
	}

	public void onComplete(RpcResponse response, long receiveTime) {
		this.rpcResponse = response;
		this.receiveTime = receiveTime;
		cdl.countDown();
		if (null != rpcRequest.getAsyCallback()&& executeAsyCallback.compareAndSet(false, true)) {
			rpcRequest.getAsyCallback().execute(response);
		}
	}

	public boolean hasAsyCallback() {
		return null != rpcRequest.getAsyCallback();
	}

	public RpcResponse getResult(long timeout) {
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
