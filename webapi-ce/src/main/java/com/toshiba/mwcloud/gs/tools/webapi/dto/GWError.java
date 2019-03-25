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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GWError {

	/**
	 * Version of web API
	 */
	@Value("${version}")
	private String version;
	
	/**
	 * Code of error
	 */
	private int errorCode;
	
	/**
	 * Message of error
	 */
	private String errorMessage;

	/**
	 * Get version of web API
	 * 
	 * @return version of web API
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Set version for web API
	 * 
	 * @param version version of web API
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Get error code 
	 * 
	 * @return error code
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * Set error code
	 * 
	 * @param errorCode error code
	 */
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Get error message
	 * 
	 * @return error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Set error message
	 * 
	 * @param errorMessage error message
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
}
