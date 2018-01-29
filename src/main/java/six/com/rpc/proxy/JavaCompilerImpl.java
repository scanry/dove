package six.com.rpc.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

/**
 * @author sixliu
 * @date 2017年12月28日
 * @email 359852326@qq.com
 * @Description
 */
public class JavaCompilerImpl extends AbstractCompiler {

	private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	private final DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<JavaFileObject>();
	private final JavaFileManagerImpl javaFileManager;
	private ProxyClassLoad proxyClassLoad;

	public JavaCompilerImpl() {
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
	protected Class<?> loadClass(String classfullName) throws Exception {
		return proxyClassLoad.loadClass(classfullName);
	}

	@Override
	protected Class<?> compile(String fullClassName, String code, ClassLoader classLoader) throws Exception {
		String packageName =fullClassName.substring(0,fullClassName.lastIndexOf("."));
		String className = fullClassName.substring(fullClassName.lastIndexOf(".")+1);
		JavaFileObject javaFileObject = new StringJavaObject(className, code);
		javaFileManager.putFileForInput(StandardLocation.SOURCE_PATH, packageName,
				className + ClassUtils.JAVA_EXTENSION, javaFileObject);
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		CompilationTask compilationTask = compiler.getTask(null, javaFileManager, diagnostics, null, null,
				Arrays.asList(javaFileObject));
		Boolean result = compilationTask.call();
		if (result == null || !result.booleanValue()) {
			throw new IllegalStateException("Compilation failed. class: " + fullClassName + ", diagnostics: ");
		}
		return proxyClassLoad.loadClass(fullClassName);
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
