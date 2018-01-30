package six.com.remote.server;

import java.util.function.Function;

import six.com.remote.Remote;
import six.com.rpc.ServiceName;
import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.server.WrapperServiceTuple;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe
 */
public interface ServerRemote extends Remote<RpcRequest, Void> {

	String getLocalHost();

	int getListenPort();

	ServerRpcConnection getServerRpcConnection(String id, Function<String, ServerRpcConnection> mappingFunction);

	WrapperServiceTuple getWrapperServiceTuple(ServiceName serviceName);

}
