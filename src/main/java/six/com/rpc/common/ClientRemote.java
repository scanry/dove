package six.com.rpc.common;

import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.protocol.RpcResponse;

/**
*@author:MG01867
*@date:2018年1月29日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public interface ClientRemote extends Remote{

	/**
	 * 执行请求
	 * 
	 * @param RPCRequest
	 * @return
	 */
	public RpcResponse execute(RpcRequest RPCRequest);
}
