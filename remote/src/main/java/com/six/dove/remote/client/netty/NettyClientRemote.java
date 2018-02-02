package com.six.dove.remote.client.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.remote.client.AbstractClientRemote;
import com.six.dove.remote.client.ClientRemoteConnection;
import com.six.dove.remote.compiler.Compiler;
import com.six.dove.remote.compiler.impl.JavaCompilerImpl;
import com.six.dove.remote.protocol.RemoteSerialize;
import com.six.dove.rpc.protocol.netty.NettyRpcDecoder;
import com.six.dove.rpc.protocol.netty.NettyRpcEncoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

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
public class NettyClientRemote extends AbstractClientRemote {

	final static Logger log = LoggerFactory.getLogger(NettyClientRemote.class);

	private NettyClientAcceptorIdleStateTrigger IdleStateTrigger = new NettyClientAcceptorIdleStateTrigger();

	private EventLoopGroup workerGroup;

	private static long DEFAULT_CALL_TIMEOUT = 6000;

	private static int WRITER_IDLE_TIME_SECONDES = 60;// 写操作空闲

	public NettyClientRemote() {
		this(0);
	}

	public NettyClientRemote(int workerGroupThreads) {
		this(new JavaCompilerImpl(), new RemoteSerialize() {
		}, workerGroupThreads, DEFAULT_CALL_TIMEOUT);
	}

	public NettyClientRemote(int workerGroupThreads, long callTimeout) {
		this(new JavaCompilerImpl(), new RemoteSerialize() {
		}, workerGroupThreads, callTimeout);
	}

	public NettyClientRemote(Compiler wrapperServiceProxyFactory, RemoteSerialize remoteSerialize,
			int workerGroupThreads, long callTimeout) {
		super("netty-rpc-client", wrapperServiceProxyFactory, remoteSerialize, callTimeout);
		workerGroup = new NioEventLoopGroup(workerGroupThreads < 0 ? 0 : workerGroupThreads);
	}

	@Override
	protected ClientRemoteConnection newRpcConnection(String callHost, int callPort) {
		final NettyConnectionImpl newClientToServerConnection = new NettyConnectionImpl(this, callHost, callPort);
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(workerGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true)
				// .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new IdleStateHandler(0, WRITER_IDLE_TIME_SECONDES, 0));
				ch.pipeline().addLast(IdleStateTrigger);
				ch.pipeline().addLast(new NettyRpcEncoder(getRemoteSerialize()));
				ch.pipeline().addLast(new NettyRpcDecoder(getRemoteSerialize()));
				ch.pipeline().addLast(newClientToServerConnection);
			}
		});
		bootstrap.connect(callHost, callPort);
		return newClientToServerConnection;
	}

	@Override
	protected void stop1() {
		if (null != workerGroup) {
			workerGroup.shutdownGracefully();
		}
	}
}
