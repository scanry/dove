package com.six.dove.transport.handler;

import com.six.dove.transport.connection.Connection;
import com.six.dove.transport.message.Message;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 传输层-接受消息处理handler
 */
public interface ReceiveMessageHandler<C extends Connection, M extends Message> {

    void connActive(C connection);

    void receive(C connection, M message);

    void connInactive(C connection);

    void exceptionCaught(C connection, Exception exception);
}
