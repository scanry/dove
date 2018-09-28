package com.six.dove.remote;

import java.util.Objects;

import com.six.dove.common.AbstractService;
import com.six.dove.remote.compiler.Compiler;
import com.six.dove.remote.compiler.JavaCompilerImpl;
import com.six.dove.transport.TransportCodec;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月10日 上午11:20:06
 * @describe 抽象远程调用端类，
 */
public abstract class AbstractRemote extends AbstractService implements Remote {

	private Compiler compiler;
	private TransportCodec transportCodec;

	public AbstractRemote(String name, TransportCodec transportCodec) {
		this(name, newDefaultCompiler(), transportCodec);
	}

	public AbstractRemote(String name, Compiler compiler, TransportCodec transportCodec) {
		super(name);
		Objects.requireNonNull(compiler);
		Objects.requireNonNull(transportCodec);
		this.compiler = compiler;
		this.transportCodec = transportCodec;
	}

	/**
	 * 提供默认编译器
	 * @return
	 */
	public static Compiler newDefaultCompiler() {
		return new JavaCompilerImpl();
	}

	@Override
	public final Compiler getCompiler() {
		return compiler;
	}

	@Override
	public final TransportCodec getTransportCodec() {
		return transportCodec;
	}
}
