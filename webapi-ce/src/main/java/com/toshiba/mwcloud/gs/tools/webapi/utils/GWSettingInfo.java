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

import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.tools.webapi.utils.Constants.SslMode;
import java.util.Properties;
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

	private static String timeZone;

	private static String authenticationMehod;

	private static String notificationInterfaceAddress;

	private static String sslMode;

	private static final Logger logger = (Logger) LoggerFactory.getLogger(GWSettingInfo.class);

	private static String blobPath;

	/**
	 * Initial setting webapi.
	 * 
	 * @throws GWException when unable to get GridStore from GridStoreFactory
	 */
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

		// logger = LoggerFactory.getLogger(GWSettingInfo.class);

		// Updated version 4.5
		setTimeZone(ToolProperties.getMessage(Constants.PROPERTY_TIME_ZONE));
		// Updated version 4.5
		setAuthenticationMethod(ToolProperties.getMessage(Constants.PROPERTY_AUTHENTICATION_METHOD));
		// Updated version 4.5
		setNotificationInterfaceAddress(
        ToolProperties.getMessage(Constants.PROPERTY_NOTIFICATION_INTERFACE_ADDRESS));
		// Updated version 4.5
		setSslMode(ToolProperties.getMessage(Constants.PROPERTY_SSL_MODE));

		setBlobPath(ToolProperties.getMessage(Constants.PROPERTY_BLOB_PATH));
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

	/**
	 * Set max get row size.
	 *
	 * @param value value row size
	 * @param systemValue system value
	 */
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

	/**
	 * Set time zone.
	 *
	 * @param value value of property
	 */
	public static void setTimeZone(String value) {
		GWSettingInfo.timeZone = value;
	}

	/**
	 * Get value of property time zone.
	 *
	 * @return Value of property timeZone
	 */
	public static String getTimeZone() {
		return timeZone;
	}

	/**
	 * Set authentication method.
	 *
	 * @param value value of property
	 */
	public static void setAuthenticationMethod(String value) {
		if (value != null) {
			if (!Validation.isValidAuthenticationMethod(value)) {
				throw new GWException(
						"Authentication method "
								+ value
								+ " is incorrect. Correct authentication method are \"LDAP\" and \"INTERNAL\". "
								+ "When not specified, uses server setting.");
			}
			GWSettingInfo.authenticationMehod = value;
		}
	}

	/**
	 * Get value of property authentication method.
	 *
	 * @return Value of property authenticationMethod
	 */
	public static String getAuthenticationMethod() {
		return authenticationMehod;
	}

	/**
	 * Set notification interface address.
	 *
	 * @param value value of property
	 */
	public static void setNotificationInterfaceAddress(String value) {
		if (value != null) {
			if (!Validation.isValidIPAddress(value)) {
				throw new GWException(
						"Notification interface address "
								+ value
								+ " is invalid. Change notification interface address to valid IP address (IPv4)."
								+ " When not specified, uses address depends on the OS.");
			}
			GWSettingInfo.notificationInterfaceAddress = ConversionUtils.formatValidIPAddress(value);
		}
	}

	/**
	 * Get value of property notification interface address.
	 *
	 * @return Value of property notificationInterfaceAddress
	 */
	public static String getNotificationInterfaceAddress() {
		return notificationInterfaceAddress;
	}

	/**
	 * Set SSL Mode.
	 *
	 * @param value value of property
	 */
	public static void setSslMode(String value) {
		if (value != null) {
			if (!Validation.isValidSslMode(value)) {
				throw new GWException(
						"SSL mode "
								+ value
								+ " is incorrect. Correct SSL mode are \"DISABLED\", \"PREFERRED\" and \"VERIFY\"."
								+ " When not specified, uses \"DISABLED\" as default.");
			}
			GWSettingInfo.sslMode = SslMode.valueOf(value).getValue();
		} else {
			GWSettingInfo.sslMode = SslMode.DISABLED.toString();
		}
	}

	/**
	 * Get value of property SSL mode.
	 *
	 * @return Value of SSL mode
	 */
	public static String getSslMode() {
		return sslMode;
	}

	/**
	 * Set properties.
	 *
	 * @param props properties
	 */
	public static void setOptionalProperty(Properties props) {
		// set time zone property
		String timeZone = GWSettingInfo.getTimeZone();
		if (timeZone != null && !timeZone.trim().equals("")) {
			props.setProperty(Constants.PROPERTY_TIME_ZONE, timeZone);
		}
		// set authentication method
		String authenticationMethod = GWSettingInfo.getAuthenticationMethod();
		if (authenticationMethod != null && !authenticationMethod.trim().equals("")) {
			props.setProperty(Constants.AUTHENTICATION_METHOD, authenticationMethod);
		}
		// set notification interface address
		String notificationInterfaceAddress = GWSettingInfo.getNotificationInterfaceAddress();
		if (notificationInterfaceAddress != null && !notificationInterfaceAddress.trim().equals("")) {
			props.setProperty(
					Constants.PROPERTY_NOTIFICATION_INTERFACE_ADDRESS, notificationInterfaceAddress);
		}
		// set SSL mode
		String sslMode = GWSettingInfo.getSslMode();
		if (! SslMode.DISABLED.toString().equals(sslMode)) {
			props.setProperty(Constants.PROPERTY_SSL_MODE, sslMode);
		}
	}

	/**
	 * set directory path of blob data.
	 * @param blobPath directory path of blob data
	 */
	public static void setBlobPath(String blobPath) {
		GWSettingInfo.blobPath = blobPath;
	}

	/**
	 * get directory path of blob data.
	 * @return directory path of blob data
	 */
	public static String getBlobPath() {
		return blobPath;
	}
	
}
