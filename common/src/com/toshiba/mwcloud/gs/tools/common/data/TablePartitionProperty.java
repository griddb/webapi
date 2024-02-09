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

package com.toshiba.mwcloud.gs.tools.common.data;

public class TablePartitionProperty {

	private String type;
	
	private String column;
	
	private int divisionCount;
	
	private String intervalValue;
	
	private String intervalUnit;

	public TablePartitionProperty(String type, String column, int divisionCount) {
		this.type = type;
		this.column = column;
		this.divisionCount = divisionCount;
	}

	public TablePartitionProperty(String type, String column, String intervalValue, String intervalUnit) {
		this.type = type;
		this.column = column;
		this.intervalValue = intervalValue;
		this.intervalUnit = intervalUnit;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public int getDivisionCount() {
		return divisionCount;
	}

	public void setDivisionCount(int divisionCount) {
		this.divisionCount = divisionCount;
	}

	public String getIntervalValue() {
		return intervalValue;
	}

	public void setIntervalValue(String intervalValue) {
		this.intervalValue = intervalValue;
	}

	public String getIntervalUnit() {
		return intervalUnit;
	}

	public void setIntervalUnit(String intervalUnit) {
		this.intervalUnit = intervalUnit;
	}
}
