package com.six.dove.remote.exception;
/**
*@author:MG01867
*@date:2018年2月7日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public class RemoteException extends AbstractRemoteException {


	/**
	 * 
	 */
	private static final long serialVersionUID = -873949230992065587L;

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