package com.six.dove.remote.connection;

import com.six.dove.remote.protocol.RemoteMsg;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 远程调用连接接口
 */
public interface RemoteConnection<S extends RemoteMsg, R> {

	/**
	 * 连接id
	 * 
	 * @return
	 */
	String getId();

	/**
	 * 连接的主机
	 * 
	 * @return
	 */
	String getHost();

	/**
	 * 连接端口
	 * 
	 * @return
	 */
	int getPort();

	/**
	 * 连接是否可用
	 * 
	 * @return
	 */
	boolean available();

	/**
	 * 连接最次活动时间
	 * 
	 * @return
	 */
	long getLastActivityTime();

	/**
	 * 发送消息
	 * 
	 * @param msg
	 * @return
	 */
	R send(S msg);

	/**
	 * 关闭
	 */
	void close();

	/**
	 * 链接key
	 * 
	 * @param host
	 *            目标主机
	 * @param port
	 *            目标端口
	 * @return
	 */
	public static String newConnectionId(String host, int port) {
		String findKey = host + ":" + port;
		return findKey;
	}

	/**
	 * 检查host和port是否有效
	 * 
	 * @param host
	 * @param port
	 */
	public static void checkAddress(String host, int port) {
		if (null == host || host.trim().length() == 0) {
			throw new IllegalArgumentException("this host must be not blank");
		}
		if (1 > port || 65535 < port) {
			throw new IllegalArgumentException("this port[" + port + "] is illegal");
		}
	}

	void happen(Event event);

	void addListener(Event event, Listener<S, R> listener);

	enum Event {
		CONNECTION, CLOSED, ILLEGAL;
	}

	@FunctionalInterface
	public interface Listener<S extends RemoteMsg, R> {

		void process(RemoteConnection<S, R> remoteConnection);
	}
}
