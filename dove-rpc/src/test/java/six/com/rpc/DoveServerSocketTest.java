package six.com.rpc;

import com.six.dove.remote.Remote;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.remote.server.socket.SocketServerRemote;
import com.six.dove.rpc.annotation.DoveService;
import com.six.dove.rpc.server.DoveServerImpl;

/**
 * @author:MG01867
 * @date:2018年2月7日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class DoveServerSocketTest {

	@DoveService(protocol = TestService.class, version = Remote.REMOTE_SERVICE_VERSION, callTimeout = 6000, callback = "beanId")
	private TestService testService;

	public class TestServiceCallBack {

		public void say(String name, RemoteResponse response) {
			if (response.isSuccessed()) {

			}
		}

	}

	public static void main(String[] args) throws InterruptedException {
		DoveServerImpl server = new DoveServerImpl(new SocketServerRemote("127.0.0.1", 80));
		server.start();
		server.register(new TestServiceImpl());
		DoveServerTest wait = new DoveServerTest();
		synchronized (wait) {
			wait.wait();
		}
	}

}
