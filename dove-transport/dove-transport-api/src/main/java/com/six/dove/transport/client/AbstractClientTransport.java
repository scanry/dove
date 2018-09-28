package com.six.dove.transport.client;

import com.six.dove.transport.*;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 客户 传输端基类
 */
public abstract class AbstractClientTransport<C extends Connection, MessageResponse extends Response>
        extends AbstractTransport<C, MessageResponse> implements ClientTransport {

    private long connectTimeout;
    private long sendTimeout;
    private long writerIdleTime;

    public AbstractClientTransport(ConnectionPool<C> connectionPool, TransportCodec transportProtocol,
                                   ReceiveMessageHandler<C, MessageResponse> receiveMessageHandler) {
        this(ClientTransport.DEFAULT_CONNECT_TIMEOUT, ClientTransport.DEFAULT_SEND_TIMEOUT, ClientTransport.DEFAULT_IDLE_TIME, connectionPool, transportProtocol, receiveMessageHandler);
    }

    public AbstractClientTransport(long connectTimeout, long sendTimeout, long writerIdleTime, ConnectionPool<C> connectionPool, TransportCodec transportProtocol,
                                   ReceiveMessageHandler<C, MessageResponse> receiveMessageHandler) {
        super(connectionPool, transportProtocol, receiveMessageHandler);
        if (connectTimeout <= 0) {
            throw new IllegalArgumentException(String.format("The connectTimeout[%s] must greater than 0", connectTimeout));
        }
        if (sendTimeout <= 0) {
            throw new IllegalArgumentException(String.format("The sendTimeout[%s] must greater than 0", sendTimeout));
        }
        if (writerIdleTime <= 0) {
            throw new IllegalArgumentException(String.format("The connectTimeout[%s] must greater than 0", writerIdleTime));
        }
        this.connectTimeout = connectTimeout;
        this.sendTimeout = connectTimeout;
        this.writerIdleTime = writerIdleTime;
    }

    @Override
    protected final void doStart(){

    }

    @Override
    public final C connect(String host, int port) {
        String connectionId = Connection.generateId(host, port);
        C connection = getConnectionPool().get(connectionId);
        if (null == connection) {
            connection = newConnection(host, port);
        } else if (!connection.available()) {
            getConnectionPool().remove(connection);
            connection = newConnection(host, port);
        }
        if (!connection.available()) {
            throw new RuntimeException(String.format("connection address[%s] failed", connectionId));
        }
        return connection;
    }

    protected abstract C newConnection(String host, int port);

    protected final long getConnectTimeout() {
        return connectTimeout;
    }

    protected final long getSendTimeout() {
        return sendTimeout;
    }

    protected final long getWriterIdleTime() {
        return writerIdleTime;
    }
}
