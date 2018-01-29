package six.com.rpc.common;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月10日 下午6:33:53 rpc 服务路径
 */
public class ServicePath {

	/**
	 * 服务主机
	 */
	private String host;

	/**
	 * 服务端口
	 */
	private int port;

	/**
	 * 服务类名
	 */
	private String path;

	/**
	 * 服务方法
	 */
	private String method;

	/**
	 * 服务方法参数
	 */
	private String[] parmasName;

	/**
	 * 服务版本
	 */
	private int version;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String[] getParmasName() {
		return parmasName;
	}

	public void setParmasName(String[] parmasName) {
		this.parmasName = parmasName;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

}
