package com.wendell.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * <p>����Exception�����ʵ�ó�����
 * @author Wendell
 * @version 1.0
 */
public abstract class ExceptionUtilities {

	public static String getStackTrace(Throwable throwable) {
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		throwable.printStackTrace(printWriter);
		return writer.toString();
	}
	
}
