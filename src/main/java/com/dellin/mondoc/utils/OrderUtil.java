package com.dellin.mondoc.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;

import java.util.*;

public final class OrderUtil {
	
	private OrderUtil() {
		throw new UnsupportedOperationException();
	}
	
	public static Date getParsedDate(String date) {
		LocalDate dateParse = LocalDate.parse(date);
		return Date.from(dateParse.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
	public static String getFormattedDate(LocalDate date, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		Date from = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
		
		return formatter.format(from);
	}
}
