package com.six.dove.remote.connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.six.dove.remote.protocol.RemoteMsg;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 抽象的远程调用接口
 */
public abstract class AbstractRemoteConnection<S extends RemoteMsg, R extends RemoteMsg> implements RemoteConnection<S, R> {

	private String id;
	private String host;
	private int port;
	private long lastActivityTime;
	private ReceiveListener<R> receiveListener;
	private Map<Event, List<Listener<S, R>>> eventMap;

	protected AbstractRemoteConnection(String host, int port) {
		RemoteConnection.checkAddress(host, port);
		this.id = RemoteConnection.newConnectionId(host, port);
		this.host = host;
		this.port = port;
		this.eventMap = new HashMap<>();
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
	public final void happen(Event event) {
		List<Listener<S, R>> list = eventMap.get(event);
		if (null != list) {
			for (Listener<S, R> listener : list) {
				listener.process(this);
			}
		}
	}

	@Override
	public final void addListener(Event event, Listener<S, R> listener) {
		eventMap.computeIfAbsent(event, key -> new ArrayList<>()).add(listener);
	}

	@Override
	public final void send(S msg, SendListener sendListener) {
		// 记录发送时间
		this.lastActivityTime = System.currentTimeMillis();
		doSend(msg, sendListener);
	}

	protected ReceiveListener<R> getReceiveListener() {
		return receiveListener;
	}
	
	@Override
	public void setReceiveListener(ReceiveListener<R> receiveListener) {
		this.receiveListener=receiveListener;
	}

	protected abstract void doSend(S msg, SendListener sendListener);

	@Override
	public final String toString() {
		return host + ":" + port;
	}
}
