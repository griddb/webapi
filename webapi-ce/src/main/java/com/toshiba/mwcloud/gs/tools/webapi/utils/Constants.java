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

import org.springframework.stereotype.Component;

@Component
public class Constants {

	public static final String ENCODING = "UTF-8";

	// =================================
	// Maximum length of names
	// =================================
	public static int LENGTH_USER = 64;
	public static int LENGTH_PASSWORD = 64;
	public static int LENGTH_CLUSTER = 64;
	public static int LENGTH_DATABASE = 64;

	// =================================
	// Properties name
	// =================================
	// Upper limitation of row acquisition
	public static final String PROP_MAX_GETROW_SIZE = "maxResponseSize";
	// Upper limitation of row registration
	public static final String PROP_MAX_PUTROW_SIZE = "maxRequestSize";
	// Upper limitation of total response from request that has multiple statements
	public static final String PROP_MAX_TOTAL_RESPONSE_SIZE = "maxTotalResponseSize";

	// System limitation of row acquisition
	public static final String PROP_MAX_SYSTEM_GETROW_SIZE = "maxSystemGetRowSize";
	// System limitation of row registration
	public static final String PROP_MAX_SYSTEM_PUTROW_SIZE = "maxSystemPutRowSize";
	// System limitation of total response from request that has multiple statements
	public static final String PROP_MAX_SYSTEM_TOTAL_RESPONSE_SIZE = "maxSystemTotalResponseSize";

	// System limitation of row acquisition
	public static final String PROP_MIN_SYSTEM_GETROW_SIZE = "minSystemGetRowSize";
	// System limitation of row registration
	public static final String PROP_MIN_SYSTEM_PUTROW_SIZE = "minSystemPutRowSize";
	//System limitation of total response from request that has multiple statements
	public static final String PROP_MIN_SYSTEM_TOTAL_RESPONSE_SIZE = "minSystemTotalResponseSize";

	public static final String PROP_FAILOVER_TIMEOUT = "failoverTimeout";
	public static final String PROP_TRANSACTION_TIMEOUT = "transactionTimeout";
	public static final String PROP_CONTAINER_CACHE_SIZE = "containerCacheSize";
	public static final String PROP_CONSISTENCY = "consistency";

	public static final String PROP_LOGIN_TIMEOUT = "loginTimeout";

	public static final String PROP_MAX_QUERY_NUM = "maxQueryNum";
	public static final String PROP_MAX_LIMIT = "maxLimit";

	public static final String PROP_PORT = "port";

	// Driver name for JDBC
	public static final String DRIVER_NAME = "com.toshiba.mwcloud.gs.sql.Driver";

	// =================================
	// Default value
	// =================================
	// Upper limitation of row acquisition (MB)
	public static final int MAX_GETROW_SIZE_DEFAULT = 20;
	// Upper limitation of row registration (MB)
	public static final int MAX_PUTROW_SIZE_DEFAULT = 20;
	// Upper limitation of total response from request that has multiple statements (MB)
	public static final int MAX_TOTAL_RESPONSE_SIZE_DEFAULT = 2048;

	// Upper limitation of row acquisition (MB)
	public static final int MAX_SYSTEM_GETROW_SIZE_DEFAULT = 2048;
	// Upper limitation of row registration (MB)
	public static final int MAX_SYSTEM_PUTROW_SIZE_DEFAULT = 2048;
	// Upper limitation of total response from request that has multiple statements (MB)
	public static final int MAX_SYSTEM_TOTAL_RESPONSE_SIZE_DEFAULT = 4000;

	// Lower limitation of row acquisition (MB)
	public static final int MIN_SYSTEM_GETROW_SIZE_DEFAULT = 1;
	// Lower limitation of row registration (MB)
	public static final int MIN_SYSTEM_PUTROW_SIZE_DEFAULT = 1;
	// Lower limitation of total response from request that has multiple statements (MB)
	public static final int MIN_SYSTEM_TOTAL_RESPONSE_SIZE_DEFAULT = 2;

	public static final String FAILOVER_TIMEOUT_DEFAULT = "5";

