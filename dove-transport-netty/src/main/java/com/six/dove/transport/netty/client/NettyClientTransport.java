package com.six.dove.transport.netty.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.six.dove.transport.Connection;
import com.six.dove.transport.client.AbstractClientTransport;
import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.netty.NettyConnection;
import com.six.dove.transport.netty.NettyReceiveMessageAdapter;
import com.six.dove.transport.netty.coder.NettyRpcDecoderAdapter;
import com.six.dove.transport.netty.coder.NettyRpcEncoderAdapter;
import com.six.dove.transport.protocol.Response;
import com.six.dove.transport.server.ReceiveMessageHandler;

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
 * @date:2018年3月27日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class NettyClientTransport<MessageResponse extends Response> extends AbstractClientTransport<NettyConnection,MessageResponse> {

	private EventLoopGroup workerGroup;

	public NettyClientTransport(int workerGroupThreads, int connectTimeout,int writerIdleTime, TransportCodec transportProtocol,
			ReceiveMessageHandler<NettyConnection,MessageResponse> receiveMessageHandler) {
		super(connectTimeout,writerIdleTime, transportProtocol, receiveMessageHandler);
		workerGroup = new NioEventLoopGroup(workerGroupThreads < 0 ? 0 : workerGroupThreads);
	}

	@Override
	protected Connection newConnection(String host, int port) {
		CountDownLatch cdl = new CountDownLatch(1);
		ClientNettyReceiveMessageAdapter clientNettyReceiveMessageAdapter = new ClientNettyReceiveMessageAdapter(cdl,
				getReceiveMessageHandler());
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(workerGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true)
				// .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new IdleStateHandler(0, getWriterIdleTime(), 0));
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
		NettyConnection nettyConnection = new NettyConnection(clientNettyReceiveMessageAdapter.channel, host, port);
		return nettyConnection;
	}

	class ClientNettyReceiveMessageAdapter extends NettyReceiveMessageAdapter<MessageResponse>{

		private CountDownLatch cdl;
		private Channel channel;

		public ClientNettyReceiveMessageAdapter(CountDownLatch cdl,
				ReceiveMessageHandler<NettyConnection,MessageResponse> receiveMessageHandler) {
			super(receiveMessageHandler);
			this.cdl=cdl;
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			this.channel = ctx.channel();
			cdl.countDown();
		}

	}

	@Override
	protected void doShutdown() {
		if (null != workerGroup) {
			workerGroup.shutdownGracefully();
		}
	}

}
