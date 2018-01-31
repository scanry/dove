package six.com.remote.server;

import six.com.remote.RpcConnection;
import six.com.rpc.protocol.RpcResponse;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 接入到服务端的连接
 */
public interface ServerRpcConnection extends RpcConnection<RpcResponse, Void> {

}
