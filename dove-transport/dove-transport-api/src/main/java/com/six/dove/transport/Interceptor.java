package com.six.dove.transport;

/**
 * @author: sixliu
 * @email: 359852326@qq.com
 * @date: 2018年10月11日 下午11:22:40
 * @version V1.0
 * @description:TODO
 */
@FunctionalInterface
public interface Interceptor<SendMsg extends Message, ReceMsg extends Message> {

	void intercept(SendMsg sendMsg, ReceMsg receMsg, Exception exception);

	enum Aop {
		/** 发送前 **/
		SEND_BEFORE,
		/** 发送后 **/
		SEND_AFTER,
		/** 发送异常 **/
		SEND_ERROR,
		/** 发送最终执行 **/
		SEND_FINALY,
		/** 接收处理前 **/
		RECV_HANDLE_BEFORE,
		/** 接收处理后 **/
		RECV_HANDLE_AFTER,
		/** 接收处理异常 **/
		RECV_HANDLE_ERROR,
		/** 接收处理最终执行 **/
		RECV_HANDLE_FINALY,
	}
}
