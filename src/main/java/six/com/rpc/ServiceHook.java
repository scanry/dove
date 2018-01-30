package six.com.rpc;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe RPC服务调用时钩子
 */
public interface ServiceHook {

	/**
	 * 默认的空钩子
	 */
	public static ServiceHook DEFAULT_HOOK = new ServiceHook() {
	};

	/**
	 * 方法调用前钩子
	 * 
	 * @param parameter
	 */
	default void beforeHook(Object[] parameter) {
	}

	/**
	 * 方法调用异常时钩子
	 * 
	 * @param parameter
	 */
	default void exceptionHook(Object[] parameter) {
	}

	/**
	 * 方法调用后钩子
	 * 
	 * @param parameter
	 */
	default void afterHook(Object[] parameter) {
	}
}
