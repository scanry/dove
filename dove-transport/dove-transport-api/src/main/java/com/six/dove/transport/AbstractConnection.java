package com.six.dove.transport;

import java.util.Objects;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 链接基类
 */
public abstract class AbstractConnection<M extends Message> implements Connection<M> {

    private String id;
    private NetAddress netAddress;
    private long lastActivityTime;

    public AbstractConnection(NetAddress netAddress) {
        Objects.requireNonNull(netAddress);
        this.netAddress = netAddress;
        this.id =Connection.generateId(netAddress);
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final NetAddress getNetAddress(){
        return  netAddress;
    }

    @Override
    public final long getLastActivityTime() {
        return lastActivityTime;
    }

    @Override
    public final void send(M message, SendListener sendListener) {
        this.lastActivityTime = System.currentTimeMillis();
        doSend(message, sendListener);
    }

    protected abstract void doSend(Message data, SendListener sendListener);
}
