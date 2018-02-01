package six.com.rpc;

import com.six.dove.rpc.server.DoveServer;

/**   
* @author liusong  
* @date   2017年8月10日 
* @email  359852326@qq.com 
*/
public class DoveServerTest {

	public static void main(String[] args) throws InterruptedException {
		DoveServer server=new DoveServer("127.0.0.1", 80);
		server.start();
		server.register(TestService.class, new TestServiceImpl());
		DoveServerTest wait=new DoveServerTest();
		synchronized (wait) {
			wait.wait();
		}
	}

}
