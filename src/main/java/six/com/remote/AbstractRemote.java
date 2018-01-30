package six.com.remote;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import six.com.rpc.Compiler;
import six.com.rpc.protocol.RpcSerialize;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月10日 上午11:20:06
 */
public abstract class AbstractRemote<S, R> implements Remote<S, R> {

	private static String MAC;
	private static String PID;

	static {
		MAC = getLocalMac();
		PID = getPid();
	}

	private static AtomicInteger requestIndex = new AtomicInteger(0);
	private RpcSerialize rpcSerialize;
	private Compiler compiler;

	public AbstractRemote(Compiler compiler, RpcSerialize rpcSerialize) {
		Objects.requireNonNull(compiler);
		Objects.requireNonNull(rpcSerialize);
		this.compiler = compiler;
		this.rpcSerialize = rpcSerialize;
	}

	@Override
	public Compiler getCompiler() {
		return compiler;
	}

	@Override
	public RpcSerialize getRpcSerialize() {
		return rpcSerialize;
	}

	public final String createRequestId(String targetHost, int targetPort, String serviceName) {
		long threadId = Thread.currentThread().getId();
		StringBuilder requestId = new StringBuilder();
		requestId.append(MAC).append("/");
		requestId.append(PID).append("/");
		requestId.append(threadId).append("@");
		requestId.append(targetHost).append(":");
		requestId.append(targetPort).append("/");
		requestId.append(serviceName).append("/");
		requestId.append(System.currentTimeMillis()).append("/");
		requestId.append(requestIndex.incrementAndGet());
		return requestId.toString();
	}
	
	public static String buildClientInterfaceWrapperClassName(Class<?> clz) {
		StringBuilder classSb = new StringBuilder();
		String instanceName = clz.getSimpleName();
		classSb.append("RpcClientInterfaceProxy$");
		classSb.append(instanceName);
		return classSb.toString();
	}

	public static final String getServiceName(String protocolClassName, Method serviceMethod) {
		Parameter[] parameter = serviceMethod.getParameters();
		StringBuilder serviceName = new StringBuilder();
		serviceName.append(protocolClassName).append("_");
		serviceName.append(serviceMethod.getName()).append("_");
		if (null != parameter) {
			String parameterTypeName = null;
			for (int i = 0, size = parameter.length; i < size; i++) {
				parameterTypeName = parameter[i].getParameterizedType().getTypeName();
				parameterTypeName = parameterTypeName.replace(".", "_");
				serviceName.append(parameterTypeName).append("_");
			}
		}
		return serviceName.toString();
	}

	public static boolean hasReturnType(Method instanceMethod) {
		if (null != instanceMethod.getReturnType() && Void.class != instanceMethod.getReturnType()
				&& !"void".equals(instanceMethod.getReturnType().getName())) {
			return true;
		} else {
			return false;
		}
	}

	public static String getLocalMac() {
		String mac = "";
		try {
			InetAddress ia = InetAddress.getLocalHost();
			byte[] macBytes = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
			StringBuffer sb = new StringBuffer("");
			for (int i = 0; i < macBytes.length; i++) {
				int temp = macBytes[i] & 0xff;
				String str = Integer.toHexString(temp);
				if (str.length() == 1) {
					sb.append("0" + str);
				} else {
					sb.append(str);
				}
			}
			mac = sb.toString().toUpperCase();
		} catch (Exception e) {
		}
		return mac;
	}

	public static String getPid() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		String pid = name.split("@")[0];
		return pid;
	}

}
