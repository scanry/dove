package com.six.dove.transport.client;


import com.six.dove.transport.protocol.Response;
import com.six.dove.transport.server.ReceiveMessageHandler;
import com.six.dove.transport.socket.ClientSocketConnection;

/**
 * @author:MG01867
 * @date:2018年4月12日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class ScoketClientReceiverMessageHandler<MessageResponse extends Response>
		implements ReceiveMessageHandler<ClientSocketConnection<MessageResponse>, MessageResponse> {

	@Override
	public void connActive(ClientSocketConnection<MessageResponse> connection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receive(ClientSocketConnection<MessageResponse> connection, MessageResponse message) {
		System.out.println("client receive:" + message);
	}

	@Override
	public void connInactive(ClientSocketConnection<MessageResponse> connection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exceptionCaught(ClientSocketConnection<MessageResponse> connection, Throwable cause) {
		// TODO Auto-generated method stub

	}

}
