package six.com.rpc.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import six.com.rpc.protocol.RpcMsg;
import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.protocol.RpcResponse;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午11:05:56
 */

public class ClientToServerConnection extends NettyConnection implements RpcConnection {

	final static Logger log = LoggerFactory.getLogger(ClientToServerConnection.class);

	private AbstractClient rpcClient;
	private String id;
	private String host;
	private int port;
	private Map<String, WrapperFuture> requestMap;

	protected ClientToServerConnection(AbstractClient rpcClient, String id, String host, int port) {
		this.rpcClient = rpcClient;
		this.id = id;
		this.host = host;
		this.port = port;
		this.requestMap = new ConcurrentHashMap<>();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcMsg msg) throws Exception {
		if (msg instanceof RpcResponse) {
			RpcResponse rpcResponse = (RpcResponse) msg;
			WrapperFuture wrapperRPCRequest = removeWrapperFuture(rpcResponse.getId());
			if (null != wrapperRPCRequest) {
				wrapperRPCRequest.onComplete(rpcResponse, System.currentTimeMillis());
				log.debug("client received rpcResponse from rpcRequest[" + wrapperRPCRequest.getRPCRequest().toString()
						+ "]");
			}
		} else {
			log.error("ClientHandler messageReceived type not support: class=" + msg.getClass());
		}
	}

	/**
	 * 发送 rpcRequest
	 * 
	 * @param rpcRequest
	 * @param callback
	 * @param callTimeout
	 * @return
	 */
	@Override
	public WrapperFuture send(RpcRequest rpcRequest) {
		WrapperFuture wrapperFuture = new WrapperFuture(rpcRequest);
		wrapperFuture.setSendTime(System.currentTimeMillis());
		putWrapperFuture(rpcRequest.getId(), wrapperFuture);
		ChannelFuture channelFuture = super.writeAndFlush(rpcRequest);
		boolean result = channelFuture.awaitUninterruptibly(rpcClient.getCallTimeout());
		if (result) {
			channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
				@Override
				public void operationComplete(Future<? super Void> future) throws Exception {
					if (future.isSuccess()) {
						log.debug("send rpcRequest successed");
					} else {
						removeWrapperFuture(rpcRequest.getId());
						wrapperFuture.onComplete(RpcResponse.SEND_FAILED, System.currentTimeMillis());
						close();
						log.debug("send rpcRequest failed");
					}
				}
			});
		}
		return wrapperFuture;
	}

	@Override
	public void putWrapperFuture(String rpcRequestId, WrapperFuture wrapperFuture) {
		requestMap.put(rpcRequestId, wrapperFuture);
	}

	@Override
	public WrapperFuture removeWrapperFuture(String rpcRequestId) {
		return requestMap.remove(rpcRequestId);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	protected void doClose() {
		requestMap.clear();
		rpcClient.removeConnection(this);
	}

}
