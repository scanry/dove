package six.com.rpc.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.rpc.util.StringUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月21日 上午9:37:23
 */
public class ConnectionPool<T extends RpcConnection> {

	final static Logger log = LoggerFactory.getLogger(ConnectionPool.class);
	private Map<String, T> connectionMap = new HashMap<>();

	public T find(String connectionKey) {
		T findNettyConnection = null;
		if (StringUtils.isNotBlank(connectionKey)) {
			findNettyConnection = connectionMap.get(connectionKey);
		}
		return findNettyConnection;
	}

	public void put(T nettyConnection) {
		if (null != nettyConnection) {
			T oldNettyConnection = find(nettyConnection.getKey());
			close(oldNettyConnection);
			connectionMap.put(nettyConnection.getKey(), nettyConnection);
		}
	}

	public void remove(T nettyConnection) {
		if (null != nettyConnection) {
			connectionMap.remove(nettyConnection.getKey());
		}
	}


	private void closeExpire(long expireTime) {
		Iterator<Map.Entry<String, T>> mapIterator = connectionMap.entrySet().iterator();
		long now = System.currentTimeMillis();
		while (mapIterator.hasNext()) {
			Map.Entry<String, T> entry = mapIterator.next();
			T connection = entry.getValue();
			if (now - connection.getLastActivityTime() >= expireTime) {
				close(connection);
				mapIterator.remove();
			}
		}
	}

	public void destroy() {
		closeExpire(0);
	}
	
	public void close(T connection) {
		if (null != connection) {
			try {
				connection.close();
			} catch (Exception e) {
			}
		}
	}
}
