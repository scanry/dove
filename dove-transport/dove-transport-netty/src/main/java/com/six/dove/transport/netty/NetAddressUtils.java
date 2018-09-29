package com.six.dove.transport.netty;

import java.net.SocketAddress;

import com.six.dove.transport.NetAddress;
import io.netty.channel.Channel;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe netty-链接地址解析工具
 */
public class NetAddressUtils {

    public static NetAddress parserNetAddress(Channel channel) {
        SocketAddress remote = channel.remoteAddress();
        String[] addressArray = remote.toString().split(":");
        return new NetAddress(addressArray[0], Integer.valueOf(addressArray[1]));
    }
}
