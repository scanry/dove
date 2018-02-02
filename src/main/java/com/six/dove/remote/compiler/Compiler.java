package com.six.dove.remote.compiler;

/**
 * @author sixliu
 * @date 2017年12月28日
 * @email 359852326@qq.com
 * @Description
 */
public interface Compiler {

	/**
	 * 代码构建器
	 * 
	 * @author MG01867
	 *
	 */
	@FunctionalInterface
	interface CodeBuilder {
		String build();
	}

	/**
	 * 通过给点的class全名获取Class并实例化,如果没有find到Class时，那么会调用提供的CodeBuilder生成代码并编译
	 * 
	 * @param fullClassName
	 *            Class全名
	 * @param parameterTypes
	 *            构建方法参数类型
	 * @param initargs
	 *            构建方法参数
	 * @param codeBuilder
	 *            当没有获取到Class，生成class builder
	 * @return
	 */
	<T> T findOrCompile(String fullClassName, Class<?>[] parameterTypes, Object[] initargs, CodeBuilder codeBuilder);
}
