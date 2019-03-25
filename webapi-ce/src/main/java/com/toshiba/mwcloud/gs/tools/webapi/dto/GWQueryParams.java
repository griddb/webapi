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

public class GWQueryParams {

	/**
	 * Limitation of the number of rows
	 */
	private int limit;

	/**
	 * Offset of the list of the rows
	 */
	private int offset = 0;

	/**
	 * Condition of getting rows
	 */
	private String condition;

	/**
	 * Type of sorting result of getting rows
	 */
	private String sort;

	/**
	 * Get the limitation of the number of rows
	 * 
	 * @return Limit of the number of rows
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * Set the limitation of the number of rows
	 * 
	 * @param limit
	 *            Limit of the number of rows
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * Get the offset of the list of rows
	 * 
	 * @return Offset of the list of the rows
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Set the offset for the list of rows
	 * 
	 * @param offset
	 *            Offset of the list of the rows
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * Get condition of getting rows
	 * 
	 * @return Condition of getting rows
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Set condition for getting rows
	 * 
	 * @param condition
	 *            Condition of getting rows
	 */
	public void setCondition(String condition) {
		this.condition = condition;
	}

	/**
	 * Get type of sorting result of getting rows
	 * 
	 * @return Type of sorting result of getting rows
	 */
	public String getSort() {
		return sort;
	}

	/**
	 * Set type of sorting result for getting rows
	 * 
	 * @param sort Type of sorting result of getting rows
	 */
	public void setSort(String sort) {
		this.sort = sort;
	}

}
