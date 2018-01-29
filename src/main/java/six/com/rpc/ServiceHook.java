package six.com.rpc;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public interface ServiceHook {

	public static ServiceHook DEFAULT_HOOK = new ServiceHook() {
		@Override
		public void beforeHook(Object[] parameter) {}

		@Override
		public void exceptionHook(Object[] parameter) {}

		@Override
		public void afterHook(Object[] parameter) {}

	};

	/**
	 * 方法调用前钩子
	 * @param parameter
	 */
	void beforeHook(Object[] parameter);
	
	/**
	 * 方法调用异常时钩子
	 * @param parameter
	 */
	void exceptionHook(Object[] parameter);
	
	/**
	 * 方法调用后钩子
	 * @param parameter
	 */
	void afterHook(Object[] parameter);
}
