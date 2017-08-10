package six.com.rpc.protocol;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月6日 下午9:12:34 rpc响应状态
 */
public interface RpcResponseStatus {

	/**
	 * 成功
	 */
	int succeed = 1;

	/**
	 * 拒绝
	 */
	int reject = 2;

	/**
	 * 超时
	 */
	int timeout = 3;

	/**
	 * 执行异常
	 */
	int invokeErr = 4;

	/**
	 * 没有找到服务
	 */
	int notFoundService = 5;
}
