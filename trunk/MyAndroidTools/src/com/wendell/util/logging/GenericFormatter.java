package com.wendell.util.logging;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import com.wendell.util.DateUtilities;
import com.wendell.util.ExceptionUtilities;
import com.wendell.util.StringUtilities;

public class GenericFormatter extends SimpleFormatter {
	
	@Override
	public String format(LogRecord r) {
		// TODO Auto-generated method stub
		String result = "["
				       .concat(DateUtilities.getFormatDate("yyyy-MM-dd HH:mm:ss"))
				       .concat("][")
				       .concat(r.getSourceClassName())
				       .concat(".")
				       .concat(r.getSourceMethodName())
				       .concat("(");
		Object[] params = r.getParameters();
		if(params != null){
			for(int i = 0;i < params.length;i++){
				if(i != 0) result = result.concat(",");
				result = result.concat(StringUtilities.toStringWhenNull(params[i], "null"));
			}
		}
		String levelName = r.getLevel().getLocalizedName();
		result = result.concat(")]")
				       .concat("\n")
				       .concat(levelName)
				       .concat(": ")
				       .concat(r.getMessage())
				       .concat("\n");
		Throwable t = r.getThrown();
		if(t != null) result = result.concat(levelName).concat(": ").concat(ExceptionUtilities.getStackTrace(t));
		result = result.concat("\n");
		return result;
	}
	
}
