package six.com.rpc.client;

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
public abstract class NettyConnection extends SimpleChannelInboundHandler<RpcMsg> {

	final static Logger log = LoggerFactory.getLogger(NettyConnection.class);

	private String host;

	private int port;

	private volatile long lastActivityTime;

	private volatile ChannelHandlerContext ctx;
	// netty Channel
	private volatile Channel channel;

	NettyConnection(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

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
		doConnect();
		super.channelInactive(ctx);
	}

	protected abstract void doConnect();

	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.fireExceptionCaught(cause);
    }
	/**
	 * 是否可用
	 * 
	 * @return
	 */
	public boolean available() {
		return null != channel && channel.isActive();
	}

	public long getLastActivityTime() {
		return lastActivityTime;
	}

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

	public String getConnectionKey() {
		return getNewConnectionKey(host, port);
	}

	public static String getNewConnectionKey(String host, int port) {
		String findKey = host + ":" + port;
		return findKey;
	}

}
