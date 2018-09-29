package com.six.dove.remote;
/**
*@author:MG01867
*@date:2018年2月7日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public interface InputStream {
	
	byte readByte();

	int readInt();

	void read(byte[] data);
	
	String from();
}
