package six.com.rpc;

import six.com.rpc.protocol.RpcSerialize;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月10日 上午11:17:33 远程调用 接口
 */
public interface Remote {

	RemoteInvokeProxyFactory getRemoteInvokeProxyFactory();

	RpcSerialize getRpcSerialize();
}
