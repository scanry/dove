package com.six.dove.remote.server.exception;

import com.six.dove.remote.exception.AbstractRemoteException;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月6日 下午9:03:46
 * @describe 远程调用 服务端 执行服务方法异常
 */
public class RemoteInvokeException extends AbstractRemoteException {

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
