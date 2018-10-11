package com.six.dove.transport.netty.client;

import com.six.dove.transport.connection.Connection;

import org.junit.jupiter.api.Test;

import com.six.dove.transport.Request;
import com.six.dove.transport.Response;
import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.handler.ReceiveMessageHandler;


/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class NettyClientTransportTest {

	@Test
	public void testStart() throws InterruptedException {
		String host = "127.0.0.1";
		int port = 8888;
		int workerIoThreads = 4;
		
		ReceiveMessageHandler<Response,Request> receiveMessageHandler = new NettyClientReceiveMessageHandlerTest();
		TransportCodec<Request,Response> transportCodec = new JavaTransportProtocol<>();
		NettyClientTransport<Request,Response> nettyClientTransport = new NettyClientTransport<>(workerIoThreads);
		nettyClientTransport.setTransportCodec(transportCodec);
		nettyClientTransport.setReceiveMessageHandler(receiveMessageHandler);
		Connection<Request> connection = nettyClientTransport.connect(host, port);
		Request data=new Request();
		connection.send(data, sendFutrue -> {
			if(sendFutrue.isSucceed()) {
				System.out.println("发送成功");
			}else {
				System.out.println("发送失败");
			}
		});
		synchronized (transportCodec) {
			transportCodec.wait(300000000);
		}
	}
}
