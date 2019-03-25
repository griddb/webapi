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

	// System limitation of row acquisition
	public static final String PROP_MAX_SYSTEM_GETROW_SIZE = "maxSystemGetRowSize";
	// System limitation of row registration
	public static final String PROP_MAX_SYSTEM_PUTROW_SIZE = "maxSystemPutRowSize";
	
	// System limitation of row acquisition
	public static final String PROP_MIN_SYSTEM_GETROW_SIZE = "minSystemGetRowSize";
	// System limitation of row registration
	public static final String PROP_MIN_SYSTEM_PUTROW_SIZE = "minSystemPutRowSize";

	public static final String PROP_FAILOVER_TIMEOUT = "failoverTimeout";
	public static final String PROP_TRANSACTION_TIMEOUT = "transactionTimeout";
	public static final String PROP_CONTAINER_CACHE_SIZE = "containerCacheSize";
	public static final String PROP_CONSISTENCY = "consistency";

	public static final String PROP_LOGIN_TIMEOUT = "loginTimeout";
	
	public static final String PROP_MAX_QUERY_NUM = "maxQueryNum";
	public static final String PROP_MAX_LIMIT = "maxLimit";

	// Driver name for JDBC
	public static final String DRIVER_NAME = "com.toshiba.mwcloud.gs.sql.Driver";

	// =================================
	// Default value
	// =================================
	// Upper limitation of row acquisition (MB)
	public static final int MAX_GETROW_SIZE_DEFAULT = 20;
	// Upper limitation of row registration (MB)
	public static final int MAX_PUTROW_SIZE_DEFAULT = 20;

	// Upper limitation of row acquisition (MB)
	public static final int MAX_SYSTEM_GETROW_SIZE_DEFAULT = 2048;
	// Upper limitation of row registration (MB)
	public static final int MAX_SYSTEM_PUTROW_SIZE_DEFAULT = 2048;
	
	// Lower limitation of row acquisition (MB)
	public static final int MIN_SYSTEM_GETROW_SIZE_DEFAULT = 1;
	// Lower limitation of row registration (MB)
	public static final int MIN_SYSTEM_PUTROW_SIZE_DEFAULT = 1;

	public static final String FAILOVER_TIMEOUT_DEFAULT = "5";
	
	public static final int MIN_FAILOVER_TIMEOUT = 0;

	public static final String TRANSACTION_TIMEOUT_DEFAULT = "30";
	
	public static final int MIN_TRANSACTION_TIMEOUT = 0;

	public static final String CONTAINER_CACHE_SIZE_DEFAULT = "100";
	
	public static final int MIN_CONTAINER_CACHE_SIZE = 0;

	public static final int LOGIN_TIMEOUT_DEFAULT = 5;
	
	public static final int MIN_LOGIN_TIMEOUT = 0;
	
	/**
	 * Limit number of rows in each TQL/SQL of function executeTQL/SQL()
	 */
	public static final int MAX_QUERY_NUM_DEFAULT = 10;
	
	public static final int MAX_SYSTEM_QUERY_NUM = 100;
	
	public static final int MIN_SYSTEM_QUERY_NUM = 0;
	
	/**
	 * Maximum rows for function getRows()
	 */
	public static final int MAX_LIMIT_DEFAULT = 1000000;
	
	public static final int MIN_SYSTEM_MAX_LIMIT = 1;

	public static final String ADMIN_HOME = "adminHome";

	public static final String PROPERTIES_FILE_PATH = "propertyFilePath";

	public static final long SIZE_TIMESTAMP = 8;
	
	/**
	 * Relative path of file griddb_webapiPath.properties
	 */
	public static final String GIRDDB_WEBAPI_PATH = "/conf/griddb_webapiPath.properties";
	
	
	/**
	 * Maximum of timeout (10 seconds) for checking connection to database
	 */
	public static final int MAX_TIME_OUT = 10000;
	
}
