package com.six.dove.transport.server;

import org.junit.Test;

import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.protocol.JavaTransportProtocol;
import com.six.dove.transport.protocol.Request;
import com.six.dove.transport.server.ReceiveMessageHandler;
import com.six.dove.transport.server.ServerTransport;
import com.six.dove.transport.server.sokcet.SocketServerTransport;
import com.six.dove.transport.socket.ServerSocketConnection;

/**
 * @author:MG01867
 * @date:2018年3月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class SocketServerTransportTest {

	@Test
	public void testStart() throws InterruptedException {
		String host = "127.0.0.1";
		int port = 8888;
		int maxMessageSize = 1000 * 1000 * 5;
		int workerIoThreads = 4;
		TransportCodec transportCodec = new JavaTransportProtocol(maxMessageSize);
		ReceiveMessageHandler<ServerSocketConnection, Request> receiveMessageHandler = new SocketServerReceiveMessageHandlerTest();
		ServerTransport<ServerSocketConnection, Request> serverTransport = new SocketServerTransport<>(host, port,
				transportCodec, receiveMessageHandler, workerIoThreads);
		serverTransport.start();
		synchronized (SocketServerTransportTest.class) {
			SocketServerTransportTest.class.wait();
		}
		serverTransport.shutdown();
	}
}
