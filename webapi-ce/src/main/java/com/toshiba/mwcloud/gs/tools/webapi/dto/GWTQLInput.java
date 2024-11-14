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

import java.util.ArrayList;
import java.util.List;

public class GWTQLInput {
	
	/**
	 * Name of container
	 */
	private String name;

	/**
	 * Statement of TQL
	 */
	private String stmt;

	/**
	 * List of selected columns
	 */
	private ArrayList<String> columns;

	/** Partial execution option. */
	private Boolean hasPartialExecution;


	/**
	 * Get name of container
	 * 
	 * @return name of container
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set name of container for query
	 * 
	 * @param name name of container
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the statement of TQL
	 * 
	 * @return statement of TQL
	 */
	public String getStmt() {
		return stmt;
	}

	/**
	 * Set the statement of TQL for query
	 * 
	 * @param stmt statement of TQL
	 */
	public void setStmt(String stmt) {
		this.stmt = stmt;
	}

	/**
	 * Get selected columns
	 * 
	 * @return a {@link List} of columns
	 */
	public ArrayList<String> getColumns() {
		return columns;
	}

	/**
	 * Set selected columns
	 * 
	 * @param columns a {@link List} of columns
	 */
	public void setColumns(ArrayList<String> columns) {
		this.columns = columns;
	}

	/**
	 * Get hasPartialExecution of query condition.
	 *
	 * @return a {@link Boolean} of query condition
	 */
	public Boolean getHasPartialExecution() {
		return hasPartialExecution;
	}

	/**
	 * Set hasPartialExecution of query condition.
	 *
	 * @param hasPartialExecution {@link Boolean} of query condition
	 */
	public void setHasPartialExecution(Boolean hasPartialExecution) {
		this.hasPartialExecution = hasPartialExecution;
	}

}
