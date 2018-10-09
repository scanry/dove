package com.six.dove.transport.netty.server;

import java.util.concurrent.atomic.AtomicInteger;

import com.six.dove.transport.message.Request;
import com.six.dove.transport.message.Response;
import com.six.dove.transport.connection.Connection;
import com.six.dove.transport.handler.ReceiveMessageHandler;

/**
 * @author:MG01867
 * @date:2018年3月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class NettyServerReceiveMessageHandlerTest
		implements ReceiveMessageHandler<Request, Response> {

	private AtomicInteger count = new AtomicInteger(0);

	@Override
	public void connActive(Connection<Response> connection) {
		System.out.println("server connection from " + connection.toString());
	}

	@Override
	public void receive(Connection<Response> connection, Request message) {
		System.out.println("server receive message:" + count.getAndIncrement());
		connection.send(new Response());
	}

	@Override
	public void connInactive(Connection<Response> connection) {
		System.out.println("server connInactive from " + connection.toString());
	}

	@Override
	public void exceptionCaught(Connection<Response> connection, Exception cause) {
		System.out.println("server exceptionCaught from " + connection.toString());
		cause.printStackTrace();
	}

}
