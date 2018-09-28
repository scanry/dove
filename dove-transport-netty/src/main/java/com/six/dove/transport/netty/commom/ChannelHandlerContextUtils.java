package com.six.dove.transport.netty.commom;

import java.net.SocketAddress;

import io.netty.channel.ChannelHandlerContext;

/**
*@author:MG01867
*@date:2018年3月30日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public class ChannelHandlerContextUtils {


	public static String getAddressStr(ChannelHandlerContext ctx) {
		String address = "";
		SocketAddress remote = ctx.channel().remoteAddress();
		if (remote != null) {
			address = remote.toString();
		}
		return address;

	}

}
