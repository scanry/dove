package six.com.rpc.server.netty;

import java.util.Objects;

import io.netty.channel.Channel;
import six.com.remote.AbstractRpcConnection;
import six.com.remote.server.ServerRpcConnection;
import six.com.rpc.protocol.RpcResponse;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class NettyServerRpcConnection extends AbstractRpcConnection<RpcResponse, Void> implements ServerRpcConnection {

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
	public Void send(RpcResponse rpcResponse) {
		channel.writeAndFlush(rpcResponse);
		return null;
	}

	@Override
	public void close() {
		channel.close();
	}

}
