package com.six.dove.remote;
/**
*@author:MG01867
*@date:2018年5月9日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public class DoveContext {

	private static ThreadLocal<AsyCallback> asyCallbackThreadLocal=new ThreadLocal<>();
	
	public static void setAsyCallback(AsyCallback asyCallback) {
		asyCallbackThreadLocal.set(asyCallback);
	}
	
	public static AsyCallback getCurrentThreadAsyCallback() {
		return asyCallbackThreadLocal.get();
	}
}
