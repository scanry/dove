package six.com.rpc;

import com.six.dove.remote.Remote;
import com.six.dove.rpc.annotation.DoveService;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月6日 下午6:21:53
 */
@DoveService(protocol = TestService.class,version=Remote.REMOTE_SERVICE_VERSION)
public class TestServiceImpl implements TestService {

	@Override
	public String say(String name) {
		return "hi:" + name;
	}

}
