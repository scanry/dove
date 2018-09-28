package com.six.dove.remote;

import java.lang.reflect.Method;

/**
*@author:MG01867
*@date:2018年4月12日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public interface InterfaceProxyMetaBuilder {

	
	/**
	 * 生成远程调用客户端代理类class name
	 * 
	 * @param protocol
	 *            远程协议class
	 * @param instanceMethod
	 *            远程协议class调用方法
	 * @return
	 */
	String generateProtocolProxyClassName(Class<?> protocol, Method instanceMethod);

	/**
	 * 生成远程调用客户端代理类class code
	 * 
	 * @param protocolClass
	 *            远程协议class
	 * @param packageName
	 *            远程协议class所在包
	 * @param className
	 *            远程协议class 代理名称
	 * @return
	 */
	InterfaceProxyMeta generateClient(Class<?> protocol, String packageName, String className);
}
