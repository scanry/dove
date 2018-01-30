package six.com.remote.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.remote.AbstractRpcConnection;
import six.com.rpc.client.AbstractClient;
import six.com.rpc.client.WrapperFuture;
import six.com.rpc.protocol.RpcRequest;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午11:05:56
 */

public abstract class AbstractClientRpcConnection extends AbstractRpcConnection<RpcRequest, WrapperFuture>
		implements ClientRpcConnection {

	final static Logger log = LoggerFactory.getLogger(AbstractClientRpcConnection.class);

	private AbstractClient rpcClient;
	private Map<String, WrapperFuture> requestMap;

	protected AbstractClientRpcConnection(AbstractClient rpcClient,String host, int port) {
		super(host, port);
		this.rpcClient = rpcClient;
		this.requestMap = new ConcurrentHashMap<>();
	}

	@Override
	public void putWrapperFuture(String rpcRequestId, WrapperFuture wrapperFuture) {
		requestMap.put(rpcRequestId, wrapperFuture);
	}

	@Override
	public WrapperFuture removeWrapperFuture(String rpcRequestId) {
		return requestMap.remove(rpcRequestId);
	}

	protected AbstractClient getAbstractClient() {
		return rpcClient;
	}
}
