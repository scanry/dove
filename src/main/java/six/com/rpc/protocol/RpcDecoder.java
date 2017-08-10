package six.com.rpc.protocol;

import java.net.SocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import six.com.rpc.exception.RpcSystenExceptions;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月23日 上午8:50:07
 * 
 * 消息编码 
 * 	<p>1.判断消息是否大于最小长度</p>
 * 	<p>2.读取消息类型</p>
 * 	<p>3.检查是否是心跳包消息 如果是心跳包，那么这里结束</p>
 *  <p>4.读取数据长度</p>
 *  <p>5.检查数据长度</p>
 *  <p>6.根据数据长度读取数据</p>
 *  <p>7.序列数据</p>
 *  <p>结束</p>
 * 
 */
public class RpcDecoder extends ByteToMessageDecoder implements RpcProtocol {

	final static Logger log = LoggerFactory.getLogger(RpcDecoder.class);
	
	private RpcSerialize rpcSerialize;

	public RpcDecoder(RpcSerialize rpcSerialize){
		if(null==rpcSerialize){
			throw new NullPointerException("rpcSerialize must be not null");
		}
		this.rpcSerialize=rpcSerialize;
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
		if (buffer.readableBytes() >= RpcProtocol.HEAD_MIN_LENGTH) {
			buffer.markReaderIndex();
			byte msgType = buffer.readByte();
			if (msgType == RpcProtocol.HEARTBEAT) {
				log.debug("received heartbeat from " + getRemoteAddress(ctx));
			} else if (msgType != RpcProtocol.REQUEST && msgType != RpcProtocol.RESPONSE) {
				buffer.resetReaderIndex();
				log.error("received illegal msg type[" + msgType + "] from" + getRemoteAddress(ctx));
				throw RpcSystenExceptions.ILLEGAL_MSG_ERR;
			} else {
				int dataLength = buffer.readInt();
				// 如果dataLength过大，可能导致问题
				if (buffer.readableBytes() < dataLength) {
					buffer.resetReaderIndex();
					return;
				}
				if (RpcProtocol.MAX_BODY_SIZE > 0 && dataLength > RpcProtocol.MAX_BODY_SIZE) {
					throw RpcSystenExceptions.BODY_TOO_BIG_ERR;
				}
				byte[] data = new byte[dataLength];
				buffer.readBytes(data);
				if(RpcProtocol.REQUEST==msgType){
					try{
						RpcRequest rpcRequest = rpcSerialize.unSerialize(data, RpcRequest.class);
						out.add(rpcRequest);
					}catch (Exception e) {
						log.error("did not unSerialize rpcRequest from "+getRemoteAddress(ctx),e);
					}
				}else if(RpcProtocol.RESPONSE==msgType){
					try{
						RpcResponse rpcResponse = rpcSerialize.unSerialize(data, RpcResponse.class);
						out.add(rpcResponse);
					}catch (Exception e) {
						log.error("did not unSerialize rpcResponse from "+getRemoteAddress(ctx),e);
					}
				}else{
					throw RpcSystenExceptions.ILLEGAL_MSG_ERR;
				}
			}
		}

	}

	public static String getRemoteAddress(ChannelHandlerContext ctx) {
		String address = "";
		SocketAddress remote = ctx.channel().remoteAddress();
		if (remote != null) {
			address = remote.toString();
		}
		return address;

	}

}
