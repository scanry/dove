package six.com.rpc.protocol;

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

/**
 * @author sixliu
 * @date 2017年12月28日
 * @email 359852326@qq.com
 * @Description
 */
public class NewRpcDecoder extends LengthFieldBasedFrameDecoder implements RpcProtocol {

	final static Logger log = LoggerFactory.getLogger(NewRpcDecoder.class);

	private RpcSerialize rpcSerialize;

	public NewRpcDecoder(RpcSerialize rpcSerialize) {
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
