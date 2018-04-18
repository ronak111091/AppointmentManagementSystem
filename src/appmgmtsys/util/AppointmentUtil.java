package appmgmtsys.util;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class AppointmentUtil {
	
	public static Date getDateObj(String date) {
		try {
			return java.sql.Date.valueOf(date);
		} catch (Exception e) {
			return null;
		}
	}

	public static Time getTimeObj(String time) {
		try {
			DateTimeFormatter strictTimeFormatter = new DateTimeFormatterBuilder().appendPattern("HH:mm")
					.optionalStart().appendPattern(":").appendFraction(ChronoField.SECOND_OF_MINUTE, 1, 2, false)
					.optionalEnd().toFormatter();
			return Time.valueOf(LocalTime.parse(time, strictTimeFormatter));
		} catch (Exception e) {
			System.out.println("Invalid time string: " + time);
			return null;
		}
	}
	
}
