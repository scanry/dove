package com.six.dove.rpc;

import java.util.concurrent.ExecutorService;

import com.six.dove.common.Service;
import com.six.dove.remote.ServiceHook;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:03:05 rpc Server 服务接口
 */
public interface DoveServer extends Service {

	/**
	 * 注册服务,将使用默认线程池调用服务
	 * 
	 * @param instance
	 *            服务协议实现实例 DoveService注解
	 */
	void register(Object instance);

	/**
	 * 注册服务,将使用默认线程池调用服务
	 * 
	 * @param protocol
	 *            服务协议接口
	 * @param instance
	 *            服务协议实现实例
	 */
	void register(Class<?> protocol, Object instance, String version);

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
	void register(Class<?> protocol, Object instance, String version, ServiceHook hook);

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
	void register(ExecutorService bizExecutorService, Class<?> protocol, Object instance,String version);

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
	void register(ExecutorService bizExecutorService, Class<?> protocol, Object instance, String version,
			ServiceHook hook);

	/**
	 * 取消服务注册
	 * 
	 * @param protocol
	 */
	void unregister(Class<?> protocol);
}
