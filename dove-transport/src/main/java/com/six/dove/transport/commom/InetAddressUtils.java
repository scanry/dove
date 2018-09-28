package com.six.dove.transport.commom;
/**
*@author:MG01867
*@date:2018年3月30日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public class InetAddressUtils {


	public static String generateConnectionKey(String host, int port) {
		return host+":"+port;
	}
	
	public static String checkHost(String host) {
		if (null == host || host.trim().length() == 0) {
			throw new IllegalArgumentException("this host must be not blank");
		}
		return host;
	}
	
	public static int checkPort(int port) {
		if (1 > port || 65535 < port) {
			throw new IllegalArgumentException("this port[" + port + "] is illegal");
		}
		return port;
	}
}
