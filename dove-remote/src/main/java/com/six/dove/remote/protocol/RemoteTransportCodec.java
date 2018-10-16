package com.six.dove.remote.protocol;

import java.nio.ByteBuffer;

import com.six.dove.transport.Message;
import com.six.dove.transport.Response;
import com.six.dove.transport.TransportCodec;
import com.six.dove.transport.exception.CodecTransportException;
import com.six.dove.transport.message.MessageProtocol;

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
	public Message decoder(ByteBuffer byteBuffer) {
		int type = byteBuffer.get();
		int bodyLength = byteBuffer.getInt();
		if (MessageProtocol.REQUEST == type) {
			byte[] body = new byte[bodyLength];
			byteBuffer.get(body);
			RemoteRequest remoteRequest = remoteSerialize.unSerialize(body, RemoteRequest.class);
			return remoteRequest;
		} else if (MessageProtocol.RESPONSE == type) {
			byte[] body = new byte[bodyLength];
			byteBuffer.get(body);
			return new Response();
		} else if (MessageProtocol.HEARTBEAT == type) {
			return null;
		} else {
			throw new CodecTransportException();
		}
	}

	@Override
	public byte[] encode(Message transportMessage) {
		byte[] body = remoteSerialize.serialize(transportMessage);
		int bodyLength = body.length;
		ByteBuffer buffer = ByteBuffer.allocate(MessageProtocol.HEAD_LENGTH + bodyLength);
		buffer.put(transportMessage.getType());
		if (bodyLength > 0) {
			buffer.putInt(bodyLength);
			buffer.put(body);
		}
		return buffer.array();
	}
}
