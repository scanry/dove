package com.six.dove.remote.serializable;
/**
*@author:MG01867
*@date:2018年4月12日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public interface WriteReadObject<T>{

	byte[] write();
	
	T read(byte[] byteSrc);
}
