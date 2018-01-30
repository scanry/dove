package six.com.rpc.protocol;

import java.io.Serializable;
import java.util.Map;

import six.com.remote.server.ServerRpcConnection;
import six.com.rpc.AsyCallback;
import six.com.rpc.ServiceName;

/**
 * @author six
 * @date 2016年6月2日 下午4:14:39 rpc 请求
 */
public class RpcRequest extends RpcMsg implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1071881684426113946L;

	// 呼叫host
	private String callHost;
	// 呼叫host端口
	private int callPort;
	// 呼叫命令
	private ServiceName serviceName;
	// 呼叫参数
	private Object[] params;

	private Map<String, Object> paramsMap;

	private transient AsyCallback asyCallback;

	private transient ServerRpcConnection serverRpcConnection;

	public RpcRequest() {
		super(RpcProtocol.REQUEST);
	}

	public String getCallHost() {
		return callHost;
	}

	public void setCallHost(String callHost) {
		this.callHost = callHost;
	}

	public int getCallPort() {
		return callPort;
	}

	public void setCallPort(int callPort) {
		this.callPort = callPort;
	}

	public ServiceName getServiceName() {
		return serviceName;
	}

	public void setServiceName(ServiceName serviceName) {
		this.serviceName = serviceName;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public Map<String, Object> getParamsMap() {
		return paramsMap;
	}

	public void setParamsMap(Map<String, Object> paramsMap) {
		this.paramsMap = paramsMap;
	}

	public AsyCallback getAsyCallback() {
		return asyCallback;
	}

	public void setAsyCallback(AsyCallback asyCallback) {
		this.asyCallback = asyCallback;
	}

	public ServerRpcConnection getServerRpcConnection() {
		return serverRpcConnection;
	}

	public void setServerRpcConnection(ServerRpcConnection serverRpcConnection) {
		this.serverRpcConnection = serverRpcConnection;
	}

	public String toString() {
		return "@" + callHost + ":" + callPort + "/" + serviceName.toString() + "/" + getId();
	}
}
