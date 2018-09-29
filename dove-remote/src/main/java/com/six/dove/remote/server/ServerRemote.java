package com.six.dove.remote.server;

import java.lang.reflect.Method;

import com.six.dove.remote.Remote;
import com.six.dove.remote.ServiceNameUtils;
import com.six.dove.remote.ServicePath;
import com.six.dove.remote.connection.RemoteConnection;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe
 */
public interface ServerRemote extends Remote<Void, RemoteResponse, RemoteRequest,RemoteConnection<RemoteResponse, RemoteRequest>> {

	/**
	 * 远程调用服务端服务默认版本
	 */
	String DEFAULT_SERVICE_VERSION = "1.0.0";
	
	int BACKLOG=1024;

	/**
	 * 服务端host
	 * 
	 * @return 一定有值
	 */
	String getLocalHost();

	/**
	 * 服务端监听端口
	 * 
	 * @return 一定有值
	 */
	int getListenPort();

	/**
	 * 生成一个ServicePath
	 * 
	 * @param serviceName
	 *            服务命名
	 * @return
	 */
	ServicePath newServicePath(ServiceNameUtils serviceName);

	/**
	 * 生成远程调用服务端代理类class name
	 * 
	 * @param protocol
	 *            协议class
	 * @param instanceMethod
	 *            协议class调用方法
	 * @return
	 */
	String generateProtocolProxyClassName(Class<?> protocol, Method instanceMethod);

	/**
	 * 生成远程调用客户端代理类class code
	 * 
	 * @param protocolClass
	 *            协议class
	 * @param packageName
	 *            协议class所在包
	 * @param className
	 *            协议class 代理名称
	 * @param instanceMethod
	 *            协议class 调用方法
	 * @return
	 */
	String generateProtocolProxyClassCode(Class<?> protocolClass, String packageName, String className,
			Method instanceMethod);

	/**
	 * 获取服务端包装的调用服务
	 * 
	 * @param serviceName
	 *            调用服务命名
	 * @return 返回可调用的服务，有可能为Null
	 */
	WrapperServiceInvoker getWrapperServiceTuple(ServiceNameUtils serviceName);

	/**
	 * 注册远程调用服务端的代理服务
	 * 
	 * @param serviceName
	 *            服务命名
	 * @param wrapperServiceTuple
	 *            代理服务包装
	 */
	void registerWrapperServiceTuple(ServiceNameUtils serviceName, WrapperServiceInvoker wrapperServiceTuple);

	/**
	 * 移除远程调用服务端的代理服务网
	 * 
	 * @param serviceName
	 *            服务命名
	 */
	void removeWrapperServiceTuple(ServiceNameUtils serviceName);

}
