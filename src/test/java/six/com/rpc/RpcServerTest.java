package six.com.rpc;

import six.com.rpc.server.NettyRpcServer;
import six.com.rpc.server.RpcServer;

/**   
* @author liusong  
* @date   2017年8月10日 
* @email  359852326@qq.com 
*/
public class RpcServerTest {

	public static void main(String[] args) throws InterruptedException {
		RpcServer server=new NettyRpcServer("127.0.0.1", 8180);
		server.register(TestService.class, new TestServiceImpl());
		RpcServerTest wait=new RpcServerTest();
		synchronized (wait) {
			wait.wait();
		}
	}

}
