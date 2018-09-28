package com.six.dove.transport;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.protocol.TransportMessage;
import com.six.dove.transport.server.ReceiveMessageHandler;

/**
 * @author:MG01867
 * @date:2018年3月27日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public abstract class AbstractTransport<Conn extends Connection,Message extends TransportMessage> implements Transport<Conn,Message>{

	private Map<String, Conn> connectionPool = new ConcurrentHashMap<>();
	private TransportCodec transportProtocol;
	private ReceiveMessageHandler<Conn,Message> receiveMessageHandler;

	public AbstractTransport(TransportCodec transportProtocol,
			ReceiveMessageHandler<Conn,Message> receiveMessageHandler) {
		Objects.requireNonNull(transportProtocol);
		this.transportProtocol = transportProtocol;
		Objects.requireNonNull(receiveMessageHandler);
		this.receiveMessageHandler = receiveMessageHandler;
	}

	@Override
	public final TransportCodec getTransportProtocol() {
		return transportProtocol;
	}

	@Override
	public final ReceiveMessageHandler<Conn,Message> getReceiveMessageHandler() {
		return receiveMessageHandler;
	}

	@Override
	public Conn getConnection(String id) {
		return connectionPool.get(id);
	}

	@Override
	public void addConnection(Conn connection) {
		connectionPool.put(connection.getId(), connection);
	}

	@Override
	public void removeAndCloseConnection(String id) {
		connectionPool.remove(id);
	}

	public void start() {
		// TODO Auto-generated method stub
		
	}
}
