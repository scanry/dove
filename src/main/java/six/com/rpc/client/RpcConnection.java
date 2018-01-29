package six.com.rpc.client;

import six.com.rpc.protocol.RpcRequest;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public interface RpcConnection extends AutoCloseable{

	String getKey();
	
	boolean available();
	
	long getLastActivityTime();
	
	void putWrapperFuture(String rpcRequestId, WrapperFuture wrapperFuture);

	WrapperFuture removeWrapperFuture(String rpcRequestId);

	WrapperFuture send(RpcRequest rpcRequest);
}
