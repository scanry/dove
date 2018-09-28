package com.six.dove.remote.protocol;
/**
*@author:MG01867
*@date:2018年4月12日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public interface RemoteRequestParam{

	byte[] write();
	
	RemoteRequestParam read(byte[] source);
}
