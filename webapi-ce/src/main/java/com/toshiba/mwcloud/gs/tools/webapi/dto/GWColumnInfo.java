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

import java.util.Set;

import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.IndexType;

public class GWColumnInfo {

	/**
	 * Name of column
	 */
	private String name;
	
	/**
	 * Type of column
	 */
	private GSType type;
	
	/**
	 * A {@link Set} of {@link IndexType}
	 */
	private Set<IndexType> index;

	/**
	 * Get name of column
	 * 
	 * @return name of {@link GWColumnInfo}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set name for {@link GWColumnInfo}
	 * 
	 * @param name name of the column
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get type of column
	 * 
	 * @return type of column
	 */
	public GSType getType() {
		return type;
	}

	/**
	 * Set type for column
	 * 
	 * @param type type of column
	 */
	public void setType(GSType type) {
		this.type = type;
	}

	/**
	 * Get a {@link Set} of {@link IndexType} of column
	 * 
	 * @return a {@link Set} of {@link IndexType} of column
	 */
	public Set<IndexType> getIndex() {
		return index;
	}

	/**
	 * Set index types for column
	 * 
	 * @param index a {@link Set} of {@link IndexType} of column
	 */
	public void setIndex(Set<IndexType> index) {
		this.index = index;
	}

	
	
}
