package com.six.dove.transport.netty;

import java.util.Objects;

import com.six.dove.transport.AbstractConnection;
import com.six.dove.transport.protocol.TransportMessage;

import io.netty.channel.Channel;

/**
 * @author:MG01867
 * @date:2018年3月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class NettyConnection extends AbstractConnection{

	private Channel channel;
	
	public NettyConnection(Channel channel,String host, int port) {
		super(host, port);
		Objects.requireNonNull(channel);
		this.channel=channel;
	}

	@Override
	public boolean available() {
		channel.remoteAddress();
		return null!=channel&&channel.isActive();
	}

	@Override
	protected void doSend(TransportMessage data,SendListener sendListener) {
		channel.writeAndFlush(data);
	}
	
	@Override
	public void close() {
		if(null!=channel) {
			channel.close();
		}
	}
}
