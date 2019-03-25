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

package com.toshiba.mwcloud.gs.tools.common;

import com.fasterxml.jackson.databind.JsonNode;

public class GridStoreWebAPINotMasterException extends GridStoreWebAPIException {
	private final NodeKey masterNode;

	public GridStoreWebAPINotMasterException(int errCode, String message, int httpStatus, int errorStatus,
			JsonNode details, NodeKey masterNode) {
		super(errCode, message, httpStatus, errorStatus, details);
		this.masterNode = masterNode;
	}

	public NodeKey getMasterNode() {
		return masterNode;
	}
}
