package com.six.dove.remote;

import com.six.dove.remote.connection.RemoteConnection;

/**
*@author:MG01867
*@date:2018年2月1日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public class RemoteUtils {

	public static void checkParma(String targetHost, int targetPort, Class<?> clz) {
		RemoteConnection.checkAddress(targetHost, targetPort);
		if (!clz.isInterface()) {
			throw new IllegalArgumentException("this clz[" + clz.getName() + "] is not tnterface");
		}
	}
}
