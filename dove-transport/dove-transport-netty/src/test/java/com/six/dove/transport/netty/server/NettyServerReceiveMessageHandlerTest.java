package com.six.dove.transport.netty.server;

import java.util.concurrent.atomic.AtomicInteger;

import com.six.dove.transport.netty.NettyConnection;
import com.six.dove.transport.message.Request;
import com.six.dove.transport.message.Response;
import com.six.dove.transport.handler.ReceiveMessageHandler;

/**
 * @author:MG01867
 * @date:2018年3月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class NettyServerReceiveMessageHandlerTest<MessageRequest extends Request>
		implements ReceiveMessageHandler<NettyConnection, MessageRequest> {

	private AtomicInteger count = new AtomicInteger(0);

	@Override
	public void connActive(NettyConnection connection) {
		System.out.println("server connection from " + connection.toString());
	}

	@Override
	public void receive(NettyConnection connection, MessageRequest message) {
		System.out.println("server receive message:" + count.getAndIncrement());
		connection.send(new Response());
	}

	@Override
	public void connInactive(NettyConnection connection) {
		System.out.println("server connInactive from " + connection.toString());
	}

	@Override
	public void exceptionCaught(NettyConnection connection, Throwable cause) {
		System.out.println("server exceptionCaught from " + connection.toString());
		cause.printStackTrace();
	}

}
