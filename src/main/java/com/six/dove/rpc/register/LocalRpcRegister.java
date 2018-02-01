package com.six.dove.rpc.register;

import java.util.List;

import com.six.dove.remote.ServiceName;
import com.six.dove.remote.ServicePath;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月10日 下午6:23:23
 */
public class LocalRpcRegister implements RpcRegister {

	@Override
	public void deploy(ServiceName serviceName, ServicePath servicePath) {

	}

	@Override
	public List<ServicePath> list(ServiceName serviceName) {
		return null;
	}

	@Override
	public void undeploy(ServiceName serviceName) {

	}

}
