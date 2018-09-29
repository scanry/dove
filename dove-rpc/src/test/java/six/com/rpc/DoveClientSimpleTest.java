package six.com.rpc;


import com.six.dove.rpc.client.DoveClientImpl;

/**
*@author:MG01867
*@date:2018年2月7日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public class DoveClientSimpleTest {

	public static void main(String[] args) {
		DoveClientImpl client = new DoveClientImpl();
		TestService testServiceSyn = client.lookupService("127.0.0.1", 80, TestService.class);
		String result = testServiceSyn.say("hi");
		System.out.println(result);
		client.stop();
	}

}
