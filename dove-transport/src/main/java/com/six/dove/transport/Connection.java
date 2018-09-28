package com.six.dove.transport;

import java.io.IOException;

import com.six.dove.transport.protocol.TransportMessage;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 传输层连接接口
 */
public interface Connection {

	static String newId(String host,int port) {
		return host+":"+port;
	}
	
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

	void send(TransportMessage data, SendListener sendListener);

	static SendListener sendListener = sendFutrue -> {};

	default void send(TransportMessage data) {
		send(data, sendListener);
	}

	/**
	 * 关闭
	 */
	void close() throws IOException;

	@FunctionalInterface
	public interface SendListener {
		void complete(SendFutrue sendFutrue);
	}

	public interface SendFutrue {
		boolean isSucceed();
	}
}
