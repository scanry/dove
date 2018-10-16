package com.six.dove.transport.client;

import com.six.dove.transport.*;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 客户 传输端基类
 */
public abstract class AbstractClientTransport<SendMsg extends Request, ReceMsg extends Response>
		extends AbstractTransport<SendMsg, ReceMsg> implements ClientTransport<SendMsg, ReceMsg> {

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
	public final Connection<SendMsg> connect(NetAddress netAddress) {
		Connection<SendMsg> connection = getConnection(netAddress);
		if (null == connection) {
			connection = newConnection(netAddress);
		} else if (!connection.available()) {
			removeConnection(connection);
			connection = newConnection(netAddress);
		}
		if (!connection.available()) {
			throw new RuntimeException(String.format("connection address[%s] failed", netAddress));
		}
		return connection;
	}

	protected abstract Connection<SendMsg> newConnection(NetAddress netAddress);

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
