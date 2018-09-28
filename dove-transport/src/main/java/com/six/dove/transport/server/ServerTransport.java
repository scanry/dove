package com.six.dove.transport.server;

import com.six.dove.transport.Connection;
import com.six.dove.transport.Transport;
import com.six.dove.transport.protocol.Request;

/**
 * @author:MG01867
 * @date:2018年3月27日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public interface ServerTransport<Conn extends Connection, MessageRequest extends Request>
		extends Transport<Conn, MessageRequest> {

}
