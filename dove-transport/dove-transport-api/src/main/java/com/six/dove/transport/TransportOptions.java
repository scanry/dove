package com.six.dove.transport;

import com.six.dove.transport.Transporter.Option;
import com.six.dove.transport.util.ConstantPool;


/**
 * @author: sixliu
 * @email: 359852326@qq.com
 * @date: 2018年10月11日 下午11:40:35
 * @version V1.0
 * @description:传输层配置选项
 */
public class TransportOptions {

	public final static Option<Long> MAX_BODY_SIZE = valueOf("");

	private final static ConstantPool<Option<Object>> pool = new ConstantPool<Option<Object>>() {

		@Override
		protected Option<Object> newConstant(int id, String name) {
			return new Option<Object>(id, name) {};
		}
	};

	@SuppressWarnings("unchecked")
	private static <T> Option<T> valueOf(String name) {
		return (Option<T>) pool.valueOf(name);
	}
}
