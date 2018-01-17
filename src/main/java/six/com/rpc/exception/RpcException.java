package six.com.rpc.exception;
/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年4月6日 下午8:59:39 
*/
public class RpcException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -132337553479313107L;

	public RpcException(String message) {
		super(message);
	}
	
	public RpcException(Throwable cause) {
        super(cause);
    }

	public RpcException(String message, Throwable cause) {
		super(message, cause);
	}
}
