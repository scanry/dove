package com.six.dove.remote.client.exception;

import com.six.dove.remote.exception.AbstractRemoteException;

/**
 * @author:MG01867
 * @date:2018年2月5日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 远程调用 客户端不能连接异常
 */
public class RemoteUnconnectException extends AbstractRemoteException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2260635134318027450L;

	public RemoteUnconnectException(String message) {
		super(message);
	}

	public RemoteUnconnectException(Throwable cause) {
		super(cause);
	}

	public RemoteUnconnectException(String message, Throwable cause) {
		super(message, cause);
	}

}
