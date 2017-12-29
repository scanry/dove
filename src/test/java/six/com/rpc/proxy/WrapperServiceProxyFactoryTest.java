package six.com.rpc.proxy;

import java.lang.reflect.Method;

import org.junit.Test;

import six.com.rpc.WrapperService;
import six.com.rpc.WrapperServiceProxyFactory;
import six.com.rpc.client.NettyRpcCilent;

/**
 * @author sixliu
 * @date 2017年12月28日
 * @email 359852326@qq.com
 * @Description
 */
public class WrapperServiceProxyFactoryTest {

	public static interface TestService {
		String hello(String name, String content)throws TestExeception;
	}

	public static class TestServiceImpl implements TestService {

		@Override
		public String hello(String name, String content) {
			return "hello world:" + name + "," + content;
		}

	}
	
	class TestExeception extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = -6878525567920345651L;
		
	}

	@Test
	public void test() {
		String[] parma = new String[] { "sixliu", "welcome you" };
		WrapperServiceProxyFactory wrapperServiceProxyFactory = new JavaWrapperServiceProxyFactory();
		TestServiceImpl testService = new TestServiceImpl();
		Object result = null;
		try {
			Method hello = TestService.class.getMethod("hello", String.class, String.class);
			WrapperService wrapperService = wrapperServiceProxyFactory.newServerWrapperService(testService, hello);
			result = wrapperService.invoke(parma);
			wrapperService = wrapperServiceProxyFactory.newServerWrapperService(testService, hello);
			result = wrapperService.invoke(parma);
			System.out.println(result);
			NettyRpcCilent client = new NettyRpcCilent();
			TestService testServiceClient=wrapperServiceProxyFactory.newClientInterfaceWrapperInstance(client,"127.0.0.1", 80, TestService.class, null);
			System.out.println(testServiceClient);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
