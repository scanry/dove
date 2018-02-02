package com.six.dove.remote.protocol;

import java.io.Serializable;

/**
 * @author six
 * @date 2016年6月2日 下午4:15:17 rpc响应
 */
public class RemoteResponse extends RemoteMsg implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7823438169107836197L;

	// 响应结果
	private Object result;

	private int status;

	private String msg;

	public RemoteResponse(int status) {
		super(RemoteProtocol.RESPONSE);
		this.status = status;
	}

	public RemoteResponse() {
		super(RemoteProtocol.RESPONSE);
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public boolean isSuccessed() {
		return status == RemoteResponseState.SUCCEED;
	}

}
