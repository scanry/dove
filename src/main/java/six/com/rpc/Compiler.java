package six.com.rpc;

/**
 * @author sixliu
 * @date 2017年12月28日
 * @email 359852326@qq.com
 * @Description
 */
public interface Compiler {

	/**
	 * 代码构建器
	 * @author MG01867
	 *
	 */
	@FunctionalInterface
	interface CodeBuilder {
		String build();
	}

	/**
	 * 获取
	 * 
	 * @param packageName
	 * @param className
	 * @param code
	 * @param parameterTypes
	 * @param initargs
	 * @return
	 */
	Object findOrCompile(String fullClassName, Class<?>[] parameterTypes, Object[] initargs, CodeBuilder codeBuilder);
}
