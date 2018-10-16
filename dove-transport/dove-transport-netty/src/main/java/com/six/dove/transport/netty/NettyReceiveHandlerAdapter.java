package com.six.dove.transport.netty;

import java.util.Objects;

import com.six.dove.transport.Message;
import com.six.dove.transport.ReceiveMessageHandler;

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
public class NettyReceiveHandlerAdapter<SendMsg extends Message, ReceMsg extends Message>
		extends SimpleChannelInboundHandler<Message> {

	private ReceiveMessageHandler<ReceMsg,SendMsg> receiveMessageHandler;

	public NettyReceiveHandlerAdapter(
			ReceiveMessageHandler<ReceMsg,SendMsg> receiveMessageHandler) {
		Objects.requireNonNull(receiveMessageHandler);
		this.receiveMessageHandler = receiveMessageHandler;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		// 这里可以做接入连接限制
		receiveMessageHandler.connActive(NettyConnection.channelToNettyConnection(ctx.channel()));
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		NettyConnection<SendMsg> nettyConnection = NettyConnection.channelToNettyConnection(ctx.channel());
		receiveMessageHandler.connInactive(nettyConnection);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Message message) {
		NettyConnection<SendMsg> nettyConnection = NettyConnection.channelToNettyConnection(ctx.channel());
		receiveMessageHandler.receive(nettyConnection,(ReceMsg)message);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// receiveMessageHandler.exceptionCaught(ctx.channel(), cause);
	}
}
