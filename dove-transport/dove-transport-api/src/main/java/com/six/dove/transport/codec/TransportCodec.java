package com.six.dove.transport.codec;

import com.six.dove.transport.buffer.DByteBuffer;
import com.six.dove.transport.message.Message;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 消息编解码
 */
public interface TransportCodec<M extends Message>{

	M decoder(DByteBuffer byteBuffer);

	byte[] encode(M message);
}
