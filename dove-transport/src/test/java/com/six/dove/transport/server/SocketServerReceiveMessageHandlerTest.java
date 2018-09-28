package com.six.dove.transport.server;

import java.util.concurrent.atomic.AtomicInteger;

import com.six.dove.transport.protocol.Request;
import com.six.dove.transport.protocol.Response;
import com.six.dove.transport.server.ReceiveMessageHandler;
import com.six.dove.transport.socket.ServerSocketConnection;

/**
 * @author:MG01867
 * @date:2018年3月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class SocketServerReceiveMessageHandlerTest implements ReceiveMessageHandler<ServerSocketConnection, Request> {

	private AtomicInteger count = new AtomicInteger(0);

	@Override
	public void connActive(ServerSocketConnection connection) {
		System.out.println("server connection from " + connection.toString());
	}

	@Override
	public void receive(ServerSocketConnection connection, Request message) {
		System.out.println("server receive message:" + count.getAndIncrement());
		connection.send(new Response());
	}

	@Override
	public void connInactive(ServerSocketConnection connection) {
		System.out.println("server connInactive from " + connection.toString());
	}

	@Override
	public void exceptionCaught(ServerSocketConnection connection, Throwable cause) {
		System.out.println("server exceptionCaught from " + connection.toString());
		cause.printStackTrace();
	}

}
