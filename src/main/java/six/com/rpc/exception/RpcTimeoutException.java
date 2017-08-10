package six.com.rpc.exception;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月6日 下午9:00:52
 * 
 *       Rpc 处理超时异常
 */
public class RpcTimeoutException extends RpcException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 602318916091395891L;

	public RpcTimeoutException(String message) {
		super(message);
	}

	public RpcTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
