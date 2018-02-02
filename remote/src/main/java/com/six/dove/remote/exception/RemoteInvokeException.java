package com.six.dove.remote.exception;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月6日 下午9:03:46
 */
public class RemoteInvokeException extends RemoteException {

	/**
	 * 执行rpc服务时异常
	 */
	private static final long serialVersionUID = -6536519516880573828L;

	public RemoteInvokeException(String message) {
		super(message);
	}

	public RemoteInvokeException(Throwable cause) {
        super(cause);
    }
	
	public RemoteInvokeException(String message, Throwable cause) {
		super(message, cause);
	}

}
