package com.six.dove.remote.server.netty;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.remote.server.AbstractServerRemote;
import com.six.dove.remote.server.ServerRemoteConnection;
import com.six.dove.remote.server.exception.RemoteSystenException;
import com.six.dove.remote.server.exception.RemoteSystenExceptions;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月21日 上午10:34:59
 *
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RemoteRequest> {

	final static Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

	private AbstractServerRemote serverRemote;

	public NettyServerHandler(AbstractServerRemote serverRemote) {
		this.serverRemote = serverRemote;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RemoteRequest rpcRequest) throws Exception {
		final String id = getId(ctx);
		ServerRemoteConnection serverRpcConnection = serverRemote.getConnection(id);
		if (null == serverRpcConnection) {
			String[] addressAndPorts = id.split(":");
			serverRpcConnection = new NettyServerRpcConnection(ctx.channel(), addressAndPorts[0],
					Integer.valueOf(addressAndPorts[1]));
			serverRemote.addConnection(serverRpcConnection);
		}
		rpcRequest.setServerRpcConnection(serverRpcConnection);
		serverRemote.execute(rpcRequest);
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
		if (cause instanceof RemoteSystenException) {
			RemoteSystenException signalErr = (RemoteSystenException) cause;
			if (signalErr.getRpcSystenType() == RemoteSystenExceptions.MSG_ILLEGAL_TYPE) {
				RemoteResponse response = new RemoteResponse();
				response.setMsg("the msg is illegal");
				ctx.writeAndFlush(response);
				log.warn("the msg is illegal from channel[" + address + "]");
			} else if (signalErr.getRpcSystenType() == RemoteSystenExceptions.MSG_TOO_BIG) {
				RemoteResponse response = new RemoteResponse();
				response.setMsg("the msg is too big");
				ctx.writeAndFlush(response);
				log.warn("the msg is too big from channel[" + address + "]");
			} else if (signalErr.getRpcSystenType() == RemoteSystenExceptions.READER_IDLE) {
				log.warn("the channel[" + address + "] is reader idle and will be close");
			}
		} else if (cause instanceof IOException) {
			log.warn("unknow err and close channel[" + address + "]");
		} else {
			log.warn("unknow err and close channel[" + address + "]", cause);
		}
		ch.close();
		serverRemote.removeConnection(getId(ctx));
	}

}
