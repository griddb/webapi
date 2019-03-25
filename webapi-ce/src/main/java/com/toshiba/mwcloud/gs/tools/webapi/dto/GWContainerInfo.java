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

import com.toshiba.mwcloud.gs.ContainerType;

public class GWContainerInfo {

	/**
	 * Name of container
	 */
	private String container_name;
	
	/**
	 * Type of container
	 */
	private ContainerType container_type;
	
	/**
	 * Is row specified
	 */
	private boolean rowkey;
	
	/**
	 * List of {@link GWColumnInfo}
	 */
	private List<GWColumnInfo> columns;

	/**
	 * Get name of container
	 * 
	 * @return name of container
	 */
	public String getContainer_name() {
		return container_name;
	}

	/**
	 * Set name for container
	 * 
	 * @param container_name name of container
	 */
	public void setContainer_name(String container_name) {
		this.container_name = container_name;
	}

	/**
	 * Get type of container
	 * 
	 * @return type of container
	 */
	public ContainerType getContainer_type() {
		return container_type;
	}

	/**
	 * Set type for container
	 * 
	 * @param container_type type of container
	 */
	public void setContainer_type(ContainerType container_type) {
		this.container_type = container_type;
	}

	/**
	 * Does container has row key ?
	 * 
	 * @return true if container has row key, else false
	 */
	public boolean isRowkey() {
		return rowkey;
	}

	/**
	 * Set row key for container
	 * 
	 * @param rowkey row key of container
	 */
	public void setRowkey(boolean rowkey) {
		this.rowkey = rowkey;
	}

	/**
	 * Get a list of column information of container
	 * 
	 * @return a {@link List} of {@link GWColumnInfo}
	 */
	public List<GWColumnInfo> getColumns() {
		return columns;
	}

	/**
	 * Set a list of column information for container
	 * 
	 * @param columns a {@link List} of {@link GWColumnInfo}
	 */
	public void setColumns(List<GWColumnInfo> columns) {
		this.columns = columns;
	}
	
}
