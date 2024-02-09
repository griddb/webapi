/*
 	Copyright (c) 2021 TOSHIBA Digital Solutions Corporation.
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

package com.toshiba.mwcloud.gs.tools.common.repository;

import com.toshiba.mwcloud.gs.tools.common.GSCluster;
import com.toshiba.mwcloud.gs.tools.common.GSNode;
import com.toshiba.mwcloud.gs.tools.common.GSUserInfo;
import com.toshiba.mwcloud.gs.tools.common.Repository;

public class RepositoryUtils {

	private RepositoryUtils() {}

	private static ClusterRepository m_repository;

	/**
	 * リポジトリ情報の初期化
	 */
	public static void init() throws Exception {
		String repositoryType = ToolProperties.getMessage("repositoryType");

		if ( (repositoryType == null) || !repositoryType.equalsIgnoreCase("RDB") ){
			m_repository = new FileClusterRepository(ToolProperties.getHomeDir());
		}
	}


	/**
	 * リポジトリ情報を読みます。
	 *
	 * @return
	 */
	public static Repository readRepository() throws Exception {
		return m_repository.readRepository();
	}

	/**
	 * リポジトリ情報を書き込みます。
	 *  (type=FILEの場合のみ）
	 *
	 * @param repository
	 */
	public static void saveRepository(Repository repository) throws Exception {
		m_repository.saveRepository(repository);

	}

	public static GSCluster<GSNode> getGSCluster(String clusterName) throws Exception {
		return m_repository.getGSCluster(clusterName);

	}

	public static GSNode getGSNode(String clusterName, String host, int port) throws Exception {
		return m_repository.getGSNode(clusterName, host, port);

	}

	/**
	 *
	 *  (type=RDBの場合のみ）
	 *
	 * @param clusterName
	 * @param user
	 * @param password
	 * @return
	 */
	public static GSUserInfo auth(String clusterName, String user, String password) throws Exception {
		// 存在しなかったらnullが返ります。
		// その他のエラーはException。
		return m_repository.auth(clusterName, user, password);
	}


	class UserInfo{
		String role;
	}
}
