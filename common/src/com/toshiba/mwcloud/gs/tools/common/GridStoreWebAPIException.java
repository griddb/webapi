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

public class GridStoreWebAPIException extends Exception {
	private int errorCode = CODE_API_OTHER_ERROR;
	private int httpStatus;
	private int errorStatus;
	private JsonNode details;

	public static int CODE_API_PARAM_ERROR			= 10;
	public static int CODE_API_AUTH_ERROR			= 11;
	public static int CODE_API_OTHER_ERROR			= 12;
	public static int CODE_API_CONNECT_ERROR	= 13;
	public static int CODE_API_CONNECT_OTHER_ERROR		= 14;


	public GridStoreWebAPIException() {
	}

	public GridStoreWebAPIException(String message) {
		super(message);
	}
	public GridStoreWebAPIException(int errorCode, String message) {
		this(message);
		this.errorCode = errorCode;
	}
	public GridStoreWebAPIException(int errorCode, String message, int httpStatus) {
		this(message);
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
	}

	public GridStoreWebAPIException(Throwable cause) {
		super(cause);
	}
	public GridStoreWebAPIException(int errorCode, Throwable cause) {
		this(cause);
		this.errorCode = errorCode;
	}

	public GridStoreWebAPIException(String message, Throwable cause) {
		super(message, cause);
	}
	public GridStoreWebAPIException(int errorCode, String message, Throwable cause) {
		this(message, cause);
		this.errorCode = errorCode;
	}
	public GridStoreWebAPIException(int errorCode, String message, String details) {
		this(message);
		this.errorCode = errorCode;
	}

	public GridStoreWebAPIException(int errorCode, String message, int httpStatus, int errorStatus, JsonNode details) {
		super(message);
		this.errorCode = errorCode;
		this.httpStatus = -1;
		this.errorStatus = -1;
		this.details = details;
	}

	public int getHttpStatus() {
		return httpStatus;
	}

	public int getErrorStatus() {
		return errorStatus;
	}

	public JsonNode getDetails() {
		return details;
	}

	public int getErrorCode() {
		return errorCode;
	}
}
