package six.com.rpc.proxy;

import java.lang.reflect.Method;

import org.junit.Test;

import six.com.rpc.Compiler;
import six.com.rpc.common.WrapperService;
import six.com.rpc.server.AbstractServer;

/**
 * @author sixliu
 * @date 2017年12月28日
 * @email 359852326@qq.com
 * @Description
 */
public class JavaCompilerImplTest {

	public static interface TestService {
		String hello(String name, String content);
	}

	public static class TestServiceImpl implements TestService {

		@Override
		public String hello(String name, String content) {
			return "hello world:" + name + "," + content;
		}

	}

	@Test
	public void test() {
		String[] paras = new String[] { "sixliu", "hello" };
		Compiler compiler = new JavaCompilerImpl();
		Class<?> protocolClass = TestService.class;
		TestService testService = new TestServiceImpl();

		try {
			Method hello = TestService.class.getMethod("hello", String.class, String.class);
			String className = AbstractServer.buildServerServiceClassName(testService, hello);
			String packageName = TestService.class.getPackage().getName();
			String fullClassName = packageName + "." + className;
			WrapperService result = (WrapperService) compiler.findOrCompile(fullClassName,
					new Class<?>[] { protocolClass }, new Object[] { testService }, () -> {
						return AbstractServer.buildServerWrapperServiceCode(protocolClass, packageName, className,
								hello);
					});
			System.out.println(result.invoke(paras));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
