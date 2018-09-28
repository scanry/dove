package com.six.dove.transport.server;

import com.six.dove.transport.protocol.TransportMessage;

/**
 * @author:MG01867
 * @date:2018年3月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */

public interface ReceiveMessageHandler<Conn, Message extends TransportMessage> {

	void connActive(Conn connection);

	void receive(Conn connection, Message message);

	void connInactive(Conn connection);

	void exceptionCaught(Conn connection,Throwable cause);
}
