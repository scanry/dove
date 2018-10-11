package com.six.dove.transport.util;

/**    
 * @author:     sixliu
 * @email:      359852326@qq.com
 * @date:       2018年10月11日 下午11:49:19   
 * @version     V1.0 
 * @description:TODO
 */
public interface Constant<T extends Constant<T>> extends Comparable<T> {

    int id();

    String name();
}
