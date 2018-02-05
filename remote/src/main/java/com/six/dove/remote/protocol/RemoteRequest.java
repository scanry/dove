package com.six.dove.remote.protocol;

import java.io.Serializable;

import com.six.dove.remote.AsyCallback;
import com.six.dove.remote.ServiceName;
import com.six.dove.remote.server.ServerRemoteConnection;

/**
 * @author six
 * @date 2016年6月2日 下午4:14:39 rpc 请求
 */
public class RemoteRequest extends RemoteMsg implements Serializable {

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

	private transient AsyCallback asyCallback;

	private transient ServerRemoteConnection serverRpcConnection;

	public RemoteRequest() {
		super(RemoteProtocol.REQUEST);
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

	public AsyCallback getAsyCallback() {
		return asyCallback;
	}

	public void setAsyCallback(AsyCallback asyCallback) {
		this.asyCallback = asyCallback;
	}
	
	public boolean isAsy() {
		return null!=asyCallback;
	}

	public ServerRemoteConnection getServerRpcConnection() {
		return serverRpcConnection;
	}

	public void setServerRpcConnection(ServerRemoteConnection serverRpcConnection) {
		this.serverRpcConnection = serverRpcConnection;
	}

	public String toString() {
		return "@" + callHost + ":" + callPort + "/" + serviceName.toString() + "/" + getId();
	}
}
