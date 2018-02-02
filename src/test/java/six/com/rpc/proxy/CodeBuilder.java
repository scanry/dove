package six.com.rpc.proxy;

import java.util.HashMap;
import java.util.Map;

import com.six.dove.remote.AsyCallback;
import com.six.dove.remote.ServiceName;
import com.six.dove.remote.client.ClientRemote;
import com.six.dove.remote.client.netty.NettyClientRemote;
import com.six.dove.remote.protocol.RemoteRequest;
import com.six.dove.remote.protocol.RemoteResponse;
import com.six.dove.remote.server.WrapperService;

import six.com.rpc.TestService;

/**
 * @author:MG01867
 * @date:2018年2月2日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class CodeBuilder<T> {

	static class MethodBuilder {

		public void to(Class<?> target) {

		}

		public void to(Object target) {

		}

		public void to(MethodCode methodCode) {

		}

		public void to(String code) {

		}
	}

	public CodeBuilder<T> name(String name) {
		return this;
	}

	public CodeBuilder<T> subclass(Class<?> clz) {
		return this;
	}

	public CodeBuilder<T> attribute(String name, Class<?> clz) {
		return this;
	}

	public CodeBuilder<T> constructor(Class<?>... clz) {
		return this;
	}

	public MethodBuilder method(String methodName) {
		return null;
	}

	public void load(ClassLoader classLoader) {

	}

	public Class<T> getLoaded() {
		return null;
	}

	static Map<Class<?>, Object> returnTypeCache = new HashMap<>();
	static {
		returnTypeCache.put(byte.class, 0);
		returnTypeCache.put(char.class, 0);
		returnTypeCache.put(short.class, 0);
		returnTypeCache.put(int.class, 0);
		returnTypeCache.put(long.class, 0);
		returnTypeCache.put(float.class, 0);
		returnTypeCache.put(double.class, 0);
		returnTypeCache.put(boolean.class, true);
	}

	private static Object parser(Class<?> returnType) {
		return returnTypeCache.get(returnType);
	}

	@FunctionalInterface
	interface MethodCode {

		Object code(String methodName, Class<?> returnType, String[] parmaTypes, Object... objects);
	}

	public static void main(String[] args) throws Exception {
		CodeBuilder<WrapperService> codeBuilder = new CodeBuilder<>();
		codeBuilder.name("");
		codeBuilder.subclass(WrapperService.class);
		codeBuilder.attribute("target", TestService.class);
		codeBuilder.constructor(TestService.class);
		MethodBuilder methodBuilder = codeBuilder.method("invoke");
		ClientRemote clientRemote = new NettyClientRemote();
		String className = "";
		String callHost = "127.0.0.1";
		int callPort = 80;
		AsyCallback asyCallback = null;
		@SuppressWarnings("unused")
		MethodCode methodCode = (methodName, returnType, parmaTypes, objects) -> {
			ServiceName serviceName = ServiceName.newServiceName(className, methodName, parmaTypes, 1);
			String id = clientRemote.createRequestId(callHost, callPort, serviceName);
			RemoteRequest request = new RemoteRequest();
			request.setId(id);
			request.setCallHost(callHost);
			request.setCallPort(callPort);
			request.setAsyCallback(asyCallback);
			request.setParams(objects);
			RemoteResponse response = clientRemote.execute(request);
			if (null == asyCallback) {
				return response.getResult();
			} else {
				return parser(returnType);
			}
		};
		methodBuilder.to(methodCode);
		codeBuilder.load(CodeBuilder.class.getClassLoader());
		codeBuilder.getClass();
	}
}
