package com.six.dove.transport.netty.coder;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.protocol.TransportMessage;
import com.six.dove.transport.protocol.TransportMessageProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月23日 上午8:50:07
 * 
 */
public class NettyRpcDecoderAdapter extends LengthFieldBasedFrameDecoder {

	final static Logger log = LoggerFactory.getLogger(NettyRpcDecoderAdapter.class);

	private TransportCodec transportCodec;

	public NettyRpcDecoderAdapter(TransportCodec transportCodec) {
		super(transportCodec.getMaxBodySzie(), TransportMessageProtocol.MSG_TYPE, TransportMessageProtocol.BODY_LENGTH);
		Objects.requireNonNull(transportCodec, "transportCodec must be not null");
		this.transportCodec = transportCodec;
	}

	@Override
	public Object decode(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
		TransportMessage transportMessage = null;
		ByteBuf frame = null;
		try {
			frame = (ByteBuf) super.decode(ctx, byteBuf);
			if (null != frame) {
				ByteBuffer byteBuffer = frame.nioBuffer();
				transportMessage = transportCodec.decoder(byteBuffer);
			}
		} finally {
			if (null != frame) {
				frame.release();
			}
		}
		return transportMessage;
	}
}
