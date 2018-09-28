package com.six.dove.transport;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 传输端
 */
public interface Transporter{

	String LOCAL_HOST="127.0.0.1";
	/**
	 * 启动
	 */
	void start();

	/**
	 * 关闭
	 */
	void shutdown();
}
