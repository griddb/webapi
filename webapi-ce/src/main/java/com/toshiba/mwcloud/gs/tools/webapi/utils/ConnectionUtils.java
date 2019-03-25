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

package com.toshiba.mwcloud.gs.tools.webapi.utils;

import java.sql.SQLException;

public class ConnectionUtils {

	/**
	 * Get message from a {@link SQLException}
	 * 
	 * @param e a {@link SQLException}
	 * @return message from {@link SQLException}
	 */
	public static String getMessage(SQLException e) {
		String msg = e.getMessage();
		if (msg != null) {
			int idx = msg.lastIndexOf(" (address=");
			if (idx > 0) {
				msg = msg.substring(0, idx);
			}
		}
		return msg;
	}
}
