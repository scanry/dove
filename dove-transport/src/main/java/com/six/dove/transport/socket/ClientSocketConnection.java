package com.six.dove.transport.socket;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;

import com.six.dove.transport.codec.TransportCodec;
import com.six.dove.transport.exception.BigMessageTransportException;
import com.six.dove.transport.exception.CodecTransportException;
import com.six.dove.transport.protocol.Response;
import com.six.dove.transport.protocol.TransportMessageProtocol;
import com.six.dove.transport.server.ReceiveMessageHandler;

/**
 * @author:MG01867
 * @date:2018年4月13日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class ClientSocketConnection<MessageResponse extends Response> extends ServerSocketConnection {

	private ReadTask readTask;
	private ReceiveMessageHandler<ClientSocketConnection<MessageResponse>, MessageResponse> receiveMessageHandler;

	public ClientSocketConnection(String host, int port, Socket socket, TransportCodec transportCodec,
			ReceiveMessageHandler<ClientSocketConnection<MessageResponse>, MessageResponse> receiveMessageHandler,
			ExecutorService executorService) throws IOException {
		super(host, port, socket, transportCodec, executorService);
		this.receiveMessageHandler=receiveMessageHandler;
		this.readTask = new ReadTask(new DataInputStream(socket.getInputStream()));
		executorService.submit(readTask);
	}

	@Override
	public void close() throws IOException {
		readTask.stop();
		super.close();
	}

	private class ReadTask implements Runnable {

		private DataInputStream inputStream;
		private volatile boolean open = true;
		private volatile Thread thread;

		private ReadTask(DataInputStream inputStream) {
			this.inputStream = inputStream;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			thread = Thread.currentThread();
			while (open) {
				byte[] readData = null;
				try {
					byte headByte = inputStream.readByte();
					int dataLength = inputStream.available();
					int allLength = dataLength + 1;
					if (allLength > TransportMessageProtocol.MAX_BODY_SIZE) {
						receiveMessageHandler.exceptionCaught(ClientSocketConnection.this,
								new BigMessageTransportException());
					} else {
						readData = new byte[allLength];
						readData[0] = headByte;
						inputStream.readFully(readData, 1, dataLength);
					}
				} catch (IOException exception) {
					receiveMessageHandler.exceptionCaught(ClientSocketConnection.this, exception);
				}
				ByteBuffer byteBuffer = ByteBuffer.wrap(readData);
				MessageResponse response = null;
				try {
					response = (MessageResponse) getTransportCodec().decoder(byteBuffer);
				} catch (Exception exception) {
					receiveMessageHandler.exceptionCaught(ClientSocketConnection.this,
							new CodecTransportException(exception));
				}
				MessageResponse finalResponse = response;
				getExecutorService().execute(() -> {
					receiveMessageHandler.receive(ClientSocketConnection.this, finalResponse);
				});
			}
		}

		private void stop() {
			open = false;
			if (null != thread) {
				thread.interrupt();
			}
		}
	}

}
