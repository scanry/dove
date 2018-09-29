package com.six.dove.transport.netty;

import com.six.dove.transport.NetAddress;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;

/**
 * @author: MG01867
 * @date: 2018/9/29
 * @email: 359852326@qq.com
 * @version:
 * @describe: netty连接 工具
 */
public class NettyConnectionUtils {

    public static NettyConnection channelToNettyConnection(Channel channel) {
        InetSocketAddress inetSocketAddress=(InetSocketAddress)channel.remoteAddress();
        NetAddress netAddress = new NetAddress(inetSocketAddress.getHostString(),inetSocketAddress.getPort());
         return new NettyConnection(channel, netAddress);
    }
}
