package six.com.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import six.com.rpc.WrapperService;
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
 */
public class ServerHandler extends SimpleChannelInboundHandler<RpcMsg> {

	final static Logger log = LoggerFactory.getLogger(ServerHandler.class);

	private RpcServer rpcServer;

	public ServerHandler(RpcServer rpcServer) {
		this.rpcServer = rpcServer;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcMsg msg) throws Exception {
		RpcResponse rpcesponse = null;
		if (msg instanceof RpcRequest) {
			rpcesponse = processRequest(ctx, (RpcRequest) msg);
		} else {
			String errMsg = "ServerHandler messageReceived type not support: class=" + msg.getClass();
			rpcesponse = new RpcResponse();
			rpcesponse.setMsg(errMsg);
			log.error(errMsg);
		}
		ctx.writeAndFlush(rpcesponse);
	}

	private RpcResponse processRequest(ChannelHandlerContext ctx, RpcRequest rpcRequest) {
		WrapperService wrapperService = rpcServer.get(rpcRequest.getCommand());
		RpcResponse rpcResponse = new RpcResponse();
		rpcResponse.setId(rpcRequest.getId());
		String address = ctx.channel().remoteAddress().toString();
		if (null != wrapperService) {
			log.debug("server received coommand[" + rpcRequest.getCommand() + "] from:" + address);
			try {
				Object result = wrapperService.invoke(rpcRequest.getParams());
				rpcResponse.setStatus(RpcResponseStatus.succeed);
				rpcResponse.setResult(result);
			} catch (Exception e) {
				String errMsg = ExceptionUtils.getExceptionMsg(e);
				rpcResponse.setStatus(RpcResponseStatus.invokeErr);
				rpcResponse.setMsg(errMsg);
				log.error("invoke request[" + address + "] err", e);
			}
		} else {
			rpcResponse.setStatus(RpcResponseStatus.notFoundService);
			rpcResponse.setMsg("did not find service by rpcRequest[" + address + "]:" + rpcRequest.getCommand());
			log.error("did not find service by rpcRequest[" + address + "]:" + rpcRequest.getCommand());
		}
		return rpcResponse;
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
				log.error("the msg is illegal from channel[" + address + "]", cause);
			} else if (signalErr.getRpcSystenType() == RpcSystenExceptions.MSG_TOO_BIG) {
				RpcResponse response = new RpcResponse();
				response.setMsg("the msg is too big");
				ctx.writeAndFlush(response);
				ctx.close();
				log.error("the msg is too big from channel[" + address + "]", cause);
			} else if (signalErr.getRpcSystenType() == RpcSystenExceptions.READER_IDLE) {
				ch.close();
				log.error("the channel[" + address + "] is reader idle and will be close", cause);
			}
		} else {
			ch.close();
			log.error("unknow err and close channel[" + address + "]", cause);
		}
	}

}
