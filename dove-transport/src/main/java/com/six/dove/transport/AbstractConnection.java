package com.six.dove.transport;

import com.six.dove.transport.protocol.TransportMessage;

/**
 * @author:MG01867
 * @date:2018年3月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public abstract class AbstractConnection implements Connection {

	private String id;
	private String host;
	private int port;
	private long lastActivityTime;

	public AbstractConnection(String host, int port) {
		this.host = host;
		this.port = port;
		this.id =Connection.newId(host, port);
	}
	
	

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public long getLastActivityTime() {
		return lastActivityTime;
	}

	@Override
	public void send(TransportMessage data, SendListener sendListener) {
		this.lastActivityTime = System.currentTimeMillis();
		doSend(data,sendListener);
	}

	protected abstract void doSend(TransportMessage data,SendListener sendListener);
}
