package com.six.dove.remote.client.exception;

import com.six.dove.remote.exception.AbstractRemoteException;

/**
*@author:MG01867
*@date:2018年2月5日
*@E-mail:359852326@qq.com
*@version:
*@describe 远程调用 客户端 发送调用请求失败异常
*/
public class RemoteSendFailedException extends AbstractRemoteException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 898661420752809664L;

	public RemoteSendFailedException(String message) {
		super(message);
	}
	
	public RemoteSendFailedException(Throwable cause) {
		super(cause);
	}

	public RemoteSendFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
