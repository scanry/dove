package six.com.rpc;


import com.six.dove.remote.client.socket.SocketClientRemote;
import com.six.dove.rpc.client.DoveClientImpl;

/**
 * @author:MG01867
 * @date:2018年2月6日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class SocketClientRemoteTest {

	public static void main(String[] args) throws InterruptedException {
		DoveClientImpl client = new DoveClientImpl(new SocketClientRemote());
		TestService testServiceSyn = client.lookupService("127.0.0.1", 80, TestService.class);
		String result = testServiceSyn.say("hi");
		System.out.println(result);
		synchronized (SocketClientRemoteTest.class) {
			SocketClientRemoteTest.class.wait();
		}
		client.stop();
	}

}
