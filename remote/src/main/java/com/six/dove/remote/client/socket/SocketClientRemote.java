package com.six.dove.remote.client.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.remote.client.AbstractClientRemote;
import com.six.dove.remote.client.AbstractClientRemoteConnection;
import com.six.dove.remote.client.ClientRemoteConnection;
import com.six.dove.remote.client.RemoteFuture;
import com.six.dove.remote.client.AbstractClientRemoteConnection.SendListener;
import com.six.dove.remote.compiler.Compiler;
import com.six.dove.remote.compiler.impl.JavaCompilerImpl;
import com.six.dove.remote.protocol.RemoteProtocol;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.remote.protocol.RemoteSerialize;

/**
 * @author:MG01867
 * @date:2018年2月6日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe
 */
public class SocketClientRemote extends AbstractClientRemote {

	final static Logger log = LoggerFactory.getLogger(SocketClientRemote.class);

	public SocketClientRemote() {
		this(new JavaCompilerImpl(), new RemoteSerialize() {
		});
	}

	public SocketClientRemote(Compiler compiler, RemoteSerialize remoteSerialize) {
		super("socket-client-remote", compiler, remoteSerialize);
	}

	@Override
	protected ClientRemoteConnection newRpcConnection(String callHost, int callPort) {
		ClientRemoteConnection clientRemoteConnection = null;
		try {
			Socket socket = new Socket(callHost, callPort);
			clientRemoteConnection = new SocketClientRemoteConnection(socket, this, callHost, callPort);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return clientRemoteConnection;
	}

	private final static AtomicInteger SEND_THREAD_INDEX = new AtomicInteger(0);
	private final static AtomicInteger REV_THREAD_INDEX = new AtomicInteger(0);

	private class SocketClientRemoteConnection extends AbstractClientRemoteConnection {
		private volatile boolean running = true;
		private Socket socket;
		private DataOutputStream outputStream;
		private DataInputStream inputStream;
		private LinkedBlockingQueue<RemoteRequestEntity> sendQueue;
		private Thread sendThead;
		private Thread revThead;
		private ExecutorService executorService;
		private int workerTheads = 10;

		protected SocketClientRemoteConnection(Socket socket, AbstractClientRemote clientRemote, String host, int port)
				throws IOException {
			super(clientRemote, host, port);
			this.socket = socket;
			this.outputStream = new DataOutputStream(this.socket.getOutputStream());// this.socket.getOutputStream();
			this.inputStream = new DataInputStream(this.socket.getInputStream());
			this.sendQueue = new LinkedBlockingQueue<>();
			this.executorService = Executors.newFixedThreadPool(workerTheads);
			this.sendThead = new Thread(() -> {
				send();
			}, "SocketClientRemoteConnection-send-thread-" + SEND_THREAD_INDEX.getAndIncrement());
			sendThead.setDaemon(true);
			sendThead.start();
			this.revThead = new Thread(() -> {
				rev();
			}, "SocketClientRemoteConnection-rev-thread-" + REV_THREAD_INDEX.getAndIncrement());
			revThead.setDaemon(true);
			revThead.start();
		}

		private void send() {
			RemoteRequestEntity entity = null;
			while (running) {
				try {
					entity = sendQueue.take();
					byte msgType = entity.request.getType();
					byte[] body = getRemoteSerialize().serialize(entity.request);
					int dataLength = body.length; // 读取消息的长度
					outputStream.writeByte(msgType);// 写入消息类型
					outputStream.writeInt(dataLength); // 先将消息长度写入，也就是消息头
					outputStream.write(body);// 消息体中包含我们要发送的数据
					outputStream.flush();
					entity.sendListener.operationComplete(true);
				} catch (Exception e) {
					entity.sendListener.operationComplete(false);
					log.error("", e);
				}
			}
		}

		private void rev() {
			while (running) {
				try {
					byte msgType = inputStream.readByte();
					if (RemoteProtocol.REQUEST == msgType || RemoteProtocol.RESPONSE == msgType) {
						int dataLength = inputStream.readInt();
						if (dataLength <= 0 || dataLength > RemoteProtocol.MAX_BODY_SIZE) {

						}
						byte[] bodyArray = new byte[dataLength];
						inputStream.readFully(bodyArray, 0, dataLength);
						ByteBuffer message = ByteBuffer.wrap(bodyArray);
						if (RemoteProtocol.REQUEST == msgType) {
							try {
								RemoteRequest decodeResult = getRemoteSerialize().unSerialize(message.array(),
										RemoteRequest.class);
								System.out.println(decodeResult);
							} catch (Exception e) {
								log.error("did not unSerialize rpcRequest from :", e);
							}
						} else {
							try {
								RemoteResponse decodeResult = getRemoteSerialize().unSerialize(message.array(),
										RemoteResponse.class);
								executorService.execute(() -> {
									RemoteFuture remoteFuture = SocketClientRemoteConnection.this
											.removeRemoteFuture(decodeResult.getId());
									if (null != remoteFuture) {
										remoteFuture.onComplete(decodeResult);
									}
								});
							} catch (Exception e) {
								log.error("did not unSerialize rpcResponse from ", e);
							}
						}
					} else {
						log.error("ClientHandler messageReceived type[" + msgType + "] not support");
					}
					// revQueue.add(decodeResult);

				} catch (IOException e) {
					log.error("", e);
				}
			}
		}

		@Override
		public boolean available() {
			return socket.isConnected();
		}

		@Override
		public synchronized void close() {
			this.running = false;
			sendThead.interrupt();
			revThead.interrupt();
			if (null != socket) {
				try {
					socket.close();
				} catch (IOException e) {
					log.error("", e);
				}
			}
		}

		@Override
		protected void write(RemoteRequest request, SendListener sendListener) {
			sendQueue.add(new RemoteRequestEntity(request, sendListener));
		}

	}

	class RemoteRequestEntity {
		RemoteRequest request;
		SendListener sendListener;

		private RemoteRequestEntity(RemoteRequest request, SendListener sendListener) {
			this.request = request;
			this.sendListener = sendListener;
		}
	}

	@Override
	protected void stop1() {
	}

}
