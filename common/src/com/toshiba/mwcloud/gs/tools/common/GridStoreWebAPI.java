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

package com.toshiba.mwcloud.gs.tools.common;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class GridStoreWebAPI {
	private final NodeKey nodeKey;
	private final String userId;
	private final String password;

	public NodeKey getNodeKey() {
		return nodeKey;
	}
	public String getUserId() {
		return userId;
	}
	public String getPassword() {
		return password;
	}

	public GridStoreWebAPI(NodeKey nodeKey, String userId, String password) {
		this.nodeKey = nodeKey;
		this.userId = userId;
		this.password = password;
	}

	public GridStoreWebAPI(GSNode node, String userId, String password) {
		this(node.getNodeKey(), userId, password);
	}

	public static final String GET = "GET";
	public static final String POST = "POST";

	private static final int CONNECT_TIMEOUT = 3000;

	public static enum AddressType { SYSTEM, CLUSTER, TRANSACTION, SYNC };

	public <Result> Result callWebApi(String method, String path,
				MultivaluedMap<String, String> params, Class<Result> resultClass)
				throws GridStoreWebAPIException {
		Client client = null;
		try {
			ClientConfig config = new DefaultClientConfig();
			client = Client.create(config);
			client.setConnectTimeout(CONNECT_TIMEOUT);
			client.addFilter(new HTTPBasicAuthFilter(userId, password));

			String url = "http://" + nodeKey.getAddress() + ":" + nodeKey.getPort() + path;
			WebResource webResource = client.resource(url);
			if (params != null && !method.equals(POST)) {
				webResource = webResource.queryParams(params);
			}
			Builder builder = webResource.accept(MediaType.APPLICATION_JSON_TYPE);
			if (params != null && method.equals(POST)) {
				builder = builder.entity(params, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
			}
			ClientResponse response = builder.method(method, ClientResponse.class);
			if (response.getStatus() != 200) {
				throwException(response);
			}

			String result = response.getEntity(String.class);
			if (resultClass == String.class) {
				return resultClass.cast(result);
			} else {
				return new ObjectMapper().readValue(result, resultClass);
			}

		} catch (JsonParseException e) {
			throw new GridStoreWebAPIException("D10000:Failed to convert result data to "+resultClass.getSimpleName()+" (node=" + nodeKey+")", e);
		} catch (JsonMappingException e) {
			throw new GridStoreWebAPIException("D10001:Failed to convert result data to "+resultClass.getSimpleName()+" (node=" + nodeKey+")", e);
		} catch (IOException e) {
			throw new GridStoreWebAPIException("D10002:Failed to convert result data to "+resultClass.getSimpleName()+" (node=" + nodeKey+")", e);
		} catch (UniformInterfaceException e){
			throw new GridStoreWebAPIException("D10003:Failed to http request (node=" + nodeKey+ ", "+e.getMessage()+")", e);
		} catch (ClientHandlerException e) {
			Throwable t = e.getCause();
			String message = "D10004:Failed to connect (node=" + nodeKey;
			if ( t != null ){
				message += ", "+t.getMessage();
			}
			message += ")";

			if ( t instanceof ConnectException ){
				throw new GridStoreWebAPIException(GridStoreWebAPIException.CODE_API_CONNECT_ERROR, message, e);
			} else if ( t instanceof SocketTimeoutException ){
				throw new GridStoreWebAPIException(GridStoreWebAPIException.CODE_API_CONNECT_ERROR, message, e);

			} else {
				throw new GridStoreWebAPIException(GridStoreWebAPIException.CODE_API_CONNECT_OTHER_ERROR, message, e);
			}

		} finally {
			if (client != null) {
				client.destroy();
			}
		}
	}

	public void postClusterStop() throws GridStoreWebAPIException {
		callWebApi(POST, "/cluster/stop", null, String.class);
	}

	public JsonNode getNodeHost(AddressType addressType) throws GridStoreWebAPIException {
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("addressType", addressType.toString().toLowerCase());
		return callWebApi(GET, "/node/host", null, JsonNode.class);
	}
	public JsonNode getNodeHost() throws GridStoreWebAPIException {
		return getNodeHost(AddressType.SYSTEM);
	}

	public JsonNode getNodeConfig() throws GridStoreWebAPIException {
		return callWebApi(GET, "/node/config", null, JsonNode.class);
	}

	public void postNodeLeave() throws GridStoreWebAPIException {
		callWebApi(POST, "/node/leave", null, String.class);
	}

	public void postClusterIncrease() throws GridStoreWebAPIException {
		callWebApi(POST, "/cluster/increase", null, String.class);
	}

	public void postNodeJoin(String clusterName, int minNodeNum) throws GridStoreWebAPIException {
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("clusterName", clusterName);
		params.add("minNodeNum", Integer.toString(minNodeNum));
		callWebApi(POST, "/node/join", params, String.class);
	}

	public void postNodeShutdown(boolean force) throws GridStoreWebAPIException {
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("force", Boolean.toString(force));
		callWebApi(POST, "/node/shutdown", params, String.class);
	}

	public String[] getNodeLog() throws GridStoreWebAPIException {
		return callWebApi(GET, "/node/log", null, String[].class);
	}

	private static class LogConfig {
		public Map<String, String> levels;
	}
	public Map<String, String> getNodeTrace() throws GridStoreWebAPIException {
		LogConfig logConfig = callWebApi(GET, "/node/trace", null, LogConfig.class);
		return logConfig.levels;
	}

	public Map<String, String> getNodeTrace(String category) throws GridStoreWebAPIException {
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("category", category);
		LogConfig logConfig = callWebApi(GET, "/node/trace", params, LogConfig.class);
		return logConfig.levels;
	}

	public void postNodeTrace(String category, String level) throws GridStoreWebAPIException {
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("category", category);
		params.add("level", level);
		callWebApi(POST, "/node/trace", params, String.class);
	}

	public JsonNode getNodeStat(AddressType addressType) throws GridStoreWebAPIException {
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("addressType", addressType.toString().toLowerCase());
		return callWebApi(GET, "/node/stat", params, JsonNode.class);
	}
	public JsonNode getNodeStat() throws GridStoreWebAPIException {
		return getNodeStat(AddressType.CLUSTER);
	}

	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class PartitionInfo {
		public NodeKeyPartition owner;
		public int pId;
		public String status;
		public NodeKeyPartition[] backup;
		public NodeKeyPartition[] catchup;
		public NodeKeyPartition[] all;
		public SqlOwner sqlOwner;
		public int maxLsn;
	}
	public static class NodeKeyPartition {
		public String address;
		public int lsn;
		public int port;
	}
	public static class SqlOwner {
		public String address;
		public int port;
	}
	public PartitionInfo[] getNodePartition(AddressType addressType) throws GridStoreWebAPIException {
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("addressType", addressType.toString().toLowerCase());
		return callWebApi(GET, "/node/partition", params, PartitionInfo[].class);
	}
	public PartitionInfo[] getNodePartition() throws GridStoreWebAPIException {
		return getNodePartition(AddressType.CLUSTER);
	}

	private void throwException(ClientResponse response) throws GridStoreWebAPIException {
		int httpStatus = response.getStatus();
		String result = response.getEntity(String.class);

		if ( httpStatus == 400 ){

			if ( result != null && !result.isEmpty()){
				JsonNode details = null;
				try {
					details = new ObjectMapper().readValue(result, JsonNode.class);

					int errorStatus = details.path("errorStatus").asInt(0);
					String errorMessage = details.path("errorMessage").asText();

					if (errorStatus == 104) {
						String address = details.path("master").path("address").asText();
						int port = details.path("master").path("port").asInt();
						NodeKey master = new NodeKey(address, port);
						throw new GridStoreWebAPINotMasterException(GridStoreWebAPIException.CODE_API_PARAM_ERROR,
								"D10004:Parameter invalid. "+errorMessage+" (node="+nodeKey+")", httpStatus, errorStatus, details, master);
					} else {
						throw new GridStoreWebAPIException(GridStoreWebAPIException.CODE_API_PARAM_ERROR,
								"D10005:Parameter invalid. "+errorMessage+" (node="+nodeKey+",errorStatus="+errorStatus+")", httpStatus, errorStatus, details);
					}
				} catch ( IOException e ){
					throw new GridStoreWebAPIException(GridStoreWebAPIException.CODE_API_PARAM_ERROR,
							"D10006: Parameter invalid. (node="+nodeKey+","+result+")", httpStatus);
				}
			} else {
				throw new GridStoreWebAPIException(GridStoreWebAPIException.CODE_API_PARAM_ERROR,
						"D10007: Parameter invalid. (node="+nodeKey+")", httpStatus );
			}

		} else if ( httpStatus == 401 ){
			throw new GridStoreWebAPIException(GridStoreWebAPIException.CODE_API_AUTH_ERROR,
					"D10008: Authentication Error (node="+nodeKey+")", httpStatus );

		} else {
			throw new GridStoreWebAPIException(GridStoreWebAPIException.CODE_API_OTHER_ERROR,
					"D10009: Http Response Error (node="+nodeKey+",httpStatus="+httpStatus+")", httpStatus);
		}
	}

}
