package com.six.dove.transport;

import java.nio.ByteBuffer;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 消息编解码
 */
public interface TransportCodec {

	Message decoder(ByteBuffer byteBuffer);

	ByteBuffer encode(Message message);
}
