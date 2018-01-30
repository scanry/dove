package six.com.remote.client;

import six.com.remote.RpcConnection;
import six.com.rpc.client.WrapperFuture;
import six.com.rpc.protocol.RpcRequest;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 客户端连接
 */
public interface ClientRpcConnection extends RpcConnection<RpcRequest, WrapperFuture> {

	void putWrapperFuture(String rpcRequestId, WrapperFuture wrapperFuture);

	WrapperFuture removeWrapperFuture(String rpcRequestId);

}
