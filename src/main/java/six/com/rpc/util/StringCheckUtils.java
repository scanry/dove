package six.com.rpc.util;


/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月12日 上午10:10:05
 */
public class StringCheckUtils {

	/**
	 * 校验string 是否是blank
	 * 
	 * @param str
	 * @param name
	 * @return
	 */
	public static String checkStrBlank(String str) {
		if (StringUtils.isBlank(str)) {
			throw new IllegalArgumentException();
		}
		return str;
	}

	/**
	 * 校验string 是否是blank
	 * 
	 * @param str
	 * @param name
	 * @return
	 */
	public static String checkStrBlank(String str, String name) {
		if (StringUtils.isBlank(str)) {
			throw new IllegalArgumentException(name + " must be not blank");
		}
		return str;
	}
}
