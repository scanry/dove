package six.com.rpc.common;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月22日 下午1:16:18
 */
public interface NettyConstant {

	final static int READ_IDLE_TIME_SECONDES = 60;// 读操作空闲30秒

	final static int WRITER_IDLE_TIME_SECONDES = 60;// 写操作空闲30秒

	final static int ALL_IDLE_TIME_SECONDES = 60;// 读写全部空闲100秒
}
