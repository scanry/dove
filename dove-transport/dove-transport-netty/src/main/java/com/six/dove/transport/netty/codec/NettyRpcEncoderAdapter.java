package com.six.dove.transport.netty.codec;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.transport.Message;
import com.six.dove.transport.TransportCodec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author 作者
 * @email: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午3:18:19
 */
public class NettyRpcEncoderAdapter
		extends MessageToByteEncoder<Message> {

	final static Logger log = LoggerFactory.getLogger(NettyRpcEncoderAdapter.class);

	private TransportCodec transportCodec;

	public NettyRpcEncoderAdapter(TransportCodec TransportCodec) {
		Objects.requireNonNull(TransportCodec, "TransportCodec must be not null");
		this.transportCodec = TransportCodec;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Message message, ByteBuf out) throws Exception {
		ByteBuffer data = transportCodec.encode(message);
		out.writeBytes(data.array());
	}

}
