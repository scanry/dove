package six.com.rpc.common;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

import six.com.rpc.ServiceHook;

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
		this.service = service;
		this.executorService = executorService;
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
