package com.six.dove.remote.exception;
/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年4月6日 下午8:59:39 
*/
public abstract class AbstractRemoteException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -132337553479313107L;

	public AbstractRemoteException(String message) {
		super(message);
	}
	
	public AbstractRemoteException(Throwable cause) {
        super(cause);
    }

	public AbstractRemoteException(String message, Throwable cause) {
		super(message, cause);
	}
}
