package com.six.dove.transport;

import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.protocol.TransportMessage;
import com.six.dove.transport.server.ReceiveMessageHandler;

/**
 * @author:MG01867
 * @date:2018年3月27日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 通信传输
 */
public interface Transport<Conn extends Connection, Message extends TransportMessage> {

	void start();

	TransportCodec getTransportProtocol();

	ReceiveMessageHandler<Conn, Message> getReceiveMessageHandler();

	Conn getConnection(String id);

	void addConnection(Conn connection);

	void removeAndCloseConnection(String id);

	void shutdown();
}
