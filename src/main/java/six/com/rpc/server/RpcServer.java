package six.com.rpc.server;

import six.com.rpc.WrapperService;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:03:05 rpc Server
 */
public interface RpcServer {

	/**
	 * <p>
	 * 基于RpcService注解(@RpcService)注册
	 * </p>
	 * <p>
	 * 只需要在类方法上 使用 @RpcService(name="RpcService") 即可
	 * </p>
	 * <p>
	 * 如果类里没有方法使用@RpcService的话那么不会注册
	 * </p>
	 * 
	 * @param tagetOb
	 *            必须有值 否则抛运行时异常
	 */
	public void register(Class<?> protocolClass,Object tagetOb);

	/**
	 * 通过rpcServiceName 获取 RpcService
	 * 
	 * @param rpcServiceName
	 * @return
	 */
	public WrapperService get(String rpcServiceName);

	/**
	 * 通过rpcServiceName 移除指定 RpcService
	 * 
	 * @param rpcServiceName
	 */
	public void remove(String rpcServiceName);
	
	public void shutdown();
}
