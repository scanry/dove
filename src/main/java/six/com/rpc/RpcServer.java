package six.com.rpc;

import java.util.concurrent.ExecutorService;

import six.com.remote.Remote;
import six.com.rpc.protocol.RpcRequest;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:03:05 rpc Server 服务接口
 */
public interface RpcServer extends Remote<RpcRequest,Void> {

	/**
	 * 注册服务,将使用默认线程池调用服务
	 * 
	 * @param protocol
	 *            服务协议接口
	 * @param instance
	 *            服务协议实现实例
	 */
	<T, I extends T> void register(Class<T> protocol, I instance);

	/**
	 * 注册服务,将使用默认线程池调用服务
	 * 
	 * @param protocol
	 *            服务协议接口
	 * @param instance
	 *            服务协议实现实例
	 * @param hook
	 *            服务调用hook
	 */
	<T, I extends T> void register(Class<T> protocol, I instance, ServiceHook hook);

	/**
	 * 注册服务,将使用给定的线程池调用服务
	 * 
	 * @param bizExecutorService
	 *            给定的线程池
	 * @param protocol
	 *            服务协议接口
	 * @param instance
	 *            服务协议实现实例
	 */
	<T, I extends T> void register(ExecutorService bizExecutorService, Class<T> protocol, I instance);

	/**
	 * 注册服务,将使用给定的线程池调用服务
	 * 
	 * @param bizExecutorService
	 *            给定的线程池
	 * @param protocol
	 *            服务协议接口
	 * @param instance
	 *            服务协议实现实例
	 * @param hook
	 *            服务调用hook
	 */
	<T, I extends T> void register(ExecutorService bizExecutorService, Class<T> protocol, I instance, ServiceHook hook);

	/**
	 * 取消服务注册
	 * 
	 * @param protocol
	 */
	void unregister(Class<?> protocol);
}
