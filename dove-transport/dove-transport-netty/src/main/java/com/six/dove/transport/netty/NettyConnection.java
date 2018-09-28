package com.six.dove.transport.netty;

import java.util.Objects;

import com.six.dove.transport.AbstractConnection;
import com.six.dove.transport.Message;

import com.six.dove.transport.NetAddress;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * @author:MG01867
 * @date: 2018年5月9日
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
        if (null != channel) {
            channel.close();
        }
    }
}
