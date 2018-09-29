package com.six.dove.remote.connection;

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

import com.six.dove.remote.exception.RemoteException;
import com.six.dove.remote.protocol.RemoteMsg;
import com.six.dove.remote.protocol.RemoteProtocol;
import com.six.dove.remote.protocol.RemoteSerialize;

/**
 * @author:MG01867
 * @date:2018年2月7日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class SocketRemoteConnection<S extends RemoteMsg, R extends RemoteMsg> extends AbstractRemoteConnection<S, R>
		implements RemoteProtocol {

	final static Logger log = LoggerFactory.getLogger(SocketRemoteConnection.class);

	private final static AtomicInteger SEND_THREAD_INDEX = new AtomicInteger(0);
	private final static AtomicInteger REV_THREAD_INDEX = new AtomicInteger(0);
	private volatile boolean running = true;
	private Socket socket;
	private RemoteSerialize remoteSerialize;
	private DataOutputStream outputStream;
	private DataInputStream inputStream;
	private LinkedBlockingQueue<RemoteRequestEntity> sendQueue;
	private Thread sendThead;
	private Thread revThead;
	private ExecutorService executorService;
	private int workerTheads = 10;
	private Class<R> revClass;

	public SocketRemoteConnection(Socket socket,String host, int port,Class<R> revClass){
		super(host, port);
		this.socket = socket;
		this.revClass=revClass;
		try {
			this.outputStream = new DataOutputStream(this.socket.getOutputStream());
			this.inputStream = new DataInputStream(this.socket.getInputStream());
		} catch (IOException e) {
			throw new RemoteException(e);
		}
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
				byte msgType = entity.msg.getType();
				byte[] body = remoteSerialize.serialize(entity.msg);
				int dataLength = body.length; // 读取消息的长度
				outputStream.writeByte(msgType);// 写入消息类型
				outputStream.writeInt(dataLength); // 先将消息长度写入，也就是消息头
				outputStream.write(body);// 消息体中包含我们要发送的数据
				outputStream.flush();
				entity.sendListener.operationComplete(true,null);
			} catch (Exception e) {
				entity.sendListener.operationComplete(false,e);
			}
		}
	}

	private void rev() {
		while (running) {
			try {
				byte msgType = inputStream.readByte();
				if (RemoteProtocol.REQUEST == msgType || RemoteProtocol.RESPONSE == msgType) {
					int dataLength = inputStream.readInt();
					if (dataLength > 0 && dataLength < RemoteProtocol.MAX_BODY_SIZE) {
						byte[] bodyArray = new byte[dataLength];
						inputStream.readFully(bodyArray, 0, dataLength);
						ByteBuffer message = ByteBuffer.wrap(bodyArray);
						try {
							R decodeResult = remoteSerialize.unSerialize(message.array(), revClass);
							executorService.execute(() -> {
								SocketRemoteConnection.this.getReceiveListener().receive(decodeResult);
							});
						} catch (Exception e) {
							log.error("did not unSerialize rpcRequest from :", e);
						}
					}
				} else {
					happen(RemoteConnection.Event.ILLEGAL);
				}
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
		if (null != socket) {
			try {
				socket.close();
			} catch (IOException e) {
				log.error("", e);
			}
		}
		sendThead.interrupt();
		revThead.interrupt();
		executorService.shutdown();
	}

	@Override
	protected void doSend(S msg, SendListener sendListener) {
		sendQueue.add(new RemoteRequestEntity(msg, sendListener));

	}

	private class RemoteRequestEntity {
		S msg;
		SendListener sendListener;

		private RemoteRequestEntity(S msg, SendListener sendListener) {
			this.msg = msg;
			this.sendListener = sendListener;
		}
	}

	@Override
	public RemoteSerialize getRemoteSerialize() {
		return remoteSerialize;
	}
}
