package com.six.dove.transport.util;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: sixliu
 * @email: 359852326@qq.com
 * @date: 2018年10月11日 下午11:48:26
 * @version V1.0
 * @description:TODO
 */
public abstract class ConstantPool<T extends Constant<T>> {

	private final ConcurrentMap<String, T> constants = new ConcurrentHashMap<String, T>();

	private final AtomicInteger nextId = new AtomicInteger(1);

	/**
	 * Shortcut of {@link #valueOf(String) valueOf(firstNameComponent.getName() +
	 * "#" + secondNameComponent)}.
	 */
	public T valueOf(Class<?> firstNameComponent, String secondNameComponent) {
		if (firstNameComponent == null) {
			throw new NullPointerException("firstNameComponent");
		}
		if (secondNameComponent == null) {
			throw new NullPointerException("secondNameComponent");
		}

		return valueOf(firstNameComponent.getName() + '#' + secondNameComponent);
	}

	/**
	 * Returns the {@link Constant} which is assigned to the specified {@code name}.
	 * If there's no such {@link Constant}, a new one will be created and returned.
	 * Once created, the subsequent calls with the same {@code name} will always
	 * return the previously created one (i.e. singleton.)
	 *
	 * @param name the name of the {@link Constant}
	 */
	public T valueOf(String name) {
		checkNotNullAndNotEmpty(name);
		return getOrCreate(name);
	}

	/**
	 * Get existing constant by name or creates new one if not exists. Threadsafe
	 *
	 * @param name the name of the {@link Constant}
	 */
	private T getOrCreate(String name) {
		T constant = constants.get(name);
		if (constant == null) {
			final T tempConstant = newConstant(nextId(), name);
			constant = constants.putIfAbsent(name, tempConstant);
			if (constant == null) {
				return tempConstant;
			}
		}

		return constant;
	}

	/**
	 * Returns {@code true} if a {@link AttributeKey} exists for the given
	 * {@code name}.
	 */
	public boolean exists(String name) {
		checkNotNullAndNotEmpty(name);
		return constants.containsKey(name);
	}

	/**
	 * Creates a new {@link Constant} for the given {@code name} or fail with an
	 * {@link IllegalArgumentException} if a {@link Constant} for the given
	 * {@code name} exists.
	 */
	public T newInstance(String name) {
		checkNotNullAndNotEmpty(name);
		return createOrThrow(name);
	}

	/**
	 * Creates constant by name or throws exception. Threadsafe
	 *
	 * @param name the name of the {@link Constant}
	 */
	private T createOrThrow(String name) {
		T constant = constants.get(name);
		if (constant == null) {
			final T tempConstant = newConstant(nextId(), name);
			constant = constants.putIfAbsent(name, tempConstant);
			if (constant == null) {
				return tempConstant;
			}
		}

		throw new IllegalArgumentException(String.format("'%s' is already in use", name));
	}

	private static String checkNotNullAndNotEmpty(String name) {
		Objects.requireNonNull(name, "name");
		if (name.isEmpty()) {
			throw new IllegalArgumentException("empty name");
		}

		return name;
	}

	protected abstract T newConstant(int id, String name);

	@Deprecated
	public final int nextId() {
		return nextId.getAndIncrement();
	}

}
