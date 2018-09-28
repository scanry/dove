package com.six.dove.transport.protocol;



/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月22日 上午9:29:10 rpc 协议 1 消息类型 2 消息长度 3 消息体
 */
public interface TransportMessageProtocol{

	public static final int MSG_TYPE = 1;
	public static final int BODY_LENGTH = 4;
	/**
	 * 协议头长度 消息类型(1)+消息长度(4)+消息体(N)
	 */
	public static final int HEAD_LENGTH = 5;
	/**
	 * 心跳包消息最小1
	 */
	public static final int HEAD_MIN_LENGTH = 1;

	// 协议体最大限制, 默认5M
	public static final int MAX_BODY_SIZE = 1024 * 1024 * 5;

	/**心跳**/
	public static final byte HEARTBEAT = 0x00;
	/**请求**/
	public static final byte REQUEST = 0x01; 
	/**响应**/
	public static final byte RESPONSE = 0x02;
	
}
