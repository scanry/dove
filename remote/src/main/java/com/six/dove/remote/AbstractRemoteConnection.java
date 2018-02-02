package com.six.dove.remote;

import com.six.dove.remote.connection.RemoteConnection;
import com.six.dove.remote.protocol.RemoteMsg;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 抽象的远程调用接口
 */
public abstract class AbstractRemoteConnection<S extends RemoteMsg, R> implements RemoteConnection<S, R> {

	private String id;
	private String host;
	private int port;
	private long lastActivityTime;

	protected AbstractRemoteConnection(String host, int port) {
		RemoteConnection.checkAddress(host, port);
		this.id = RemoteConnection.newConnectionId(host, port);
		this.host = host;
		this.port = port;
	}

	@Override
	public final String getId() {
		return id;
	}

	@Override
	public final String getHost() {
		return host;
	}

	@Override
	public final int getPort() {
		return port;
	}

	@Override
	public final long getLastActivityTime() {
		return lastActivityTime;
	}

	@Override
	public final R send(S msg) {
		// 记录发送时间
		this.lastActivityTime = System.currentTimeMillis();
		return doSend(msg);
	}

	protected abstract R doSend(S msg);

	@Override
	public final String toString() {
		return host + ":" + port;
	}
}
