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

import java.util.List;
import java.util.regex.Pattern;

import com.toshiba.mwcloud.gs.tools.webapi.dto.GWColumnInfo;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWContainerInfo;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWQueryParams;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWSQLInput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWTQLInput;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWBadRequestException;

public class Validation {

	public static Pattern userPattern = Pattern.compile("^[A-Z0-9_.\\-/=][A-Z0-9_.\\-/=#]*$", Pattern.CASE_INSENSITIVE);

	public static Pattern clusterPattern = Pattern.compile("^[A-Z0-9_]+$", Pattern.CASE_INSENSITIVE);

	public static Pattern databasePattern = Pattern.compile("^[A-Z0-9_]+$", Pattern.CASE_INSENSITIVE);

	public static Pattern containerPattern = Pattern.compile("^[A-Z0-9_]+(@[A-Z0-9_]+)?$", Pattern.CASE_INSENSITIVE);

	public static Pattern doubleZeroPattern = Pattern.compile("^[-0.DE]+$", Pattern.CASE_INSENSITIVE);

	public static Pattern columnNamePattern = Pattern.compile("^[A-Z0-9_]+$", Pattern.CASE_INSENSITIVE);

	/**
	 * Validate a {@link GWSQLInput}
	 * 
	 * @param tqlInput
	 *            a {@link GWSQLInput} object
	 */
	public static void validateGWTQLInput(GWTQLInput tqlInput) {

		String container = tqlInput.getName();
		if (container == null || container.trim().isEmpty()) {
			throw new GWBadRequestException("Container invalid");
		}
		String statement = tqlInput.getStmt();

		if (statement == null || statement.trim().isEmpty()) {
			throw new GWBadRequestException("Statement invalid");
		}

		if (containsNotAllowedCharacter(statement)) {
			throw new GWBadRequestException("Invalid character(s) found");
		}

		// Check if statement contains 'EXPLAIN' ('ANALYZE' if existed always
		// comes behind 'EXPLAIN')
		if (statement.trim().length() > 6) {
			String explain = statement.substring(0, 7);
			if (explain.equalsIgnoreCase("EXPLAIN")) {
				throw new GWBadRequestException("'EXPLAIN/ANALYZE' is not supported");
			}
		}
	}

	/**
	 * Validate parameters for getting rows
	 * 
	 * @param gwQueryParams
	 *            a {@link GWQueryParams} object
	 */
	public static void validateInputParams(GWQueryParams gwQueryParams) {

		if (null == gwQueryParams) {
			throw new GWBadRequestException("Query parameter is null");
		}

		if (gwQueryParams.getLimit() <= 0) {
			throw new GWBadRequestException("'limit' is invalid");
		}

		if (gwQueryParams.getOffset() < 0) {
			throw new GWBadRequestException("'offset' is invalid");
		}

		if ((null != gwQueryParams.getCondition() && containsNotAllowedCharacter(gwQueryParams.getCondition()))
				|| (null != gwQueryParams.getSort() && containsNotAllowedCharacter(gwQueryParams.getSort()))) {
			throw new GWBadRequestException("Invalid character(s) found");
		}
	}

	/**
	 * Validate data for putting rows
	 * 
	 * @param input
	 *            a {@link List} of {@link List} of {@link Object}
	 */
	public static void validatePutRowsInput(List<List<Object>> input) {

		if (null == input || input.size() == 0) {
			throw new GWBadRequestException("Rows data is empty");
		}
	}

	/**
	 * Validate container before creating
	 * 
	 * @param gwContainerInfo
	 *            a {@link GWContainerInfo} object
	 */
	public static void validateGWContainerInfo(GWContainerInfo gwContainerInfo) {
		String containerName = gwContainerInfo.getContainer_name();
		if ((containerName == null) || (containerName.length() == 0)) {
			throw new GWBadRequestException("Container name is invalid");
		}
		if (gwContainerInfo.getContainer_type() == null) {
			throw new GWBadRequestException("'container_type' is required");
		}
		List<GWColumnInfo> listColumns = gwContainerInfo.getColumns();
		if (listColumns == null || listColumns.size() == 0) {
			throw new GWBadRequestException("'columns' is required");
		}
	}

	/**
	 * Check if a string contains not-allowed characters
	 * 
	 * @param s
	 *            a {@link String} to be checked
	 * @return true if it does, else false
	 */
	public static boolean containsNotAllowedCharacter(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Validate a {@link GWSQLInput}
	 *
	 * @param sqlInput
	 *            a {@link GWSQLInput} object
	 */
	public static void validateGWSQLInput(GWSQLInput sqlInput) {

		if (sqlInput == null) {
			throw new GWBadRequestException("SQL input is invalid");
		}

		if (null == sqlInput.getType() || !"sql-select".equals(sqlInput.getType())) {
			throw new GWBadRequestException("Type of query is invalid");
		}

		if (null == sqlInput.getStmt() || sqlInput.getStmt().trim().length() == 0) {
			throw new GWBadRequestException("Statement is invalid");
		}

		if (containsNotAllowedCharacter(sqlInput.getStmt())) {
			throw new GWBadRequestException("Invalid character(s) found");
		}
	}
}
