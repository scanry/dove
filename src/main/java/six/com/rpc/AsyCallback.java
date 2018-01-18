package six.com.rpc;

import six.com.rpc.protocol.RpcResponse;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月7日 上午9:10:35
 * 
 *       rpc 服务异步调用 回调接口
 */
@FunctionalInterface
public interface AsyCallback {

	/**
	 * rpc 服务异步调用 回调方法
	 * 
	 * @param result
	 *            rpc调用返回结果
	 */
	public void execute(RpcResponse response);
}
