package six.com.rpc.server;

import six.com.rpc.AbstractRemote;
import six.com.rpc.RemoteInvokeProxyFactory;
import six.com.rpc.protocol.RpcSerialize;

/**
 * @author sixliu
 * @date 2017年12月29日
 * @email 359852326@qq.com
 * @Description
 */
public abstract class AbstractServer extends AbstractRemote implements RpcServer {

	public AbstractServer(RemoteInvokeProxyFactory wrapperServiceProxyFactory, RpcSerialize rpcSerialize) {
		super(wrapperServiceProxyFactory, rpcSerialize);
	}
}
