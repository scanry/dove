package com.six.dove.transport.connection;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 连接池
 */
public interface ConnectionPool<C extends Connection> {

    C get(String id);

    void add(C connection);

    void remove(C connection);
}

