package six.com.rpc.proxy;

import java.lang.reflect.Method;

import org.junit.Test;

import com.six.dove.remote.compiler.Compiler;
import com.six.dove.remote.compiler.impl.JavaCompilerImpl;
import com.six.dove.remote.server.AbstractServerRemote;
import com.six.dove.remote.server.WrapperService;

import six.com.rpc.TestService;
import six.com.rpc.TestServiceImpl;

/**
 * @author sixliu
 * @date 2017年12月28日
 * @email 359852326@qq.com
 * @Description
 */
public class JavaCompilerImplTest {

	@Test
	public void test() {
		String[] paras = new String[] { "sixliu" };
		Compiler compiler = new JavaCompilerImpl();
		Class<?> protocolClass = TestService.class;
		TestService testService = new TestServiceImpl();
		TestServerRemote testServerRemote = new TestServerRemote("", "127.0.0.0", 8080);
		try {
			Method hello = TestService.class.getMethod("say", String.class);
			String className = "SSSSSSSSSSSSSSSSSS";
			String packageName = protocolClass.getPackage().getName();
			String fullClassName = packageName + "." + className;
			WrapperService result = (WrapperService) compiler.findOrCompile(fullClassName,
					new Class<?>[] { protocolClass }, new Object[] { testService }, () -> {
						return testServerRemote.generateProtocolProxyClassCode(protocolClass, packageName, className,
								hello);
					});
			System.out.println(result.invoke(paras));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class TestServerRemote extends AbstractServerRemote {

		public TestServerRemote(String name, String localHost, int listenPort) {
			super(name, localHost, listenPort);
		}

		@Override
		protected void stop2() {

		}

		@Override
		protected void doStart() {

		}

	}

}
