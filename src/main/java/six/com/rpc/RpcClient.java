package six.com.rpc;

import six.com.rpc.client.ClientToServerConnection;
import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.protocol.RpcResponse;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 上午10:03:14 rpc服务调用 客户端
 */
public interface RpcClient {


	/**
	 * 根据请求主机:端口+服务名称生成 requestId
	 * 
	 * @param targetHost
	 *            请求主机
	 * @param targetPort
	 *            请求端口
	 * @param serviceName
	 *            服务名称
	 * @return
	 */
	public String createRequestId(String targetHost, int targetPort, String serviceName);

	/**
	 * 同步执行请求
	 * 
	 * @param RPCRequest
	 * @return
	 */
	public RpcResponse synExecute(RpcRequest RPCRequest);

	/**
	 * 异步执行请求，请求完成时 ，执行回调callback
	 * 
	 * @param rpcRequest
	 * @param callback
	 */
	public void asyExecute(RpcRequest rpcRequest, AsyCallback callback);

	/**
	 * 
	 * <p>
	 * 基于目标服务器host:ip寻找 rpcService。
	 * </p>
	 * <p>
	 * 寻找到的rpcService,当实际执行rpcService方法时存在系统已经给出的异常定义
	 * </p>
	 * <p>
	 * 如果callback等于null那么 为同步调用，当callback 不等于null时 为异步调用
	 * </p>
	 * 
	 * @param targetHost
	 *            提供rpc服务的主机 必须有值 否则抛运行时异常
	 * @param targetPort
	 *            提供rpc服务的端口 必须有值 否则抛运行时异常
	 * @param clz
	 *            提供rpc服务的class 必须有值 否则抛运行时异常
	 * @param callback
	 *            调用rpc服务的回调方法，当callback 不等于null时 为异步调用
	 * @return
	 */
	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz, AsyCallback callback);

	public <T> T lookupService(String targetHost, int targetPort, Class<?> clz );
	
	/**
	 * 获取call 超时时间
	 * 
	 * @return
	 */
	public long getCallTimeout();

	/**
	 * 从缓存中移除链接
	 * 
	 * @param connection
	 */
	public void removeConnection(ClientToServerConnection connection);
	
	void shutdown();
}
