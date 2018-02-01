package com.six.dove.remote.server.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.remote.compiler.Compiler;
import com.six.dove.remote.compiler.impl.JavaCompilerImpl;
import com.six.dove.remote.protocol.RemoteSerialize;
import com.six.dove.remote.server.AbstractServerRemote;
import com.six.dove.rpc.protocol.netty.NettyRpcDecoder;
import com.six.dove.rpc.protocol.netty.NettyRpcEncoder;

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
import io.netty.util.concurrent.DefaultEventExecutorGroup;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:11:44
 */
public class NettyServerRemote extends AbstractServerRemote {

	final static Logger log = LoggerFactory.getLogger(NettyServerRemote.class);

	public static final String OS_NAME = System.getProperty("os.name");

	private static boolean isLinuxPlatform = false;

	static {
		if (OS_NAME != null && OS_NAME.toLowerCase().contains("linux")) {
			isLinuxPlatform = true;
		}
	}

	private final static int ALL_IDLE_TIME_SECONDES = 60;// 读写全部空闲60秒

	private NettyServerAcceptorIdleStateTrigger idleStateTrigger = new NettyServerAcceptorIdleStateTrigger();

	private EventLoopGroup bossGroup;

	private EventLoopGroup workerIoGroup;

	private DefaultEventExecutorGroup workerCodeGroup;

	private ServerBootstrap serverBootstrap;

	private boolean useEpoll;

	private Thread startThread;

	public NettyServerRemote(String loaclHost, int trafficPort) {
		this(loaclHost, trafficPort, 0, 0, 0);
	}

	public NettyServerRemote(String loaclHost, int trafficPort, int workerIoThreads, int workerCodeThreads,
			int workerBizThreads) {
		this(new JavaCompilerImpl(), new RemoteSerialize() {
		}, loaclHost, trafficPort, workerIoThreads, workerCodeThreads, workerBizThreads);
	}

	public NettyServerRemote(Compiler compiler, RemoteSerialize remoteSerialize, String loaclHost, int trafficPort,
			int workerIoThreads, int workerCodeThreads, int workerBizThreads) {
		super("netty-rpc-server", loaclHost, trafficPort, compiler, remoteSerialize);
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
		workerCodeGroup = new DefaultEventExecutorGroup(
				workerCodeThreads <= 0 ? DEFAULT_EVENT_LOOP_THREADS : workerCodeThreads, new ThreadFactory() {
					private AtomicInteger threadIndex = new AtomicInteger(0);

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "NettyRpcServer-worker-code-thread_" + this.threadIndex.incrementAndGet());
					}
				});

		serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(bossGroup, workerIoGroup)
				.localAddress(new InetSocketAddress(getLocalHost(), getListenPort()))
				.channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(workerCodeGroup, new IdleStateHandler(0, 0, ALL_IDLE_TIME_SECONDES));
						ch.pipeline().addLast(workerCodeGroup, idleStateTrigger);
						ch.pipeline().addLast(workerCodeGroup, new NettyRpcEncoder(getRemoteSerialize()));
						ch.pipeline().addLast(workerCodeGroup, new NettyRpcDecoder(getRemoteSerialize()));
						ch.pipeline().addLast(workerCodeGroup, new NettyServerHandler(NettyServerRemote.this));
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

	}

	private boolean useEpoll() {
		return isLinuxPlatform && useEpoll && Epoll.isAvailable();
	}

	@Override
	protected void doStart() {
		startThread.start();
	}

	@Override
	protected void stop2() {
		if (null != bossGroup) {
			bossGroup.shutdownGracefully();
		}
		if (null != workerIoGroup) {
			workerIoGroup.shutdownGracefully();
		}
		if (null != workerCodeGroup) {
			workerCodeGroup.shutdownGracefully();
		}

	}
}
