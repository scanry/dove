package six.com.rpc.exception;


/**   
 * @author sixliu   
 * @date   2017年12月28日 
 * @email  359852326@qq.com  
 * @Description 
 */
public class RpcClientException extends RpcException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 2264089475665246236L;

	public RpcClientException(String message) {
		super(message);
	}

	public RpcClientException(Throwable cause) {
        super(cause);
    }
	
	public RpcClientException(String message, Throwable cause) {
		super(message, cause);
	}

}
