package com.six.dove.remote.compiler;

import java.lang.reflect.Constructor;

/**
 * @author:MG01867
 * @date:2018年1月29日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe
 */
public abstract class AbstractCompiler implements Compiler {

	@SuppressWarnings("unchecked")
	@Override
	public final <T> T findOrCompile(String fullClassName, Class<?>[] parameterTypes, Object[] initargs,
			CodeBuilder codeBuilder) {
		try {
			Class<?> clz = findClass(fullClassName, codeBuilder);
			Constructor<?> constructor = null;
			if (null == parameterTypes || parameterTypes.length == 0) {
				constructor = clz.getConstructor();
			} else {
				constructor = clz.getConstructor(parameterTypes);
			}
			if (null == initargs || initargs.length == 0) {
				return (T) constructor.newInstance();
			} else {
				return (T) constructor.newInstance(initargs);
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
					clz = compile(fullClassName, code);
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
	protected abstract Class<?> compile(String fullClassName, String code) throws Exception;

}
