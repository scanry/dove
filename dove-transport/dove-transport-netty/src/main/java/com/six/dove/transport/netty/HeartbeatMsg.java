package com.six.dove.transport.netty;

import com.six.dove.transport.MessageProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月22日 上午11:03:20 
 * <p>心跳数据包 只包含 数据类型 占用一个字节</p>
 */
public class HeartbeatMsg {

	private static final ByteBuf HEARTBEAT_BUF;

	static {
		ByteBuf buf = Unpooled.buffer(MessageProtocol.HEAD_LENGTH);
		buf.writeByte(MessageProtocol.HEARTBEAT);
		HEARTBEAT_BUF = Unpooled.unreleasableBuffer(buf).asReadOnly();
	}

	public static ByteBuf heartbeatMsg() {
		return HEARTBEAT_BUF.duplicate();
	}

}
