package six.com.remote.client;

import six.com.remote.Remote;
import six.com.rpc.client.WrapperFuture;
import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.protocol.RpcResponse;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe
 */
public interface ClientRemote extends Remote<RpcRequest, RpcResponse, RpcRequest, WrapperFuture, ClientRpcConnection> {

	/**
	 * 远程调用客户端调用服务端超时时间
	 * 
	 * @return
	 */
	long getCallTimeout();
}
