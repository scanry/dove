package com.six.dove.transport.netty;

import com.six.dove.transport.message.MessageProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version: 心跳数据包 只包含 数据类型 占用一个字节
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
