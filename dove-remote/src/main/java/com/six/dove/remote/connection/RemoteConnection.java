package com.six.dove.remote.connection;

import com.six.dove.remote.protocol.RemoteMsg;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 远程调用连接接口
 */
public interface RemoteConnection<S extends RemoteMsg, R extends RemoteMsg> {

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
	 *            需要发送的消息
	 * @param sendListener
	 *            发送结果监听
	 */
	void send(S msg, SendListener sendListener);

	/**
	 * 设置消息接收处理
	 * 
	 * @param receiveListener
	 */
	void setReceiveListener(ReceiveListener<R> receiveListener);

	/**
	 * 关闭
	 */
	void close();

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
		CONNECTION, CLOSED, ILLEGAL, UNKNOW;
	}

	@FunctionalInterface
	public interface ReceiveListener<R extends RemoteMsg> {
		void receive(R msg);
	}

	@FunctionalInterface
	public interface SendListener {

		void operationComplete(boolean isSuccess, Exception e);
	}

	@FunctionalInterface
	public interface Listener<S extends RemoteMsg, R extends RemoteMsg> {

		void process(RemoteConnection<S, R> remoteConnection);
	}
}
