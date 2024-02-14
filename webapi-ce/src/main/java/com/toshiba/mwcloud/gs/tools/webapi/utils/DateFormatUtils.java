/*
 	Copyright (c) 2019 TOSHIBA Digital Solutions Corporation.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.toshiba.mwcloud.gs.tools.webapi.utils;

import com.toshiba.mwcloud.gs.TimeUnit;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateFormatUtils {

	private static final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Z"));

	private static final ThreadLocal<SimpleDateFormat> format = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				simpleDateFormat.setTimeZone(cal.getTimeZone());
				return simpleDateFormat;
		}
	};

	private DateFormatUtils(){}

	/**
	 * Format a {@link Date}
	 * 
	 * @param date a {@link Date} object
	 * @return a {@link String} represent a {@link Date}
	 */
	public static String format(Date date) {
		return format.get().format(date);
	}

	/**
	 * Parse a {@link Date}
	 * 
	 * @param str a {@link String} that needs to parse
	 * @return a {@link Date}
	 * @throws ParseException when string is invalid
	 */
	public static Date parse(String str) throws ParseException {
		return format.get().parse(str);
	}

	/**
	 * Get the DateTimeFormatter base on time precision
	 *
	 * @param unit the precision time unit of timestamp
	 * @return DateTimeFormatter value
	 */
	public static DateTimeFormatter getDateTimeFormatter(TimeUnit unit) {
		String format = "";
		switch (unit) {
			case MILLISECOND:
				format = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
				break;
			case MICROSECOND:
				format = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX";
				break;
			case NANOSECOND:
				format = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX";
				break;
			default:
				throw new IllegalArgumentException("Invalid time unit. Valid unit is MILLISECOND, MICROSECOND and NANOSECOND.");
		}
		return DateTimeFormatter.ofPattern(format);
	}


}
