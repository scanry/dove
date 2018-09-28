package com.six.dove.remote.protocol;

import java.nio.ByteBuffer;

import com.six.dove.common.utils.JavaSerializeUtils;
import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.exception.CodecTransportException;
import com.six.dove.transport.protocol.Request;
import com.six.dove.transport.protocol.Response;
import com.six.dove.transport.protocol.TransportMessage;
import com.six.dove.transport.protocol.TransportMessageProtocol;

/**
 * @author:MG01867
 * @date:2018年4月27日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class RemoteTransportCodec implements TransportCodec {

	private int maxMessageSize;
	private RemoteSerialize remoteSerialize;

	public RemoteTransportCodec(int maxMessageSize, RemoteSerialize remoteSerialize) {
		this.maxMessageSize = maxMessageSize;
		this.remoteSerialize = remoteSerialize;
	}

	@Override
	public int getMaxBodySzie() {
		return maxMessageSize;
	}

	@Override
	public TransportMessage decoder(ByteBuffer byteBuffer) {
		int type = byteBuffer.get();
		int bodyLength = byteBuffer.getInt();
		if (TransportMessageProtocol.REQUEST == type) {
			byte[] body = new byte[bodyLength];
			byteBuffer.get(body);
			RemoteRequest remoteRequest = remoteSerialize.unSerialize(body, RemoteRequest.class);
			return remoteRequest;
		} else if (TransportMessageProtocol.RESPONSE == type) {
			byte[] body = new byte[bodyLength];
			byteBuffer.get(body);
			return new Response();
		} else if (TransportMessageProtocol.HEARTBEAT == type) {
			return null;
		} else {
			throw new CodecTransportException();
		}
	}

	@Override
	public byte[] encode(TransportMessage transportMessage) {
		byte[] body = remoteSerialize.serialize(transportMessage);
		int bodyLength = body.length;
		ByteBuffer buffer = ByteBuffer.allocate(TransportMessageProtocol.HEAD_LENGTH + bodyLength);
		buffer.put(transportMessage.getType());
		if (bodyLength > 0) {
			buffer.putInt(bodyLength);
			buffer.put(body);
		}
		return buffer.array();
	}
}
