package com.six.dove.transport.server;

import com.six.dove.transport.*;
import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.connection.Connection;
import com.six.dove.transport.connection.ConnectionPool;
import com.six.dove.transport.handler.ReceiveMessageHandler;
import com.six.dove.transport.message.Request;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 服务 传输端-基类
 */
public abstract class AbstractServerTransport<C extends Connection, M extends Request>
        extends AbstractTransport<C, M> implements ServerTransport {

    private NetAddress netAddress;

    public AbstractServerTransport(int port, ConnectionPool<C> connectionPool, TransportCodec transportProtocol,
                                   ReceiveMessageHandler<C, M> receiveMessageHandler) {
        this(Transporter.LOCAL_HOST, port, connectionPool, transportProtocol, receiveMessageHandler);
    }

    public AbstractServerTransport(String host, int port, ConnectionPool<C> connectionPool, TransportCodec transportProtocol,
                                   ReceiveMessageHandler<C, M> receiveMessageHandler) {
        this(new NetAddress(host, port), connectionPool, transportProtocol, receiveMessageHandler);
    }

    public AbstractServerTransport(NetAddress netAddress, ConnectionPool<C> connectionPool, TransportCodec transportProtocol,
                                   ReceiveMessageHandler<C, M> receiveMessageHandler) {
        super(connectionPool, transportProtocol, receiveMessageHandler);
        this.netAddress = netAddress;
    }

    @Override
    public final void doStart() {
        innerDoStart(netAddress);
    }

    protected abstract void innerDoStart(NetAddress netAddress);

    @Override
    public final NetAddress getNetAddress() {
        return netAddress;
    }
}
