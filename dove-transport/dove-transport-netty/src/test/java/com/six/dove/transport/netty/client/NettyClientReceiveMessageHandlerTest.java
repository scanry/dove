package com.six.dove.transport.netty.client;

import java.util.concurrent.atomic.AtomicInteger;

import com.six.dove.transport.netty.NettyConnection;
import com.six.dove.transport.Response;
import com.six.dove.transport.ReceiveMessageHandler;

/**
 * @author:MG01867
 * @date:2018年3月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class NettyClientReceiveMessageHandlerTest<MessageResponse extends Response>
		implements ReceiveMessageHandler<NettyConnection, MessageResponse> {

	private AtomicInteger count = new AtomicInteger(0);

	@Override
	public void connActive(NettyConnection connection) {
		System.out.println("client connection from " + connection.toString());
	}

	@Override
	public void receive(NettyConnection connection, Response message) {
		System.out.println("client receive message:" + count.getAndIncrement());
	}

	@Override
	public void connInactive(NettyConnection connection) {
		System.out.println("client connInactive from " + connection.toString());
	}

	@Override
	public void exceptionCaught(NettyConnection connection, Throwable cause) {
		System.out.println("clientexceptionCaught from " + connection.toString());
		cause.printStackTrace();
	}

}
