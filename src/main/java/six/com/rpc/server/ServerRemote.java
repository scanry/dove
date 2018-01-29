package six.com.rpc.server;

import java.util.concurrent.ExecutorService;

import six.com.rpc.common.Remote;
import six.com.rpc.common.WrapperServiceTuple;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public interface ServerRemote extends Remote {

	/**
	 * 通过rpcServiceName 获取 RpcService
	 * 
	 * @param rpcServiceName
	 * @return
	 */
	WrapperServiceTuple getWrapperServiceTuple(String rpcServiceName);

	/**
	 * 获取执行业务方法的线程池
	 * 
	 * @return
	 */
	ExecutorService getDefaultBizExecutorService();
}
