package com.six.dove.remote.client;
/**
*@author:MG01867
*@date:2018年5月9日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public class ClientRemoteUtils {

	public static String getClientRemoteProxyKey(String callHost, int callPort, Class<?> clz) {
		return callHost + ":" + callPort + "@" + clz.getName();
	}
}
