package com.six.transport.socket.server;

import com.six.dove.transport.connection.AbstractConnection;
import com.six.dove.transport.connection.Connection;
import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * @author:MG01867
 * @date:2018年4月13日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class ServerSocketConnection extends AbstractConnection implements Connection {

	final static Logger log = LoggerFactory.getLogger(ServerSocketConnection.class);

	private Socket socket;
	private DataOutputStream outputStream;
	private ExecutorService executorService;
	private TransportCodec transportCodec;

	public ServerSocketConnection(String host, int port, Socket socket, TransportCodec transportCodec,
                                  ExecutorService executorService) throws IOException {
		super(host, port);
		Objects.requireNonNull(socket);
		this.socket = socket;
		this.outputStream = new DataOutputStream(socket.getOutputStream());
		this.transportCodec = transportCodec;
		this.executorService = executorService;
	}

	protected ExecutorService getExecutorService() {
		return executorService;
	}
	
	protected TransportCodec getTransportCodec() {
		return transportCodec;
	}
	
	@Override
	public boolean available() {
		return socket.isConnected();
	}

	@Override
	public void close() throws IOException {
		socket.close();
	}

	@Override
	protected void doSend(Message data, SendListener sendListener) {
		executorService.submit(new SendTask(outputStream, data, sendListener));
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

	class SendTask implements Runnable {

		private DataOutputStream outputStream;
		private Message message;
		private SendListener sendListener;

		private SendTask(DataOutputStream outputStream, Message message, SendListener sendListener) {
			this.outputStream = outputStream;
			this.message = message;
			this.sendListener = sendListener;
		}

		@Override
		public void run() {
			boolean send = false;
			byte[] data = transportCodec.encode(message);
			try {
				outputStream.write(data);
				outputStream.flush();
				send = true;
			} catch (IOException exception) {
				log.error("outputStream write or flush exception", exception);
			}
			if (send) {
				sendListener.complete(succeedSendFutrue);
			} else {
				sendListener.complete(faildSendFutrue);
			}
		}

	}
}
