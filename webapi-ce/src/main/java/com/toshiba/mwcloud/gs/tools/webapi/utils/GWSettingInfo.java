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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toshiba.mwcloud.gs.tools.common.repository.ToolProperties;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWException;

public class GWSettingInfo {

	private static String failoverTimeout = Constants.FAILOVER_TIMEOUT_DEFAULT;

	private static String transactionTimeout = Constants.TRANSACTION_TIMEOUT_DEFAULT;

	private static String containerCacheSize = Constants.CONTAINER_CACHE_SIZE_DEFAULT;

	// Consistency sets default value of API
	private static String consistensy;

	// Upper limitation of row acquisition size (Byte)
	private static long maxGetRowSize;

	// Upper limitation of row registration size (Byte)
	private static long maxPutRowSize;

	private static int loginTimeout;
	
	private static int maxQueryNum;
	
	private static int maxLimit;

	private static Logger logger;

	public static void init() throws GWException {
		setFailoverTimeout(ToolProperties.getMessage(Constants.PROP_FAILOVER_TIMEOUT));
		setTransactionTimeout(ToolProperties.getMessage(Constants.PROP_TRANSACTION_TIMEOUT));
		setContainerCacheSize(ToolProperties.getMessage(Constants.PROP_CONTAINER_CACHE_SIZE));
		setConsistency(ToolProperties.getMessage(Constants.PROP_CONSISTENCY));

		setMaxGetRowSize(ToolProperties.getMessage(Constants.PROP_MAX_GETROW_SIZE),
				ToolProperties.getMessage(Constants.PROP_MAX_SYSTEM_GETROW_SIZE));
		setMaxPutRowSize(ToolProperties.getMessage(Constants.PROP_MAX_PUTROW_SIZE),
				ToolProperties.getMessage(Constants.PROP_MAX_SYSTEM_PUTROW_SIZE));

		setLoginTimeout(ToolProperties.getMessage(Constants.PROP_LOGIN_TIMEOUT));
		
		setMaxQueryNum(ToolProperties.getMessage(Constants.PROP_MAX_QUERY_NUM));
		setMaxLimit(ToolProperties.getMessage(Constants.PROP_MAX_LIMIT));

		logger = LoggerFactory.getLogger(GWSettingInfo.class);
		
	}

	private static void setFailoverTimeout(String value) throws GWException {
		failoverTimeout = Constants.FAILOVER_TIMEOUT_DEFAULT;
		if (value != null) {
			try {
				int intValue = Integer.parseInt(value);
				if (intValue < Constants.MIN_FAILOVER_TIMEOUT) {
					throw new GWException("property '" + Constants.PROP_FAILOVER_TIMEOUT + "' can not smaller than " + Constants.MIN_FAILOVER_TIMEOUT);
				}
				failoverTimeout = value;
			} catch (NumberFormatException e) {
				throw new GWException("property '" + Constants.PROP_FAILOVER_TIMEOUT + "' is invalid.");
			}
		}

	}

	public static String getFailoverTimeout() {
		return failoverTimeout;
	}

	private static void setTransactionTimeout(String value) throws GWException {
		transactionTimeout = Constants.TRANSACTION_TIMEOUT_DEFAULT;
		if (value != null) {
			try {
				int intValue = Integer.parseInt(value);
				if (intValue < Constants.MIN_TRANSACTION_TIMEOUT) {
					throw new GWException("property '" + Constants.PROP_TRANSACTION_TIMEOUT + "' can not smaller than " + Constants.MIN_TRANSACTION_TIMEOUT);
				}
				transactionTimeout = value;
			} catch (NumberFormatException e) {
				throw new GWException("property '" + Constants.PROP_TRANSACTION_TIMEOUT + "' is invalid.");
			}
		}
	}

	public static String getTransactionTimeout() {
		return transactionTimeout;
	}

	private static void setContainerCacheSize(String value) throws GWException {
		containerCacheSize = Constants.CONTAINER_CACHE_SIZE_DEFAULT;
		if (value != null) {
			try {
				int intValue = Integer.parseInt(value);
				if (intValue < Constants.MIN_CONTAINER_CACHE_SIZE) {
					throw new GWException("property '" + Constants.PROP_CONTAINER_CACHE_SIZE + "' can not smaller than " + Constants.MIN_CONTAINER_CACHE_SIZE);
				}
				containerCacheSize = value;
			} catch (NumberFormatException e) {
				throw new GWException("property '" + Constants.PROP_CONTAINER_CACHE_SIZE + "' is invalid.");
			}
		}
	}

	public static String getContainerCacheSize() {
		return containerCacheSize;
	}

	private static void setConsistency(String value) {
		if (value != null) {
			consistensy = value;
		}
	}

	public static String getConsistensy() {
		return consistensy;
	}

	public static void setMaxGetRowSize(String value, String systemValue) throws GWException {
		maxGetRowSize = Constants.MAX_GETROW_SIZE_DEFAULT;
		if (value != null) {
			try {
				maxGetRowSize = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new GWException("property '" + Constants.PROP_MAX_GETROW_SIZE + "' is invalid.");
			}
		}

		int maxSystemGetRowSize = Constants.MAX_SYSTEM_GETROW_SIZE_DEFAULT;
		if (systemValue != null) {
			try {
				maxSystemGetRowSize = Integer.parseInt(systemValue);
			} catch (NumberFormatException e) {
				throw new GWException("property '" + Constants.PROP_MAX_SYSTEM_GETROW_SIZE + "' is invalid.");
			}
		}

		if (maxGetRowSize > maxSystemGetRowSize) {
			throw new GWException("property '" + Constants.PROP_MAX_GETROW_SIZE + "' can not larger than '"
					+ Constants.PROP_MAX_SYSTEM_GETROW_SIZE + "' " + maxSystemGetRowSize + ".");
		}
		
		int minSystemGetRowSize = Constants.MIN_SYSTEM_GETROW_SIZE_DEFAULT;
		if (maxGetRowSize < minSystemGetRowSize) {
			throw new GWException("property '" + Constants.PROP_MAX_GETROW_SIZE + "' can not smaller than '"
					+ Constants.PROP_MIN_SYSTEM_GETROW_SIZE + "' " + minSystemGetRowSize + ".");
		}

		maxGetRowSize = maxGetRowSize * 1024 * 1024;
	}

