package com.six.dove.remote.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.six.dove.remote.AsyCallback;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.remote.protocol.RemoteResponseState;
import com.six.dove.transport.client.ClientTransport;
import com.six.dove.transport.message.Request;

/**
 * @author:MG01867
 * @date:2018年4月12日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class AsyClientInoker extends AbstractClientInoker {

	private final static int default_listen_request_max = 1000;
	private static ScheduledExecutorService scheduledExecutorService;
	static {
		scheduledExecutorService = Executors.newScheduledThreadPool(default_listen_request_max, new ThreadFactory() {

			private AtomicInteger thradIndex = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r,
						"asyClientInoker-remoteFuture-clean-thread-" + thradIndex.getAndIncrement());
				thread.setDaemon(true);
				return thread;
			}
		});
	}

	private final AtomicBoolean isExecuteAsyCallback = new AtomicBoolean(false);
	private AsyCallback asyCallback;

	public AsyClientInoker(String callHost, int callPort, long callTimeout, ClientTransport clientTransport,
			Request request,AsyCallback asyCallback) {
		super(callHost, callPort, callTimeout, clientTransport, request);
		this.asyCallback=asyCallback;
	}

	@Override
	protected RemoteResponse getResult(Request request) {
		scheduledExecutorService.schedule(() -> {
			ClientInoker clientInoker = removeSelfFromCache();
			clientInoker.onComplete(new RemoteResponse(RemoteResponseState.TIMEOUT));
		}, getCallTimeout() + 100, TimeUnit.MILLISECONDS);
		return null;
	}

	@Override
	public void onComplete(RemoteResponse response) {
		super.onComplete(response);
		if (isExecuteAsyCallback.compareAndSet(false, true)) {
			asyCallback.execute(response);
		}
	}
}