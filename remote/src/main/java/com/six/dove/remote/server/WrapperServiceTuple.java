package com.six.dove.remote.server;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

import com.six.dove.remote.ServiceHook;

/**
 * @author sixliu
 * @date 2017年12月28日
 * @email 359852326@qq.com
 * @Description
 */
public class WrapperServiceTuple {

	private WrapperService service;
	private ExecutorService executorService;
	private ServiceHook hook;

	public WrapperServiceTuple(WrapperService service, ExecutorService executorService,ServiceHook hook) {
		Objects.requireNonNull(service);
		Objects.requireNonNull(executorService);
		Objects.requireNonNull(hook);
		this.service = service;
		this.executorService = executorService;
		this.hook=hook;
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
}
