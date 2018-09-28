package com.six.dove.transport.exception;

/**
 * @author:MG01867
 * @date:2018年4月12日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class CodecTransportException extends TransportException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5683264318250783994L;

	public CodecTransportException() {
		super();
	}

	public CodecTransportException(String message) {
		super(message);
	}

	public CodecTransportException(String message, Throwable cause) {
		super(message, cause);
	}

	public CodecTransportException(Throwable cause) {
		super(cause);
	}

	protected CodecTransportException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
