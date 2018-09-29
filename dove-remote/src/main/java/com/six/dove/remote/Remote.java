package com.six.dove.remote;

import com.six.dove.common.Service;
import com.six.dove.remote.compiler.Compiler;
import com.six.dove.transport.codec.TransportCodec;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月10日 上午11:17:33 远程调用 接口
 * @describe 远程调用端
 *           execute方法的参数R_S,execute方法返回R_R，远程调用端连接发送方法的参数C_S,远程调用端连接发送方法返回C_R
 */
public interface Remote extends Service {

	/**
	 * 默认远程调用超时时间
	 */
	final long DEFAULT_CALL_TIMEOUT = 6000;
	
	Compiler getCompiler();
	
	TransportCodec getTransportCodec();
}


