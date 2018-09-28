package com.six.dove.transport.netty.server;

import org.junit.Test;

import com.six.dove.transport.TransportCodec;
import com.six.dove.transport.netty.NettyConnection;
import com.six.dove.transport.protocol.JavaTransportProtocol;
import com.six.dove.transport.Request;
import com.six.dove.transport.ReceiveMessageHandler;

/**
 * @author:MG01867
 * @date:2018年3月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class NettyServerTransportTest {

	@Test
	public void testStart() throws InterruptedException {
		String host = "127.0.0.1";
		int port = 8888;
		int maxMessageSize = 1000 * 1000 * 5;
		int workerIoThreads = 4;
		int allIdleTimeSeconds = 60;
		TransportCodec transportCodec = new JavaTransportProtocol(maxMessageSize);
		ReceiveMessageHandler<NettyConnection, Request> receiveMessageHandler = new NettyServerReceiveMessageHandlerTest<>();
		NettyServerTransport<Request> serverTransport = new NettyServerTransport<>(host, port, transportCodec,
				receiveMessageHandler, workerIoThreads, allIdleTimeSeconds);
		serverTransport.start();
		synchronized (NettyServerTransportTest.class) {
			NettyServerTransportTest.class.wait();
		}
	}
}
