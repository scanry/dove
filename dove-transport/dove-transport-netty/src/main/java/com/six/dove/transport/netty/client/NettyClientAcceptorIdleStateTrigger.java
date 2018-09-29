package com.six.dove.transport.netty.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.transport.netty.HeartbeatMsg;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author: MG01867
 * @date: 2018/9/29
 * @email: 359852326@qq.com
 * @version:
 * @describe: netty连接 空闲心跳
 */
@ChannelHandler.Sharable
public class NettyClientAcceptorIdleStateTrigger extends ChannelInboundHandlerAdapter {

	final static Logger log = LoggerFactory.getLogger(NettyClientAcceptorIdleStateTrigger.class);

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleState state = ((IdleStateEvent) evt).state();
			if (state == IdleState.WRITER_IDLE) {
				ctx.writeAndFlush(HeartbeatMsg.heartbeatMsg());
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

}
