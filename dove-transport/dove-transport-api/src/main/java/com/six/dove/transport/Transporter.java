package com.six.dove.transport;

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
	 * 启动
	 */
	default void start() {}

	/**
	 * 关闭
	 */
	default void shutdown() {}

	/**
	 * 设置 消息体最大size
	 * 
	 * @param maxBodySzie
	 */
	void setMaxBodySzie(int maxBodySzie);

	/**
	 * 设置编解码器
	 * 
	 * @param transportCodec
	 */
	void setTransportCodec(TransportCodec transportCodec);

	/**
	 * 设置消息接收handler
	 * 
	 * @param receiveMessageHandler
	 */
	void setReceiveMessageHandler(ReceiveMessageHandler<ReceMsg, SendMsg> receiveMessageHandler);

	/**
	 * 设置消息拦截器
	 * 
	 * @param aop
	 * @param interceptor
	 */
	void addInterceptor(Interceptor.Aop aop, Interceptor<ReceMsg, SendMsg> interceptor);
}
