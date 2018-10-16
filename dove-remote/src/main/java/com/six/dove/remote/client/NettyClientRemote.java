package com.six.dove.remote.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.remote.compiler.Compiler;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.transport.ReceiveMessageHandler;
import com.six.dove.transport.TransportCodec;
import com.six.dove.transport.client.ClientTransport;
import com.six.dove.transport.netty.NettyConnection;
import com.six.dove.transport.netty.client.NettyClientTransport;

/**
 * @author:MG01867
 * @date:2018年4月27日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class NettyClientRemote extends AbstractClientRemote {

	final static Logger log = LoggerFactory.getLogger(NettyClientRemote.class);

	private ClientTransport<?, ?> clientTransport;
	private int workerIoThreads;
	private int connectTimeout;
	private int allIdleTimeSeconds;

	public NettyClientRemote(int workerIoThreads, int connectTimeout) {
		this(null, null);
	}

	public NettyClientRemote(Compiler compiler, TransportCodec transportCodec) {
		super("NettyClientRemote", compiler, transportCodec);
		clientTransport = new NettyClientTransport<>(workerIoThreads, connectTimeout, allIdleTimeSeconds,
				transportCodec, new NettyClientReceiveMessageHandler());
	}

	@Override
	protected void doStart() {
		clientTransport.start();
	}

	@Override
	protected void doStop() {
		if (null != clientTransport) {
			clientTransport.shutdown();
		}
	}

	private class NettyClientReceiveMessageHandler implements ReceiveMessageHandler<NettyConnection, RemoteResponse> {

		@Override
		public void receive(NettyConnection connection, RemoteResponse message) {
			ClientInoker clientInoker = AbstractClientInoker.removeClientInokerFromCache(message.getId());
			if (null != clientInoker) {
				clientInoker.onComplete(message);
			} else {
				log.warn(String.format("unfound clientInoker by id[%s]", message.getId()));
			}
		}

		@Override
		public void connActive(NettyConnection connection) {
			// TODO Auto-generated method stub

		}

		@Override
		public void connInactive(NettyConnection connection) {
			// TODO Auto-generated method stub

		}

		@Override
		public void exceptionCaught(NettyConnection connection, Throwable cause) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	protected ClientTransport<?, ?> getClientTransport() {
		return clientTransport;
	}

}
