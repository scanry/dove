package com.six.dove.transport.netty.client;

import com.six.dove.transport.connection.Connection;
import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.netty.NettyConnection;
import com.six.dove.transport.message.Request;
import com.six.dove.transport.message.Response;
import com.six.dove.transport.message.Message;
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
		int maxMessageSize = 1000 * 1000 * 5;
		int workerIoThreads = 4;
		int allIdleTimeSeconds = 60;
		int connectTimeout = 3000;
		
		ReceiveMessageHandler<NettyConnection,Response> receiveMessageHandler = new NettyClientReceiveMessageHandlerTest<>();
		TransportCodec transportProtocol = new JavaTransportProtocol(maxMessageSize);
		NettyClientTransport<Response> nettyClientTransport = new NettyClientTransport<>(workerIoThreads, connectTimeout,
				allIdleTimeSeconds, transportProtocol, receiveMessageHandler);
		Connection connection = nettyClientTransport.find(host, port);
		Message data=new Request();
		connection.send(data, sendFutrue -> {
			if(sendFutrue.isSucceed()) {
				System.out.println("发送成功");
			}else {
				System.out.println("发送失败");
			}
		});
		synchronized (transportProtocol) {
			transportProtocol.wait(300000000);
		}
	}
}
