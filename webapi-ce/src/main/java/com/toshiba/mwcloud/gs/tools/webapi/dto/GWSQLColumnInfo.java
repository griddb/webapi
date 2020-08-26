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

public class GWSQLColumnInfo {

	/**
	 * Name of SQL column
	 */
	private String name;
	
	/**
	 * Type of SQL column
	 */
	private String type;
	
	/**
	 * Constructor for {@link GWColumnInfo}
	 */
	public GWSQLColumnInfo(){}
	
	/**
	 * Constructor for {@link GWColumnInfo}
	 * 
	 * @param name Name of SQL column
	 * @param type Type of SQL column
	 */
	public GWSQLColumnInfo(String name, String type){
		this.name = name;
		this.type = type;
	}

	/**
	 * Get name of SQL column
	 * 
	 * @return name of SQL column
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set name for SQL column
	 * 
	 * @param name name of SQL column
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get type of SQL column
	 * 
	 * @return type of SQL column
	 */
	public String getType() {
		return type;
	}

	/**
	 * Set type for SQL column
	 * 
	 * @param type type of SQL column
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	
}
