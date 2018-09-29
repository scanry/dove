package com.six.dove.transport.netty.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.six.dove.transport.*;
import com.six.dove.transport.client.AbstractClientTransport;
import com.six.dove.transport.client.ClientTransport;
import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.connection.ConnectionPool;
import com.six.dove.transport.handler.ReceiveMessageHandler;
import com.six.dove.transport.message.Response;
import com.six.dove.transport.netty.NettyConnection;
import com.six.dove.transport.netty.NettyReceiveMessageAdapter;
import com.six.dove.transport.netty.codec.NettyRpcDecoderAdapter;
import com.six.dove.transport.netty.codec.NettyRpcEncoderAdapter;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author:MG01867
 * @date: 2018年5月9日
 * @email: 359852326@qq.com
 * @version:
 * @describe netty-客户 传输端基类
 */
public class NettyClientTransport<M extends Response> extends AbstractClientTransport<NettyConnection, M> {

	private EventLoopGroup workerGroup;

	public NettyClientTransport(int workerGroupThreads, ConnectionPool<NettyConnection> connectionPool,
			TransportCodec transportProtocol, ReceiveMessageHandler<NettyConnection, M> receiveMessageHandler) {
		this(workerGroupThreads, ClientTransport.DEFAULT_CONNECT_TIMEOUT, ClientTransport.DEFAULT_SEND_TIMEOUT,
				ClientTransport.DEFAULT_IDLE_TIME, connectionPool, transportProtocol, receiveMessageHandler);
	}

	public NettyClientTransport(int workerGroupThreads, long connectTimeout, long sendTimeout, long writerIdleTime,
			ConnectionPool<NettyConnection> connectionPool, TransportCodec transportProtocol,
			ReceiveMessageHandler<NettyConnection, M> receiveMessageHandler) {
		super(connectTimeout, sendTimeout, writerIdleTime, connectionPool, transportProtocol, receiveMessageHandler);
		if (workerGroupThreads <= 0) {
			throw new IllegalArgumentException(
					String.format("The workerGroupThreads[%s] must greater than 0", workerGroupThreads));
		}
		workerGroup = new NioEventLoopGroup(workerGroupThreads);
	}

	@Override
	protected NettyConnection newConnection(String host, int port) {
		CountDownLatch cdl = new CountDownLatch(1);
		ClientNettyReceiveMessageAdapter clientNettyReceiveMessageAdapter = new ClientNettyReceiveMessageAdapter(cdl,
				getReceiveMessageHandler());
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(workerGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) {
				ch.pipeline().addLast(new IdleStateHandler(0, (int) getWriterIdleTime(), 0));
				ch.pipeline().addLast(new NettyClientAcceptorIdleStateTrigger());
				ch.pipeline().addLast(new NettyRpcEncoderAdapter(getTransportProtocol()));
				ch.pipeline().addLast(new NettyRpcDecoderAdapter(getTransportProtocol()));
				ch.pipeline().addLast(clientNettyReceiveMessageAdapter);
			}
		});
		bootstrap.connect(host, port);
		try {
			cdl.await(getConnectTimeout(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// ignore
		}
		return new NettyConnection(clientNettyReceiveMessageAdapter.channel, new NetAddress(host, port));
	}

	class ClientNettyReceiveMessageAdapter extends NettyReceiveMessageAdapter<M> {

		private CountDownLatch cdl;
		private Channel channel;

		ClientNettyReceiveMessageAdapter(CountDownLatch cdl,
				ReceiveMessageHandler<NettyConnection, M> receiveMessageHandler) {
			super(receiveMessageHandler);
			this.cdl = cdl;
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) {
			super.channelActive(ctx);
			this.channel=ctx.channel();
			cdl.countDown();
		}

	}

	@Override
	protected void doShutdown() {
        workerGroup.shutdownGracefully();
	}

}
