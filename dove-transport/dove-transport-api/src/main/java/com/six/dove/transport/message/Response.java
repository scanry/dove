package com.six.dove.transport.message;

/**
 * @author:MG01867
 * @date:2018年4月12日
 * @email:359852326@qq.com
 * @version:
 * @describe 响应消息
 */
public class Response extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4597604827003244481L;

	public Response() {
		super(MessageProtocol.RESPONSE);
	}

}
