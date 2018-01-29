package six.com.rpc;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
@FunctionalInterface
public interface ServiceHook {

	public static ServiceHook DEFAULT_HOOK = new ServiceHook() {
		@Override
		public void hook(Object parameter) {

		}
	};

	void hook(Object parameter);
}
