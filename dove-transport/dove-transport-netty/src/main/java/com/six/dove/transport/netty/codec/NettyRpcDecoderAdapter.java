package com.six.dove.transport.netty.codec;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.transport.Message;
import com.six.dove.transport.TransportCodec;
import com.six.dove.transport.message.MessageProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月23日 上午8:50:07
 * 
 */
public class NettyRpcDecoderAdapter
		extends LengthFieldBasedFrameDecoder {

	final static Logger log = LoggerFactory.getLogger(NettyRpcDecoderAdapter.class);

	private TransportCodec transportCodec;

	public NettyRpcDecoderAdapter(int maxBodySzie,
			TransportCodec transportCodec) {
		super(maxBodySzie, MessageProtocol.MSG_TYPE, MessageProtocol.BODY_LENGTH);
		Objects.requireNonNull(transportCodec, "transportCodec must be not null");
		this.transportCodec = transportCodec;
	}

	@Override
	public Object decode(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
		Message message = null;
		ByteBuf frame = null;
		try {
			frame = (ByteBuf) super.decode(ctx, byteBuf);
			if (null != frame) {
				ByteBuffer byteBuffer = frame.nioBuffer();
				message = transportCodec.decoder(byteBuffer);
			}
		} finally {
			if (null != frame) {
				frame.release();
			}
		}
		return message;
	}
}
