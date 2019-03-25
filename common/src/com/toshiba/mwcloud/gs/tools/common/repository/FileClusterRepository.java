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

package com.toshiba.mwcloud.gs.tools.common.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toshiba.mwcloud.gs.tools.common.GSCluster;
import com.toshiba.mwcloud.gs.tools.common.GSNode;
import com.toshiba.mwcloud.gs.tools.common.GSUserInfo;
import com.toshiba.mwcloud.gs.tools.common.Repository;

public class FileClusterRepository extends ClusterRepository {

	private static final String VERSION = "2.9.0";
	private static final String HASH_ALGORITHM = "SHA-256";
	private static final Charset PASSWORD_CHARSET = Charset.forName("UTF-8");

	private String jsonFilePath;
	private String legacyPasswordFilePath;

	public FileClusterRepository(String homeDir){
		jsonFilePath = homeDir + "/conf/repository.json";
		legacyPasswordFilePath = homeDir + "/conf/password";
	}

	private File getRepositoryFile() {
		return new File(jsonFilePath);
	}

	private File getLegacyPasswordFile() {
		return new File(legacyPasswordFilePath);
	}

	public Repository readRepository() throws Exception {
		return readRepository(getRepositoryFile());
	}
	private Repository readRepository(File repositoryFile) throws Exception {
		try {
			return new ObjectMapper().readValue(repositoryFile, Repository.class);

		} catch (IOException e) {
			throw e;
		}
	}

	public void saveRepository(Repository repository) throws Exception {
		saveRepository(getRepositoryFile(), repository);
	}
	private synchronized void saveRepository(File repositoryFile, Repository repository) throws Exception {
		try {
			if ( repositoryFile.length() != 0 ) {
				Repository oldRepository = new ObjectMapper().readValue(repositoryFile, Repository.class);
				String lastModified = repository.getHeader().getLastModified();
				String oldLastModified = oldRepository.getHeader().getLastModified();
				if (!lastModified.equals(oldLastModified)) {
					throw new Exception("Optimistic lock failed for repository file.");
				}
			}

			DateFormat dateFormat = new SimpleDateFormat(Repository.DATE_FORMAT);
			repository.getHeader().setLastModefied(dateFormat.format(new Date()));
			repository.getHeader().setVersion(VERSION);
			new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(repositoryFile, repository);

		} catch (IOException e) {
			throw new Exception("Error while saving repository file.", e);
		}
	}


	public GSCluster<GSNode> getGSCluster(String clusterName) throws Exception {

		Repository rep = readRepository();

		GSCluster<GSNode> cluster = null;
		List<GSCluster<GSNode>> clusters = rep.getClusters();
		for ( GSCluster<GSNode> cl : clusters ){
			if ( clusterName.equals(cl.getName())){
				cluster = cl;
				break;
			}
		}

		if (cluster == null) {
			return null;
		}

		for (GSNode node : rep.getNodes()) {
			if (node.getClusterName().equals(clusterName)) {
				cluster.getNodes().add(new GSNode(node.getAddress(), node.getPort()));
			}
		}

		return cluster;
	}

	public GSNode getGSNode(String clusterName, String host, int port) throws Exception {
		Repository rep = readRepository();

		GSNode node = null;
		List<GSNode> nodes = rep.getNodes();
		for (GSNode n : nodes) {
			if (host.equals(n.getAddress()) && port == n.getPort()) {
				node = n;
				break;
			}
		}

		return node;
	}

	public GSUserInfo auth(String clusterName, String userId, String password) throws Exception {
		GSUserInfo userInfo = null;

		userInfo = legacyAuth(userId, password);

		return userInfo;
	}

	private GSUserInfo legacyAuth(String userId, String password) throws Exception {
		GSUserInfo userInfo = null;

		if (userId == null || password == null) {
			return null;
		}

		String hash = computeHash(password);

		Map<String,String> users = readPasswordFile();
		for (Entry<String, String> user : users.entrySet()) {
			if (user.getKey().equals(userId) && user.getValue().equals(hash)) {
				userInfo = new GSUserInfo();
			}
		}

		return userInfo;
	}

	private String computeHash(String str) throws Exception {
		MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
		md.reset();
		md.update(str.getBytes());
		byte[] hash = md.digest();
		StringBuilder builder = new StringBuilder(64);
		for (int i= 0; i < hash.length; ++i) {
			builder.append(String.format("%02x", hash[i]));
		}
		return builder.toString();
	}

	private Map<String,String> readPasswordFile() throws Exception {
		File file = getLegacyPasswordFile();
		BufferedReader reader = null;
		try {
			Map<String,String> result = new HashMap<String,String>();
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), PASSWORD_CHARSET));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] cols = line.split(",");
				result.put(cols[0], cols[1]);
			}
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
}
