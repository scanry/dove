package com.six.dove.transport;

/**
 * @author: sixliu
 * @email: 359852326@qq.com
 * @date: 2018年10月11日 下午11:18:58
 * @version V1.0
 * @Description://TODO
 */
public interface MessageHandler<C extends Message> {

	void handle(C message);
}
