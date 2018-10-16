package com.six.dove.transport;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 传输端抽象类
 */
public abstract class AbstractTransport<SendMsg extends Message, ReceMsg extends Message>
		implements Transporter<SendMsg, ReceMsg> {

	public final static int DEFAULT_MAX_BODY_SIZE=1024*1000*5; 
	private int maxBodySzie=DEFAULT_MAX_BODY_SIZE;
	private ConcurrentHashMap<NetAddress, Connection<SendMsg>> connectionPool = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Interceptor.Aop, List<Interceptor<ReceMsg, SendMsg>>> interceptors = new ConcurrentHashMap<>();
	private TransportCodec transportProtocol;
	private ReceiveMessageHandler<ReceMsg, SendMsg> receiveMessageHandler;

	protected final Connection<SendMsg> getConnection(NetAddress netAddress) {
		return connectionPool.get(netAddress);
	}

	protected final void addConnection(Connection<SendMsg> connection) {
		connectionPool.put(connection.getNetAddress(), connection);
	}

	protected final void removeConnection(Connection<SendMsg> connection) {
		if (null != connection) {
			if (connection.closed()) {
				connection.close();
			}
			connectionPool.remove(connection.getNetAddress());
		}
	}

	@Override
	public final void setMaxBodySzie(int maxBodySzie) {
		this.maxBodySzie = maxBodySzie;
	}

	@Override
	public final void setTransportCodec(TransportCodec transportProtocol) {
		Objects.requireNonNull(transportProtocol);
		this.transportProtocol = transportProtocol;
	}

	@Override
	public final void setReceiveMessageHandler(ReceiveMessageHandler<ReceMsg, SendMsg> receiveMessageHandler) {
		Objects.requireNonNull(receiveMessageHandler);
		this.receiveMessageHandler = receiveMessageHandler;
	}

	@Override
	public final void addInterceptor(Interceptor.Aop aop, Interceptor<ReceMsg, SendMsg> interceptor) {
		Objects.requireNonNull(aop);
		Objects.requireNonNull(interceptor);
		interceptors.computeIfAbsent(aop, key -> new LinkedList<>()).add(interceptor);
	}

	protected final List<Interceptor<ReceMsg, SendMsg>> listInterceptor(Interceptor.Aop aop) {
		Objects.requireNonNull(aop);
		return interceptors.get(aop);
	}

	protected final int getMaxBodySzie() {
		return maxBodySzie;
	}

	protected final TransportCodec getTransportCodec() {
		return transportProtocol;
	}

	protected final ReceiveMessageHandler<ReceMsg, SendMsg> getReceiveMessageHandler() {
		return receiveMessageHandler;
	}
}
