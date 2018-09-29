package com.six.dove.remote.client;

import com.six.dove.remote.AsyCallback;
import com.six.dove.remote.Remote;
import com.six.dove.remote.protocol.RemoteRequestParam;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 远程调用客户端接口
 */
public interface ClientRemote extends Remote {

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
	<T> T getOrNewRemoteProtocolProxy(String callHost, int callPort, Class<?> clz, long callTimeout);

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
	<T> T getOrNewRemoteProtocolProxy(String callHost, int callPort, Class<?> clz, long callTimeout,
			AsyCallback callback);

	/**
	 * 同步执行远程命令
	 * 
	 * @param callHost
	 *            远程主机
	 * @param callPort
	 *            远程端口
	 * @param command
	 *            远程命令
	 * @param paramBytes
	 *            请求参数
	 * @param callTimeout
	 *            超时
	 * @return
	 */
	<T> T invoke(String callHost, int callPort, String command,RemoteRequestParam requestParam, long callTimeout);

	/**
	 * 异步执行远程命令
	 * 
	 * @param callHost
	 *            远程主机
	 * @param callPort
	 *            远程端口
	 * @param command
	 *            远程命令
	 * @param paramBytes
	 *            请求参数
	 * @param callTimeout
	 *            超时
	 * @param callback
	 *            异步回调
	 */
	void invoke(String callHost, int callPort, String command,RemoteRequestParam requestParam, long callTimeout, AsyCallback callback);
}
