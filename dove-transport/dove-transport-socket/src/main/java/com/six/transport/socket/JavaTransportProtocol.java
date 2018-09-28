package com.six.transport.socket;

import com.six.dove.common.utils.JavaSerializeUtils;
import com.six.dove.transport.*;
import com.six.dove.transport.exception.CodecTransportException;

import java.nio.ByteBuffer;

/**
 * @author:MG01867
 * @date:2018年3月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class JavaTransportProtocol implements TransportCodec{

	private int maxMessageSize;

	public JavaTransportProtocol(int maxMessageSize) {
		if (maxMessageSize <= 0) {
			throw new IllegalArgumentException("this maxMessageSize must be > 0");
		}
		this.maxMessageSize = maxMessageSize;
	}

	@Override
	public int getMaxBodySzie() {
		return maxMessageSize;
	}

	@Override
	public Message decoder(ByteBuffer byteBuffer) {
		int type=byteBuffer.get();
		int bodyLength=byteBuffer.getInt();
		if(MessageProtocol.REQUEST==type) {
			byte[] body=new byte[bodyLength];
			byteBuffer.get(body);
			return new Request() {
				
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
			};
		}else if(MessageProtocol.RESPONSE==type) {
			byte[] body=new byte[bodyLength];
			byteBuffer.get(body);
			return new Response() {
				
				/**
				 * 
				 */
				private static final long serialVersionUID = -2512113179574108115L;
			};
		}else if(MessageProtocol.HEARTBEAT==type) {
			return null;
		}else {
			throw new CodecTransportException();
		}
	}

	@Override
	public byte[] encode(Message transportMessage) {
		byte[] body=JavaSerializeUtils.serialize(transportMessage);
		int bodyLength=body.length;
		ByteBuffer buffer = ByteBuffer.allocate(MessageProtocol.HEAD_LENGTH+bodyLength);
		buffer.put(transportMessage.getType());
		if(bodyLength>0) {
			buffer.putInt(bodyLength);
			buffer.put(body);
		}
		return buffer.array();
	}

}
