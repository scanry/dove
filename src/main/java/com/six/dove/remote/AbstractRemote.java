package com.six.dove.remote;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.common.AbstractService;
import com.six.dove.remote.compiler.Compiler;
import com.six.dove.remote.connection.RemoteConnection;
import com.six.dove.remote.protocol.RemoteMsg;
import com.six.dove.remote.protocol.RemoteSerialize;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月10日 上午11:20:06
 * @describe 抽象远程调用端类，
 */
public abstract class AbstractRemote<R_S, R_R, C_S extends RemoteMsg, C_R, C extends RemoteConnection<C_S, C_R>>
		extends AbstractService implements Remote<R_S, R_R, C_S, C_R, C> {

	final static Logger log = LoggerFactory.getLogger(AbstractRemote.class);

	public static final int DEFAULT_SERVICE_VERSION = 1;
	private Map<String, C> connectionPool = new ConcurrentHashMap<>();
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
		stop1();
	}

	protected abstract void stop1();

	protected abstract String generateProtocolProxyClassName(Class<?> protocol, Method instanceMethod);

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

}
