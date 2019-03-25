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

package com.toshiba.mwcloud.gs.tools.webapi.dto;

import java.util.List;

public class GWTQLOutputAggregation {

	/**
	 * List of column information
	 */
	private List<GWTQLColumnInfo> columns;
	
	/**
	 * List of rows
	 */
	private List<List<Object>> results;
	
	/**
	 * Get list of column information
	 * 
	 * @return columns a {@link List} of {@link GWTQLColumnInfo}
	 */
	public List<GWTQLColumnInfo> getColumns() {
		return columns;
	}

	/**
	 * Set the list of column information 
	 * 
	 * @param columns a {@link List} of {@link GWTQLColumnInfo}
	 */
	public void setColumns(List<GWTQLColumnInfo> columns) {
		this.columns = columns;
	}

	/**
	 * Get list of rows
	 * 
	 * @return a {@link List} of {@link List} of {@link Object}
	 */
	public List<List<Object>> getResults() {
		return results;
	}

	/**
	 * Set list of rows
	 * 
	 * @param results a {@link List} of {@link List} of {@link Object}
	 */
	public void setResults(List<List<Object>> results) {
		this.results = results;
	}
}
