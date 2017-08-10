package six.com.rpc.exception;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月22日 上午10:28:55
 */
public class RpcSystenExceptions {

	public static int MSG_ILLEGAL_TYPE = 1;
	public static int MSG_TOO_BIG = 2;
	public static int READER_IDLE = 3;

	/**
	 * 非法的消息类型
	 */
	public static final RpcSystenException ILLEGAL_MSG_ERR = new RpcSystenException(MSG_ILLEGAL_TYPE,
			"illegal msg type");
	/**
	 * Protocol body 太大
	 */
	public static final RpcSystenException BODY_TOO_BIG_ERR = new RpcSystenException(MSG_TOO_BIG, "msg is too big");

	/**
	 * Read idle 链路检测
	 */
	public static final RpcSystenException READER_IDLE_ERR = new RpcSystenException(READER_IDLE, "reader idle");

	/**
	 * Read idle 链路检测
	 */
	public static final RpcSystenException UN_SERIALIZE_ERR = new RpcSystenException(READER_IDLE,
			"did not unSerialize");
}
