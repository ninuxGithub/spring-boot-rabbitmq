package com.example.demo;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateUtil {
	
	private static String date_format_pattern ="yyyy-MM-dd";
	
	private static final ThreadLocal<SimpleDateFormat> localDateFormat = new ThreadLocal<SimpleDateFormat>(){

		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat(date_format_pattern);
		}
		
	};
	
	
	public static int dayDiff(String s, String e) throws ParseException {
		long longs = localDateFormat.get().parse(s).getTime();
		long longe = localDateFormat.get().parse(e).getTime();
		int diff=(int) (Math.abs(longs -longe) / (1000 * 60 * 60 * 24));  
		return diff;
	} 
	

}
