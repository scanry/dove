package com.six.dove.remote.server;

import com.six.dove.remote.connection.AbstractRemoteConnection;
import com.six.dove.remote.protocol.RemoteResponse;

/**
 * @author:MG01867
 * @date:2018年2月1日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public abstract class AbstractServerRemoteConnection extends AbstractRemoteConnection<RemoteResponse, Void>
		implements ServerRemoteConnection {

	protected AbstractServerRemoteConnection(String host, int port) {
		super(host, port);
	}

}
