package six.com.rpc.client;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import six.com.rpc.AsyCallback;
import six.com.rpc.NettyConstant;
import six.com.rpc.RpcClient;
import six.com.rpc.RemoteInvokeProxyFactory;
import six.com.rpc.exception.RpcException;
import six.com.rpc.exception.RpcInvokeException;
import six.com.rpc.exception.RpcNotFoundServiceException;
import six.com.rpc.exception.RpcRejectServiceException;
import six.com.rpc.exception.RpcTimeoutException;
import six.com.rpc.protocol.RpcDecoder;
import six.com.rpc.protocol.RpcEncoder;
import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.protocol.RpcResponse;
import six.com.rpc.protocol.RpcResponseStatus;
import six.com.rpc.protocol.RpcSerialize;
import six.com.rpc.proxy.JavaRemoteInvokeProxyFactory;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:11:07
 * 
 *       基于netty 4.19final 实现的 简易 rpc 调用客户端
 * 
 *       <p>
 *       注意:
 *       </p>
 *       <p>
 *       所有rpc服务调用都有可能抛出一下异常:
 *       </p>
 *       <p>
 *       1.链接或者请求超时
 *       </p>
 *       <p>
 *       2.服务拒绝
 *       </p>
 *       <p>
 *       3.未发现服务
 *       </p>
 *       <p>
 *       4.执行异常
 *       </p>
 * 
 *       <p>
 *       具体参考 six.com.crawler.rpc.exception 包下的异常
 *       </p>
 * 
 * 
 */
public class NettyRpcCilent extends AbstractClient implements RpcClient {

	final static Logger log = LoggerFactory.getLogger(NettyRpcCilent.class);

	private ClientAcceptorIdleStateTrigger IdleStateTrigger = new ClientAcceptorIdleStateTrigger();

	private EventLoopGroup workerGroup;
	/**
	 * 链接池
	 */
	private ConnectionPool<ClientToServerConnection> pool;

	/**
	 * 用来存放服务，
	 */
	private Map<String, Object> serviceWeakHashMap;
	// 请求超时时间 10秒
	private long callTimeout = 30000;
	// 建立连接超时时间 60秒
	private long connectionTimeout = 10000;

	public NettyRpcCilent() {
		this(0);
	}

	public NettyRpcCilent(int workerGroupThreads) {
		this(new JavaRemoteInvokeProxyFactory(), new RpcSerialize() {
		}, workerGroupThreads);
	}

	public NettyRpcCilent(RemoteInvokeProxyFactory wrapperServiceProxyFactory, RpcSerialize rpcSerialize,
			int workerGroupThreads) {
		super(wrapperServiceProxyFactory, rpcSerialize);
		workerGroup = new NioEventLoopGroup(workerGroupThreads < 0 ? 0 : workerGroupThreads);
		pool = new ConnectionPool<>();
		serviceWeakHashMap = Collections.synchronizedMap(new java.util.WeakHashMap<>());
	}

