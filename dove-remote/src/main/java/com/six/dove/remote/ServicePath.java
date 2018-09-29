package com.six.dove.remote;

import java.io.Serializable;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月10日 下午6:33:53 rpc 服务路径
 */
public class ServicePath implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3140010579417461673L;

	/**
	 * 服务主机
	 */
	private String host;

	/**
	 * 服务端口
	 */
	private int port;

	private transient ServiceNameUtils serviceName;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ServiceNameUtils getServiceName() {
		return serviceName;
	}

	public void setServiceName(ServiceNameUtils serviceName) {
		this.serviceName = serviceName;
	}
}
