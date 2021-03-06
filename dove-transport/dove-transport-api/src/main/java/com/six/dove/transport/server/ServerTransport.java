package com.six.dove.transport.server;

import com.six.dove.transport.NetAddress;
import com.six.dove.transport.Request;
import com.six.dove.transport.Response;
import com.six.dove.transport.Transporter;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 服务 传输端
 */
public interface ServerTransport<SendMsg extends Response, ReceMsg extends Request>
		extends Transporter<SendMsg, ReceMsg> {

	NetAddress getNetAddress();
}
