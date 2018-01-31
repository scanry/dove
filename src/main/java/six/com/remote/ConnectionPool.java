package six.com.remote;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.remote.client.ClientRpcConnection;
import six.com.rpc.util.StringUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月21日 上午9:37:23
 */
public class ConnectionPool<T extends ClientRpcConnection> {

	final static Logger log = LoggerFactory.getLogger(ConnectionPool.class);
	private Map<String, T> connectionMap = new HashMap<>();

	public T find(String id) {
		T findNettyConnection = null;
		if (StringUtils.isNotBlank(id)) {
			findNettyConnection = connectionMap.get(id);
		}
		return findNettyConnection;
	}

	public void put(T nettyConnection) {
		if (null != nettyConnection) {
			T oldNettyConnection = find(nettyConnection.getId());
			close(oldNettyConnection);
			connectionMap.put(nettyConnection.getId(), nettyConnection);
		}
	}

	public void remove(String id) {
		if (null != id) {
			connectionMap.remove(id);
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
