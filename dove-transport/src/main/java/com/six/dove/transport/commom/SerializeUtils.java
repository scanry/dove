package com.six.dove.transport.commom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月10日 上午11:03:42
 * 
 *       rpc序列化接口类,默认采用java 本身序列化
 */
public class SerializeUtils {

	/**
	 * 序列化对象
	 * 
	 * @param ob
	 * @return
	 */
	public static byte[] serialize(Object object) {
		byte[] dts = null;
		if (null != object) {
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ObjectOutputStream obstr = new ObjectOutputStream(baos);) {
				obstr.writeObject(object);
				dts = baos.toByteArray();
			} catch (IOException e) {
				throw new RuntimeException("default java's serializer err:" + object.getClass(), e);
			}
		}
		return dts;

	}

	/**
	 * 反序列化
	 * 
	 * @param data
	 * @param clz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T unSerialize(byte[] data) {
		Object result = null;
		if (null != data && data.length > 0) {
			try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
					ObjectInputStream ois = new ObjectInputStream(bais);) {
				result = ois.readObject();
			} catch (Exception e) {
				throw new RuntimeException("default java's unSerializer exception", e);
			}
		}
		return (T)result;
	}
}
