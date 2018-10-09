package com.six.dove.transport;

import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.connection.ConnectionPool;
import com.six.dove.transport.handler.ReceiveMessageHandler;
import com.six.dove.transport.message.Message;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 传输端
 */
public interface Transporter<SendMsg extends Message, ReceMsg extends Message> {

	String LOCAL_HOST = "127.0.0.1";

	void setMaxBodySzie(int maxBodySzie);
	
	void setConnectionPool(ConnectionPool connectionPool);

	void setTransportProtocol(TransportCodec<SendMsg, ReceMsg> transportProtocol);

	void setReceiveMessageHandler(ReceiveMessageHandler<ReceMsg,SendMsg> receiveMessageHandler);

	/**
	 * 启动
	 */
	void start();

	/**
	 * 关闭
	 */
	void shutdown();
}
