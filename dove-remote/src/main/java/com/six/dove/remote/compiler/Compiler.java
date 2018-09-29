package com.six.dove.remote.compiler;

/**
 * @author sixliu
 * @date 2017年12月28日
 * @email 359852326@qq.com
 * @Description
 */
public interface Compiler {

	/**
	 * 动态编译代码
	 * 
	 * @param fullClassName
	 *            Class全名
	 * @param code
	 *            当没有获取到Class，生成class builder
	 * @return
	 */
	Class<?> compile(String fullClassName, String code);
	
}
