package com.six.dove.transport.netty.client;

import org.junit.Test;

import com.six.dove.transport.Connection;
import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.netty.NettyConnection;
import com.six.dove.transport.protocol.JavaTransportProtocol;
import com.six.dove.transport.protocol.Request;
import com.six.dove.transport.protocol.Response;
import com.six.dove.transport.protocol.TransportMessage;
import com.six.dove.transport.server.ReceiveMessageHandler;


/**
 * @author:MG01867
 * @date:2018年3月30日
 * @E-mail:359852326@qq.com
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
		TransportMessage data=new Request();
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
