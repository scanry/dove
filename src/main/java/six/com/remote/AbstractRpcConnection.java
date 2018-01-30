package six.com.remote;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe
 */
public abstract class AbstractRpcConnection<T, R> implements RpcConnection<T, R> {

	private String id;
	private String host;
	private int port;
	private long lastActivityTime;

	protected AbstractRpcConnection(String host, int port) {
		RpcConnection.checkAddress(host, port);
		this.id = RpcConnection.newConnectionId(host, port);
		this.host = host;
		this.port = port;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public long getLastActivityTime() {
		return lastActivityTime;
	}

	protected void updateLastActivityTime() {
		this.lastActivityTime = System.currentTimeMillis();
	}

	public String toString() {
		return host + ":" + port;
	}
}
