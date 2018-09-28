package com.six.dove.transport.client.socket;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.transport.Connection;
import com.six.dove.transport.Connection.SendFutrue;
import com.six.dove.transport.client.AbstractClientTransport;
import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.protocol.JavaTransportProtocol;
import com.six.dove.transport.protocol.Response;
import com.six.dove.transport.server.ReceiveMessageHandler;
import com.six.dove.transport.socket.ClientSocketConnection;

/**
 * @author:MG01867
 * @date:2018年4月12日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class SocketClientTransport<MessageResponse extends Response>
		extends AbstractClientTransport<ClientSocketConnection<MessageResponse>, MessageResponse> {

	final static Logger log = LoggerFactory.getLogger(SocketClientTransport.class);

	private ExecutorService workerExecutorService;

	public SocketClientTransport(int workerSize, int connectTimeout, int writerIdleTime,
			TransportCodec transportProtocol,
			ReceiveMessageHandler<ClientSocketConnection<MessageResponse>, MessageResponse> receiveMessageHandler) {
		super(connectTimeout, writerIdleTime, transportProtocol, receiveMessageHandler);
		workerExecutorService = Executors.newFixedThreadPool(workerSize);
	}

	public SocketClientTransport(int workerSize, int connectTimeout, int writerIdleTime, int maxMessageSize,
			ReceiveMessageHandler<ClientSocketConnection<MessageResponse>, MessageResponse> receiveMessageHandler) {
		super(connectTimeout, writerIdleTime, new JavaTransportProtocol(maxMessageSize), receiveMessageHandler);
		workerExecutorService = Executors.newFixedThreadPool(workerSize);
	}

	@Override
	protected Connection newConnection(String host, int port) {
		Socket socket = null;
		try {
			socket = new Socket(host, port);
			return new ClientSocketConnection<>(host, port, socket, getTransportProtocol(), getReceiveMessageHandler(),
					workerExecutorService);
		} catch (IOException exception) {
			throw new RuntimeException(String.format("create sokcet to address[%s:%s]", host, port), exception);
		}
	}

	@Override
	protected void doShutdown() {
		workerExecutorService.shutdown();
	}

	static SendFutrue succeedSendFutrue = new SendFutrue() {

		@Override
		public boolean isSucceed() {
			return true;
		}
	};
	static SendFutrue faildSendFutrue = new SendFutrue() {
		@Override
		public boolean isSucceed() {
			return false;
		}
	};

}
