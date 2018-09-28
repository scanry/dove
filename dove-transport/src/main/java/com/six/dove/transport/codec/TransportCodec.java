package com.six.dove.transport.codec;

import java.nio.ByteBuffer;

import com.six.dove.transport.protocol.TransportMessage;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月22日 上午9:29:10 rpc 协议 1 消息类型 2 消息长度 3 消息体
 */
public interface TransportCodec{

	int getMaxBodySzie();
	
	TransportMessage decoder(ByteBuffer byteBuffer);

	byte[] encode(TransportMessage message);
	
}
