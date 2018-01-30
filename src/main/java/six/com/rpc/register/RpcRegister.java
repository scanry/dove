package six.com.rpc.register;

import java.util.List;

import six.com.rpc.ServiceName;
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
	 * 发布服务
	 * 
	 * @param servicePath
	 */
	void deploy(ServiceName serviceName,ServicePath servicePath);

	/**
	 * 获取指定服务的路径
	 * 
	 * @param serviceName
	 * @return
	 */
	List<ServicePath> list(ServiceName serviceName);

	/**
	 * 取消发布服务
	 * 
	 * @param servicePath
	 */
	void undeploy(ServiceName serviceName);

}
