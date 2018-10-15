package com.six.dove.transport;

import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.connection.ConnectionPool;
import com.six.dove.transport.handler.ReceiveMessageHandler;
import com.six.dove.transport.util.Constant;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 传输端
 */
public interface Transporter<SendMsg extends Message, ReceMsg extends Message> {

	/**
	 * 设置
	 * 
	 * @param maxBodySzie
	 */
	void setMaxBodySzie(int maxBodySzie);

	void setConnectionPool(ConnectionPool connectionPool);

	void setTransportCodec(TransportCodec<SendMsg, ReceMsg> transportCodec);

	void setReceiveMessageHandler(ReceiveMessageHandler<ReceMsg, SendMsg> receiveMessageHandler);

	void addMessageHandler(MessageHandler<ReceMsg> messageHandler);

	void addInterceptor(Interceptor.Aop aop, Interceptor<ReceMsg, SendMsg> interceptor);

	/**
	 * 启动
	 */
	void start();

	/**
	 * 关闭
	 */
	void shutdown();

	interface Option<T> extends Constant<Option<T>>, Comparable<Option<T>> {

	}
}
