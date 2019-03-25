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

public class GWContainerListOuput {

	/**
	 * List of container names
	 */
	private List<String> names;
	
	/**
	 * Total container
	 */
	private int total;
	
	/**
	 * Offset of list container names
	 */
	private int offset;
	
	/**
	 * Limit of list container names
	 */
	private int limit;

	/**
	 * Get the list of container names
	 * 
	 * @return a {@link List} of {@link String}
	 */
	public List<String> getNames() {
		return names;
	}

	/**
	 * Set the list of container names
	 * 
	 * @param names a {@link List} of {@link String}
	 */
	public void setNames(List<String> names) {
		this.names = names;
	}

	/**
	 * Get total container names
	 * 
	 * @return total container names
	 */
	public int getTotal() {
		return total;
	}

	/**
	 * Set total container names
	 * 
	 * @param total total container names
	 */
	public void setTotal(int total) {
		this.total = total;
	}

	/**
	 * Get offset of the list of container names
	 * 
	 * @return offset of list of container names
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Set offset of the list of container names
	 * 
	 * @param offset offset of the list of container names
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * Get limitation of the list of container names
	 * 
	 * @return limit of the list of container names
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * Set limitation of the list of container names
	 * 
	 * @param limit limit of the list of container names
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
}
