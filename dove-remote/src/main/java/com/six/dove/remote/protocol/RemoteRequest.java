package com.six.dove.remote.protocol;


import com.six.dove.transport.Request;

/**
*@author:MG01867
*@date:2018年4月12日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public class RemoteRequest extends Request{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8275317164882837963L;
	
	private String command;
	private RemoteRequestParam remoteRequestParam;
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public RemoteRequestParam getRemoteRequestParam() {
		return remoteRequestParam;
	}
	public void setRemoteRequestParam(RemoteRequestParam remoteRequestParam) {
		this.remoteRequestParam = remoteRequestParam;
	}
}
