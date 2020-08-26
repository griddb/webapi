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

public class GWSQLInput {

	/**
	 * Type of SQL
	 */
	private String type;

	/**
	 * Statement of SQL
	 */
	private String stmt;

	/**
	 * Get type of SQL
	 * 
	 * @return type of SQL
	 */
	public String getType() {
		return type;
	}

	/**
	 * Set type for SQL
	 * 
	 * @param type type of SQL
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Get the statement of SQL
	 * 
	 * @return statement of SQL
	 */
	public String getStmt() {
		return stmt;
	}

	/**
	 * Set the statement for SQL
	 * 
	 * @param stmt statement of SQL
	 */
	public void setStmt(String stmt) {
		this.stmt = stmt;
	}

}
