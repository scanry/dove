package com.six.dove.transport.connection;

import com.six.dove.transport.Message;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 连接池
 */
public interface ConnectionPool{

	<SendMsg extends Message>Connection<SendMsg> get(String id);

	<SendMsg extends Message>void add(Connection<SendMsg> connection);

	<SendMsg extends Message>void remove(Connection<SendMsg> connection);
}

