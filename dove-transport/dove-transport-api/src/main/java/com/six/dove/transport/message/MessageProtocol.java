package com.six.dove.transport.message;


/**
 * @author:MG01867
 * @date:2018年3月30日
 * @email:359852326@qq.com
 * @version:
 * @describe 消息协议
 */
public interface MessageProtocol {

    int MSG_TYPE = 1;
    int BODY_LENGTH = 4;
    /**
     * 协议头长度 消息类型(1)+消息长度(4)+消息体(N)
     */
    int HEAD_LENGTH = MSG_TYPE + BODY_LENGTH;

    /**
     * 心跳包消息最小1
     */
    int HEAD_MIN_LENGTH = 1;

    /**
     * 默认协议体最大限制, 默认5M
     **/
    int DEFAULT_MAX_BODY_SIZE = 1024 * 1024 * 5;

    /**
     * 心跳
     **/
    byte HEARTBEAT = 0x00;
    /**
     * 请求
     **/
    byte REQUEST = 0x01;
    /**
     * 响应
     **/
    byte RESPONSE = 0x02;
}
