package six.com.rpc.client;

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
import six.com.rpc.RpcClient;
import six.com.rpc.common.NettyConstant;
import six.com.rpc.compiler.JavaCompilerImpl;
import six.com.rpc.Compiler;
import six.com.rpc.protocol.RpcDecoder;
import six.com.rpc.protocol.RpcEncoder;
import six.com.rpc.protocol.RpcSerialize;

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

	private static long DEFAULT_CALL_TIMEOUT = 6000;

	public NettyRpcCilent() {
		this(0);
	}

	public NettyRpcCilent(int workerGroupThreads) {
		this(new JavaCompilerImpl(), new RpcSerialize() {
		}, workerGroupThreads, DEFAULT_CALL_TIMEOUT);
	}

	public NettyRpcCilent(int workerGroupThreads, long callTimeout) {
		this(new JavaCompilerImpl(), new RpcSerialize() {
		}, workerGroupThreads, callTimeout);
	}

	public NettyRpcCilent(Compiler wrapperServiceProxyFactory, RpcSerialize rpcSerialize, int workerGroupThreads,
			long callTimeout) {
		super(wrapperServiceProxyFactory, rpcSerialize, callTimeout);
		workerGroup = new NioEventLoopGroup(workerGroupThreads < 0 ? 0 : workerGroupThreads);
	}

	@Override
	protected RpcConnection newRpcConnection(String id, String callHost, int callPort) {
		final ClientToServerConnection newClientToServerConnection = new ClientToServerConnection(this, id, callHost,
				callPort);
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
		return newClientToServerConnection;
	}

	@Override
	public void shutdown() {
		if (null != workerGroup) {
			workerGroup.shutdownGracefully();
		}
	}
}
