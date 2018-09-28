package com.six.dove.transport.client;

import com.six.dove.transport.Connection;
import com.six.dove.transport.Transport;
import com.six.dove.transport.protocol.Response;

/**
 * @author:MG01867
 * @date:2018年3月27日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public interface ClientTransport<Conn extends Connection, MessageResponse extends Response>  extends Transport <Conn,MessageResponse> {

	int getConnectTimeout();
	
	int getWriterIdleTime();
	
	Connection find(String host, int port);
}
