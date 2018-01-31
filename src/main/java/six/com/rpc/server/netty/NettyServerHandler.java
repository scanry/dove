package six.com.rpc.server.netty;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import six.com.remote.server.ServerRpcConnection;
import six.com.rpc.exception.RpcSystenException;
import six.com.rpc.exception.RpcSystenExceptions;
import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.protocol.RpcResponse;
import six.com.rpc.server.AbstractServer;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月21日 上午10:34:59
 *
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

	final static Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

	private AbstractServer rpcServer;

	public NettyServerHandler(AbstractServer rpcServer) {
		this.rpcServer = rpcServer;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
		final String id = getId(ctx);
		ServerRpcConnection serverRpcConnection = rpcServer.getConnection(id);
		if (null == serverRpcConnection) {
			String[] addressAndPorts = id.split(":");
			serverRpcConnection = new NettyServerRpcConnection(ctx.channel(), addressAndPorts[0],
					Integer.valueOf(addressAndPorts[1]));
			rpcServer.addConnection(serverRpcConnection);
		}
		rpcRequest.setServerRpcConnection(serverRpcConnection);
		rpcServer.execute(rpcRequest);
	}

	private static String getId(ChannelHandlerContext ctx) {
		String addressAndPort = ctx.channel().remoteAddress().toString();
		addressAndPort = addressAndPort.replace("/", "");
		return addressAndPort;
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
				log.warn("the msg is illegal from channel[" + address + "]");
			} else if (signalErr.getRpcSystenType() == RpcSystenExceptions.MSG_TOO_BIG) {
				RpcResponse response = new RpcResponse();
				response.setMsg("the msg is too big");
				ctx.writeAndFlush(response);
				log.warn("the msg is too big from channel[" + address + "]");
			} else if (signalErr.getRpcSystenType() == RpcSystenExceptions.READER_IDLE) {
				log.warn("the channel[" + address + "] is reader idle and will be close");
			}
		} else if (cause instanceof IOException) {
			log.warn("unknow err and close channel[" + address + "]");
		} else {
			log.warn("unknow err and close channel[" + address + "]", cause);
		}
		ch.close();
		rpcServer.removeConnection(getId(ctx));
	}

}
