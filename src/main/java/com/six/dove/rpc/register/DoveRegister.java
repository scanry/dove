package com.six.dove.rpc.register;

import java.util.List;

import com.six.dove.common.Service;
import com.six.dove.remote.ServiceName;
import com.six.dove.remote.ServicePath;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月10日 下午6:12:56
 * 
 *       rpc服务注册器
 */
public interface DoveRegister extends Service {

	/**
	 * 发布服务
	 * 
	 * @param servicePath
	 */
	void deploy(ServiceName serviceName, ServicePath servicePath);

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
