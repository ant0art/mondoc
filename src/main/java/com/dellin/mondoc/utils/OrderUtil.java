package com.dellin.mondoc.utils;

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
}
