package com.six.dove.transport.netty.server;


import org.junit.jupiter.api.Test;

import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.netty.client.JavaTransportProtocol;
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
public class NettyServerTransportTest {

	@Test
	public void testStart() throws InterruptedException {
		int port = 8888;
		int maxMessageSize = 1000 * 1000 * 5;
		int workerIoThreads = 4;
		int allIdleTimeSeconds = 60;
		TransportCodec<Response,Request> transportCodec = new JavaTransportProtocol<>();
		ReceiveMessageHandler<Request, Response> receiveMessageHandler = new NettyServerReceiveMessageHandlerTest();
		NettyServerTransport<Response,Request> serverTransport = new NettyServerTransport<>(port,workerIoThreads, allIdleTimeSeconds);
		serverTransport.setMaxBodySzie(maxMessageSize);
		serverTransport.setTransportCodec(transportCodec);
		serverTransport.setReceiveMessageHandler(receiveMessageHandler);
		serverTransport.start();
		synchronized (NettyServerTransportTest.class) {
			NettyServerTransportTest.class.wait();
		}
	}
}
