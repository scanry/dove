package com.six.dove.remote;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public interface RemoteConnection<T, R> {

	String getId();

	String getHost();

	int getPort();

	boolean available();

	long getLastActivityTime();

	R send(T msg);

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

	public static void checkAddress(String host, int port) {
		if (null == host || host.trim().length() == 0) {
			throw new IllegalArgumentException("this host must be not blank");
		}
		if (1 > port || 65535 < port) {
			throw new IllegalArgumentException("this port[" + port + "] is illegal");
		}
	}

}
