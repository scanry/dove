package com.six.dove.remote.client;

import com.six.dove.remote.protocol.RemoteResponse;

/**
 * @author:MG01867
 * @date:2018年4月12日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public interface ClientInoker{

	long getSendTime();
	
	long getReceiveTime();
	
	long getCallTimeout();
	
	RemoteResponse invoke();
	
	void onComplete(RemoteResponse remoteResponse);
}