	public static long getMaxGetRowSize() {
		return maxGetRowSize;
	}

	private static void setMaxPutRowSize(String value, String systemValue) throws GWException {
		maxPutRowSize = Constants.MAX_PUTROW_SIZE_DEFAULT;
		if (value != null) {
			try {
				maxPutRowSize = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new GWException("property '" + Constants.PROP_MAX_PUTROW_SIZE + "' is invalid.");
			}
		}

		int maxSystemPutRowSize = Constants.MAX_SYSTEM_PUTROW_SIZE_DEFAULT;
		if (systemValue != null) {
			try {
				maxSystemPutRowSize = Integer.parseInt(systemValue);
			} catch (NumberFormatException e) {
				throw new GWException("property '" + Constants.PROP_MAX_SYSTEM_PUTROW_SIZE + "' is invalid.");
			}
		}

		if (maxPutRowSize > maxSystemPutRowSize) {
			throw new GWException("property '" + Constants.PROP_MAX_PUTROW_SIZE + "' can not larget than '"
					+ Constants.PROP_MAX_SYSTEM_PUTROW_SIZE + "' " + maxSystemPutRowSize + ".");
		}
		
		int minSystemPutRowSize = Constants.MIN_SYSTEM_PUTROW_SIZE_DEFAULT;
		if (maxPutRowSize < minSystemPutRowSize) {
			throw new GWException("property '" + Constants.PROP_MAX_PUTROW_SIZE + "' can not smaller than '"
					+ Constants.PROP_MIN_SYSTEM_PUTROW_SIZE + "' " + minSystemPutRowSize + ".");
		}

		maxPutRowSize = maxPutRowSize * 1024 * 1024;
	}

	public static long getMaxPutRowSize() {
		return maxPutRowSize;
	}

	/**
	 * Setting value of property LoginTime
	 * 
	 * @param value
	 *            Value of property LoginTime (seconds)
	 * @throws GWException
	 *             Setting failed
	 */
	public static void setLoginTimeout(String value) throws GWException {
		loginTimeout = Constants.LOGIN_TIMEOUT_DEFAULT;
		if (value != null) {
			try {
				loginTimeout = Integer.parseInt(value);
				if (loginTimeout < Constants.MIN_LOGIN_TIMEOUT) {
					throw new GWException("property '" + Constants.PROP_LOGIN_TIMEOUT + "' can not smaller than " + Constants.MIN_LOGIN_TIMEOUT);
				}
			} catch (NumberFormatException e) {
				throw new GWException("property '" + Constants.PROP_LOGIN_TIMEOUT + "' is invalid");
			}
		}
	}

	/**
	 * Get value of property LoginTime
	 * 
	 * @return Value of property LoginTime
	 */
	public static int getLoginTimeout() {
		return loginTimeout;
	}

	public static Logger getLogger() {
		return logger;
	}

	/**
	 * Set value of property maxQueryNum
	 * 
	 * @param value max query number
	 */
	public static void setMaxQueryNum(String value) {
		GWSettingInfo.maxQueryNum = Constants.MAX_QUERY_NUM_DEFAULT;
		if (value != null) {
			try {
				GWSettingInfo.maxQueryNum = Integer.parseInt(value);
				
				if (GWSettingInfo.maxQueryNum < Constants.MIN_SYSTEM_QUERY_NUM || GWSettingInfo.maxQueryNum > Constants.MAX_SYSTEM_QUERY_NUM) {
					throw new GWException("property '" + Constants.PROP_MAX_QUERY_NUM + "' can not smaller than " + Constants.MIN_SYSTEM_QUERY_NUM + " and larger than " + Constants.MAX_SYSTEM_QUERY_NUM);
				}
			} catch (NumberFormatException e) {
				throw new GWException("property '" + Constants.PROP_MAX_QUERY_NUM + "' is invalid");
			}
		}
	}
	
	/**
	 * Get value of property maxQueryNum
	 * 
	 * @return Value of property maxQueryNum
	 */
	public static int getMaxQueryNum() {
		return maxQueryNum;
	}

	/**
	 * Set value of property maxLimi
	 * 
	 * @param value max limit
	 */
	public static void setMaxLimit(String value) {
		GWSettingInfo.maxLimit = Constants.MAX_LIMIT_DEFAULT;
		if (value != null) {
			try {
				GWSettingInfo.maxLimit = Integer.parseInt(value);
				if (GWSettingInfo.maxLimit < Constants.MIN_SYSTEM_MAX_LIMIT) {
					throw new GWException("property '" + Constants.PROP_MAX_LIMIT + "' can not smaller than " + Constants.MIN_SYSTEM_MAX_LIMIT);
				}
			} catch (NumberFormatException e) {
				throw new GWException("property '" + Constants.PROP_MAX_LIMIT + "' is invalid");
			}
		}
	}
	
	/**
	 * Get value of property maxLimit
	 * 
	 * @return Value of property maxLimit
	 */
	public static int getMaxLimit() {
		return maxLimit;
	}

	
}
