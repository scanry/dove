package com.six.dove.remote;

import com.six.dove.remote.protocol.RemoteRequest;

/**
 * @author:MG01867
 * @date:2018年2月22日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public interface RemoteInvoker<T> {

	public T invoke(RemoteRequest rpcRequest);
}
