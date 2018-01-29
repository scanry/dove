package six.com.rpc.client;

import six.com.rpc.protocol.RpcRequest;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public interface RpcConnection extends AutoCloseable {

	/**
	 * 链接key
	 * 
	 * @param host
	 *            目标主机
	 * @param port
	 *            目标端口
	 * @return
	 */
	public static String newConnectionKey(String host, int port) {
		String findKey = host + ":" + port;
		return findKey;
	}

	String getId();
	
	String getHost();
	
	int getPort();

	boolean available();

	long getLastActivityTime();

	void putWrapperFuture(String rpcRequestId, WrapperFuture wrapperFuture);

	WrapperFuture removeWrapperFuture(String rpcRequestId);

	WrapperFuture send(RpcRequest rpcRequest);
}
