package com.six.dove.transport;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 传输层-接受消息处理handler
 */
public interface ReceiveMessageHandler<ReceMsg extends Message,SendMsg extends Message> {

	void connActive(Connection<SendMsg> connection);

	void receive(Connection<SendMsg> connection, ReceMsg message);

	void connInactive(Connection<SendMsg> connection);

	void exceptionCaught(Connection<SendMsg> connection, Exception exception);
}
