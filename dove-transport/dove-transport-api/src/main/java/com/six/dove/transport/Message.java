package com.six.dove.transport;

import java.io.Serializable;

/**
 * @author:MG01867
 * @date:2018年3月30日
 * @email:359852326@qq.com
 * @version:
 * @describe 传输消息
 */
public abstract class Message implements Serializable {

	private static final long serialVersionUID = 5753584654821223320L;

	private final byte type;

	private String id;

	private byte[] data;

	public Message(byte type) {
		this.type = type;
	}

	public byte getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}