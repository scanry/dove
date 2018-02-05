package com.six.dove.remote.server.netty;

import java.util.Objects;

import com.six.dove.remote.AbstractRemoteConnection;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.remote.server.ServerRemoteConnection;

import io.netty.channel.Channel;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class NettyServerRpcConnection extends AbstractRemoteConnection<RemoteResponse, Void> implements ServerRemoteConnection {

	private Channel channel;

	protected NettyServerRpcConnection(Channel channel,String host, int port) {
		super(host, port);
		Objects.requireNonNull(channel);
		this.channel = channel;
	}

	@Override
	public boolean available() {
		return channel.isActive();
	}

	@Override
	protected Void doSend(RemoteResponse rpcResponse) {
		channel.writeAndFlush(rpcResponse);
		return null;
	}

	@Override
	public void close() {
		channel.close();
	}

}
