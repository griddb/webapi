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

import java.util.Properties;

import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.GridStoreFactory;
import com.toshiba.mwcloud.gs.tools.common.GSCluster;
import com.toshiba.mwcloud.gs.tools.common.GSNode;
import com.toshiba.mwcloud.gs.tools.common.repository.RepositoryUtils;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWBadRequestException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWUnauthorizedException;

public class GridStoreUtils {

	/**
	 * Get the target cluster information from the repository and obtain the
	 * GridStore object. The database to be connected is "public".
	 *
	 * @param userid
	 *            user name
	 * @param password
	 *            password
	 * @param clusterName
	 *            name of cluster
	 * @param dbName
	 *            name of database
	 * @return a GridStore object
	 * @throws GSException
	 *             when unable to get GridStore from GridStoreFactory
	 */
	public static GridStore getGridStore(String clusterName, String dbName, String userid, String password)
			throws GSException {

		GSCluster<GSNode> cluster = null;
		try {
			cluster = RepositoryUtils.getGSCluster(clusterName);
		} catch (Exception e) {
			throw new GWException("Repository is invalid");
		}

		if (cluster == null) {
			throw new GWBadRequestException("Cluster not found in repository");
		}

		Properties props = new Properties();
		switch (cluster.getMode()) {
		case FIXED_LIST:
			props.setProperty("notificationMember", cluster.getTransactionMember());
			break;
		case PROVIDER:
			props.setProperty("notificationProvider", cluster.getProviderUrl());
			break;
		case MULTICAST:
		default:
			props.setProperty("notificationAddress", cluster.getAddress());
			props.setProperty("notificationPort", Integer.toString(cluster.getPort()));
			break;
		}
		props.setProperty("clusterName", clusterName);
		props.setProperty("database", dbName);
		props.setProperty("user", userid);
		props.setProperty("password", password);

		props.setProperty(Constants.PROP_TRANSACTION_TIMEOUT, GWSettingInfo.getTransactionTimeout());
		props.setProperty(Constants.PROP_FAILOVER_TIMEOUT, GWSettingInfo.getFailoverTimeout());
		props.setProperty(Constants.PROP_CONTAINER_CACHE_SIZE, GWSettingInfo.getContainerCacheSize());
		if (GWSettingInfo.getConsistensy() != null) {
			props.setProperty(Constants.PROP_CONSISTENCY, GWSettingInfo.getConsistensy());
		}

		try {
			return GridStoreFactory.getInstance().getGridStore(props);
		} catch (Exception e) {
			throw new GWUnauthorizedException("Failed to connect to cluster");
		}

	}

}
