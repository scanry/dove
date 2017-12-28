package six.com.rpc.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import six.com.rpc.AsyCallback;
import six.com.rpc.RpcClient;
import six.com.rpc.WrapperService;
import six.com.rpc.WrapperServiceProxyFactory;
import six.com.rpc.protocol.RpcRequest;

/**
 * @author sixliu
 * @date 2017年12月28日
 * @email 359852326@qq.com
 * @Description
 */
public class JavaWrapperServiceProxyFactory implements WrapperServiceProxyFactory {

	private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	private final DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<JavaFileObject>();
	private final JavaFileManagerImpl javaFileManager;
	private ProxyClassLoad proxyClassLoad;

	public JavaWrapperServiceProxyFactory() {
		StandardJavaFileManager manager = compiler.getStandardFileManager(diagnosticCollector, null, null);
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		proxyClassLoad = AccessController.doPrivileged(new PrivilegedAction<ProxyClassLoad>() {
			public ProxyClassLoad run() {
				return new ProxyClassLoad(loader);
			}
		});
		javaFileManager = new JavaFileManagerImpl(manager, proxyClassLoad);
	}

	@Override
	public WrapperService newServerWrapperService(Object instance, Method instanceMethod) {
		String packageName = instance.getClass().getPackage().getName();
		String className = buildServerSerbviceClassName(instance, instanceMethod);
		WrapperService wrapperService = null;
		try {
			Class<?> clz = findClass(packageName, className,()->{
				return buildServerWrapperServiceCode(packageName, className, instance, instanceMethod);
			});
			Constructor<?> constructor = clz.getConstructor(instance.getClass());
			wrapperService = (WrapperService) constructor.newInstance(instance);
			return wrapperService;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T newClientInterfaceWrapperInstance(RpcClient rpcClient,String targetHost, int targetPort,Class<?> clz,AsyCallback asyCallback) {
		String packageName = clz.getPackage().getName();
		String className = buildClientInterfaceWrapperClassName(clz);
		T t = null;
		try {
			Class<?> WrapperClass = findClass(packageName, className,()->{
				return buildClientInterfaceWrapperCode(rpcClient, targetHost, targetPort, packageName, className, clz, asyCallback);
			});
			Constructor<?> constructor = WrapperClass.getConstructor(RpcClient.class);
			t = (T) constructor.newInstance(rpcClient);
			return t;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Class<?> findClass(String packageName, String className,BuildCode buildCode)
			throws Exception {
		String classfullName = packageName + "." + className;
		Class<?> clz = proxyClassLoad.loadClass(classfullName);
		if (null == clz) {
			synchronized (proxyClassLoad) {
				clz = proxyClassLoad.loadClass(classfullName);
				if (null == clz) {
					String code=buildCode.buildCode();
					clz = compile(packageName, classfullName, className, code, this.getClass().getClassLoader());
				}
			}
		}
		return clz;
	}

	@FunctionalInterface
	interface BuildCode{
		String buildCode();
	}
	private static String buildServerSerbviceClassName(Object instance, Method instanceMethod) {
		StringBuilder classSb = new StringBuilder();
		String instanceName = instance.getClass().getSimpleName();
		String instanceMethodName = instanceMethod.getName();
		classSb.append("Proxy$");
		classSb.append(instanceName).append("$");
		classSb.append(instanceMethodName);
		Parameter[] parameter = instanceMethod.getParameters();
		if (null != parameter && parameter.length > 0) {
			classSb.append("$");
			String parameterTypeName = null;
			StringBuilder invokePamasSb = new StringBuilder();
			for (int i = 0, size = parameter.length; i < size; i++) {
				parameterTypeName = parameter[i].getParameterizedType().getTypeName();
				parameterTypeName = parameterTypeName.replace(".", "_");
				invokePamasSb.append(parameterTypeName).append("$");
			}
			invokePamasSb.deleteCharAt(invokePamasSb.length() - 1);
			classSb.append(invokePamasSb);
		}
		return classSb.toString();
	}
	
	private static String buildClientInterfaceWrapperClassName(Class<?> clz) {
		StringBuilder classSb = new StringBuilder();
		String instanceName = clz.getSimpleName();
		classSb.append("Proxy$");
		classSb.append(instanceName).append("$");
		classSb.append(System.currentTimeMillis());
		return classSb.toString();
	}

	private Class<?> compile(String packageName, String classfullName, String className, String code,
			ClassLoader classLoader) throws Exception {
		JavaFileObject javaFileObject = new StringJavaObject(className, code);
		javaFileManager.putFileForInput(StandardLocation.SOURCE_PATH, packageName,
				className + ClassUtils.JAVA_EXTENSION, javaFileObject);
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		CompilationTask compilationTask = compiler.getTask(null, javaFileManager, diagnostics, null, null,
				Arrays.asList(javaFileObject));
		Boolean result = compilationTask.call();
		if (result == null || !result.booleanValue()) {
			throw new IllegalStateException("Compilation failed. class: " + classfullName + ", diagnostics: ");
		}
		return proxyClassLoad.loadClass(classfullName);
	}

	private String buildServerWrapperServiceCode(String packageName, String className, Object instance, Method instanceMethod) {
		String method = instanceMethod.getName();
		String instanceType = instance.getClass().getCanonicalName();
		Parameter[] parameter = instanceMethod.getParameters();
		StringBuilder clz = new StringBuilder();
		clz.append("package ").append(packageName).append(";\n");
		clz.append("import ").append(instanceType).append(";\n");
		clz.append("public class ").append(className).append(" implements six.com.rpc.WrapperService {\n");
		clz.append("	private ").append(instanceType).append(" instance;\n");
		clz.append("	public " + className + "(").append(instanceType).append(" instance").append("){\n");
		clz.append("		this.instance=instance;\n");
		clz.append("	}\n");
		clz.append("	@Override\n");
		clz.append("	public Object invoke(Object[] paras)throws Exception{\n");
		String invokePamasStr = "";
		if (null != parameter && parameter.length > 0) {
			String parameterTypeName = null;
			StringBuilder invokePamasSb = new StringBuilder();
			for (int i = 0, size = parameter.length; i < size; i++) {
				parameterTypeName = parameter[i].getParameterizedType().getTypeName();
				invokePamasSb.append("(").append(parameterTypeName).append(")").append("paras[").append(i).append("],");
			}
			invokePamasSb.deleteCharAt(invokePamasSb.length() - 1);
			invokePamasStr = invokePamasSb.toString();
		}
		clz.append("		return this.instance.").append(method);
		clz.append("(").append(invokePamasStr).append(");\n");
		clz.append("	}\n");
		clz.append("}\n");
		return clz.toString();
	}
	
	private String buildClientInterfaceWrapperCode(RpcClient rpcClient,String targetHost, int targetPort,String packageName, String className, Class<?> clz,AsyCallback asyCallback) {
		String importClass=rpcClient.getClass().getCanonicalName();
		String returnType=null;
		StringBuilder clzSb = new StringBuilder();
		clzSb.append("package ").append(packageName).append(";\n");
		clzSb.append("import ").append(importClass).append(";\n");
		clzSb.append("import six.com.rpc.protocol.RpcRequest").append(";\n");
		clzSb.append("public class ").append(className).append(" implements six.com.rpc.WrapperService {\n");
		clzSb.append("	private ").append(importClass).append(" rpcClient;\n");
		clzSb.append("	public " + className + "(").append(importClass).append(" rpcClient").append("){\n");
		clzSb.append("		this.instance=instance;\n");
		clzSb.append("	}\n");
		clzSb.append("	@Override\n");
		clzSb.append("	public "+returnType+" invoke(Object[] paras)throws Exception{\n");
		clzSb.append("	       String serviceName = getServiceName(clz.getName(), method.getName());\n");
		clzSb.append("	       RpcRequest rpcRequest = new RpcRequest();\n");
		clzSb.append("	       rpcRequest.setId(requestId);\n");
		clzSb.append("	       rpcRequest.setCommand(serviceName);\n");
		clzSb.append("	       rpcRequest.setCallHost(targetHost);\n");
		clzSb.append("	       rpcRequest.setCallPort(targetPort);\n");
		clzSb.append("	       rpcRequest.setParams(args);\n");
		clzSb.append("	       rpcRequest.setAsyCallback(asyCallback);\n");
		clzSb.append("	       rpcClient.execute(rpcRequest);\n");
		clzSb.append("	}\n");
		clzSb.append("}\n");
		return clzSb.toString();
	}

	static class ProxyClassLoad extends ClassLoader {

		private final Map<String, JavaFileObject> classes = new HashMap<String, JavaFileObject>();

		public ProxyClassLoad(ClassLoader parentClassLoader) {
			super(parentClassLoader);
		}

		@Override
		protected Class<?> findClass(final String className) throws ClassNotFoundException {
			JavaFileObject file = classes.get(className);
			if (null != file) {
				byte[] bytes = ((StringJavaObject) file).getByteCode();
				return defineClass(className, bytes, 0, bytes.length);
			} else {
				return null;
			}
		}

		Collection<JavaFileObject> files() {
			return Collections.unmodifiableCollection(classes.values());
		}

		void add(final String qualifiedClassName, final JavaFileObject javaFile) {
			classes.put(qualifiedClassName, javaFile);
		}

	}

	static class StringJavaObject extends SimpleJavaFileObject {

		// 源代码
		private CharSequence content;
		private ByteArrayOutputStream bytecode;

		// 遵循Java规范的类名及文件
		public StringJavaObject(String javaFileName, CharSequence content) {
			super(ClassUtils.toURI(javaFileName + ClassUtils.JAVA_EXTENSION), Kind.SOURCE);
			this.content = content;
		}

		public StringJavaObject(URI uri, Kind kind) {
			super(uri, kind);
			content = null;
		}

		public StringJavaObject(final String name, final Kind kind) {
			super(ClassUtils.toURI(name), kind);
			content = null;
		}

		// 文本文件代码
		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return content;
		}

		@Override
		public InputStream openInputStream() {
			return new ByteArrayInputStream(getByteCode());
		}

		@Override
		public OutputStream openOutputStream() {
			return bytecode = new ByteArrayOutputStream();
		}

		public byte[] getByteCode() {
			return bytecode.toByteArray();
		}

	}

	@SuppressWarnings("unused")
	private static final class JavaFileManagerImpl extends ForwardingJavaFileManager<JavaFileManager> {

		private final ProxyClassLoad classLoader;

		private final Map<URI, JavaFileObject> fileObjects = new HashMap<URI, JavaFileObject>();

		public JavaFileManagerImpl(JavaFileManager fileManager, ProxyClassLoad classLoader) {
			super(fileManager);
			this.classLoader = classLoader;
		}

		@Override
		public FileObject getFileForInput(Location location, String packageName, String relativeName)
				throws IOException {
			FileObject o = fileObjects.get(uri(location, packageName, relativeName));
			if (o != null)
				return o;
			return super.getFileForInput(location, packageName, relativeName);
		}

		public void putFileForInput(StandardLocation location, String packageName, String relativeName,
				JavaFileObject file) {
			fileObjects.put(uri(location, packageName, relativeName), file);
		}

		private URI uri(Location location, String packageName, String relativeName) {
			return ClassUtils.toURI(location.getName() + '/' + packageName + '/' + relativeName);
		}

		@Override
		public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName, Kind kind,
				FileObject outputFile) throws IOException {
			JavaFileObject file = new StringJavaObject(qualifiedName, kind);
			classLoader.add(qualifiedName, file);
			return file;
		}

		@Override
		public ClassLoader getClassLoader(JavaFileManager.Location location) {
			return classLoader;
		}

		@Override
		public String inferBinaryName(Location loc, JavaFileObject file) {
			if (file instanceof StringJavaObject)
				return file.getName();
			return super.inferBinaryName(loc, file);
		}

		@Override
		public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
				throws IOException {
			Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);

			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			List<URL> urlList = new ArrayList<URL>();
			Enumeration<URL> e = contextClassLoader.getResources("com");
			while (e.hasMoreElements()) {
				urlList.add(e.nextElement());
			}

			ArrayList<JavaFileObject> files = new ArrayList<JavaFileObject>();

			if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
				for (JavaFileObject file : fileObjects.values()) {
					if (file.getKind() == Kind.CLASS && file.getName().startsWith(packageName)) {
						files.add(file);
					}
				}

				files.addAll(classLoader.files());
			} else if (location == StandardLocation.SOURCE_PATH && kinds.contains(JavaFileObject.Kind.SOURCE)) {
				for (JavaFileObject file : fileObjects.values()) {
					if (file.getKind() == Kind.SOURCE && file.getName().startsWith(packageName)) {
						files.add(file);
					}
				}
			}

			for (JavaFileObject file : result) {
				files.add(file);
			}

			return files;
		}
	}

	

}
