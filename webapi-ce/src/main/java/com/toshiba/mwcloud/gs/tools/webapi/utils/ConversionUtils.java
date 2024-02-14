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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.toshiba.mwcloud.gs.ColumnInfo;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.IndexType;
import com.toshiba.mwcloud.gs.TimeUnit;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWColumnInfo;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWContainerInfo;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWBadRequestException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWException;

public class ConversionUtils {

	public static Pattern doubleZeroPattern = Pattern.compile("^[-0.DE]+$", Pattern.CASE_INSENSITIVE);

	/**
	 * Convert an object to type {@link Boolean}
	 * 
	 * @param value
	 *            an {@link Object}
	 * @return a {@link Boolean} value
	 * @throws GWException
	 *             unable to convert
	 */
	public static boolean convertToBoolean(Object value) throws GWException {
		if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();

		} else if (value instanceof String) {
			return Boolean.parseBoolean((String) value);

		} else {
			throw new GWException("The specified data cannot be converted to BOOL type.");
		}
	}

	/**
	 * Convert an object to {@link String}
	 * 
	 * @param value
	 *            an {@link Object}
	 * @return a {@link String}
	 * @throws GWException
	 *             unable to convert
	 */
	public static String convertToString(Object value) throws GWException {
		if (value instanceof String) {
			return (String) value;

		} else if ((value instanceof Integer) || (value instanceof Long) || (value instanceof BigInteger)
				|| (value instanceof Float)) {
			return String.valueOf(value);

		} else if (value instanceof BigInteger) {
			return String.valueOf(value);

		} else if (value instanceof Double) {
			if (((Double) value == Double.NEGATIVE_INFINITY) || ((Double) value == Double.POSITIVE_INFINITY)) {
				throw new GWException("The specified data cannot be converted to STRING type.");
			}
			return String.valueOf(value);

		} else {
			throw new GWException("The specified data cannot be converted to STRING type.");
		}
	}

	/**
	 * Convert an object to {@link Byte}
	 * 
	 * @param value
	 *            an {@link Object}
	 * @return a {@link Byte}
	 * @throws GWException
	 *             unable to convert
	 */
	public static byte convertToByte(Object value) throws GWException {
		if (value instanceof String) {
			try {
				return Byte.parseByte((String) value);
			} catch (Exception e) {
				throw new GWException("The specified data cannot be converted to BYTE type.");
			}

		} else if (value instanceof BigInteger) {
			int intValue = ((BigInteger) value).intValue();
			if (intValue < Byte.MIN_VALUE) {
				throw new GWException("The specified data cannot be converted to BYTE type. (lower limit over)");
			} else if (intValue > Byte.MAX_VALUE) {
				throw new GWException("The specified data cannot be converted to BYTE type. (upper limit over)");
			}
			return Byte.parseByte(String.valueOf(intValue));

		} else {
			try {
				return Byte.parseByte(String.valueOf(value));
			} catch (Exception e) {
				throw new GWException("The specified data cannot be converted to BYTE type.");
			}
		}
	}

	/**
	 * Convert an object to {@link Short}
	 * 
	 * @param value
	 *            an {@link Object}
	 * @return a {@link Short}
	 * @throws GWException
	 *             unable to convert
	 */
	public static short convertToShort(Object value) throws GWException {
		if (value instanceof String) {
			try {
				return Short.parseShort((String) value);
			} catch (Exception e) {
				throw new GWException("The specified data cannot be converted to SHORT type.");
			}

		} else if (value instanceof BigInteger) {
			int intValue = ((BigInteger) value).intValue();
			if (intValue < Short.MIN_VALUE)
				throw new GWException("The specified data cannot be converted to SHORT type. (lower limit over)");
			if (intValue > Short.MAX_VALUE)
				throw new GWException("The specified data cannot be converted to SHORT type. (upper limit over)");

			return Short.parseShort(String.valueOf(intValue));

		} else {
			try {
				return Short.parseShort(String.valueOf(value));
			} catch (Exception e) {
				throw new GWException("The specified data cannot be converted to SHORT type.");
			}
		}
	}

	/**
	 * Convert an object to {@link Integer}
	 * 
	 * @param value
	 *            an {@link Object}
	 * @return a {@link Integer}
	 * @throws GWException
	 *             unable to convert
	 */
	public static int convertToInt(Object value) throws GWException {
		if (value instanceof String) {
			try {
				return Integer.parseInt((String) value);
			} catch (Exception e) {
				throw new GWException("The specified data cannot be converted to INTEGER type.");
			}

		} else if (value instanceof BigInteger) {
			long longValue = ((BigInteger) value).longValue();
			if (longValue < Integer.MIN_VALUE)
				throw new GWException("The specified data cannot be converted to INTEGER type. (lower limit over)");
			if (longValue > Integer.MAX_VALUE)
				throw new GWException("The specified data cannot be converted to INTEGER type. (upper limit over)");

			return Integer.parseInt(String.valueOf(value));

		} else if (value instanceof Integer) {
			return Integer.parseInt(String.valueOf(value));
		} else {
			try {
				return Integer.parseInt(String.valueOf(value));
			} catch (Exception e) {
				throw new GWException("The specified data cannot be converted to INTEGER type.");
			}
		}
	}

	/**
	 * Convert an object to {@link Long}
	 * 
	 * @param value
	 *            an {@link Object}
	 * @return a {@link Long}
	 * @throws GWException
	 *             unable to convert
	 */
	public static long convertToLong(Object value) throws GWException {
		if (value instanceof String) {
			try {
				return Long.parseLong((String) value);
			} catch (Exception e) {
				throw new GWException("The specified data cannot be converted to LONG type.");
			}

		} else if ((value instanceof BigInteger) || (value instanceof Long)) {
			return Long.parseLong(String.valueOf(value));

		} else if ((value instanceof Integer)) {
			return Long.parseLong(String.valueOf(value));
		} else {
			try {
				return Long.parseLong(String.valueOf(value));
			} catch (Exception e) {
				throw new GWException("The specified data cannot be converted to LONG type.");
			}
		}
	}

	/**
	 * Convert an object to {@link Float}
	 * 
	 * @param value
	 *            an {@link Object}
	 * @return a {@link Float}
	 * @throws GWException
	 *             unable to convert
	 */
	public static float convertToFloat(Object value) throws GWException {

		if (value instanceof String) {
			try {
				float f = Float.parseFloat((String) value);
				double d = Double.parseDouble((String) value);

				if (Float.isInfinite(f)) {
					throw new GWException("The specified data cannot be converted to FLOAT type.");
				}
				if ((f == 0F) && (d != 0D)) {
					throw new GWException("The specified data cannot be converted to FLOAT type.");
				}
				return f;
			} catch (Exception e) {
				throw new GWException("The specified data cannot be converted to FLOAT type.");
			}
		} else if ((value instanceof Integer) || (value instanceof Long) || (value instanceof Float)) {
			return Float.parseFloat(String.valueOf(value));

		} else if (value instanceof BigDecimal || value instanceof BigInteger) {
			String a = null;
			double d = 0D;
			long l = 0L;
			if (value instanceof BigDecimal) {
				d = ((BigDecimal) value).doubleValue();
				a = String.valueOf(d);
			} else if (value instanceof BigInteger) {
				l = ((BigInteger) value).longValue();
				a = String.valueOf(l);
			}
			float f = Float.parseFloat(a);
			if (Float.isInfinite(f)) {
				throw new GWException("The specified data cannot be converted to FLOAT type. (upper limit over)");
			}
			if ((f == 0F) && ((d != 0D) || (l != 0L))) {
				throw new GWException("The specified data cannot be converted to FLOAT type. (lower limit over)");
			}
			return f;
		} else {
			try {
				return Float.parseFloat(String.valueOf(value));
			} catch (Exception e) {
				throw new GWException("The specified data cannot be converted to FLOAT type.");
			}
		}
	}

	/**
	 * Convert an object to {@link Double}
	 * 
	 * @param value
	 *            an {@link Object}
	 * @return a {@link Double}
	 * @throws GWException
	 *             unable to convert
	 */
	public static double convertToDouble(Object value) throws GWException {

		if (value instanceof String) {
			try {
				double d = Double.parseDouble((String) value);
				if (Double.isInfinite(d)) {
					throw new GWException("The specified data cannot be converted to DOUBLE type.");
				}
				if (d == 0) {
					Matcher m1 = doubleZeroPattern.matcher((String) value);
					if (!m1.find()) {
						throw new GWException("The specified data cannot be converted to DOUBLE type.");
					}
				}
				return d;
			} catch (Exception e) {
				throw new GWException("The specified data cannot be converted to DOUBLE type.");
			}

		} else if ((value instanceof Integer) || (value instanceof Long) || (value instanceof Float)) {
			return Double.parseDouble(String.valueOf(value));

		} else if (value instanceof BigDecimal) {
			BigDecimal bigDec = (BigDecimal) value;
			double doubleValue = bigDec.doubleValue();
			if ((doubleValue == Double.NEGATIVE_INFINITY)) {
				throw new GWException("The specified data cannot be converted to DOUBLE type. (lower limit over)");
			} else if ((doubleValue == Double.POSITIVE_INFINITY)) {
				throw new GWException("The specified data cannot be converted to DOUBLE type. (upper limit over)");
			}

			if (bigDec.compareTo(BigDecimal.valueOf(doubleValue)) != 0) {
				throw new GWException("The specified data cannot be converted to DOUBLE type.");
			}

			return Double.parseDouble(String.valueOf(value));

		} else {
			try {
				return Double.parseDouble(String.valueOf(value));
			} catch (Exception e) {
				throw new GWException("The specified data cannot be converted to DOUBLE type.");
			}
		}
	}

	/**
	 * Convert a {@link ContainerInfo} to {@link GWContainerInfo}
	 * 
	 * @param containerInfo
	 *            a {@link ContainerInfo} object
	 * @return a {@link GWContainerInfo} object
	 */
	public static GWContainerInfo convertToGWContainerInfo(ContainerInfo containerInfo) {

		GWContainerInfo gwContainerInfo = new GWContainerInfo();
		gwContainerInfo.setContainer_name(containerInfo.getName());
		gwContainerInfo.setContainer_type(containerInfo.getType());
		gwContainerInfo.setRowkey(containerInfo.isRowKeyAssigned());
		List<GWColumnInfo> listColumns = new ArrayList<>();
		int columnCount = containerInfo.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			GWColumnInfo column = new GWColumnInfo();
			column.setName(containerInfo.getColumnInfo(i).getName());
			column.setType(containerInfo.getColumnInfo(i).getType());
			column.setIndex(containerInfo.getColumnInfo(i).getIndexTypes());
			column.setTimePrecision(containerInfo.getColumnInfo(i).getTimePrecision());
			listColumns.add(column);
		}
		gwContainerInfo.setColumns(listColumns);
		return gwContainerInfo;
	}

	/**
	 * Convert a {@link GWContainerInfo} to {@link ContainerInfo}
	 * 
	 * @param gwContainerInfo
	 *            a {@link GWContainerInfo} object
	 * @return a {@link ContainerInfo} object
	 */
	public static ContainerInfo convertToContainerInfo(GWContainerInfo gwContainerInfo) {

		ContainerInfo containerInfo = new ContainerInfo();
		containerInfo.setName(gwContainerInfo.getContainer_name());
		containerInfo.setRowKeyAssigned(gwContainerInfo.isRowkey());
		containerInfo.setType(gwContainerInfo.getContainer_type());
		List<GWColumnInfo> listGWColumns = gwContainerInfo.getColumns();
		List<ColumnInfo> listColumns = new ArrayList<>();
		for (int i = 0; i < listGWColumns.size(); i++) {
			String name = listGWColumns.get(i).getName();
			GSType type = listGWColumns.get(i).getType();
			if (type == null) {
				throw new GWBadRequestException("Empty column type");
			}
			Set<IndexType> indexTypes = listGWColumns.get(i).getIndex();
			ColumnInfo column = new ColumnInfo(name, type, indexTypes);
			if (type == GSType.TIMESTAMP) {
				TimeUnit precision = listGWColumns.get(i).getTimePrecision();
				if (precision == TimeUnit.MICROSECOND || precision == TimeUnit.NANOSECOND) {
					ColumnInfo ci = column;
					ColumnInfo.Builder builder = new ColumnInfo.Builder(ci);
					builder.setTimePrecision(precision);
					ColumnInfo tmp = builder.toInfo();
					column = tmp;
				}
			}
			listColumns.add(column);
		}
		containerInfo.setColumnInfoList(listColumns);
		return containerInfo;
	}

	/**
	 * Check if a {@link List} contains a specified string ignoring case
	 * sensitive. If it does, return the index of that string else return -1
	 * 
	 * @param list
	 *            the list that needs to check
	 * @param target
	 *            target string
	 * @return position of the target string
	 */
	public static int checkContains(List<String> list, String target) {
		for (String string : list) {
			if (string.equalsIgnoreCase(target)) {
				return list.indexOf(string);
			}
		}
		return -1;
	}

	/**
	 * Pattern to find 'LIMIT' and 'OFFSET' value in a TQL
	 */
	private static final String TQL_FIND_LIMIT_OFFSET_VALUE_REGEX = ".*LIMIT\\s+([0-9]+)\\s*(?:\\sOFFSET\\s+([0-9]+))?\\s*$";

	/**
	 * Get Limit And Offset From Stmt.
	 * 
	 * @param statement the statement sql
	 * @return list 
	 */
	public static List<Integer> getLimitAndOffsetFromStmt(String statement) {
		Pattern stmtPattern = Pattern.compile(TQL_FIND_LIMIT_OFFSET_VALUE_REGEX, Pattern.CASE_INSENSITIVE);
		List<Integer> result = new ArrayList<>();
		Integer limit = null;
		Integer offset = null;
		Matcher matcher = stmtPattern.matcher(statement);
		if (matcher.find()) {
			try {
				limit = Integer.valueOf(matcher.group(1));
				try {
					offset = Integer.valueOf(matcher.group(2));
				} catch (IndexOutOfBoundsException e) {
					System.out.println("'offset' is null");
				}
			} catch (NumberFormatException ex) {
				System.out.println("'offset' or/and 'limit' is invalid");
			}
		}
		result.add(limit);
		result.add(offset);
		return result;
	}

	/**
	 * Change "limit" of a statement if it is over MAX_LIMIT
	 * 
	 * @param statement
	 *            the statement that needs to modify
	 * @param newLimitValue
	 *            new limit value
	 * @return new statement
	 */
	public static String modifyLimitofStatement(String statement, int newLimitValue) {
		String newStatement = replaceGroup(TQL_FIND_LIMIT_OFFSET_VALUE_REGEX, statement, 1,
				Integer.toString(newLimitValue));
		return newStatement;
	}

	/**
	 * TQL type
	 */
	public enum TQLStatementType {
		SELECT_ALL,
		AGGREGATION,
		TIME_SERIES_INTERPOLATION,
		UNKNOW
	}

	private final static String TQL_SELECT_ALL_TYPE_CHECK_REGEX = "^\\s*(select\\s*\\*)";
	private final static String TQL_SELECT_AGGREGATION_TYPE_CHECK_REGEX = "^\\s*(select\\s+(?:MAX\\([^()]+\\)|MIN\\([^()]+\\)|COUNT\\([^()]+\\)|SUM\\([^()]+\\)|AVG\\([^()]+\\)|VARIANCE\\([^()]+\\)|STDDEV\\([^()]+\\)|TIME_AVG\\([^()]+\\)))";
	private final static String TQL_SELECT_TIME_SERIES_INTERPOLATION_TYPE_CHECK_REGEX = "^\\s*select\\s+(?:TIME_NEXT|TIME_NEXT_ONLY|TIME_PREV|TIME_PREV_ONLY|TIME_INTERPOLATED|TIME_SAMPLING)\\(";

	/**
	 * Check TQL type
	 * 
	 * @param statement
	 *            the TQL statement
	 * @return type of TQL
	 */
	public static TQLStatementType checkStatementType(String statement) {
		if (isTQLSelectAllStmt(statement)) {
			return TQLStatementType.SELECT_ALL;
		} else if (isTQLAggregationStmt(statement)) {
			return TQLStatementType.AGGREGATION;
		} else if (isTQLTimeSeriesInterpolationStmt(statement)) {
			return TQLStatementType.TIME_SERIES_INTERPOLATION;
		}
		return TQLStatementType.UNKNOW;
	}

	private static boolean isTQLSelectAllStmt(String statement) {
		Pattern stmtPattern = Pattern.compile(TQL_SELECT_ALL_TYPE_CHECK_REGEX, Pattern.CASE_INSENSITIVE);
		Matcher matcher = stmtPattern.matcher(statement);
		return matcher.find();
	}

	private static boolean isTQLAggregationStmt(String statement) {
		Pattern stmtPattern = Pattern.compile(TQL_SELECT_AGGREGATION_TYPE_CHECK_REGEX, Pattern.CASE_INSENSITIVE);
		Matcher matcher = stmtPattern.matcher(statement);
		return matcher.find();
	}

	private static boolean isTQLTimeSeriesInterpolationStmt(String statement) {
		Pattern stmtPattern = Pattern.compile(TQL_SELECT_TIME_SERIES_INTERPOLATION_TYPE_CHECK_REGEX,
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = stmtPattern.matcher(statement);
		return matcher.find();
	}

	/**
	 * Pattern to find 'LIMIT' and 'OFFSET' clause in TQL
	 */
	private static final String TQL_FIND_LIMIT_OFFSET_CLAUSE_REGEX = ".*(LIMIT\\s+[0-9]+)\\s*(\\sOFFSET\\s+[0-9]+)?\\s*$";
	private static final String TQL_FIND_OFFSET_CLAUSE_REGEX = "\\s+(OFFSET\\s+[0-9]+)\\s*$";

	/**
	 * Remove off set and limit option from TQL
	 * 
	 * @param statement
	 *            statement that needs to remove 'limit' and 'offset'
	 * @return TQL without option limit and offset
	 */
	public static String removeTQLLimitOffSet(String statement) {
		String newStatement = replaceGroup(TQL_FIND_LIMIT_OFFSET_CLAUSE_REGEX, statement, 1, "");
		newStatement = replaceGroup(TQL_FIND_OFFSET_CLAUSE_REGEX, newStatement, 1, "");
		return newStatement.trim();
	}

	private static final String TQL_REMOVE_ORDERBY_AND_LIMIT_EXIST_REGEX = "[\\s*)'](ORDER\\s+BY([^']*))\\s*$";
	private static final String TQL_REMOVE_LIMIT_REGEX = ".*(LIMIT\\s+[0-9]+\\s*(\\sOFFSET\\s+[0-9]+)?\\s*)$";

	/**
	 * Convert select * TQL to select count(*)
	 * 
	 * @param statement
	 *            the statement that needs to convert to "count(*)" type
	 * @return a new statement type "count(*)"
	 */
	public static String convertToCountTQL(String statement) {
		String trimStatemnt = statement.trim();
		String selectCountStatement = replaceGroup(TQL_SELECT_ALL_TYPE_CHECK_REGEX, trimStatemnt, 1, "select count(*)");
		String noOrderBySelectCountStatement = replaceGroup(TQL_REMOVE_ORDERBY_AND_LIMIT_EXIST_REGEX, selectCountStatement, 1, "");
		String noLimitAndOrderSelectCountStatement = replaceGroup(TQL_REMOVE_LIMIT_REGEX, noOrderBySelectCountStatement, 1, "");

		return noLimitAndOrderSelectCountStatement.trim();
	}

	/**
	 * Replace a group of characters in a string
	 * 
	 * @param regex
	 *            regex of group characters
	 * @param source
	 *            source string
	 * @param groupToReplace
	 *            group to replace
	 * @param replacement
	 *            the replace string
	 * @return a new {@link String} that is replaced
	 */
	public static String replaceGroup(String regex, String source, int groupToReplace, String replacement) {
		return replaceGroup(regex, source, groupToReplace, 1, replacement);
	}

	/**
	 * Replace a group of characters in a string
	 * 
	 * @param regex
	 *            regex of group characters
	 * @param source
	 *            source string
	 * @param groupToReplace
	 *            group to replace
	 * @param groupOccurrence
	 *            group occurrence
	 * @param replacement
	 *            the replace string
	 * @return a new {@link String} that is replaced
	 */
	public static String replaceGroup(String regex, String source, int groupToReplace, int groupOccurrence,
			String replacement) {
		Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(source);
		for (int i = 0; i < groupOccurrence; i++)
			if (!m.find())
				return source;
		return new StringBuilder(source).replace(m.start(groupToReplace), m.end(groupToReplace), replacement)
				.toString();
	}

	/**
	 * Format valid IPv4 address.
	 *
	 * @param ipAddress the ip address
	 * @return the string
	 */
	public static String formatValidIPAddress(String ipAddress) {
		String[] parts = ipAddress.split("\\.");
		String outputIPAddress = "";
		for (String s : parts) {
			int i = Integer.parseInt(s);
			String part = String.valueOf(i) + Constants.DOT_CHARACTER;
			outputIPAddress += part;
		}
		return outputIPAddress.substring(0, outputIPAddress.length() - 1);
	}

}
