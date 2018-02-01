package com.six.dove.remote.client;

import com.six.dove.remote.Remote;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe
 */
public interface ClientRemote extends Remote<RemoteRequest, RemoteResponse, RemoteRequest, RemoteFuture, ClientRemoteConnection> {

	/**
	 * 远程调用客户端调用服务端超时时间
	 * 
	 * @return
	 */
	long getCallTimeout();
}
