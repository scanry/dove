package six.com.rpc.protocol;

import java.io.Serializable;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月20日 下午4:04:15 
*/
public abstract class RpcMsg implements Serializable{

	private static final long serialVersionUID = 5753584654821223320L;
	private String id;
	private byte type;//1请求 2响应
	
	public RpcMsg(byte type){
		this.type=type;
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
