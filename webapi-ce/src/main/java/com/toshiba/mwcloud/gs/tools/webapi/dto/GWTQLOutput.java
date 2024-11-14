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

public class GWTQLOutput {

	/**
	 * List of column information
	 */
	private List<GWTQLColumnInfo> columns;
	
	/**
	 * List of rows
	 */
	private List<List<Object>> results;

	/**
	 * Offset of the result of query
	 */
	private int offset;
	
	/**
	 * Limit of the query
	 */
	private int limit;
	
	/**
	 * Total rows of the container
	 */
	private long total;
	
	/** Size of the result (byte). */
	private long responseSizeByte;

	/**
	 * Get list of column information
	 * 
	 * @return a {@link List} of {@link GWTQLColumnInfo}
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

	/**
	 * Get offset of the query
	 * 
	 * @return offset of the query
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Set offset of the query
	 * 
	 * @param offset offset of the query
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * Get limit of the query
	 * 
	 * @return limit of the query
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * Set limit of the query
	 * 
	 * @param limit limit of the query
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * Get total rows of the container
	 * 
	 * @return total rows of the container
	 */
	public long getTotal() {
		return total;
	}

	/**
	 * Set total rows of the container
	 * 
	 * @param total total rows of the container
	 */
	public void setTotal(long total) {
		this.total = total;
	}

	/**
	 * Get size of the result.
	 *
	 * @return size of the result
	 */
	public long getResponseSizeByte() {
		return responseSizeByte;
	}

	/**
	 * Set size of the result.
	 *
	 * @param size of the result
	 */
	public void setResponseSizeByte(long size) {
		this.responseSizeByte = size;
	}
	
	
}
