package com.six.dove.rpc.protocol.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.remote.protocol.RemoteMsg;
import com.six.dove.remote.protocol.RemoteProtocol;
import com.six.dove.remote.protocol.RemoteSerialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月20日 下午3:18:19
 */
public class NettyRpcEncoder extends MessageToByteEncoder<RemoteMsg> implements RemoteProtocol {

	final static Logger log = LoggerFactory.getLogger(NettyRpcEncoder.class);
	
	private RemoteSerialize remoteSerialize;
	
	public NettyRpcEncoder(RemoteSerialize remoteSerialize){
		if(null==remoteSerialize){
			throw new NullPointerException("rpcSerialize must be not null");
		}
		this.remoteSerialize=remoteSerialize;
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, RemoteMsg msg, ByteBuf out) throws Exception {
		byte msgType = msg.getType();
		byte[] body =remoteSerialize.serialize(msg); // 将对象转换为byte
		int dataLength = body.length; // 读取消息的长度
		out.writeByte(msgType);// 写入消息类型
		out.writeInt(dataLength); // 先将消息长度写入，也就是消息头
		out.writeBytes(body); // 消息体中包含我们要发送的数据
	}

}
