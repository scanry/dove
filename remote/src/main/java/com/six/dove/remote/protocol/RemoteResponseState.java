package com.six.dove.remote.protocol;

/**
 * @author:MG01867
 * @date:2018年2月1日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 远程调用响应状态
 */
public interface RemoteResponseState {

	/**
	 * 成功
	 */
	int SUCCEED = 1;

	/**
	 * 拒绝
	 */
	int REJECT = 2;

	/**
	 * 超时
	 */
	int TIMEOUT = 3;

	/**
	 * 执行异常
	 */
	int INVOKE_ERR = 4;

	/**
	 * 没有找到服务
	 */
	int UNFOUND_SERVICE = 5;

	/**
	 * 链接失败
	 */
	int CONNECT_FAILED = 6;
	/**
	 * 发送失败
	 */
	int SEND_FAILED = 7;

}
