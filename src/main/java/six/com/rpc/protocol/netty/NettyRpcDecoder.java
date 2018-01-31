package six.com.rpc.protocol.netty;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import six.com.rpc.exception.RpcSystenExceptions;
import six.com.rpc.protocol.RpcProtocol;
import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.protocol.RpcResponse;
import six.com.rpc.protocol.RpcSerialize;

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
public class NettyRpcDecoder extends LengthFieldBasedFrameDecoder implements RpcProtocol {

	final static Logger log = LoggerFactory.getLogger(NettyRpcDecoder.class);

	private RpcSerialize rpcSerialize;

	public NettyRpcDecoder(RpcSerialize rpcSerialize) {
		super(RpcProtocol.MAX_BODY_SIZE, RpcProtocol.MSG_TYPE, RpcProtocol.BODY_LENGTH);
		Objects.requireNonNull(rpcSerialize, "rpcSerialize must be not null");
		this.rpcSerialize = rpcSerialize;
	}

	@Override
	public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		Object decodeResult = null;
		ByteBuf frame = null;
		try {
			frame = (ByteBuf) super.decode(ctx, in);
			if (null != frame) {
				ByteBuffer byteBuffer = frame.nioBuffer();
				byte msgType = byteBuffer.get();
				if (msgType == RpcProtocol.HEARTBEAT) {
					log.debug("received heartbeat from " + getRemoteAddress(ctx));
				} else {
					int dataLength = byteBuffer.getInt();
					byte[] data = null;
					if (RpcProtocol.REQUEST == msgType) {
						data = new byte[dataLength];
						byteBuffer.get(data);
						try {
							decodeResult = rpcSerialize.unSerialize(data, RpcRequest.class);
						} catch (Exception e) {
							log.error("did not unSerialize rpcRequest from " + getRemoteAddress(ctx), e);
						}
					} else if (RpcProtocol.RESPONSE == msgType) {
						data = new byte[dataLength];
						byteBuffer.get(data);
						try {
							decodeResult = rpcSerialize.unSerialize(data, RpcResponse.class);
						} catch (Exception e) {
							log.error("did not unSerialize rpcResponse from " + getRemoteAddress(ctx), e);
						}
					} else {
						throw RpcSystenExceptions.ILLEGAL_MSG_ERR;
					}
				}
			}
		} catch (Exception e) {
			log.error("decode exception, " + getRemoteAddress(ctx));
			ctx.close().addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					log.info("closeChannel: close the connection to remote address[{}] result: {}",
							getRemoteAddress(ctx), future.isSuccess());
				}
			});
		} finally {
			if (null != frame) {
				frame.release();
			}
		}
		return decodeResult;
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