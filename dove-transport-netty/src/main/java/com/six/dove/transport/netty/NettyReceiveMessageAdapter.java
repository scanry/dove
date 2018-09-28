package com.six.dove.transport.netty;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.transport.Connection;
import com.six.dove.transport.Transport;
import com.six.dove.transport.protocol.TransportMessage;
import com.six.dove.transport.server.ReceiveMessageHandler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月21日 上午10:34:59
 *
 */
public class NettyReceiveMessageAdapter<Message extends TransportMessage>
		extends SimpleChannelInboundHandler<TransportMessage> {

	final static Logger log = LoggerFactory.getLogger(NettyReceiveMessageAdapter.class);

	private Transport<NettyConnection, Message> transport;
	private ReceiveMessageHandler<NettyConnection, Message> receiveMessageHandler;

	public NettyReceiveMessageAdapter(ReceiveMessageHandler<NettyConnection, Message> receiveMessageHandler) {
		Objects.requireNonNull(receiveMessageHandler);
		this.receiveMessageHandler = receiveMessageHandler;
	}

	protected String getNettyConnectionId(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();
		String remoteAddress = channel.remoteAddress().toString();
		String[] remoteAddressArray = remoteAddress.split(":");
		return Connection.newId(remoteAddressArray[0], Integer.valueOf(remoteAddressArray[0]));
	}

	private static NettyConnection channelToNettyConnection(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();
		String remoteAddress = channel.remoteAddress().toString();
		String[] remoteAddressArray = remoteAddress.split(":");
		NettyConnection nettyConnection = new NettyConnection(channel, remoteAddressArray[0],
				Integer.valueOf(remoteAddressArray[0]));
		return nettyConnection;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelActive();
		// 这里可以做接入连接限制
		String connectionId = null;
		NettyConnection nettyConnection = transport.getConnection(connectionId);
		if (null == nettyConnection) {
			nettyConnection = channelToNettyConnection(ctx);
			transport.addConnection(nettyConnection);
		}
		receiveMessageHandler.connActive(nettyConnection);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		NettyConnection nettyConnection = transport.getConnection(getNettyConnectionId(ctx));
		if (null != nettyConnection) {
			receiveMessageHandler.connInactive(nettyConnection);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TransportMessage message) throws Exception {
		if (null != message) {
			NettyConnection nettyConnection = transport.getConnection(getNettyConnectionId(ctx));
			if (null != nettyConnection) {
				receiveMessageHandler.receive(nettyConnection, (Message) message);
			}
		}
	}

	/**
	 * 高低水位控制
	 */
	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		ChannelConfig conf = ctx.channel().config();
		if (channel.isWritable()) {
			conf.setAutoRead(true);
		} else {
			conf.setAutoRead(false);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// receiveMessageHandler.exceptionCaught(ctx.channel(), cause);
	}
}
