package com.six.dove.transport.netty;

import java.net.InetSocketAddress;
import java.util.Objects;

import com.six.dove.transport.connection.AbstractConnection;
import com.six.dove.transport.message.Message;

import com.six.dove.transport.NetAddress;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe netty 链接
 */
public class NettyConnection extends AbstractConnection {

    private static final AttributeKey<NettyConnection> WRAPPER_CONNECTION = AttributeKey.valueOf("dove.connection");
    private Channel channel;

    public static NettyConnection channelToNettyConnection(Channel channel) {
        Attribute<NettyConnection> attr = channel.attr(WRAPPER_CONNECTION);
        NettyConnection stockChannel = attr.get();
        if (stockChannel == null) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) channel.remoteAddress();
            NetAddress netAddress = new NetAddress(inetSocketAddress.getHostString(), inetSocketAddress.getPort());
            NettyConnection newNChannel = new NettyConnection(channel, netAddress);
            stockChannel = attr.setIfAbsent(newNChannel);
            if (stockChannel == null) {
                stockChannel = newNChannel;
            }
        }
        return stockChannel;
    }

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
