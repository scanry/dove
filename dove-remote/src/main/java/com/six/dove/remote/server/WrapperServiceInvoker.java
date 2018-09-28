package com.six.dove.remote.server;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.common.utils.ExceptionUtils;
import com.six.dove.remote.RemoteInvoker;
import com.six.dove.remote.ServiceHook;
import com.six.dove.remote.connection.RemoteConnection.SendListener;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.remote.protocol.RemoteResponseState;

/**
 * @author sixliu
 * @date 2017年12月28日
 * @email 359852326@qq.com
 * @Description
 */
public class WrapperServiceInvoker implements RemoteInvoker<Void> {

	final static Logger log = LoggerFactory.getLogger(WrapperServiceInvoker.class);
	private static SendListener sendListener = (result, exception) -> {
	};
	private WrapperService service;
	private ExecutorService executorService;
	private ServiceHook hook;

	public WrapperServiceInvoker(WrapperService service, ExecutorService executorService, ServiceHook hook) {
		Objects.requireNonNull(service);
		Objects.requireNonNull(executorService);
		Objects.requireNonNull(hook);
		this.service = service;
		this.executorService = executorService;
		this.hook = hook;
	}

	public WrapperService getWrapperService() {
		return service;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public ServiceHook getHook() {
		return hook;
	}

	public Void invoke(RemoteRequest rpcRequest) {
		RemoteResponse rpcResponse = rpcRequest.getResponse();
		try {
			executorService.submit(() -> {
				Object[] params = rpcRequest.getParams();
				try {
					hook.beforeHook(params);
					Object result = service.invoke(params);
					hook.afterHook(params);
					rpcResponse.setStatus(RemoteResponseState.SUCCEED);
					rpcResponse.setResult(result);
				} catch (Exception e) {
					hook.exceptionHook(params);
					String errMsg = ExceptionUtils.getExceptionMsg(e);
					rpcResponse.setStatus(RemoteResponseState.INVOKE_ERR);
					rpcResponse.setMsg(errMsg);
					log.error(String.format("invoke request[{}] err", rpcRequest.getServerRpcConnection().toString()),
							e);
				}
				rpcRequest.getServerRpcConnection().send(rpcResponse, sendListener);
			});
		} catch (RejectedExecutionException e) {
			// 业务处理线程池满了，拒绝异常
			rpcResponse.setStatus(RemoteResponseState.REJECT);
			String msg = String.format("the service is too busy and reject rpcRequest[{}]:{}",
					rpcRequest.getServerRpcConnection().toString(), rpcRequest.getServiceName());
			rpcResponse.setMsg(msg);
			log.error(msg);
			rpcRequest.getServerRpcConnection().send(rpcResponse, sendListener);
		}
		return null;
	}
}
