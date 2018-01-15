package six.com.rpc;

import java.lang.reflect.Method;

import six.com.rpc.client.AbstractClient;

/**
 * @author sixliu
 * @date 2017年12月28日
 * @email 359852326@qq.com
 * @Description
 */
public interface RemoteInvokeProxyFactory {

	@FunctionalInterface
	interface BuildCode {
		String buildCode();
	}
	/**
	 * 构建rpc服务端包装 service
	 * 
	 * @param instance
	 *            实际调用的实例
	 * @param protocolMethod
	 *            实例调用的协议方法
	 * @return 返回一个动态生成的包装实例
	 */
	WrapperService newServerWrapperService(Object instance, Method protocolMethod);

	/**
	 * 构建rpc客户端接口包装实例
	 * 
	 * @param clz
	 *            协议接口class
	 * @return 接口包装实例
	 */
	<T> T newClientInterfaceWrapperInstance(AbstractClient rpcClient,String targetHost, int targetPort,Class<?> clz,AsyCallback asyCallback);
}
