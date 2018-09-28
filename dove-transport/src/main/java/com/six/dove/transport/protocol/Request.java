package com.six.dove.transport.protocol;
/**
*@author:MG01867
*@date:2018年4月12日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public class Request extends TransportMessage{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3785832578235175559L;
	
	public Request() {
		super(TransportMessageProtocol.REQUEST);
	}
}
