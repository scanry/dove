package com.six.dove.remote;

import java.lang.reflect.Method;

import com.six.dove.common.Service;
import com.six.dove.common.utils.ClassUtils;
import com.six.dove.remote.compiler.Compiler;
import com.six.dove.remote.connection.RemoteConnection;
import com.six.dove.remote.protocol.RemoteMsg;
import com.six.dove.remote.protocol.RemoteSerialize;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月10日 上午11:17:33 远程调用 接口
 * @describe 远程调用端
 *           execute方法的参数R_S,execute方法返回R_R，远程调用端连接发送方法的参数C_S,远程调用端连接发送方法返回C_R
 */
public interface Remote<R_S, R_R, C_S extends RemoteMsg, C_R, C extends RemoteConnection<C_S, C_R>> extends Service {

	/**
	 * 默认远程调用协议版本
	 */
	final String REMOTE_SERVICE_VERSION="1.0.0";
	
	/**
	 * 默认远程调用超时时间
	 */
	final long DEFAULT_CALL_TIMEOUT= 6000;
	/**
	 * 构建远程调用服务网服务命名
	 * 
	 * @param fullClassName
	 *            协议class全名
	 * @param protocolMethod
	 *            协议方法
	 * @param version
	 *            协议版本号
	 * @return
	 */
	public static ServiceName newServiceName(String fullClassName, Method protocolMethod, String version) {
		return ServiceName.newServiceName(fullClassName, protocolMethod.getName(),
				ClassUtils.parmaTypes(protocolMethod), version);
	}
	/**
	 * 执行
	 * 
	 * @param msg
	 * @return
	 */
	R_R execute(R_S msg);

	/**
	 * 获取一个编译器
	 * 
	 * @return
	 */
	Compiler getCompiler();

	/**
	 * 获取远程调用端序列化器
	 * 
	 * @return
	 */
	RemoteSerialize getRemoteSerialize();

	/**
	 * 获取接入的连接
	 * 
	 * @param id
	 *            host:port。例如: 127.0.0.1:8080
	 * @return 返回已经接入的连接，有可能为Null
	 */
	C getConnection(String id);

	/**
	 * 添加 接入到的连接
	 * 
	 * @param id
	 *            host:port。例如: 127.0.0.1:8080
	 * @return
	 */
	void addConnection(C connection);

	/**
	 * 移除 接入到的连接
	 * 
	 * @param id
	 *            host:port。例如: 127.0.0.1:8080
	 * @return 返回已经接入到的连接，有可能为Null
	 */
	C removeConnection(String id);

}
