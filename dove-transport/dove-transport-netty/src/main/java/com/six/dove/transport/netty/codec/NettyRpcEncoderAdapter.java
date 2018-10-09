package com.six.dove.transport.netty.codec;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.message.Message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午3:18:19
 */
public class NettyRpcEncoderAdapter extends MessageToByteEncoder<Message>{

	final static Logger log = LoggerFactory.getLogger(NettyRpcEncoderAdapter.class);

	private TransportCodec<? extends Message,? extends Message> transportCodec;

	public NettyRpcEncoderAdapter(TransportCodec<? extends Message,? extends Message> TransportCodec) {
		Objects.requireNonNull(TransportCodec, "TransportCodec must be not null");
		this.transportCodec = TransportCodec;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Message message, ByteBuf out) throws Exception {
		byte[] data=transportCodec.encode(null);
		out.writeBytes(data);
	}

}
