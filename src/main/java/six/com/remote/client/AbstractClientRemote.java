package six.com.remote.client;

import six.com.remote.AbstractRemote;
import six.com.remote.ConnectionPool;
import six.com.remote.RpcConnection;
import six.com.rpc.Compiler;
import six.com.rpc.client.WrapperFuture;
import six.com.rpc.exception.RpcException;
import six.com.rpc.exception.RpcInvokeException;
import six.com.rpc.exception.RpcNotFoundServiceException;
import six.com.rpc.exception.RpcRejectServiceException;
import six.com.rpc.exception.RpcTimeoutException;
import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.protocol.RpcResponse;
import six.com.rpc.protocol.RpcResponseStatus;
import six.com.rpc.protocol.RpcSerialize;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe
 */
public abstract class AbstractClientRemote extends AbstractRemote<RpcRequest, RpcResponse>
		implements ClientRemote {

	/**
	 * 链接池
	 */
	private ConnectionPool<ClientRpcConnection> pool;
	// 请求超时时间 6秒
	private long callTimeout = 6000;

	public AbstractClientRemote(Compiler compiler, RpcSerialize rpcSerialize, long callTimeout) {
		super(compiler, rpcSerialize);
		this.callTimeout = callTimeout;
		this.pool = new ConnectionPool<>();
	}

	@Override
	public RpcResponse execute(RpcRequest rpcRequest) {
		WrapperFuture wrapperFuture = null;
		ClientRpcConnection clientToServerConnection = null;
		try {
			clientToServerConnection = findHealthyRpcConnection(rpcRequest);
		} catch (Exception e) {
			if (null != rpcRequest.getAsyCallback()) {
				rpcRequest.getAsyCallback().execute(RpcResponse.CONNECT_FAILED);
				return RpcResponse.CONNECT_FAILED;
			} else {
				throw new RpcException(e);
			}
		}
		try {
			wrapperFuture = clientToServerConnection.send(rpcRequest);
		} catch (Exception e) {
			clientToServerConnection.removeWrapperFuture(rpcRequest.getId());
			throw new RpcException(e);
		}
		if (!wrapperFuture.hasAsyCallback()) {
			RpcResponse rpcResponse = wrapperFuture.getResult(getCallTimeout());
			if (null == rpcResponse) {
				clientToServerConnection.removeWrapperFuture(rpcRequest.getId());
				throw new RpcTimeoutException(
						"execute rpcRequest[" + rpcRequest.toString() + "] timeout[" + getCallTimeout() + "]");
			} else if (rpcResponse.getStatus() == RpcResponseStatus.UNFOUND_SERVICE) {
				throw new RpcNotFoundServiceException(rpcResponse.getMsg());
			} else if (rpcResponse.getStatus() == RpcResponseStatus.REJECT) {
				throw new RpcRejectServiceException(rpcResponse.getMsg());
			} else if (rpcResponse.getStatus() == RpcResponseStatus.INVOKE_ERR) {
				throw new RpcInvokeException(rpcResponse.getMsg());
			} else {
				return rpcResponse;
			}
		} else {
			return null;
		}
	}

	private ClientRpcConnection findHealthyRpcConnection(RpcRequest rpcRequest) {
		String callHost = rpcRequest.getCallHost();
		int callPort = rpcRequest.getCallPort();
		String id = RpcConnection.newConnectionId(callHost, callPort);
		ClientRpcConnection clientToServerConnection = pool.find(id);
		if (null == clientToServerConnection) {
			synchronized (pool) {
				clientToServerConnection = pool.find(id);
				if (null == clientToServerConnection) {
					clientToServerConnection = newRpcConnection(callHost, callPort);
					pool.put(clientToServerConnection);
				}
			}
		}
		if (null != clientToServerConnection) {
			long startTime = System.currentTimeMillis();
			// 判断是否可用，如果不可用等待可用直到超时
			while (!clientToServerConnection.available()) {
				long spendTime = System.currentTimeMillis() - startTime;
				if (spendTime > getCallTimeout()) {
					try {
						clientToServerConnection.close();
					} catch (Exception e) {
					}
					throw new RpcTimeoutException("connected " + callHost + ":" + callPort + " timeout:" + spendTime);
				}
			}
		}
		return clientToServerConnection;
	}

	protected abstract ClientRpcConnection newRpcConnection(String callHost, int callPort);

	/**
	 * 从缓存中移除链接
	 * 
	 * @param connection
	 */
	public void removeConnection(AbstractClientRpcConnection connection) {
		pool.remove(connection);
	}

	public long getCallTimeout() {
		return callTimeout;
	}
}
