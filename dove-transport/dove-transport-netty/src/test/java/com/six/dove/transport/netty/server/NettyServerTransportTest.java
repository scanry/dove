package com.six.dove.transport.netty.server;



import org.junit.jupiter.api.Test;

import com.six.dove.transport.ReceiveMessageHandler;
import com.six.dove.transport.Request;
import com.six.dove.transport.Response;
import com.six.dove.transport.netty.JavaTransportProtocol;

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
		int workerIoThreads = 4;
		int allIdleTimeSeconds = 60;
		JavaTransportProtocol transportCodec = new JavaTransportProtocol();
		ReceiveMessageHandler<Request, Response> receiveMessageHandler = new NettyServerReceiveMessageHandlerTest();
		NettyServerTransport<Response,Request> serverTransport = new NettyServerTransport<>(port,workerIoThreads, allIdleTimeSeconds);
		serverTransport.setTransportCodec(transportCodec);
		serverTransport.setReceiveMessageHandler(receiveMessageHandler);
		serverTransport.start();
		System.out.println("netty服务端启动成功");
		synchronized (NettyServerTransportTest.class) {
			NettyServerTransportTest.class.wait();
		}
	}
}
