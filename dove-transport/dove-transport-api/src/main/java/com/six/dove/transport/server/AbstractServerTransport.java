package com.six.dove.transport.server;

import com.six.dove.transport.AbstractTransport;
import com.six.dove.transport.NetAddress;
import com.six.dove.transport.Request;
import com.six.dove.transport.Response;

/**
 * @author: Administrator
 * @date: 2018-9-28
 * @time: 22:32:58
 * @email: 359852326@qq.com
 * @version:
 * @describe 服务 传输端-基类
 */
public abstract class AbstractServerTransport<SendMsg extends Response, ReceMsg extends Request>
		extends AbstractTransport<SendMsg, ReceMsg> implements ServerTransport<SendMsg, ReceMsg> {

	private NetAddress netAddress;

	public AbstractServerTransport(int port) {
		this("127.0.0.1", port);
	}

	public AbstractServerTransport(String host, int port) {
		this(new NetAddress(host, port));
	}

	public AbstractServerTransport(NetAddress netAddress) {
		this.netAddress = netAddress;
	}

	@Override
	public final void doStart() {
		innerDoStart(netAddress);
	}

	protected abstract void innerDoStart(NetAddress netAddress);

	@Override
	public final NetAddress getNetAddress() {
		return netAddress;
	}
}
