package six.com.rpc.exception;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月6日 下午9:21:55
 */
public class RpcRejectServiceException extends RpcException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8557664964373398694L;

	public RpcRejectServiceException(String message) {
		super(message);
	}

	public RpcRejectServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
