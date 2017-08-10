package six.com.rpc.exception;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月6日 下午9:03:46
 */
public class RpcInvokeException extends RpcException {

	/**
	 * 执行rpc服务时异常
	 */
	private static final long serialVersionUID = -6536519516880573828L;

	public RpcInvokeException(String message) {
		super(message);
	}

	public RpcInvokeException(Throwable cause) {
        super(cause);
    }
	
	public RpcInvokeException(String message, Throwable cause) {
		super(message, cause);
	}

}
