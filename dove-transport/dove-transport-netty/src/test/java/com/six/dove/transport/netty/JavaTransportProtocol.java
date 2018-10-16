package com.six.dove.transport.netty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import com.six.dove.transport.Message;
import com.six.dove.transport.Request;
import com.six.dove.transport.Response;
import com.six.dove.transport.TransportCodec;
import com.six.dove.transport.exception.CodecTransportException;
import com.six.dove.transport.message.MessageProtocol;

/**
 * @author:MG01867
 * @date:2018年3月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class JavaTransportProtocol implements TransportCodec{

	@Override
	public Message decoder(ByteBuffer byteBuffer) {
		int type = byteBuffer.get();
		if (MessageProtocol.REQUEST == type) {
			Request request = unSerialize(byteBuffer.array(), Request.class);
			return request;
		} else if (MessageProtocol.RESPONSE == type) {
			Response response = unSerialize(byteBuffer.array(), Response.class);
			return response;
		} else if (MessageProtocol.HEARTBEAT == type) {
			return null;
		} else {
			throw new CodecTransportException();
		}
	}

	@Override
	public ByteBuffer encode(Message message) {
		byte[] data = serialize(message);
		ByteBuffer byteBuffer = ByteBuffer.allocate(data.length);
		byteBuffer.put(data);
		return byteBuffer;
	}

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
				throw new RuntimeException("default java's codec err:" + object.getClass(), e);
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
	public static <T> T unSerialize(byte[] data, Class<T> clz) {
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
