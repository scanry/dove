package six.com.rpc;

import com.six.dove.rpc.annotation.DoveService;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月6日 下午6:21:53
 */
@DoveService(protocol = TestService.class)
public class TestServiceImpl implements TestService {

	@Override
	public String say(String name) {
		return "你好:" + name;
	}

}
