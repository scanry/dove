package com.six.dove.transport.message;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:40:52
 * @email: 359852326@qq.com
 * @version:
 * @describe 心跳消息
 */
public class HeartBeat extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1368703047202041850L;

	public HeartBeat() {
		super(MessageProtocol.HEARTBEAT);
	}
}