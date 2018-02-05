package com.six.dove.remote.client;

import java.lang.reflect.Method;

import com.six.dove.remote.AsyCallback;
import com.six.dove.remote.Remote;
import com.six.dove.remote.ServiceName;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 远程调用客户端接口
 */
public interface ClientRemote
		extends Remote<RemoteRequest, RemoteResponse, RemoteRequest, RemoteFuture, ClientRemoteConnection> {


	/**
	 * 创建请求id
	 * 
	 * @param callHost
	 *            远程主机
	 * @param callPort
	 *            远程端口
	 * @param serviceName
	 *            远程服务名称
	 * @return
	 */
	String createRequestId(String callHost, int callPort, ServiceName serviceName);

	/**
	 * 获取远程服务代理，没有时new一个新的实例
	 * 
	 * @param callHost
	 *            远程主机
	 * @param callPort
	 *            远程端口
	 * @param clz
	 *            远程服务协议
	 * @return
	 */
	<T> T getOrNewRemoteProtocolProxy(String callHost, int callPort, Class<?> clz,String version);

	/**
	 * 获取远程服务代理，没有时new一个新的实例
	 * 
	 * @param callHost
	 *            远程主机
	 * @param callPort
	 *            远程端口
	 * @param clz
	 *            远程服务协议
	 * @param callback
	 *            异步回调
	 * @return
	 */
	<T> T getOrNewRemoteProtocolProxy(String callHost, int callPort, Class<?> clz,String version, AsyCallback callback);

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
	String generateProtocolProxyClassCode(Class<?> protocol, String packageName, String className,String version);
}
