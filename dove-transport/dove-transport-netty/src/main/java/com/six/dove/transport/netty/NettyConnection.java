package com.six.dove.transport.netty;

import java.util.Objects;

import com.six.dove.transport.connection.AbstractConnection;
import com.six.dove.transport.message.Message;

import com.six.dove.transport.NetAddress;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe netty 链接
 */
public class NettyConnection extends AbstractConnection {

    private Channel channel;

    public NettyConnection(Channel channel, NetAddress netAddress) {
        super(netAddress);
        Objects.requireNonNull(channel);
        this.channel = channel;
    }

    @Override
    public final boolean available() {
        return null != channel && channel.isActive();
    }

    @Override
    protected void doSend(Message data, SendListener sendListener) {
        ChannelFuture future = channel.writeAndFlush(data);
        future.addListener(result -> sendListener.complete(result::isSuccess));
    }

    @Override
    public void close() {
        channel.close();
    }
}
