package com.six.dove.remote.exception;
/**
*@author:MG01867
*@date:2018年2月5日
*@E-mail:359852326@qq.com
*@version:
*@describe  远程调用端 未知异常
*/
public class RemoteUnknownException extends RemoteException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 7342117372002256117L;

	public RemoteUnknownException(String message) {
		super(message);
	}

	public RemoteUnknownException(Throwable cause) {
		super(cause);
	}

	public RemoteUnknownException(String message, Throwable cause) {
		super(message, cause);
	}

}
