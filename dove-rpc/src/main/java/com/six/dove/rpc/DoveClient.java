package com.six.dove.rpc;

import com.six.dove.common.Service;
import com.six.dove.remote.AsyCallback;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:03:14 rpc服务调用 客户端
 */
public interface DoveClient extends Service {

	/**
	 * 
	 * <p>
	 * 基于服务注册器寻找 rpcService。
	 * </p>
	 * <p>
	 * 寻找到的rpcService,当实际执行rpcService方法时存在系统已经给出的异常定义
	 * </p>
	 * <p>
	 * 同步调用
	 * </p>
	 * 
	 * @param clz
	 *            提供rpc服务的class 必须有值 否则抛运行时异常
	 * @return
	 */
	<T> T lookupService(Class<?> clz);

	
	/**
	 * 
	 * <p>
	 * 基于服务注册器寻找 rpcService。
	 * </p>
	 * <p>
	 * 寻找到的rpcService,当实际执行rpcService方法时存在系统已经给出的异常定义
	 * </p>
	 * <p>
	 * 同步调用
	 * </p>
	 * 
	 * @param clz
	 *            提供rpc服务的class 必须有值 否则抛运行时异常
	 * @param version
	 *            提供rpc服务的协议版本
	 * @return
	 */
	<T> T lookupService(Class<?> clz, String version,long callTimeout);


	/**
	 * 
	 * <p>
	 * 基于服务注册器寻找 rpcService。
	 * </p>
	 * <p>
	 * 寻找到的rpcService,当实际执行rpcService方法时存在系统已经给出的异常定义
	 * </p>
	 * <p>
	 * 异步调用
	 * </p>
	 * 
	 * @param clz
	 *            提供rpc服务的class 必须有值 否则抛运行时异常
	 * @param callback
	 *            调用rpc服务的回调方法，
	 * @return
	 */
	<T> T lookupService(Class<?> clz,AsyCallback callback);
	
	/**
	 * 
	 * <p>
	 * 基于服务注册器寻找 rpcService。
	 * </p>
	 * <p>
	 * 寻找到的rpcService,当实际执行rpcService方法时存在系统已经给出的异常定义
	 * </p>
	 * <p>
	 * 异步调用
	 * </p>
	 * 
	 * @param clz
	 *            提供rpc服务的class 必须有值 否则抛运行时异常
	 * @param version
	 *            提供rpc服务的协议版本
	 * @param callback
	 *            调用rpc服务的回调方法，
	 * @return
	 */
	<T> T lookupService(Class<?> clz, String version,long callTimeout, AsyCallback callback);

	/**
	 * 
	 * <p>
	 * 基于目标服务器host:ip寻找 rpcService。
	 * </p>
	 * <p>
	 * 寻找到的rpcService,当实际执行rpcService方法时存在系统已经给出的异常定义
	 * </p>
	 * <p>
	 * 同步调用
	 * </p>
	 * 
	 * @param targetHost
	 *            提供rpc服务的主机 必须有值 否则抛运行时异常
	 * @param targetPort
	 *            提供rpc服务的端口 必须有值 否则抛运行时异常
	 * @param clz
	 *            提供rpc服务的class 必须有值 否则抛运行时异常
	 * @return
	 */
	<T> T lookupService(String targetHost, int targetPort, Class<?> clz);

	/**
	 * 
	 * <p>
	 * 基于目标服务器host:ip寻找 rpcService。
	 * </p>
	 * <p>
	 * 寻找到的rpcService,当实际执行rpcService方法时存在系统已经给出的异常定义
	 * </p>
	 * <p>
	 * 同步调用
	 * </p>
	 * 
	 * @param targetHost
	 *            提供rpc服务的主机 必须有值 否则抛运行时异常
	 * @param targetPort
	 *            提供rpc服务的端口 必须有值 否则抛运行时异常
	 * @param clz
	 *            提供rpc服务的class 必须有值 否则抛运行时异常
	 * @param version
	 *            提供rpc服务的协议版本
	 * @return
	 */
	<T> T lookupService(String targetHost, int targetPort, Class<?> clz, String version,long callTimeout);

	/**
	 * 
	 * <p>
	 * 基于目标服务器host:ip寻找 rpcService。
	 * </p>
	 * <p>
	 * 寻找到的rpcService,当实际执行rpcService方法时存在系统已经给出的异常定义
	 * </p>
	 * <p>
	 * 异步调用
	 * </p>
	 * 
	 * @param targetHost
	 *            提供rpc服务的主机 必须有值 否则抛运行时异常
	 * @param targetPort
	 *            提供rpc服务的端口 必须有值 否则抛运行时异常
	 * @param clz
	 *            提供rpc服务的class 必须有值 否则抛运行时异常
	 * @param callback
	 *            调用rpc服务的回调方法，
	 * @return
	 */
	<T> T lookupService(String targetHost, int targetPort, Class<?> clz,AsyCallback callback);
	
	/**
	 * 
	 * <p>
	 * 基于目标服务器host:ip寻找 rpcService。
	 * </p>
	 * <p>
	 * 寻找到的rpcService,当实际执行rpcService方法时存在系统已经给出的异常定义
	 * </p>
	 * <p>
	 * 异步调用
	 * </p>
	 * 
	 * @param targetHost
	 *            提供rpc服务的主机 必须有值 否则抛运行时异常
	 * @param targetPort
	 *            提供rpc服务的端口 必须有值 否则抛运行时异常
	 * @param clz
	 *            提供rpc服务的class 必须有值 否则抛运行时异常
	 * @param version
	 *            提供rpc服务的协议版本
	 * @param callback
	 *            调用rpc服务的回调方法，
	 * @return
	 */
	<T> T lookupService(String targetHost, int targetPort, Class<?> clz, String version,long callTimeout, AsyCallback callback);

}
