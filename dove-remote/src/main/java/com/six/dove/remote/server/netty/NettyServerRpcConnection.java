package com.six.dove.remote.server.netty;

import java.util.Objects;

import com.six.dove.remote.connection.AbstractRemoteConnection;
import com.six.dove.remote.connection.RemoteConnection;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;

import io.netty.channel.Channel;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class NettyServerRpcConnection extends AbstractRemoteConnection<RemoteResponse, RemoteRequest> implements RemoteConnection<RemoteResponse, RemoteRequest> {

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
	protected void doSend(RemoteResponse rpcResponse,SendListener sendListener) {
		channel.writeAndFlush(rpcResponse);
	}

	@Override
	public void close() {
		channel.close();
	}

}
