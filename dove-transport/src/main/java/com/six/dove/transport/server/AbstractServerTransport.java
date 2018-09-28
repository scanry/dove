package com.six.dove.transport.server;

import com.six.dove.transport.AbstractTransport;
import com.six.dove.transport.Connection;
import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.commom.InetAddressUtils;
import com.six.dove.transport.protocol.Request;

/**
 * @author:MG01867
 * @date:2018年3月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public abstract class AbstractServerTransport<Conn extends Connection, MessageRequest extends Request>
		extends AbstractTransport<Conn, MessageRequest> implements ServerTransport<Conn, MessageRequest> {

	private String host;
	private int port;

	public AbstractServerTransport(String host, int port, TransportCodec transportProtocol,
			ReceiveMessageHandler<Conn, MessageRequest> receiveMessageHandler) {
		super(transportProtocol, receiveMessageHandler);
		this.host = InetAddressUtils.checkHost(host);
		this.port = InetAddressUtils.checkPort(port);
	}

	@Override
	public final void start() {
		doStart(host, port);
	}

	protected abstract void doStart(String host, int port);

	public final String getHost() {
		return host;
	}

	public final int getPort() {
		return port;
	}

}
