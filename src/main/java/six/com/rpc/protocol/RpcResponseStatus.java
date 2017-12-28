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
	int SUCCEED = 1;

	/**
	 * 拒绝
	 */
	int REJECT= 2;

	/**
	 * 超时
	 */
	int TIMEOUT = 3;

	/**
	 * 执行异常
	 */
	int INVOKE_ERR= 4;

	/**
	 * 没有找到服务
	 */
	int UNFOUND_SERVICE= 5;
}
