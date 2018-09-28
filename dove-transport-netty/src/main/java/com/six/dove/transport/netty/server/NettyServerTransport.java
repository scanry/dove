package com.six.dove.transport.netty.server;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.netty.NettyConnection;
import com.six.dove.transport.netty.NettyReceiveMessageAdapter;
import com.six.dove.transport.netty.coder.NettyRpcDecoderAdapter;
import com.six.dove.transport.netty.coder.NettyRpcEncoderAdapter;
import com.six.dove.transport.protocol.Request;
import com.six.dove.transport.server.AbstractServerTransport;
import com.six.dove.transport.server.ReceiveMessageHandler;

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
 * @author:MG01867
 * @date:2018年3月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class NettyServerTransport<MessageRequest extends Request> extends AbstractServerTransport<NettyConnection,MessageRequest>{

	final static Logger log = LoggerFactory.getLogger(NettyServerTransport.class);

	private static boolean isLinuxPlatform = false;
	static {
		String OS_NAME = System.getProperty("os.name");
		if (OS_NAME != null && OS_NAME.toLowerCase().contains("linux")) {
			isLinuxPlatform = true;
		}
	}

	private EventLoopGroup bossGroup;

	private EventLoopGroup workerIoGroup;

	private ServerBootstrap serverBootstrap;

	private boolean useEpoll;

	private int workerIoThreads;

	private int allIdleTimeSeconds;

	private Thread startThread;

	public NettyServerTransport(String host, int port, TransportCodec transportProtocol,
			ReceiveMessageHandler<NettyConnection,MessageRequest> receiveMessageHandler, int workerIoThreads, int allIdleTimeSeconds) {
		super(host, port, transportProtocol, receiveMessageHandler);
		this.workerIoThreads = workerIoThreads;
	}

	@Override
	protected void doStart(String host, int port) {
		bossGroup = new NioEventLoopGroup(1, new ThreadFactory() {
			private AtomicInteger threadIndex = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "NettyRpcServer-boss-io-thread_" + this.threadIndex.incrementAndGet());
			}
		});

		if (useEpoll()) {
			workerIoGroup = new EpollEventLoopGroup(workerIoThreads <= 0 ? 0 : workerIoThreads, new ThreadFactory() {
				private AtomicInteger threadIndex = new AtomicInteger(0);

				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, "NettyRpcServer-worker-io-thread_" + this.threadIndex.incrementAndGet());
				}
			});
		} else {
			workerIoGroup = new NioEventLoopGroup(workerIoThreads <= 0 ? 0 : workerIoThreads, new ThreadFactory() {
				private AtomicInteger threadIndex = new AtomicInteger(0);

				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, "NettyRpcServer-worker-io-thread_" + this.threadIndex.incrementAndGet());
				}
			});
		}
		serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(bossGroup, workerIoGroup).localAddress(new InetSocketAddress(host, port))
				.channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new IdleStateHandler(0, 0, allIdleTimeSeconds));
						ch.pipeline().addLast(new NettyServerAcceptorIdleStateTrigger());
						ch.pipeline().addLast(new NettyRpcEncoderAdapter(getTransportProtocol()));
						ch.pipeline().addLast(new NettyRpcDecoderAdapter(getTransportProtocol()));
						ch.pipeline().addLast(new NettyReceiveMessageAdapter<MessageRequest>(getReceiveMessageHandler()));
					}
				}).option(ChannelOption.SO_BACKLOG, 1024).option(ChannelOption.SO_REUSEADDR, true)
				.childOption(ChannelOption.SO_KEEPALIVE, false).option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		startThread = new Thread(() -> {
			try {
				ChannelFuture sync = serverBootstrap.bind().sync();
				sync.channel().closeFuture().sync();
			} catch (InterruptedException e) {
				log.error("netty serverBootstrap err", e);
			} finally {
				workerIoGroup.shutdownGracefully();
				bossGroup.shutdownGracefully();
			}
		}, "netty-start-thread");
		startThread.setDaemon(true);
		startThread.start();
	}

	private boolean useEpoll() {
		return isLinuxPlatform && useEpoll && Epoll.isAvailable();
	}

	@Override
	public void shutdown() {
		if (null != bossGroup) {
			bossGroup.shutdownGracefully();
		}
		if (null != workerIoGroup) {
			workerIoGroup.shutdownGracefully();
		}
	}
}
