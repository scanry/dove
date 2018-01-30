package six.com.rpc.register;

import six.com.rpc.ServicePath;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月10日 下午6:12:56
 * 
 *       rpc服务注册器
 */
public interface RpcRegister {

	/**
	 * 注册 服务
	 * 
	 * @param servicePath
	 */
	public void register(ServicePath servicePath);

	/**
	 * 取消注册服务
	 * 
	 * @param servicePath
	 */
	public void unRegister(ServicePath servicePath);

}
