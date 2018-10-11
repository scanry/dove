package com.six.dove.transport.netty.server;

import java.net.InetSocketAddress;

import com.six.dove.transport.*;
import com.six.dove.transport.exception.TransportException;
import com.six.dove.transport.message.Request;
import com.six.dove.transport.message.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.transport.netty.NettyReceiveHandlerAdapter;
import com.six.dove.transport.netty.codec.NettyRpcDecoderAdapter;
import com.six.dove.transport.netty.codec.NettyRpcEncoderAdapter;
import com.six.dove.transport.server.AbstractServerTransport;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe netty-服务 传输端
 */
public class NettyServerTransport<SendMsg extends Response, ReceMsg extends Request>
		extends AbstractServerTransport<SendMsg, ReceMsg> {

	final static Logger log = LoggerFactory.getLogger(NettyServerTransport.class);

	private EventLoopGroup bossGroup;

	private EventLoopGroup workerIoGroup;

	private ServerBootstrap serverBootstrap;

	private int workerThreads;

	private int allIdleTimeSeconds;

	public NettyServerTransport(int port, int workerIoThreads, int allIdleTimeSeconds) {
		super(port);
		this.workerThreads = workerIoThreads;
	}

	@Override
	protected void innerDoStart(NetAddress netAddress) {
		serverBootstrap = new ServerBootstrap();
		initGroup(serverBootstrap);
		serverBootstrap.localAddress(new InetSocketAddress(netAddress.getHost(), netAddress.getPort()));
		serverBootstrap.channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class);
		serverBootstrap.childHandler(buildChannelInitializer());
		serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
		serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
		serverBootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, false);
		serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
		Thread startThread = new Thread(() -> {
			try {
				ChannelFuture sync = serverBootstrap.bind().sync();
				sync.channel().closeFuture().sync();
			} catch (InterruptedException e) {
				throw new TransportException("netty serverBootstrap err", e);
			} finally {
				doShutdown();
			}
		}, "netty-start-thread");
		startThread.setDaemon(true);
		startThread.start();
	}

	private void initGroup(ServerBootstrap serverBootstrap) {
		bossGroup = new NioEventLoopGroup(1);
		if (Epoll.isAvailable()) {
			workerIoGroup = new EpollEventLoopGroup(workerThreads);
		} else {
			workerIoGroup = new NioEventLoopGroup(workerThreads);
		}
		serverBootstrap.group(bossGroup, workerIoGroup);
	}

	private ChannelInitializer<SocketChannel> buildChannelInitializer() {
		return new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) {
				ch.pipeline().addLast(new IdleStateHandler(0, 0, allIdleTimeSeconds));
				ch.pipeline().addLast(new NettyServerAcceptorIdleStateTrigger());
				ch.pipeline().addLast(new NettyRpcEncoderAdapter(getTransportCodec()));
				ch.pipeline().addLast(new NettyRpcDecoderAdapter<>(getMaxBodySzie(), getTransportCodec()));
				ch.pipeline().addLast(new NettyReceiveHandlerAdapter<>(getReceiveMessageHandler()));
			}
		};
	}

	@Override
	protected void doShutdown() {
		workerIoGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
	}
}
