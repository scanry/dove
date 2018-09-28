package com.six.dove.transport.server.sokcet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.exception.TransportException;
import com.six.dove.transport.protocol.Request;
import com.six.dove.transport.server.AbstractServerTransport;
import com.six.dove.transport.server.ReceiveMessageHandler;
import com.six.dove.transport.socket.ServerSocketConnection;

/**
 * @author:MG01867
 * @date:2018年4月13日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class SocketServerTransport<MessageRequest extends Request>
		extends AbstractServerTransport<ServerSocketConnection, MessageRequest> {

	final static Logger log = LoggerFactory.getLogger(SocketServerTransport.class);

	private ServerSocket serverSocket;
	private ExecutorService executorService;
	private Thread accpetThread;
	private int ioWorkerThreads;
	private volatile boolean start;

	public SocketServerTransport(String host, int port, TransportCodec transportProtocol,
			ReceiveMessageHandler<ServerSocketConnection, MessageRequest> receiveMessageHandler, int ioWorkerThreads) {
		super(host, port, transportProtocol, receiveMessageHandler);
		this.ioWorkerThreads = ioWorkerThreads;
	}

	@SuppressWarnings("unchecked")
	private void accept() {
		while (start) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				Socket acceptSocket = socket;
				ServerSocketConnection serverSocketConnection = new ServerSocketConnection(
						socket.getInetAddress().getHostAddress(), socket.getPort(), socket, getTransportProtocol(),
						executorService);
				executorService.execute(() -> {
					try {
						int size = acceptSocket.getInputStream().available();
						byte[] daya = new byte[size];
						acceptSocket.getInputStream().read(daya);
						ByteBuffer buffer = ByteBuffer.wrap(daya);
						MessageRequest messageRequest = (MessageRequest) getTransportProtocol().decoder(buffer);
						getReceiveMessageHandler().receive(serverSocketConnection, messageRequest);
					} catch (IOException exception) {
						getReceiveMessageHandler().exceptionCaught(serverSocketConnection, exception);
					}
				});
			} catch (Exception exception) {
				if (exception instanceof InterruptedException) {
					log.warn("interrupted serverSocket accept thread", exception);
				} else {
					log.error("serverSocket accept exception", exception);
					if (null != socket) {
						try {
							socket.close();
						} catch (IOException exception1) {
							log.error("close accept exception", exception1);
						}
					}
				}
			}
		}
	}

	@Override
	protected synchronized void doStart(String host, int port) {
		if (!start) {
			start = true;
			SocketAddress endpoint = new InetSocketAddress(host, port);
			try {
				serverSocket = new ServerSocket();
				serverSocket.bind(endpoint, 1024);
			} catch (Exception e) {
				throw new TransportException("SocketServerTransport init exception", e);
			}
			this.accpetThread = new Thread(() -> {
				accept();
			}, "socketServerTransport-accpet-thread");
			accpetThread.setDaemon(true);
			this.executorService = Executors.newFixedThreadPool(ioWorkerThreads);
			accpetThread.start();
		}
	}

	@Override
	public synchronized void shutdown() {
		start = false;
		accpetThread.interrupt();
		executorService.shutdown();
	}
}
