package com.six.dove.common.utils;


/**
 * @author six
 * @date 2016年6月24日 上午10:47:06
 */

public class ObjectCheckUtils {

	private ObjectCheckUtils() {
	}

	/**
	 * 校验 数字num是否处于 有效区间
	 * 
	 * @param num
	 * @param minNum
	 * @param maxNum
	 * @param text
	 * @return
	 */
	public static int checkIntValid(int num, int minNum, int maxNum, String text) {
		if (num < minNum || num > num) {
			throw new IllegalArgumentException(text + "[num] is Illegal");
		}
		return num;
	}

	/**
	 * 检查目标对象是否为null
	 * 
	 * @param arg
	 * @param text
	 * @return
	 */
	public static <T> T checkNotNull(T arg, String text) {
		if (arg == null) {
			throw new NullPointerException(text);
		}
		return arg;
	}

	public static <T> T checkNotNull(T arg) {
		if (arg == null) {
			throw new NullPointerException();
		}
		return arg;
	}

	public static int checkPositive(int i, String name) {
		if (i <= 0) {
			throw new IllegalArgumentException(name + ": " + i + " (expected: > 0)");
		}
		return i;
	}

	public static long checkPositive(long i, String name) {
		if (i <= 0) {
			throw new IllegalArgumentException(name + ": " + i + " (expected: > 0)");
		}
		return i;
	}

	public static int checkPositiveOrZero(int i, String name) {
		if (i < 0) {
			throw new IllegalArgumentException(name + ": " + i + " (expected: >= 0)");
		}
		return i;
	}

	public static <T> T[] checkNonEmpty(T[] array, String name) {
		checkNotNull(array, name);
		checkPositive(array.length, name + ".length");
		return array;
	}


	public static <T> T[] checkNonEmpty(T[] array) {
		checkNotNull(array);
		checkPositive(array.length, "length");
		return array;
	}

	public static byte[] checkNonEmpty(byte[] array) {
		checkNotNull(array);
		checkPositive(array.length, "length");
		return array;
	}

	public static int intValue(Integer wrapper, int defaultValue) {
		return wrapper != null ? wrapper.intValue() : defaultValue;
	}

	public static long longValue(Long wrapper, long defaultValue) {
		return wrapper != null ? wrapper.longValue() : defaultValue;
	}

}