	@Override
	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz, final AsyCallback asyCallback) {
		checkParma(targetHost, targetPort, clz);
		return getRemoteInvokeProxyFactory().newClientInterfaceWrapperInstance(this, targetHost, targetPort, clz,
				asyCallback);
	}

	@SuppressWarnings("unchecked")
	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz) {
		checkParma(targetHost, targetPort, clz);
		String key = serviceKey(targetHost, targetPort, clz);
		Object service = serviceWeakHashMap.computeIfAbsent(key, mapkey -> {
			return getRemoteInvokeProxyFactory().newClientInterfaceWrapperInstance(this, targetHost, targetPort, clz,
					null);
		});
		return (T) service;
	}

	public String buildClass(Class<?> clz) {
		return null;
	}

	private void checkParma(String targetHost, int targetPort, Class<?> clz) {
		if (null == targetHost || targetHost.trim().length() == 0) {
			throw new IllegalArgumentException("this targetHost must be not blank");
		}
		if (1 > targetPort || 65535 < targetPort) {
			throw new IllegalArgumentException("this targetPort[" + targetPort + "] is illegal");
		}
		if (!clz.isInterface()) {
			throw new IllegalArgumentException("this clz[" + clz.getName() + "] is not tnterface");
		}
	}

	/**
	 * rpc service key=目标host+:+目标端口+service class name
	 * 
	 * @param targetHost
	 * @param targetPort
	 * @param clz
	 * @return
	 */
	private String serviceKey(String targetHost, int targetPort, Class<?> clz) {
		String key = targetHost + ":" + targetPort + "/" + clz.getName();
		return key;
	}

	@Override
	public RpcResponse execute(RpcRequest rpcRequest) {
		WrapperFuture wrapperFuture = null;
		ClientToServerConnection clientToServerConnection = null;
		try {
			clientToServerConnection = findHealthyNettyConnection(rpcRequest);
		} catch (Exception e) {
			if (null != rpcRequest.getAsyCallback()) {
				rpcRequest.getAsyCallback().execute(RpcResponse.CONNECT_FAILED);
				return RpcResponse.CONNECT_FAILED;
			} else {
				throw new RpcException(e);
			}
		}
		try {
			wrapperFuture = clientToServerConnection.send(rpcRequest, callTimeout);
		} catch (Exception e) {
			clientToServerConnection.removeWrapperFuture(rpcRequest.getId());
			throw new RpcException(e);
		}
		if (!wrapperFuture.hasAsyCallback()) {
			RpcResponse rpcResponse = wrapperFuture.getResult(callTimeout);
			if (null == rpcResponse) {
				clientToServerConnection.removeWrapperFuture(rpcRequest.getId());
				throw new RpcTimeoutException(
						"execute rpcRequest[" + rpcRequest.toString() + "] timeout[" + callTimeout + "]");
			} else if (rpcResponse.getStatus() == RpcResponseStatus.UNFOUND_SERVICE) {
				throw new RpcNotFoundServiceException(rpcResponse.getMsg());
			} else if (rpcResponse.getStatus() == RpcResponseStatus.REJECT) {
				throw new RpcRejectServiceException(rpcResponse.getMsg());
			} else if (rpcResponse.getStatus() == RpcResponseStatus.INVOKE_ERR) {
				throw new RpcInvokeException(rpcResponse.getMsg());
			} else {
				return rpcResponse;
			}
		} else {
			return null;
		}
	}

	/**
	 * 获取可用的netty 链接
	 * 
	 * @param rpcRequest
	 * @return
	 */
	private ClientToServerConnection findHealthyNettyConnection(RpcRequest rpcRequest) {
		String callHost = rpcRequest.getCallHost();
		int callPort = rpcRequest.getCallPort();
		String findKey = NettyConnection.getNewConnectionKey(callHost, callPort);
		ClientToServerConnection clientToServerConnection = pool.find(findKey);
		if (null == clientToServerConnection) {
			synchronized (pool) {
				clientToServerConnection = pool.find(findKey);
				if (null == clientToServerConnection) {
					clientToServerConnection = newNettyConnection(callHost, callPort);
				}
			}
		}
		return clientToServerConnection;
	}

	private ClientToServerConnection newNettyConnection(String callHost, int callPort) {
		final ClientToServerConnection newClientToServerConnection = new ClientToServerConnection(NettyRpcCilent.this,
				callHost, callPort);
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(workerGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true)
				// .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new IdleStateHandler(0, NettyConstant.WRITER_IDLE_TIME_SECONDES, 0));
				ch.pipeline().addLast(IdleStateTrigger);
				ch.pipeline().addLast(new RpcEncoder(getRpcSerialize()));
				ch.pipeline().addLast(new RpcDecoder(getRpcSerialize()));
				ch.pipeline().addLast(newClientToServerConnection);
			}
		});
		bootstrap.connect(callHost, callPort);
		long startTime = System.currentTimeMillis();
		// 判断是否可用，如果不可用等待可用直到超时
		while (!newClientToServerConnection.available()) {
			long spendTime = System.currentTimeMillis() - startTime;
			if (spendTime > connectionTimeout) {
				newClientToServerConnection.close();
				throw new RpcTimeoutException("connected " + callHost + ":" + callPort + " timeout:" + spendTime);
			}
		}
		pool.put(newClientToServerConnection);
		return newClientToServerConnection;
	}

	public long getCallTimeout() {
		return callTimeout;
	}

	public void removeConnection(ClientToServerConnection connection) {
		pool.remove(connection);
	}

	@Override
	public void shutdown() {
		if (null != workerGroup) {
			workerGroup.shutdownGracefully();
		}
	}
}
