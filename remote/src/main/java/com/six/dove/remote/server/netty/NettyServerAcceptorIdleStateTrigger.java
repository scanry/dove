package com.six.dove.remote.server.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.remote.server.exception.RemoteSystenExceptions;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月22日 下午1:09:17
 */
@ChannelHandler.Sharable
public class NettyServerAcceptorIdleStateTrigger extends ChannelInboundHandlerAdapter {

	final static Logger log = LoggerFactory.getLogger(NettyServerAcceptorIdleStateTrigger.class);

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleState state = ((IdleStateEvent) evt).state();
			if (state == IdleState.ALL_IDLE) {
				throw RemoteSystenExceptions.READER_IDLE_ERR;
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

}
