package com.six.dove.transport;

import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.connection.ConnectionPool;
import com.six.dove.transport.handler.ReceiveMessageHandler;
import com.six.dove.transport.message.Message;

import java.util.Objects;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe
 */
public abstract class AbstractTransport<SendMsg extends Message, ReceMsg extends Message>
		implements Transporter<SendMsg, ReceMsg> {

	private int maxBodySzie;
	private ConnectionPool connectionPool;
	private TransportCodec<SendMsg, ReceMsg> transportProtocol;
	private ReceiveMessageHandler<ReceMsg,SendMsg> receiveMessageHandler;

	
	@Override
	public final void start() {
		doStart();
	}
	
	@Override
	public final void setMaxBodySzie(int maxBodySzie) {
		this.maxBodySzie = maxBodySzie;
	}
	
	@Override
	public final void setConnectionPool(ConnectionPool connectionPool) {
		Objects.requireNonNull(connectionPool);
		this.connectionPool = connectionPool;
	}

	@Override
	public final void setTransportProtocol(TransportCodec<SendMsg, ReceMsg> transportProtocol) {
		Objects.requireNonNull(transportProtocol);
		this.transportProtocol = transportProtocol;
	}

	@Override
	public final void setReceiveMessageHandler(
			ReceiveMessageHandler<ReceMsg,SendMsg> receiveMessageHandler) {
		Objects.requireNonNull(receiveMessageHandler);
		this.receiveMessageHandler = receiveMessageHandler;
	}
	
	protected final int getMaxBodySzie() {
		return maxBodySzie;
	}
	
	protected final ConnectionPool getConnectionPool() {
		return connectionPool;
	}

	protected final TransportCodec<SendMsg, ReceMsg> getTransportCodec() {
		return transportProtocol;
	}

	protected final ReceiveMessageHandler<ReceMsg,SendMsg> getReceiveMessageHandler() {
		return receiveMessageHandler;
	}

	@Override
	public final void shutdown() {
		doShutdown();
	}

	protected abstract void doStart();

	protected abstract void doShutdown();
}
