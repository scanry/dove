package com.six.dove.transport.exception;
/**
*@author:MG01867
*@date:2018年4月12日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public class TransportException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8829833119821831818L;

	
    public TransportException() {
        super();
    }

    public TransportException(String message) {
        super(message);
    }


    public TransportException(String message, Throwable cause) {
        super(message, cause);
    }


    public TransportException(Throwable cause) {
        super(cause);
    }


    protected TransportException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
