package com.six.dove.remote.server;

import com.six.dove.remote.Remote;
import com.six.dove.remote.ServiceName;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe
 */
public interface ServerRemote extends Remote<RemoteRequest, Void, RemoteResponse, Void, ServerRemoteConnection> {

	/**
	 * 服务端host
	 * 
	 * @return 一定有值
	 */
	String getLocalHost();

	/**
	 * 服务端监听端口
	 * 
	 * @return 一定有值
	 */
	int getListenPort();

	/**
	 * 获取服务端包装的调用服务
	 * 
	 * @param serviceName
	 *            调用服务命名
	 * @return 返回可调用的服务，有可能为Null
	 */
	WrapperServiceTuple getWrapperServiceTuple(ServiceName serviceName);

}
