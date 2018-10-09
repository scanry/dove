package com.six.dove.transport.client;

import com.six.dove.transport.*;
import com.six.dove.transport.connection.Connection;
import com.six.dove.transport.message.Request;
import com.six.dove.transport.message.Response;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 客户 传输端基类
 */
public abstract class AbstractClientTransport<SendMsg extends Request, ReceMsg extends Response>
		extends AbstractTransport<SendMsg, ReceMsg> implements ClientTransport<SendMsg,ReceMsg>{

	private long connectTimeout;
	private long sendTimeout;
	private long writerIdleTime;

	public AbstractClientTransport(long connectTimeout, long sendTimeout, long writerIdleTime) {
		if (connectTimeout <= 0) {
			throw new IllegalArgumentException(
					String.format("The connectTimeout[%s] must greater than 0", connectTimeout));
		}
		if (sendTimeout <= 0) {
			throw new IllegalArgumentException(String.format("The sendTimeout[%s] must greater than 0", sendTimeout));
		}
		if (writerIdleTime <= 0) {
			throw new IllegalArgumentException(
					String.format("The connectTimeout[%s] must greater than 0", writerIdleTime));
		}
		this.connectTimeout = connectTimeout;
		this.sendTimeout = connectTimeout;
		this.writerIdleTime = writerIdleTime;
	}

	@Override
	protected final void doStart() {

	}

	@Override
	public final Connection<SendMsg> connect(String host, int port) {
		String connectionId = Connection.generateId(host, port);
		Connection<SendMsg> connection = getConnectionPool().get(connectionId);
		if (null == connection) {
			connection = newConnection(host, port);
		} else if (!connection.available()) {
			getConnectionPool().remove(connection);
			connection = newConnection(host, port);
		}
		if (!connection.available()) {
			throw new RuntimeException(String.format("connection address[%s] failed", connectionId));
		}
		return connection;
	}

	protected abstract Connection<SendMsg> newConnection(String host, int port);

	protected final long getConnectTimeout() {
		return connectTimeout;
	}

	protected final long getSendTimeout() {
		return sendTimeout;
	}

	protected final long getWriterIdleTime() {
		return writerIdleTime;
	}
}
