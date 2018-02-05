package six.com.rpc;

import com.six.dove.remote.Remote;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.rpc.annotation.DoveService;
import com.six.dove.rpc.server.DoveServerImpl;

/**
 * @author liusong
 * @date 2017年8月10日
 * @email 359852326@qq.com
 */
public class DoveServerTest {

	@DoveService(protocol = TestService.class,version=Remote.REMOTE_SERVICE_VERSION,callTimeout=6000,callback="beanId")
	private TestService testService;
	
	public class TestServiceCallBack{

		public void say(String name,RemoteResponse response) {
			if(response.isSuccessed()) {
				
			}
		}
		
	}
	
	
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
