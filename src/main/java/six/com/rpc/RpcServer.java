package six.com.rpc;

import java.util.concurrent.ExecutorService;

import six.com.rpc.common.Remote;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:03:05 rpc Server
 */
public interface RpcServer extends Remote{

	public <T, I extends T> void register(Class<T> protocol, I instance);
	
	public <T, I extends T> void register(Class<T> protocol, I instance, ServiceHook hook);

	public <T, I extends T> void register(ExecutorService bizExecutorService, Class<T> protocol, I instance);

	public <T, I extends T> void register(ExecutorService bizExecutorService, Class<T> protocol, I instance,
			ServiceHook hook);

	public void unregister(Class<?> protocol);
}
