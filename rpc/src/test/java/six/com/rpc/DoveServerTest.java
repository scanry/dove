package six.com.rpc;

import com.six.dove.rpc.server.DoveServerImpl;

/**
 * @author liusong
 * @date 2017年8月10日
 * @email 359852326@qq.com
 */
public class DoveServerTest {

	public static void main(String[] args) throws InterruptedException {
		DoveServerImpl server = new DoveServerImpl("127.0.0.1", 80);
		server.start();
		server.register(new TestServiceImpl());
		DoveServerTest wait = new DoveServerTest();
		synchronized (wait) {
			wait.wait();
		}
	}

}
