package com.six.dove.transport.protocol;

import java.io.Serializable;

/**
 * @author:MG01867
 * @date:2018年3月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public abstract class TransportMessage implements Serializable {

	private static final long serialVersionUID = 5753584654821223320L;

	private String id;

	private final byte type;// 1请求 2响应

	public TransportMessage(byte type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public byte getType() {
		return type;
	}
}