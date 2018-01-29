package six.com.rpc.server;

import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import six.com.rpc.common.WrapperServiceTuple;
import six.com.rpc.exception.RpcSystenException;
import six.com.rpc.exception.RpcSystenExceptions;
import six.com.rpc.protocol.RpcMsg;
import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.protocol.RpcResponse;
import six.com.rpc.protocol.RpcResponseStatus;
import six.com.rpc.util.ExceptionUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月21日 上午10:34:59
 *
 */
public class ServerHandler extends SimpleChannelInboundHandler<RpcMsg> {

	final static Logger log = LoggerFactory.getLogger(ServerHandler.class);

	private AbstractServer rpcServer;

	public ServerHandler(AbstractServer rpcServer) {
		this.rpcServer = rpcServer;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcMsg msg) throws Exception {
		RpcResponse rpcesponse = null;
		if (msg instanceof RpcRequest) {
			processRequest(ctx, (RpcRequest) msg);
		} else {
			String errMsg = "ServerHandler messageReceived type not support: class=" + msg.getClass();
			rpcesponse = new RpcResponse();
			rpcesponse.setMsg(errMsg);
			log.error(errMsg);
			ctx.writeAndFlush(rpcesponse);
		}
	}

	private void processRequest(ChannelHandlerContext ctx, RpcRequest rpcRequest) {
		final WrapperServiceTuple wrapperServiceTuple = rpcServer.getWrapperServiceTuple(rpcRequest.getCommand());
		final RpcResponse rpcResponse = new RpcResponse();
		rpcResponse.setId(rpcRequest.getId());
		String address = ctx.channel().remoteAddress().toString();
		if (null != wrapperServiceTuple) {
			try {
				wrapperServiceTuple.getExecutorService().submit(() -> {
					log.debug("server received coommand[" + rpcRequest.getCommand() + "] from:" + address);
					try {
						wrapperServiceTuple.getHook().beforeHook(rpcRequest.getParams());
						Object result = wrapperServiceTuple.getWrapperService().invoke(rpcRequest.getParams());
						wrapperServiceTuple.getHook().afterHook(rpcRequest.getParams());
						rpcResponse.setStatus(RpcResponseStatus.SUCCEED);
						rpcResponse.setResult(result);
					} catch (Exception e) {
						wrapperServiceTuple.getHook().exceptionHook(rpcRequest.getParams());
						String errMsg = ExceptionUtils.getExceptionMsg(e);
						rpcResponse.setStatus(RpcResponseStatus.INVOKE_ERR);
						rpcResponse.setMsg(errMsg);
						log.error("invoke request[" + address + "] err", e);
					}
					ctx.writeAndFlush(rpcResponse);
				});
			} catch (RejectedExecutionException e) {
				// 业务处理线程池满了，拒绝异常
				rpcResponse.setStatus(RpcResponseStatus.REJECT);
				String msg = "the service is too busy and reject rpcRequest[" + address + "]:"
						+ rpcRequest.getCommand();
				rpcResponse.setMsg(msg);
				log.error(msg);
				ctx.writeAndFlush(rpcResponse);
			}
		} else {
			rpcResponse.setStatus(RpcResponseStatus.UNFOUND_SERVICE);
			String msg = "unfound service by rpcRequest[" + address + "]:" + rpcRequest.getCommand();
			rpcResponse.setMsg(msg);
			log.error(msg);
			ctx.writeAndFlush(rpcResponse);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Channel ch = ctx.channel();
		String address = ctx.channel().remoteAddress().toString();
		if (cause instanceof RpcSystenException) {
			RpcSystenException signalErr = (RpcSystenException) cause;
			if (signalErr.getRpcSystenType() == RpcSystenExceptions.MSG_ILLEGAL_TYPE) {
				RpcResponse response = new RpcResponse();
				response.setMsg("the msg is illegal");
				ctx.writeAndFlush(response);
				ctx.close();
				log.warn("the msg is illegal from channel[" + address + "]");
			} else if (signalErr.getRpcSystenType() == RpcSystenExceptions.MSG_TOO_BIG) {
				RpcResponse response = new RpcResponse();
				response.setMsg("the msg is too big");
				ctx.writeAndFlush(response);
				ctx.close();
				log.warn("the msg is too big from channel[" + address + "]");
			} else if (signalErr.getRpcSystenType() == RpcSystenExceptions.READER_IDLE) {
				ch.close();
				log.warn("the channel[" + address + "] is reader idle and will be close");
			}
		} else if (cause instanceof IOException) {
			ch.close();
			log.warn("unknow err and close channel[" + address + "]");
		} else {
			ch.close();
			log.warn("unknow err and close channel[" + address + "]", cause);
		}
	}

}
