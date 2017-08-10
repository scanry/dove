package six.com.rpc.util;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月9日 上午11:23:59
 */
public class ExceptionUtils {

	public static String getExceptionMsg(Exception exception) {
		String msg = null;
		if (null != exception) {
			StringBuilder msgSb = new StringBuilder();
			Throwable throwable = exception;
			while (null != throwable) {
				msgSb.append(throwable.getClass());
				msgSb.append(":");
				msgSb.append(throwable.getMessage());
				msgSb.append("\n");
				StackTraceElement[] stackTraceElements = throwable.getStackTrace();
				if (null != stackTraceElements) {
					for (StackTraceElement stackTraceElement : stackTraceElements) {
						msgSb.append("\t\t\t");
						msgSb.append(stackTraceElement.getClassName());
						msgSb.append(".");
						msgSb.append(stackTraceElement.getMethodName());
						msgSb.append("(");
						msgSb.append(stackTraceElement.getFileName());
						msgSb.append(":");
						msgSb.append(stackTraceElement.getLineNumber());
						msgSb.append(")");
						msgSb.append("\n");
					}
				}
				throwable = throwable.getCause();
			}
			msg = msgSb.toString();
		}
		return msg;
	}

}
