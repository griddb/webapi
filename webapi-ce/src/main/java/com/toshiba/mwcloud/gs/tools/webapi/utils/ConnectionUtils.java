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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toshiba.mwcloud.gs.tools.common.GSCluster;
import com.toshiba.mwcloud.gs.tools.common.GSNode;
import com.toshiba.mwcloud.gs.tools.common.repository.RepositoryUtils;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWBadRequestException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWUnauthorizedException;

public class ConnectionUtils {

	private static final int TXN_AUTH_FAILED = 10005;

	private static final int TXN_CLUSTER_NAME_INVALID = 10053;

	/**
	 * Get connection to a GridDB cluster
	 *
	 * @param clusterName cluster name
	 * @param dbName database name
	 * @param userid user name
	 * @param password password
	 * @return a {@link Connection} to GridDB cluster
	 */
	public static Connection getConnection(String clusterName, String dbName, String userid, String password) {

		GSCluster<GSNode> cluster = null;
		try {
			cluster = RepositoryUtils.getGSCluster(clusterName);
			if (cluster == null) {
				throw new GWBadRequestException("Cluster not found");
			}
		} catch (Exception e) {
			throw new GWBadRequestException("Repository is invalid");
		}

		String url = null;
		switch (cluster.getMode()) {
		case FIXED_LIST:
			String sqlMember = cluster.getSqlMember();
			if (sqlMember == null) {
				throw new GWBadRequestException("SQL is not active");
			}
			url = "jdbc:gs:///" + clusterName + "/" + dbName + "?notificationMember=" + sqlMember;
			break;
		case PROVIDER:
			String providerUrl = cluster.getProviderUrl();
			if (providerUrl == null) {
				throw new GWBadRequestException("SQL is not active");
			}
			url = "jdbc:gs:///" + clusterName + "/" + dbName + "?notificationProvider=" + providerUrl;
			break;
		case MULTICAST:
		default:
			String jdbcAddress = cluster.getJdbcAddress();
			int jdbcPort = cluster.getJdbcPort();
			if (jdbcAddress == null || jdbcPort <= 0) {
				throw new GWBadRequestException("SQL is not active");
			}
			url = "jdbc:gs://" + jdbcAddress + ":" + jdbcPort + "/" + clusterName + "/" + dbName;
			break;
		}

		Connection conn = null;
		Properties props = new Properties();
		props.setProperty("user", userid);
		props.setProperty("password", password);

		// updated 4.5.0 version
		GWSettingInfo.setOptionalProperty(props);
		DriverManager.setLoginTimeout(GWSettingInfo.getLoginTimeout());

		try {
			conn = java.sql.DriverManager.getConnection(url, props);
		} catch (SQLException e) {
			int errCode = e.getErrorCode();

			Logger logger = LoggerFactory.getLogger(ConnectionUtils.class);
			logger.error("SQL connection error(url=" + url + ", user=" + userid + ", password=" + password + ", code="
					+ errCode);

			switch (errCode) {
			case TXN_AUTH_FAILED:
				throw new GWUnauthorizedException(e.getMessage());
			case TXN_CLUSTER_NAME_INVALID:
				throw new GWBadRequestException(e.getMessage());
			default:
				throw new GWException(e.getMessage());
			}
		}

		return conn;
	}

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
