package de.qualityminds.gta.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	public static boolean isValidDate(String input, String format) {
		try {
			getDate(input, format);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static Date getDate(String input, String format) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.parse(input);
	}
}
