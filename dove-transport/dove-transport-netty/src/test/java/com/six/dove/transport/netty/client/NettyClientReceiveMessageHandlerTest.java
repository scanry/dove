package com.six.dove.transport.netty.client;

import java.util.concurrent.atomic.AtomicInteger;

import com.six.dove.transport.Request;
import com.six.dove.transport.Response;
import com.six.dove.transport.connection.Connection;
import com.six.dove.transport.handler.ReceiveMessageHandler;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class NettyClientReceiveMessageHandlerTest
        implements ReceiveMessageHandler<Response, Request> {

    private AtomicInteger count = new AtomicInteger(0);

    @Override
    public void connActive(Connection<Request> connection) {
        System.out.println("client connection from " + connection.toString());
    }

    @Override
    public void receive(Connection<Request> connection, Response message) {
        System.out.println("client receive message:" + count.getAndIncrement());
    }

    @Override
    public void connInactive(Connection<Request> connection) {
        System.out.println("client connInactive from " + connection.toString());
    }

    @Override
    public void exceptionCaught(Connection<Request> connection, Exception exception) {
        System.out.println("clientexceptionCaught from " + connection.toString());
        exception.printStackTrace();
    }

}
