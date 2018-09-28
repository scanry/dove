package com.six.dove.transport.client;


import com.six.dove.transport.Connection;
import com.six.dove.transport.client.socket.SocketClientTransport;
import com.six.dove.transport.protocol.Request;
import com.six.dove.transport.protocol.Response;
import com.six.dove.transport.protocol.TransportMessage;
import com.six.dove.transport.server.ReceiveMessageHandler;
import com.six.dove.transport.socket.ClientSocketConnection;

/**
 * @author:MG01867
 * @date:2018年4月12日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class SocketClientTransportTest {

	public static void main(String[] args) throws InterruptedException {
		String host = "127.0.0.1";
		int port = 8888;
		int maxMessageSize = 1000 * 1000 * 5;
		int workerIoThreads = 4;
		int allIdleTimeSeconds = 60;
		int connectTimeout = 3000;

		ReceiveMessageHandler<ClientSocketConnection<Response>,Response> receiveMessageHandler = new ScoketClientReceiverMessageHandler<>();
		SocketClientTransport<Response> nettyClientTransport = new SocketClientTransport<>(workerIoThreads,
				connectTimeout, allIdleTimeSeconds, maxMessageSize, receiveMessageHandler);
		Connection connection = nettyClientTransport.find(host, port);
		TransportMessage data=new Request();
		connection.send(data, sendFutrue -> {
			if (sendFutrue.isSucceed()) {
				System.out.println("发送成功");
			} else {
				System.out.println("发送失败");
			}
		});
		synchronized (SocketClientTransportTest.class) {
			SocketClientTransportTest.class.wait(3000);
		}
	}

}
