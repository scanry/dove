package com.six.dove.transport.protocol;

import java.nio.ByteBuffer;

import com.six.dove.common.utils.JavaSerializeUtils;
import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.exception.CodecTransportException;
import com.six.dove.transport.protocol.TransportMessage;

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
	public TransportMessage decoder(ByteBuffer byteBuffer) {
		int type=byteBuffer.get();
		int bodyLength=byteBuffer.getInt();
		if(TransportMessageProtocol.REQUEST==type) {
			byte[] body=new byte[bodyLength];
			byteBuffer.get(body);
			return new Request() {
				
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
			};
		}else if(TransportMessageProtocol.RESPONSE==type) {
			byte[] body=new byte[bodyLength];
			byteBuffer.get(body);
			return new Response() {
				
				/**
				 * 
				 */
				private static final long serialVersionUID = -2512113179574108115L;
			};
		}else if(TransportMessageProtocol.HEARTBEAT==type) {
			return null;
		}else {
			throw new CodecTransportException();
		}
	}

	@Override
	public byte[] encode(TransportMessage transportMessage) {
		byte[] body=JavaSerializeUtils.serialize(transportMessage);
		int bodyLength=body.length;
		ByteBuffer buffer = ByteBuffer.allocate(TransportMessageProtocol.HEAD_LENGTH+bodyLength);
		buffer.put(transportMessage.getType());
		if(bodyLength>0) {
			buffer.putInt(bodyLength);
			buffer.put(body);
		}
		return buffer.array();
	}

}
