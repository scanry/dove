package com.six.dove.transport.netty.client;

import com.six.dove.transport.Message;
import com.six.dove.transport.TransportCodec;
import com.six.dove.transport.buffer.DByteBuffer;

/**
 * @author:MG01867
 * @date:2018年3月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class JavaTransportProtocol<SendMsg extends Message,ReceMsg extends Message> implements TransportCodec<SendMsg,ReceMsg> {

	@Override
	public ReceMsg decoder(DByteBuffer byteBuffer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] encode(SendMsg message) {
		// TODO Auto-generated method stub
		return null;
	}



}
