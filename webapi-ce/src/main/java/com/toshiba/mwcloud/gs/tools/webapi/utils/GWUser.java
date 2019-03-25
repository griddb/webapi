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

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.toshiba.mwcloud.gs.tools.webapi.exception.GWUnauthorizedException;

public class GWUser {

	private static String base64Regex = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$";

	private static Pattern basicAuthenPattern = Pattern.compile(base64Regex);
	/**
	 * User name of GridDB user
	 */
	private String username;

	/**
	 * Password of GridDB user
	 */
	private String password;

	/**
	 * Get user name of GridDB user
	 * 
	 * @return user name of GridDB user
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set user name for GridDB user
	 * 
	 * @param username
	 *            user name of GridDB user
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Get password of GridDB user
	 * 
	 * @return password of GridDB user
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set password for GridDB user
	 * 
	 * @param password
	 *            of GridDB user
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Get {@link GWUser} from basic authentication.
	 * 
	 * <br>
	 * <br>
	 * <b>Processing flow:</b>
	 * <ol>
	 * <li>Get encoded string from {@code authorization}</li>
	 * <li>Decoded obtained string and split into an array of string(include
	 * user name and password)</li>
	 * <li>Set user name and password into a {@link GWUser} object and return
	 * </li>
	 * </ol>
	 * 
	 * @param authorization
	 *            basic authentication
	 * @return a {@link GWUser} object
	 */
	public static GWUser getUserfromAuthorization(String authorization) {

		if (null == authorization || !authorization.contains("Basic ")) {
			throw new GWUnauthorizedException("Basic authentication not found");
		}
		GWUser user = new GWUser();
		String decodeString = authorization.substring(6);
		Matcher matcher = basicAuthenPattern.matcher(decodeString);
		if (matcher.find()) {
			String usernamepassword = new String(Base64.getDecoder().decode(decodeString));
			if (usernamepassword.contains(":")) {
				String[] arr = usernamepassword.split(":", 2);
				user.setUsername(arr[0]);
				user.setPassword(arr[1]);
			} else {
				throw new GWUnauthorizedException("Basic authentication is invalid format");
			}
		} else {
			throw new GWUnauthorizedException("Basic authentication is not encoded by Base64");
		}
		return user;
	}

}
