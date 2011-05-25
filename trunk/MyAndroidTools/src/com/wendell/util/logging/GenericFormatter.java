package com.wendell.util.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.wendell.util.DateUtilities;

public class GenericFormatter extends Formatter {
	
	@Override
	public String format(LogRecord r) {
		// TODO Auto-generated method stub
		return "[".concat(DateUtilities.getFormatDate("yyyy-MM-dd HH:mm:ss"))
		       .concat("][")
		       .concat(r.getLevel().getLocalizedName())
		       .concat("]")
		       .concat(r.getClass().getName())
		       .concat(":")
		       .concat(r.getMessage())
		       .concat("\n");
	}
	
}
