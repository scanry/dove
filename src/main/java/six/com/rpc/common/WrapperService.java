package six.com.rpc.common;


/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月7日 下午3:30:35 rpc 服务端服务包装接口
 */
@FunctionalInterface
public interface WrapperService {

	/**
	 * 服务端服务包装 方法
	 * @param paras
	 * @return
	 */
	Object invoke(Object[] paras)throws Exception;
}
