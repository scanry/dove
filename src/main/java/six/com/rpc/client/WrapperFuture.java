package six.com.rpc.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import six.com.rpc.AsyCallback;
import six.com.rpc.exception.RpcTimeoutException;
import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.protocol.RpcResponse;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午10:44:16
 */
public class WrapperFuture {

	private volatile FutureState state = FutureState.DOING;

	private volatile long sendTime;

	private volatile long receiveTime;

	private volatile RpcRequest rpcRequest;

	private volatile RpcResponse rpcResponse;

	private AsyCallback asyCallback;

	private final CountDownLatch cdl=new CountDownLatch(1);

	public WrapperFuture(RpcRequest rpcRequest, AsyCallback asyCallback) {
		this.rpcRequest = rpcRequest;
		this.asyCallback = asyCallback;
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
		done();
		if (null == asyCallback) {
			cdl.countDown();
		} else {
			asyCallback.execute(response.getResult());
		}
	}

	private synchronized void done() {
		state = FutureState.DONE;
	}
	
	public RpcResponse getResult(long timeout) {
		if (state.isDoneState()) {
			return rpcResponse;
		}
		try {
			if(timeout<=0){
				cdl.await();
			}else{
				cdl.await(timeout, TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException e) {}
		if (state.isDoingState()){
			throw new RpcTimeoutException("execute rpcRequest[" + rpcRequest.toString() + "] timeout["+timeout+"]");
		}
		return rpcResponse;
	}
}
