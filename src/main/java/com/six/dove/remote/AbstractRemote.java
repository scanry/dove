package com.six.dove.remote;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.common.AbstractService;
import com.six.dove.remote.client.ClientRemoteConnection;
import com.six.dove.remote.compiler.Compiler;
import com.six.dove.remote.protocol.RemoteSerialize;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月10日 上午11:20:06
 * @describe 抽象远程调用端类，
 */
public abstract class AbstractRemote<R_S, R_R, C_S, C_R, C extends RemoteConnection<C_S, C_R>> extends AbstractService
		implements Remote<R_S, R_R, C_S, C_R, C> {

	final static Logger log = LoggerFactory.getLogger(AbstractRemote.class);

	private static String MAC;
	private static String PID;
	public static final int DEFAULT_SERVICE_VERSION = 1;
	private Map<String, C> connectionPool = new ConcurrentHashMap<>();
	/**
	 * 链接池
	 */
	ConnectionPool<ClientRemoteConnection> pool;

	static {
		MAC = getLocalMac();
		PID = getPid();
	}

	private static AtomicInteger requestIndex = new AtomicInteger(0);
	private RemoteSerialize remoteSerialize;
	private Compiler compiler;

	public AbstractRemote(String name, Compiler compiler, RemoteSerialize remoteSerialize) {
		super(name);
		Objects.requireNonNull(compiler);
		Objects.requireNonNull(remoteSerialize);
		this.compiler = compiler;
		this.remoteSerialize = remoteSerialize;
	}

	@Override
	public final Compiler getCompiler() {
		return compiler;
	}

	@Override
	public final RemoteSerialize getRemoteSerialize() {
		return remoteSerialize;
	}

	@Override
	public final C getConnection(String id) {
		return connectionPool.get(id);
	}

	@Override
	public final void addConnection(C connection) {
		connectionPool.put(connection.getId(), connection);
	}

	@Override
	public final C removeConnection(String id) {
		return connectionPool.remove(id);
	}

	private void closeExpire(long expireTime) {
		Iterator<Map.Entry<String, C>> mapIterator = connectionPool.entrySet().iterator();
		long now = System.currentTimeMillis();
		while (mapIterator.hasNext()) {
			Map.Entry<String, C> entry = mapIterator.next();
			C connection = entry.getValue();
			if (now - connection.getLastActivityTime() >= expireTime) {
				close(connection);
				mapIterator.remove();
			}
		}
	}

	public static void close(RemoteConnection<?, ?> connection) {
		if (null != connection) {
			try {
				connection.close();
			} catch (Exception e) {
				log.error("close RpcConnection[" + connection.getId() + "] exception", e);
			}
		}
	}

	@Override
	protected final void doStop() {
		closeExpire(0);
		doStop();
	}

	protected abstract void shutdown();

	protected static void checkParma(String targetHost, int targetPort, Class<?> clz) {
		RemoteConnection.checkAddress(targetHost, targetPort);
		if (!clz.isInterface()) {
			throw new IllegalArgumentException("this clz[" + clz.getName() + "] is not tnterface");
		}
	}

	protected abstract String generateProtocolProxyClassName(Class<?> protocol, Method instanceMethod);

	/**
	 * 
	 * @param targetHost
	 * @param targetPort
	 * @param serviceName
	 * @return
	 */
	public final String createRequestId(String targetHost, int targetPort, String serviceName) {
		long threadId = Thread.currentThread().getId();
		StringBuilder requestId = new StringBuilder();
		requestId.append(MAC).append("/");
		requestId.append(PID).append("/");
		requestId.append(threadId).append("@");
		requestId.append(targetHost).append(":");
		requestId.append(targetPort).append("/");
		requestId.append(serviceName).append("/");
		requestId.append(System.currentTimeMillis()).append("/");
		requestId.append(requestIndex.incrementAndGet());
		return requestId.toString();
	}

	public static final String getServiceName(String protocolClassName, Method serviceMethod) {
		Parameter[] parameter = serviceMethod.getParameters();
		StringBuilder serviceName = new StringBuilder();
		serviceName.append(protocolClassName).append("_");
		serviceName.append(serviceMethod.getName()).append("_");
		if (null != parameter) {
			String parameterTypeName = null;
			for (int i = 0, size = parameter.length; i < size; i++) {
				parameterTypeName = parameter[i].getParameterizedType().getTypeName();
				parameterTypeName = parameterTypeName.replace(".", "_");
				serviceName.append(parameterTypeName).append("_");
			}
		}
		return serviceName.toString();
	}

	private static String getLocalMac() {
		String mac = "";
		try {
			InetAddress ia = InetAddress.getLocalHost();
			byte[] macBytes = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
			StringBuffer sb = new StringBuffer("");
			for (int i = 0; i < macBytes.length; i++) {
				int temp = macBytes[i] & 0xff;
				String str = Integer.toHexString(temp);
				if (str.length() == 1) {
					sb.append("0" + str);
				} else {
					sb.append(str);
				}
			}
			mac = sb.toString().toUpperCase();
		} catch (Exception e) {
		}
		return mac;
	}

	private static String getPid() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		String pid = name.split("@")[0];
		return pid;
	}

}
