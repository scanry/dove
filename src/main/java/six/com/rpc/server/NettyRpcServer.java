package six.com.rpc.server;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
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
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import six.com.rpc.AbstractRemote;
import six.com.rpc.NettyConstant;
import six.com.rpc.RpcService;
import six.com.rpc.WrapperService;
import six.com.rpc.WrapperServiceProxyFactory;
import six.com.rpc.WrapperServiceTuple;
import six.com.rpc.protocol.RpcDecoder;
import six.com.rpc.protocol.RpcEncoder;
import six.com.rpc.protocol.RpcSerialize;
import six.com.rpc.proxy.JavaWrapperServiceProxyFactory;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:11:44
 */
public class NettyRpcServer extends AbstractRemote implements RpcServer {

	final static Logger log = LoggerFactory.getLogger(NettyRpcServer.class);

	public static final String OS_NAME = System.getProperty("os.name");

	private static boolean isLinuxPlatform = false;

	private static final int DEFAULT_EVENT_LOOP_THREADS;

	static {
		if (OS_NAME != null && OS_NAME.toLowerCase().contains("linux")) {
			isLinuxPlatform = true;
		}
		DEFAULT_EVENT_LOOP_THREADS = Math.max(1, NettyRuntime.availableProcessors() * 2);
	}

	private Map<String, WrapperServiceTuple> registerMap = new ConcurrentHashMap<>();

	private ServerAcceptorIdleStateTrigger idleStateTrigger = new ServerAcceptorIdleStateTrigger();

	private String loaclHost;

	private int trafficPort;

	private EventLoopGroup bossGroup;

	private EventLoopGroup workerIoGroup;

	private DefaultEventExecutorGroup workerCodeGroup;

	private ExecutorService defaultBizExecutorService;

	private ServerBootstrap serverBootstrap;

	private WrapperServiceProxyFactory wrapperServiceProxyFactory;

	private boolean useEpoll;

	public NettyRpcServer(String loaclHost, int trafficPort) {
		this(loaclHost, trafficPort, 0, 0, 0, new RpcSerialize() {
		});
	}

	public NettyRpcServer(String loaclHost, int trafficPort, int workerIoThreads, int workerCodeThreads,
			int workerBizThreads) {
		this(loaclHost, trafficPort, workerIoThreads, workerCodeThreads, workerBizThreads, new RpcSerialize() {
		});
	}

	public NettyRpcServer(String loaclHost, int trafficPort, int workerIoThreads, int workerCodeThreads,
			int workerBizThreads, RpcSerialize rpcSerialize) {
		super(rpcSerialize);
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
		defaultBizExecutorService = Executors.newFixedThreadPool(
				workerBizThreads <= 0 ? DEFAULT_EVENT_LOOP_THREADS : workerBizThreads, new ThreadFactory() {
					private AtomicInteger threadIndex = new AtomicInteger(0);

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "NettyRpcServer-worker-biz-thread_" + this.threadIndex.incrementAndGet());
					}
				});
		serverBootstrap = new ServerBootstrap();
		wrapperServiceProxyFactory = new JavaWrapperServiceProxyFactory();
	}

	@Override
	public void start() {
		serverBootstrap.group(bossGroup, workerIoGroup)
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
		try {
			Channel ch = serverBootstrap.bind(NettyRpcServer.this.loaclHost, NettyRpcServer.this.trafficPort).sync()
					.channel();
			ch.closeFuture().sync();
		} catch (InterruptedException e) {
			log.error("netty serverBootstrap err", e);
		} finally {
			workerIoGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	@Override
	public void register(Class<?> protocol, Object instance) {
		register(defaultBizExecutorService, protocol, instance);
	}

	@Override
	public void register(ExecutorService bizExecutorService, Class<?> protocol, Object instance) {
		Objects.requireNonNull(bizExecutorService, "bizExecutorService must not be null");
		Objects.requireNonNull(instance, "instance must not be null");
		if (null == protocol) {
			protocol = instance.getClass();
		} else {
			if (!protocol.isAssignableFrom(instance.getClass())) {
				throw new RuntimeException("protocolClass " + protocol.getName()
						+ " is not implemented by protocolImpl which is of class " + instance.getClass());
			}
		}
		String protocolName = protocol.getName();
		Method[] protocolMethods = protocol.getMethods();
		String methodName = null;
		WrapperService wrapperService = null;
		for (Method protocolMethod : protocolMethods) {
			RpcService rpcAnnotation = protocolMethod.getAnnotation(RpcService.class);
			methodName = null != rpcAnnotation ? rpcAnnotation.name() : protocolMethod.getName();
			final String serviceName = getServiceName(protocolName, methodName);
			wrapperService = wrapperServiceProxyFactory.newServerWrapperService(instance, protocolMethod);
			registerMap.put(serviceName, new WrapperServiceTuple(wrapperService, defaultBizExecutorService));
		}

	}

	@Override
	public WrapperServiceTuple getWrapperServiceTuple(String rpcServiceName) {
		return registerMap.get(rpcServiceName);
	}

	@Override
	public void remove(String rpcServiceName) {
		registerMap.remove(rpcServiceName);
	}

	@Override
	public ExecutorService getDefaultBizExecutorService() {
		return defaultBizExecutorService;
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
		if (null != workerCodeGroup) {
			workerCodeGroup.shutdownGracefully();
		}
		if (null != defaultBizExecutorService) {
			defaultBizExecutorService.shutdown();
		}
	}
}
