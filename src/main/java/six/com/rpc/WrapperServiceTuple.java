package six.com.rpc;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * @author sixliu
 * @date 2017年12月28日
 * @email 359852326@qq.com
 * @Description
 */
public class WrapperServiceTuple {

	private WrapperService service;
	private ExecutorService executorService;

	public WrapperServiceTuple(WrapperService service, ExecutorService executorService) {
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
}