	public static final int MIN_FAILOVER_TIMEOUT = 0;

	public static final String TRANSACTION_TIMEOUT_DEFAULT = "30";

	public static final int MIN_TRANSACTION_TIMEOUT = 0;

	public static final String CONTAINER_CACHE_SIZE_DEFAULT = "100";

	public static final int MIN_CONTAINER_CACHE_SIZE = 0;

	public static final int LOGIN_TIMEOUT_DEFAULT = 5;

	public static final int MIN_LOGIN_TIMEOUT = 0;
	
	public static final int PORT_DEFAULT = 8081;

	/**
	 * Limit number of rows in each TQL/SQL of function executeTQL/SQL()
	 */
	public static final int MAX_QUERY_NUM_DEFAULT = 10;

	public static final int MAX_SYSTEM_QUERY_NUM = 100;

	public static final int MIN_SYSTEM_QUERY_NUM = 1;

	/**
	 * Maximum rows for function getRows()
	 */
	public static final int MAX_LIMIT_DEFAULT = 1000000;

	public static final int MIN_SYSTEM_MAX_LIMIT = 1;

	public static final String ADMIN_HOME = "adminHome";

	public static final String PROPERTIES_FILE_PATH = "propertyFilePath";

	public static final long SIZE_TIMESTAMP = 8;
	

	public static final long SIZE_TIMESTAMP_MICROSECOND   = 8;
	public static final long SIZE_TIMESTAMP_NANOSECOND    = 9;

	public static final String DOT_CHARACTER = ".";

	/** Relative path of file griddb_webapiPath.properties */
	public static final String GIRDDB_WEBAPI_PATH = "/conf/griddb_webapiPath.properties";

	/** Time Zone property in file griddb_webapi.properties */
	public static final String PROPERTY_TIME_ZONE = "timeZone";

	/** Authentication Method property in file griddb_webapi.properties */
	public static final String PROPERTY_AUTHENTICATION_METHOD = "authenticationMethod";

	/** Authentication Method variable name. */
	public static final String AUTHENTICATION_METHOD = "authentication";

	/** Notification Interface Address property in file griddb_webapi.properties */
	public static final String PROPERTY_NOTIFICATION_INTERFACE_ADDRESS =
			"notificationInterfaceAddress";

	/** SSL Mode property in file griddb_webapi.properties */
	public static final String PROPERTY_SSL_MODE = "sslMode";

	/**
	 * Maximum of timeout (10 seconds) for checking connection to database
	 */
	public static final int MAX_TIME_OUT = 10000;
	
	/** Convert from MB to byte */
	public static final long MB_TO_BYTE = 1024 * 1024;

	/**
	 * Zip file extension.
	 */
	public static final String ZIP_FILE_EXT = ".zip";

	/**
	 * directory path of blob data.
	 */
	public static final String PROPERTY_BLOB_PATH = "blobPath";

	/**
	 * Application name when connecting to GridDB.
	*/
	public static final String PROPERTY_APPLICATION_NAME = "applicationName";
  
	/** application name value in grid store */
	public static final String WEBAPI_DEFAULT_NAME= "webapi";

	/** The Enum AuthenticationMethod. */
	public enum AuthenticationMethod {

		/** The ldap. */
		LDAP("LDAP"),

		/** The internal. */
		INTERNAL("INTERNAL");

		/** The value. */
		public final String value;

		/**
		 * Instantiates a new authentication method.
		 *
		 * @param value the value
		 */
		AuthenticationMethod(String value) {
			this.value = value;
		}
	}

	/** The Enum SSL Mode. */
	public static enum SslMode {
		/** The DISABLED mode. */
		DISABLED("DISABLED"),
		/** The PREFERRED mode. */
		PREFERRED("PREFERRED"),
		/** The VERIFY mode. */
		VERIFY("VERIFY");
		private String value;

		SslMode(String value) {
			this.value = value;
		}

		/**
		 * Get value SSL mode.
		 *
		 * @return value sslMode
		 */
		public String getValue() {
			return value;
		}
	}

}
