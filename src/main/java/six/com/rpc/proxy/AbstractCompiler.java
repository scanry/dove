package six.com.rpc.proxy;

import java.lang.reflect.Constructor;

import six.com.rpc.Compiler;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe
 */
public abstract class AbstractCompiler implements Compiler {

	@Override
	public final Object findOrCompile(String fullClassName, Class<?>[] parameterTypes, Object[] initargs,
			CodeBuilder codeBuilder) {
		try {
			Class<?> clz = findClass(fullClassName, codeBuilder);
			Constructor<?> constructor = null;
			if (null == parameterTypes) {
				constructor = clz.getConstructor();
			} else {
				constructor = clz.getConstructor(parameterTypes);
			}
			if (null == initargs) {
				return constructor.newInstance();
			} else {
				return constructor.newInstance(initargs);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Class<?> findClass(String fullClassName, CodeBuilder codeBuilder) throws Exception {
		Class<?> clz = loadClass(fullClassName);
		if (null == clz) {
			synchronized (this) {
				clz = loadClass(fullClassName);
				if (null == clz) {
					String code = null;
					if (null == codeBuilder || null == (code = codeBuilder.build())) {
						throw new RuntimeException("the codeBuilder or codeBuilder's result is null");
					}
					clz = compile(fullClassName, code, this.getClass().getClassLoader());
				}
			}
		}
		return clz;
	}

	/**
	 * 加载class
	 * 
	 * @param classfullName
	 * @return
	 * @throws Exception
	 */
	protected abstract Class<?> loadClass(String classfullName) throws Exception;

	/**
	 * 编译class
	 * 
	 * @param fullClassName
	 * @param code
	 * @param classLoader
	 * @return
	 * @throws Exception
	 */
	protected abstract Class<?> compile(String fullClassName, String code, ClassLoader classLoader) throws Exception;

}
