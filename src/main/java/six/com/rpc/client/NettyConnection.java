package six.com.rpc.client;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import six.com.rpc.protocol.RpcMsg;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月21日 上午9:08:36
 */
public abstract class NettyConnection extends SimpleChannelInboundHandler<RpcMsg> implements RpcConnection {

	final static Logger log = LoggerFactory.getLogger(NettyConnection.class);

	private volatile long lastActivityTime;

	private volatile ChannelHandlerContext ctx;
	// netty Channel
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
		lastActivityTime = System.currentTimeMillis();
		return channel.writeAndFlush(t);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		close();
		super.channelInactive(ctx);
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

	/**
	 * 是否可用
	 * 
	 * @return
	 */
	@Override
	public boolean available() {
		return null != channel && channel.isActive();
	}

	@Override
	public long getLastActivityTime() {
		return lastActivityTime;
	}

	@Override
	public void close() {
		if (null != ctx) {
			ctx.close();
		}
		doClose();
	}

	protected abstract void doClose();

	public void disconnect() {
		if (null != ctx) {
			ctx.disconnect();
		}
	}
}
