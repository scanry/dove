package com.six.dove.remote.protocol;

/**
 * @author:MG01867
 * @date:2018年2月1日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 远程调用响应结果常量
 */
public class RemoteResponseConstants {


	/**
	 * 连接失败响应结果
	 */
	public transient final static RemoteResponse CONNECT_FAILED = new RemoteResponse(RemoteResponseState.CONNECT_FAILED);

	/**
	 * 发送失败响应结果
	 */
	public transient final static RemoteResponse SEND_FAILED = new RemoteResponse(RemoteResponseState.SEND_FAILED);

	/**
	 * 远程调用超时响应结果
	 */
	public transient final static RemoteResponse TIME_OUT = new RemoteResponse(RemoteResponseState.TIMEOUT);
}
