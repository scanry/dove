package com.six.dove.transport;

/**
 * @author:MG01867
 * @date:2018年3月27日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public interface Codec<T> {

	T Decode(byte[] data);

	byte[] encode(T data);
}
