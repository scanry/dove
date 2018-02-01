package com.six.dove.remote.exception;
/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年4月6日 下午8:59:39 
*/
public class RemoteException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -132337553479313107L;

	public RemoteException(String message) {
		super(message);
	}
	
	public RemoteException(Throwable cause) {
        super(cause);
    }

	public RemoteException(String message, Throwable cause) {
		super(message, cause);
	}
}
