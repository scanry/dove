package com.six.dove.remote.server.exception;

import com.six.dove.remote.exception.AbstractRemoteException;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月22日 上午10:18:08
 * 
 *       rpc系统定义常见异常
 */
public class RemoteSystenException extends AbstractRemoteException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2308882870528693053L;

	private int rpcSystenType;

	public RemoteSystenException(int rpcSystenType, String message) {
		super(message);
		this.rpcSystenType = rpcSystenType;
	}

	public int getRpcSystenType() {
		return rpcSystenType;
	}
}
