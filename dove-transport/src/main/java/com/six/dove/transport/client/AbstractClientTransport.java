package com.six.dove.transport.client;

import com.six.dove.transport.AbstractTransport;
import com.six.dove.transport.Connection;
import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.commom.InetAddressUtils;
import com.six.dove.transport.protocol.Response;
import com.six.dove.transport.server.ReceiveMessageHandler;

/**
 * @author:MG01867
 * @date:2018年3月27日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public abstract class AbstractClientTransport<Conn extends Connection, MessageResponse extends Response>
		extends AbstractTransport<Conn, MessageResponse> implements ClientTransport<Conn, MessageResponse>{

	private static int default_connectTimeout = 3000;
	private static int default_writerIdleTime = 30000;

	private int connectTimeout = 3000;
	private int writerIdleTime;

	public AbstractClientTransport(int connectTimeout, int writerIdleTime, TransportCodec transportProtocol,
			ReceiveMessageHandler<Conn, MessageResponse> receiveMessageHandler) {
		super(transportProtocol, receiveMessageHandler);
		this.connectTimeout = connectTimeout <= 0 ? default_connectTimeout : connectTimeout;
		this.writerIdleTime = writerIdleTime <= 0 ? default_writerIdleTime : writerIdleTime;
	}

	@Override
	public void start() {
	}

	@Override
	public final int getConnectTimeout() {
		return connectTimeout;
	}

	@Override
	public final int getWriterIdleTime() {
		return writerIdleTime;
	}

	@Override
	public final Connection find(String host, int port) {
		String connectionId = InetAddressUtils.generateConnectionKey(host, port);
		Connection connection = getConnection(connectionId);
		if (null == connection) {
			connection = newConnection(host, port);
		} else if (!connection.available()) {
			removeAndCloseConnection(connectionId);
			connection = find(host, port);
		}
		if (!connection.available()) {
			throw new RuntimeException(String.format("connection address[%s] failed", connectionId));
		}
		return connection;
	}

	protected abstract Connection newConnection(String host, int port);

	@Override
	public void shutdown() {
		doShutdown();
	}

	protected abstract void doShutdown();
}
