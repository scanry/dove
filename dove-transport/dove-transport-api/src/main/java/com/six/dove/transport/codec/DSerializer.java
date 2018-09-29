package com.six.dove.transport.codec;

import com.six.dove.transport.buffer.DByteBuffer;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 23:43:08
 * @email: 359852326@qq.com
 * @version:
 * @describe 序列化器
 */
public interface DSerializer {

    void write(DByteBuffer byteBuffer);

    void read(DByteBuffer byteBuffer);
}
