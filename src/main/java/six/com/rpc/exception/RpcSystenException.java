package six.com.rpc.exception;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月22日 上午10:18:08
 * 
 *       rpc系统定义常见异常
 */
public class RpcSystenException extends RpcException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2308882870528693053L;

	private int rpcSystenType;

	public RpcSystenException(int rpcSystenType, String message) {
		super(message);
		this.rpcSystenType = rpcSystenType;
	}

	public int getRpcSystenType() {
		return rpcSystenType;
	}
}
