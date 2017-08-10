package six.com.rpc.server;


import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import six.com.rpc.AbstractRemote;
import six.com.rpc.NettyConstant;
import six.com.rpc.RpcService;
import six.com.rpc.WrapperService;
import six.com.rpc.exception.RpcInvokeException;
import six.com.rpc.protocol.RpcDecoder;
import six.com.rpc.protocol.RpcEncoder;
import six.com.rpc.protocol.RpcSerialize;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:11:44
 */
public class NettyRpcServer extends AbstractRemote implements RpcServer {

	final static Logger log = LoggerFactory.getLogger(NettyRpcServer.class);

	private Map<String, WrapperService> registerMap = new ConcurrentHashMap<String, WrapperService>();

	private ServerAcceptorIdleStateTrigger idleStateTrigger = new ServerAcceptorIdleStateTrigger();

	private String loaclHost;

	private int trafficPort;

	private Thread thread;

	private EventLoopGroup bossGroup;

	private EventLoopGroup workerGroup;

	private ServerBootstrap serverBootstrap;

	public NettyRpcServer(String loaclHost, int trafficPort) {
		this(loaclHost, trafficPort, 0, 0, new RpcSerialize() {
		});
	}

	public NettyRpcServer(String loaclHost, int trafficPort, int bossGroupThreads, int workerGroupThreads,
			RpcSerialize rpcSerialize) {
		super(rpcSerialize);
		this.loaclHost = loaclHost;
		this.trafficPort = trafficPort;
		bossGroup = new NioEventLoopGroup(bossGroupThreads < 0 ? 0 : bossGroupThreads);
		workerGroup = new NioEventLoopGroup(workerGroupThreads < 0 ? 0 : workerGroupThreads);
		serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new IdleStateHandler(0, 0, NettyConstant.ALL_IDLE_TIME_SECONDES));
						ch.pipeline().addLast(idleStateTrigger);
						ch.pipeline().addLast(new RpcEncoder(getRpcSerialize()));
						ch.pipeline().addLast(new RpcDecoder(getRpcSerialize()));
						ch.pipeline().addLast(new ServerHandler(NettyRpcServer.this));
					}
				}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
		thread = new Thread(new Runner());
		thread.setDaemon(true);
		thread.start();
	}

	static class WrapperServiceImpl implements WrapperService {

		Object tagetOb;
		Method method;

		public WrapperServiceImpl(Object tagetOb, Method method) {
			this.tagetOb = tagetOb;
			this.method = method;
		}

		@Override
		public Object invoke(Object[] paras) {
			try {
				return method.invoke(tagetOb, paras);
			} catch (Exception e) {
				throw new RpcInvokeException(e);
			}
		}

	}


	@Override
	public void register(Class<?> protocol, Object instance) {
		Objects.requireNonNull(instance, "tagetOb must not be null");
		if(null==protocol){
			protocol=instance.getClass();
		}else{
			if (!protocol.isAssignableFrom(instance.getClass())) {
				throw new RuntimeException("protocolClass " + protocol.getName()
						+ " is not implemented by protocolImpl which is of class " + instance.getClass());
			}
		}
		String protocolName = protocol.getName();
		Method[] protocolMethods = protocol.getMethods();
		String methodName=null;
		for (Method protocolMethod : protocolMethods) {
			RpcService rpcAnnotation = protocolMethod.getAnnotation(RpcService.class);
			methodName=null!=rpcAnnotation?rpcAnnotation.name():protocolMethod.getName();
			final String serviceName = getServiceName(protocolName, methodName);
			registerMap.put(serviceName, new WrapperServiceImpl(instance, protocolMethod));
			log.info("register rpc service:" + serviceName);
		}
	}

	@Override
	public WrapperService get(String rpcServiceName) {
		return registerMap.get(rpcServiceName);
	}

	@Override
	public void remove(String rpcServiceName) {
		registerMap.remove(rpcServiceName);
	}

	class Runner implements Runnable {
		@Override
		public void run() {
			try {
				Channel ch = serverBootstrap.bind(loaclHost, trafficPort).sync().channel();
				ch.closeFuture().sync();
			} catch (InterruptedException e) {
				log.error("netty serverBootstrap err", e);
			} finally {
				workerGroup.shutdownGracefully();
				bossGroup.shutdownGracefully();
			}
		}
	}

	@PreDestroy
	public void destroy() {
		if (null != workerGroup) {
			workerGroup.shutdownGracefully();
		}
		if (null != bossGroup) {
			bossGroup.shutdownGracefully();
		}
	}
}
