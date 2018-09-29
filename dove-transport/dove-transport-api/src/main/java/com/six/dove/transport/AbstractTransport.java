package com.six.dove.transport;

import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.connection.Connection;
import com.six.dove.transport.connection.ConnectionPool;
import com.six.dove.transport.handler.ReceiveMessageHandler;
import com.six.dove.transport.message.Message;

import java.util.Objects;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe
 */
public abstract class AbstractTransport<C extends Connection, M extends Message> implements Transporter{

    private ConnectionPool<C> connectionPool;
    private TransportCodec transportProtocol;
    private ReceiveMessageHandler<C, M> receiveMessageHandler;

    public AbstractTransport(ConnectionPool<C> connectionPool,TransportCodec transportProtocol,
                             ReceiveMessageHandler<C, M> receiveMessageHandler) {
        Objects.requireNonNull(connectionPool);
        Objects.requireNonNull(transportProtocol);
        Objects.requireNonNull(receiveMessageHandler);
        this.connectionPool = connectionPool;
        this.transportProtocol = transportProtocol;
        this.receiveMessageHandler = receiveMessageHandler;
    }

    @Override
    public final void start() {
        doStart();
    }

    @Override
    public final void shutdown() {
        doShutdown();
    }

    protected abstract void doStart();

    protected abstract void doShutdown();

    protected final ConnectionPool<C> getConnectionPool() {
        return connectionPool;
    }

    protected final TransportCodec getTransportProtocol() {
        return transportProtocol;
    }

    protected final ReceiveMessageHandler<C, M> getReceiveMessageHandler() {
        return receiveMessageHandler;
    }
}
