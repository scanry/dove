package com.six.dove.transport.client;

import com.six.dove.transport.Connection;
import com.six.dove.transport.Message;
import com.six.dove.transport.NetAddress;
import com.six.dove.transport.Transporter;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 客户 传输端
 */
public interface ClientTransport<SendMsg extends Message, ReceMsg extends Message> extends Transporter<SendMsg, ReceMsg>{

	/**
	 * 默认链接超时
	 **/
	long DEFAULT_CONNECT_TIMEOUT = 3000;

	/**
	 * 默认发送超时
	 **/
	long DEFAULT_SEND_TIMEOUT = 3000;

	/**
	 * 默认空闲超时
	 **/
	long DEFAULT_IDLE_TIME = 3000;

	/**
	 * 通过host和port获取链接
	 *
	 * @param host 主机名字或者ip
	 * @param port 端口
	 * @return 返回可用链接
	 */
	Connection<SendMsg> connect(NetAddress netAddress);

}
