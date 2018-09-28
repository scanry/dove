package com.six.dove.transport.protocol;
/**
*@author:MG01867
*@date:2018年4月12日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public class HeartBeat extends TransportMessage{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1368703047202041850L;

	public HeartBeat() {
		super(TransportMessageProtocol.HEARTBEAT);
	}
}