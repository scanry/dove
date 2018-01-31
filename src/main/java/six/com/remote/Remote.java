package six.com.remote;

import six.com.rpc.Compiler;
import six.com.rpc.protocol.RpcSerialize;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月10日 上午11:17:33 远程调用 接口
 * @describe 远程调用端
 *           execute方法的参数R_S,execute方法返回R_R，远程调用端连接发送方法的参数C_S,远程调用端连接发送方法返回C_R
 */
public interface Remote<R_S, R_R, C_S, C_R, C extends RpcConnection<C_S, C_R>> {

	/**
	 * 启动远程调用端
	 */
	void start();

	/**
	 * 关闭远程调用端
	 */
	void shutdown();

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
	RpcSerialize getRpcSerialize();

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
