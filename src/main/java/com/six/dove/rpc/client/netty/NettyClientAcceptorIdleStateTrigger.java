package com.six.dove.rpc.client.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.remote.protocol.HeartbeatMsg;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月22日 下午1:10:36
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
