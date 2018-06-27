package com.java.nio.examples;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NIO_Selector {
	
	public static void main(String[] args) {
		System.out.println(getHour(1522747566441L));
	}
	
	public static int getHour(long millseconds) {
		Date date = new Date(millseconds);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(millseconds);
		
		System.out.println(c.get(c.YEAR));
		System.out.println(c.get(c.MONTH));
		System.out.println(c.get(c.DAY_OF_MONTH));
		System.out.println(date);
		return c.get(c.HOUR_OF_DAY);
	}

}
