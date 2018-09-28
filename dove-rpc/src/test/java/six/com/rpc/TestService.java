package six.com.rpc;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月6日 下午6:21:37
 */
public interface TestService {

	default String say(String name) {
		System.out.println(name);
		return null;
	}

}
