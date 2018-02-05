package com.six.dove.remote.protocol;

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
public interface RemoteSerialize {

	/**
	 * 序列化对象
	 * 
	 * @param ob
	 * @return
	 */
	default byte[] serialize(Object object) {
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
	default <T> T unSerialize(byte[] data, Class<T> clz) {
		T result = null;
		if (null != data && data.length > 0 && null != clz) {
			try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
					ObjectInputStream ois = new ObjectInputStream(bais);) {
				Object ob = ois.readObject();
				result = clz.cast(ob);
			} catch (Exception e) {
				throw new RuntimeException("default java's unSerializer err:" + clz, e);
			}
		}
		return result;
	}
}
