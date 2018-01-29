package six.com.rpc.server;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import six.com.rpc.Compiler;
import six.com.rpc.RpcServer;
import six.com.rpc.common.NettyConstant;
import six.com.rpc.compiler.JavaCompilerImpl;
import six.com.rpc.protocol.RpcDecoder;
import six.com.rpc.protocol.RpcEncoder;
import six.com.rpc.protocol.RpcSerialize;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:11:44
 */
public class NettyRpcServer extends AbstractServer implements RpcServer {

	final static Logger log = LoggerFactory.getLogger(NettyRpcServer.class);

	public static final String OS_NAME = System.getProperty("os.name");

	private static boolean isLinuxPlatform = false;

	static {
		if (OS_NAME != null && OS_NAME.toLowerCase().contains("linux")) {
			isLinuxPlatform = true;
		}
	}

	private ServerAcceptorIdleStateTrigger idleStateTrigger = new ServerAcceptorIdleStateTrigger();

	private String loaclHost;

	private int trafficPort;

	private EventLoopGroup bossGroup;

	private EventLoopGroup workerIoGroup;

	private DefaultEventExecutorGroup workerCodeGroup;

	private ServerBootstrap serverBootstrap;

	private boolean useEpoll;

	private Thread startThread;

	public NettyRpcServer(String loaclHost, int trafficPort) {
		this(loaclHost, trafficPort, 0, 0, 0);
	}

	public NettyRpcServer(String loaclHost, int trafficPort, int workerIoThreads, int workerCodeThreads,
			int workerBizThreads) {
		this(new JavaCompilerImpl(), new RpcSerialize() {
		}, loaclHost, trafficPort, workerIoThreads, workerCodeThreads, workerBizThreads);
	}

	public NettyRpcServer(Compiler compiler, RpcSerialize rpcSerialize, String loaclHost, int trafficPort,
			int workerIoThreads, int workerCodeThreads, int workerBizThreads) {
		super(compiler, rpcSerialize);
		this.loaclHost = loaclHost;
		this.trafficPort = trafficPort;
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
				.localAddress(new InetSocketAddress(NettyRpcServer.this.loaclHost, NettyRpcServer.this.trafficPort))
				.channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(workerCodeGroup,
								new IdleStateHandler(0, 0, NettyConstant.ALL_IDLE_TIME_SECONDES));
						ch.pipeline().addLast(workerCodeGroup, idleStateTrigger);
						ch.pipeline().addLast(workerCodeGroup, new RpcEncoder(getRpcSerialize()));
						ch.pipeline().addLast(workerCodeGroup, new RpcDecoder(getRpcSerialize()));
						ch.pipeline().addLast(workerCodeGroup, new ServerHandler(NettyRpcServer.this));
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

	@Override
	protected void doStart() {
		startThread.start();
	}

	private boolean useEpoll() {
		return isLinuxPlatform && useEpoll && Epoll.isAvailable();
	}

	@Override
	protected void doShutdown() {
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
