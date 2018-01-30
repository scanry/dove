package six.com.remote.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.remote.AbstractRemote;
import six.com.rpc.Compiler;
import six.com.rpc.protocol.RpcRequest;
import six.com.rpc.protocol.RpcResponse;
import six.com.rpc.protocol.RpcResponseStatus;
import six.com.rpc.protocol.RpcSerialize;
import six.com.rpc.server.WrapperServiceTuple;
import six.com.rpc.util.ExceptionUtils;

/**
 * @author:MG01867
 * @date:2018年1月30日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public abstract class AbstractServerRemote extends AbstractRemote<RpcRequest, Void> implements ServerRemote {

	final static Logger log = LoggerFactory.getLogger(AbstractServerRemote.class);
	private String localHost;
	private int listenPort;
	private Map<String, ServerRpcConnection> serverRpcConnectionCache = new ConcurrentHashMap<>();

	public AbstractServerRemote(String localHost, int listenPort, Compiler compiler, RpcSerialize rpcSerialize) {
		super(compiler, rpcSerialize);
		this.localHost = localHost;
		this.listenPort = listenPort;
	}

	@Override
	public Void execute(RpcRequest rpcRequest) {
		RpcResponse rpcResponse = new RpcResponse();
		rpcResponse.setId(rpcRequest.getId());
		WrapperServiceTuple wrapperServiceTuple = getWrapperServiceTuple(rpcRequest.getServiceName());
		String address = rpcRequest.getServerRpcConnection().toString();
		if (null != wrapperServiceTuple) {
			try {
				wrapperServiceTuple.getExecutorService().submit(() -> {
					log.debug("server received coommand[" + rpcRequest.getServiceName() + "] from:" + address);
					try {
						wrapperServiceTuple.getHook().beforeHook(rpcRequest.getParams());
						Object result = wrapperServiceTuple.getWrapperService().invoke(rpcRequest.getParams());
						wrapperServiceTuple.getHook().afterHook(rpcRequest.getParams());
						rpcResponse.setStatus(RpcResponseStatus.SUCCEED);
						rpcResponse.setResult(result);
					} catch (Exception e) {
						wrapperServiceTuple.getHook().exceptionHook(rpcRequest.getParams());
						String errMsg = ExceptionUtils.getExceptionMsg(e);
						rpcResponse.setStatus(RpcResponseStatus.INVOKE_ERR);
						rpcResponse.setMsg(errMsg);
						log.error("invoke request[" + address + "] err", e);
					}
					rpcRequest.getServerRpcConnection().send(rpcResponse);
				});
			} catch (RejectedExecutionException e) {
				// 业务处理线程池满了，拒绝异常
				rpcResponse.setStatus(RpcResponseStatus.REJECT);
				String msg = "the service is too busy and reject rpcRequest[" + address + "]:"
						+ rpcRequest.getServiceName();
				rpcResponse.setMsg(msg);
				log.error(msg);
				rpcRequest.getServerRpcConnection().send(rpcResponse);
			}
		} else {
			rpcResponse.setStatus(RpcResponseStatus.UNFOUND_SERVICE);
			String msg = "unfound service by rpcRequest[" + address + "]:" + rpcRequest.getServiceName();
			rpcResponse.setMsg(msg);
			log.error(msg);
			rpcRequest.getServerRpcConnection().send(rpcResponse);
		}
		return null;
	}

	@Override
	public String getLocalHost() {
		return localHost;
	}

	@Override
	public int getListenPort() {
		return listenPort;
	}

	@Override
	public ServerRpcConnection getServerRpcConnection(String id,
			Function<String, ServerRpcConnection> mappingFunction) {
		return serverRpcConnectionCache.computeIfAbsent(id, mappingFunction);
	}

}
