package com.six.dove.remote.server.socket;

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

import com.six.dove.remote.connection.AbstractRemoteConnection;
import com.six.dove.remote.connection.RemoteConnection;
import com.six.dove.remote.connection.SocketRemoteConnection;
import com.six.dove.remote.exception.RemoteException;
import com.six.dove.remote.protocol.RemoteProtocol;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.remote.server.AbstractServerRemote;
import com.six.dove.remote.server.ServerRemote;

/**
 * @author:MG01867
 * @date:2018年2月7日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class SocketServerRemote extends AbstractServerRemote implements RemoteProtocol {

	final static Logger log = LoggerFactory.getLogger(SocketServerRemote.class);

	private ServerSocket serverSocket;
	private Thread accpetThread;
	private ExecutorService executorService;

	public SocketServerRemote(String localHost, int listenPort) {
		this(localHost, listenPort, DEFAULT_EVENT_LOOP_THREADS);
	}

	public SocketServerRemote(String localHost, int listenPort, int workerCodeThreads) {
		super("socket-server-remote", localHost, listenPort);
		this.accpetThread = new Thread(() -> {
			accept();
		}, "SocketServerRemote-accpet-thread");
		accpetThread.setDaemon(true);
		this.executorService = Executors.newFixedThreadPool(workerCodeThreads);
	}

	@Override
	protected void doStart() {
		SocketAddress endpoint = new InetSocketAddress(getLocalHost(), getListenPort());
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(endpoint, ServerRemote.BACKLOG);
			accpetThread.start();
		} catch (Exception e) {
			throw new RemoteException("SocketServerRemote start exception", e);
		}
	}

	private void accept() {
		while (isRunning()) {
			try {
				Socket socket = serverSocket.accept();
				executorService.execute(new ReceiveTask(socket));
			} catch (Exception e) {

			}
		}
	}

	private class ReceiveTask implements Runnable {
		Socket socket;

		private ReceiveTask(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				int size = socket.getInputStream().available();
				System.out.println();
				byte[] daya = new byte[size];
				socket.getInputStream().read(daya);
				ByteBuffer buffer = ByteBuffer.wrap(daya);
				RemoteRequest remoteRequest = (RemoteRequest) decoder(buffer);
				final String id = socket.getInetAddress().toString() + ":" + socket.getPort();
				RemoteConnection<RemoteResponse, RemoteRequest> serverRpcConnection = getConnection(id);
				if (null == serverRpcConnection) {
					String[] addressAndPorts = id.split(":");
					serverRpcConnection = new SocketServerRpcConnection(socket, addressAndPorts[0],
							Integer.valueOf(addressAndPorts[1]));
					addConnection(serverRpcConnection);
				}
				remoteRequest.setServerRpcConnection(serverRpcConnection);
				execute(remoteRequest);
			} catch (IOException e) {
				log.error("", e);
			}
		}

	}

	@Override
	protected void stop2() {
		if (null != serverSocket) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				log.error("close SocketServerRemote.serverSocket IOException", e);
			}
		}
	}

	class SocketServerRpcConnection extends AbstractRemoteConnection<RemoteResponse, RemoteRequest>
			implements RemoteConnection<RemoteResponse, RemoteRequest> {

		private SocketRemoteConnection<RemoteResponse, RemoteRequest> socketRemoteConnection;

		protected SocketServerRpcConnection(Socket socket, String host, int port) {
			super(socket.getInetAddress().getHostAddress(), socket.getPort());
			this.socketRemoteConnection = new SocketRemoteConnection<>(socket, host, port, RemoteRequest.class);
		}

		@Override
		public boolean available() {
			return socketRemoteConnection.available();
		}

		@Override
		public void close() {
			socketRemoteConnection.close();
		}

		@Override
		protected void doSend(RemoteResponse msg, SendListener sendListener) {
			socketRemoteConnection.send(msg, sendListener);
		}

	}

}
