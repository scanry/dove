package com.six.dove.rpc.spring;

/**
 * @author:MG01867
 * @date:2018年2月2日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 
 */
public class DoveConfig {

	private String host;
	private int port;
	private String[] scanPackages;
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public String[] getScanPackages() {
		return scanPackages;
	}

}
