package com.six.dove.remote.exception;

/**
 * @author:MG01867
 * @date:2018年2月5日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 远程调用服务端 服务端服务不可用异常
 */
public class RemoteServiceUnavailableException extends RemoteException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4112563996209497664L;

	public RemoteServiceUnavailableException(String message) {
		super(message);
	}

	public RemoteServiceUnavailableException(Throwable cause) {
		super(cause);
	}

	public RemoteServiceUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

}
