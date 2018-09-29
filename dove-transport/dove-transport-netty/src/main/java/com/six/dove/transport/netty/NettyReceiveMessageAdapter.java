package com.six.dove.transport.netty;

import java.util.Objects;

import com.six.dove.transport.handler.ReceiveMessageHandler;
import com.six.dove.transport.message.Message;

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe netty handler适配器
 */
public class NettyReceiveMessageAdapter<M extends Message> extends SimpleChannelInboundHandler<M> {

	private ReceiveMessageHandler<NettyConnection, M> receiveMessageHandler;

	public NettyReceiveMessageAdapter(ReceiveMessageHandler<NettyConnection, M> receiveMessageHandler) {
		Objects.requireNonNull(receiveMessageHandler);
		this.receiveMessageHandler = receiveMessageHandler;
	}


	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.fireChannelActive();
		// 这里可以做接入连接限制
		NettyConnection nettyConnection = NettyConnectionUtils.channelToNettyConnection(ctx.channel());
		receiveMessageHandler.connActive(nettyConnection);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		NettyConnection nettyConnection = NettyConnectionUtils.channelToNettyConnection(ctx.channel());
		receiveMessageHandler.connInactive(nettyConnection);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, M message) {
		NettyConnection nettyConnection = NettyConnectionUtils.channelToNettyConnection(ctx.channel());
		receiveMessageHandler.receive(nettyConnection, message);
	}

	/**
	 * 高低水位控制
	 */
	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();
		ChannelConfig conf = ctx.channel().config();
		if (channel.isWritable()) {
			conf.setAutoRead(true);
		} else {
			conf.setAutoRead(false);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// receiveMessageHandler.exceptionCaught(ctx.channel(), cause);
	}
}
