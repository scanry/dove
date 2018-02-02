package six.com.rpc.proxy;

import com.six.dove.remote.server.WrapperService;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import six.com.rpc.TestServiceImpl;

/**
 * @author:MG01867
 * @date:2018年2月2日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class ByteBuddyTest {

	public static void main(String[] args) throws Exception {
		DynamicType.Builder<WrapperService> builder = new ByteBuddy().subclass(WrapperService.class);
		Class<?> dynamicType = builder.method(ElementMatchers.named("invoke"))
				.intercept(MethodDelegation.to(new TestServiceImpl())).make()
				.load(ByteBuddyTest.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER).getLoaded();
		WrapperService service = (WrapperService) dynamicType.getConstructor().newInstance();
		System.out.println(service.invoke(new Object[] {"six"}));
	}

}
