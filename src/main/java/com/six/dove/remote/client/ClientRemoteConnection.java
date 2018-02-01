package com.six.dove.remote.client;

import com.six.dove.remote.RemoteConnection;
import com.six.dove.remote.protocol.RemoteRequest;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 客户端连接
 */
public interface ClientRemoteConnection extends RemoteConnection<RemoteRequest, WrapperFuture> {

	void putWrapperFuture(String rpcRequestId, WrapperFuture wrapperFuture);

	WrapperFuture removeWrapperFuture(String rpcRequestId);

}
