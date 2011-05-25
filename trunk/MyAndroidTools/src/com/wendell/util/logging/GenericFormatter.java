package com.wendell.util.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.wendell.util.DateUtilities;
import com.wendell.util.ExceptionUtilities;

public class GenericFormatter extends Formatter {
	
	@Override
	public String format(LogRecord r) {
		// TODO Auto-generated method stub
		String result = "["
				       .concat(r.getLevel().getLocalizedName())
				       .concat("][")
				       .concat(DateUtilities.getFormatDate("yyyy-MM-dd HH:mm:ss"))
				       .concat("]")
				       .concat(r.getSourceClassName())
				       .concat(":")
				       .concat(r.getMessage());
		Throwable t = r.getThrown();
		if(t != null){
			result = result.concat("\n").concat(ExceptionUtilities.getStackTrace(t));
		}
		result = result.concat("\n");
		return result;
	}
	
}
