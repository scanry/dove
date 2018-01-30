package six.com.rpc.client.netty;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import six.com.remote.client.AbstractClientRpcConnection;
import six.com.rpc.client.AbstractClient;
import six.com.rpc.client.WrapperFuture;
import six.com.rpc.protocol.RpcMsg;
import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.protocol.RpcResponse;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe
 */
public class NettyConnectionImpl extends AbstractClientRpcConnection implements ChannelInboundHandler {

	final static Logger log = LoggerFactory.getLogger(NettyConnectionImpl.class);
	private NettyHandler nettyHandler;

	protected NettyConnectionImpl(AbstractClient rpcClient, String host, int port) {
		super(rpcClient, host, port);
		this.nettyHandler = new NettyHandler();
	}

	public class NettyHandler extends SimpleChannelInboundHandler<RpcMsg> {

		private volatile ChannelHandlerContext ctx;
		private volatile Channel channel;

		public ChannelHandlerContext getContext() {
			return ctx;
		}

		public Channel getChannel() {
			return channel;
		}

		@Override
		public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
			this.ctx = ctx;
			this.channel = ctx.channel();
		}

		protected ChannelFuture writeAndFlush(RpcMsg t) {
			return channel.writeAndFlush(t);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			close();
			super.channelInactive(ctx);
		}

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, RpcMsg msg) throws Exception {
			if (msg instanceof RpcResponse) {
				RpcResponse rpcResponse = (RpcResponse) msg;
				WrapperFuture wrapperRPCRequest = removeWrapperFuture(rpcResponse.getId());
				if (null != wrapperRPCRequest) {
					wrapperRPCRequest.onComplete(rpcResponse, System.currentTimeMillis());
					log.debug("client received rpcResponse from rpcRequest["
							+ wrapperRPCRequest.getRPCRequest().toString() + "]");
				}
			} else {
				log.error("ClientHandler messageReceived type not support: class=" + msg.getClass());
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			if (cause instanceof IOException) {
				String address = ctx.channel().remoteAddress().toString();
				close();
				log.warn("connection to remote[" + address + "] exception and the connection was closed");
			} else {
				ctx.fireExceptionCaught(cause);
			}
		}
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		nettyHandler.handlerAdded(ctx);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		nettyHandler.handlerRemoved(ctx);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		nettyHandler.channelRegistered(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		nettyHandler.channelUnregistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		nettyHandler.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		nettyHandler.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		nettyHandler.channelRead(ctx, msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		nettyHandler.channelReadComplete(ctx);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		nettyHandler.userEventTriggered(ctx, evt);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		nettyHandler.channelWritabilityChanged(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		nettyHandler.exceptionCaught(ctx, cause);
	}

	/**
	 * 是否可用
	 * 
	 * @return
	 */
	@Override
	public boolean available() {
		return null != nettyHandler.channel && nettyHandler.channel.isActive();
	}

	@Override
	public WrapperFuture send(RpcRequest rpcRequest) {
		WrapperFuture wrapperFuture = new WrapperFuture(rpcRequest);
		wrapperFuture.setSendTime(System.currentTimeMillis());
		putWrapperFuture(rpcRequest.getId(), wrapperFuture);
		ChannelFuture channelFuture = nettyHandler.writeAndFlush(rpcRequest);
		boolean result = channelFuture.awaitUninterruptibly(getAbstractClient().getCallTimeout());
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
	public void close() {
		if (null != nettyHandler.ctx) {
			nettyHandler.ctx.close();
		}
		getAbstractClient().removeConnection(this);
	}

}
