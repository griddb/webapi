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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toshiba.mwcloud.gs.tools.common.GridStoreRemoteCommandUtils.RemoteCommandResult;
import com.toshiba.mwcloud.gs.tools.common.GridStoreWebAPI.AddressType;
import com.toshiba.mwcloud.gs.tools.common.GridStoreWebAPI.PartitionInfo;

public class GridStoreCommandUtils {

	public static Watcher startNode(final GSNode node, final String userId, final String password, String osPassword)
			throws GridStoreCommandException {
		return startNode(node, userId, password, osPassword, -1);
	}

	public static Watcher startNode(final GSNode node, final String userId, final String password, String osPassword,
			int waitTime) throws GridStoreCommandException {
		final GridStoreWebAPI webapi = new GridStoreWebAPI(node, userId, password);
		CombinedStatus status = null;
		status = getCombinedStatus(webapi);
		switch (status) {
		case STOPPED:
			break;
		case STARTING:
			return new StatusWatcher(webapi, CombinedStatus.STARTED);
		case STARTED:
		case WAIT:
		case SERVICING:
			return NullWatcher.INSTANCE;

		case STOPPING:
		case ABNORMAL:
		default:
			throw new IllegalStateException("D10101: Node status is invalid. (status=" + status + ")");
		}

		String command = "gs_startnode -u " + userId + "/" + password;
		if (waitTime > -1) {
			command += " -w " + waitTime;
		}
		command += " --checkPort " + node.getNodeKey().getPort();
		RemoteCommandResult commandResult = GridStoreRemoteCommandUtils.executeRemoteCommand(node, osPassword, command);
		if (commandResult.getExitStatus() != 0) {
			throw new GridStoreCommandException(
					"D10102: Failed to start " + node + ". (status=" + commandResult.getExitStatus() + ",messages="
							+ commandResult.getStdout() + commandResult.getStderr() + ")");
		}

		return new StatusWatcher(webapi, CombinedStatus.STARTED);
	}

	public static Watcher stopNode(final GSNode node, String userId, String password, boolean force)
			throws GridStoreCommandException {
		final GridStoreWebAPI webapi = new GridStoreWebAPI(node, userId, password);

		CombinedStatus status = getCombinedStatus(webapi);
		switch (status) {
		case STOPPING:
			if (force) {
				break;
			} else {
				return new StatusWatcher(webapi, CombinedStatus.STOPPED);
			}
		case STOPPED:
			return NullWatcher.INSTANCE;

		case STARTING:
			throw new IllegalStateException("D10103: The node of status 'STARTING' cannot stop. (node=" + node + ")");

		case STARTED:
			break;

		case SERVICING:
		case WAIT:
			if (!force) {
				throw new IllegalStateException(
						"D10104: The node joined cluster cannot stop. (node=" + node + ", status=" + status + ")");
			}
			break;
		case ABNORMAL:
		default:
			if (!force) {
				throw new IllegalStateException(
						"D10105: Node status is invalid. (node=" + node + ", status=" + status + ")");
			}
		}

		try {
			webapi.postNodeShutdown(force);
		} catch (GridStoreWebAPIException e) {
			throw new GridStoreCommandException("D10106: Failed to stop node. msg=[" + e.getMessage() + "]", e);
		}

		return new StatusWatcher(webapi, CombinedStatus.STOPPED);
	}

	private static Watcher joinCluster(String clusterName, int designatedCount, GSNode node, String userId,
			String password, boolean waitServicing) throws GridStoreCommandException {
		final GridStoreWebAPI webapi = new GridStoreWebAPI(node, userId, password);

		CombinedStatus status = getCombinedStatus(webapi);
		switch (status) {
		case STARTED:
			break;
		case WAIT:
		case SERVICING:
			return NullWatcher.INSTANCE;

		case STOPPED:
		case STARTING:
		case STOPPING:
		case ABNORMAL:
		default:
			throw new IllegalStateException(
					"D10107: The status of node must be \"STARTED\". : status=[" + status + "]");
		}

		try {
			webapi.postNodeJoin(clusterName, designatedCount);
		} catch (GridStoreWebAPIException e) {
			throw new GridStoreCommandException(
					"D10108: An error occurred while joining to cluster. msg=[" + e.getMessage() + "]", e);
		}

		if (waitServicing) {
			return new StatusWatcher(webapi, CombinedStatus.SERVICING);
		} else {
			return new StatusWatcher(webapi, CombinedStatus.SERVICING, CombinedStatus.WAIT);
		}
	}

	public static <T> Watcher joinCluster(GSCluster<T> cluster, GSNode node, String userId, String password)
			throws GridStoreCommandException {
		if (cluster.getNode(node.getNodeKey()) == null) {
			throw new IllegalStateException(
					"D10109: Node \"" + node.getIdentifier() + "\" is not in cluster definition.");
		}

		if (!getStatCluster(cluster, userId, password)) {
			throw new GridStoreCommandException(
					"D10148: Current cluster configuration is mismatched with cluster definition.");
		}

		int designatedCount = cluster.getNodes().size();
		int serviceCount = cluster.getStat().getServiceNodeCount();
		int waitCount = cluster.getStat().getWaitNodeCount();

		boolean waitService = false;
		switch (cluster.getStat().getClusterStatus()) {
		case SERVICE_UNSTABLE:
			waitService = true;
			break;
		case INIT_WAIT:
			if ((designatedCount - 1) == (waitCount + serviceCount)) {
				waitService = true;
			}
			break;
		case WAIT:
			if ((designatedCount / 2) <= (waitCount + serviceCount)) {
				waitService = true;
			}
			break;
		default:
			break;
		}

		return joinCluster(cluster.getName(), designatedCount, node, userId, password, waitService);

	}

	public static Watcher leaveCluster(GSNode node, String userId, String password, boolean force)
			throws GridStoreCommandException {
		final GridStoreWebAPI webapi = new GridStoreWebAPI(node, userId, password);

		CombinedStatus status = getCombinedStatus(webapi);
		switch (status) {
		case WAIT:
		case SERVICING:
			break;

		case STARTED:
		case STOPPED:
		case STARTING:
		case STOPPING:
			return NullWatcher.INSTANCE;

		case ABNORMAL:
		default:
			throw new IllegalStateException(
					"D10140: Node status is invalid. The status must be 'SERVICING' or 'WAIT'. : status=[" + status
							+ "]");
		}

		if (!force) {
			try {
				JsonNode stats = webapi.getNodeStat();
				String address = stats.path("cluster").path("nodeList").get(0).path("address").textValue();
				int port = stats.path("cluster").path("nodeList").get(0).path("port").asInt();

				PartitionInfo[] pInfoList = webapi.getNodePartition();
				for (PartitionInfo pInfo : pInfoList) {
					if (pInfo.owner != null) {
						if ((pInfo.backup.length == 0) && address.equals(pInfo.owner.address)
								&& (port == pInfo.owner.port)) {

							throw new GridStoreCommandException(
									"D10141: Some data in this node will be unavailable. Please use leave force command to forcibly stop.");
						}
					} else {
						if (pInfo.backup.length != 0) {
							for (GridStoreWebAPI.NodeKeyPartition key : pInfo.backup) {
								if (address.equals(key.address) && (port == key.port)) {
									throw new GridStoreCommandException(
											"D10142: Some data in this node will be unavailable. Please use leave force command to forcibly stop.");
								}
							}
						}
					}
				}

			} catch (GridStoreWebAPIException e) {
				throw new GridStoreCommandException("D10143: Failed to check node status. msg=[" + e.getMessage() + "]",
						e);
			}
		}

		try {
			webapi.postNodeLeave();
		} catch (GridStoreWebAPIException e) {
			throw new GridStoreCommandException(
					"D10112: An error occurred while leaving from cluster. msg=[" + e.getMessage() + "]", e);
		}

		return new StatusWatcher(webapi, CombinedStatus.STARTED);
	}

	public static Watcher appendCluster(GSNode masterNode, GSNode node, String userId, String password)
			throws GridStoreCommandException {
		final GridStoreWebAPI masterApi = new GridStoreWebAPI(masterNode, userId, password);
		final GridStoreWebAPI nodeApi = new GridStoreWebAPI(node, userId, password);

		CombinedStatus masterStatus = getCombinedStatus(masterApi);
		if (masterStatus != CombinedStatus.SERVICING) {
			throw new IllegalStateException("D10113: Master node status must be SERVICING.");
		}

		JsonNode stats = getStat(masterNode, userId, password);
		int activeCount = getActiveCount(stats);
		int desinatedCount = getDesinatedCount(stats);
		if (activeCount != desinatedCount) {
			throw new IllegalStateException("D10114: Cluster is unstable. (designatedCount=" + desinatedCount
					+ ", activeCount=" + activeCount + ")");
		}
		if (desinatedCount == 1) {
			throw new IllegalStateException("D10115: Single node cluster is not expandable.");
		}

		CombinedStatus status = getCombinedStatus(nodeApi);
		if (status != CombinedStatus.STARTED) {
			throw new IllegalStateException("D10116: Status of the node must be 'STARTED'. : status=[" + status + "]");
		}

		try {
			String clusterName = getClusterName(stats);
			nodeApi.postNodeJoin(clusterName, 0);
			masterApi.postClusterIncrease();
		} catch (GridStoreWebAPIException e) {
			throw new GridStoreCommandException(
					"D10117: An error occurred while appending cluster. : msg=[" + e.getMessage() + "]", e);
		}

		return new StatusWatcher(nodeApi, CombinedStatus.SERVICING);
	}

	public static <T> Watcher appendCluster(GSCluster<T> cluster, GSNode node, String userId, String password)
			throws GridStoreCommandException {
		if (cluster.getNode(node.getNodeKey()) != null) {
			throw new IllegalStateException("D10118: Node is a part of cluster definition.");
		}

		if (!getStatCluster(cluster, userId, password)) {
			throw new GridStoreCommandException(
					"D10150: Current cluster configuration is mismatched with cluster definition.");
		}

		if (cluster.getStat().getClusterStatus() != ClusterStatus.SERVICE_STABLE) {
			throw new GridStoreCommandException("D10151: All nodes in the cluster must be 'SERVICING'.");
		}
		GSNode masterNode = cluster.getStat().getMasterNode();
		if (masterNode == null) {
			throw new IllegalStateException("D10119: All nodes in the cluster are stopped.");
		}

		return appendCluster(masterNode, node, userId, password);
	}

	public static <T> List<Watcher> startCluster(GSCluster<T> cluster, String userId, String password)
			throws GridStoreCommandException {

		int designatedCount = cluster.getNodes().size();
		if (designatedCount == 0) {
			throw new IllegalArgumentException(
					"D10124: There is no node in cluster definition '$" + cluster.getName() + "'.");
		}

		if (!getStatCluster(cluster, userId, password)) {
			throw new GridStoreCommandException(
					"D10146: Current cluster configuration is mismatched with cluster definition.");
		}

		List<Watcher> watchers = new ArrayList<Watcher>();
		if (cluster.getStat().getClusterStatus() == ClusterStatus.SERVICE_STABLE) {
			watchers.add(NullWatcher.INSTANCE);
			return watchers;
		}

		List<GSNode> targetList = new ArrayList<GSNode>();
		int countJoin = 0;
		for (Object obj : cluster.getNodes()) {
			GSNode node = (GSNode) obj;
			CombinedStatus status = node.getStat().getCombinedStatus();
			switch (status) {
			case WAIT:
			case SERVICING:
				countJoin++;
				break;
			case STARTED:
				targetList.add(node);
				break;
			case STOPPED:
			case STARTING:
			case STOPPING:
			case ABNORMAL:
			default:
				break;
			}
		}

		int requiredCount = 0;
		if ((cluster.getStat().getClusterStatus() == ClusterStatus.STOP)
				|| (cluster.getStat().getClusterStatus() == ClusterStatus.INIT_WAIT)) {
			requiredCount = designatedCount;
		} else {
			requiredCount = designatedCount / 2 + 1;
		}
		if ((countJoin + targetList.size()) < requiredCount) {
			throw new GridStoreCommandException(
					"D10147: There is not enough number of STARTED/WAIT nodes to start cluster. : requiredNodeNum="
							+ requiredCount);
		}

		String clusterName = cluster.getName();

		Watcher watcher = NullWatcher.INSTANCE;
		for (GSNode node : targetList) {
			try {
				watcher = GridStoreCommandUtils.joinCluster(clusterName, designatedCount, node, userId, password, true);
				watchers.add(watcher);
			} catch (GridStoreCommandException e) {
				throw new GridStoreCommandException(
						"D10125: An error occurred while starting cluster. : msg=[" + e.getMessage() + "]", e);
			}
		}

		return watchers;
	}

	public static Watcher stopCluster(final GSNode masterNode, String userId, String password)
			throws GridStoreCommandException {
		final GridStoreWebAPI masterApi = new GridStoreWebAPI(masterNode, userId, password);
		CombinedStatus status = getCombinedStatus(masterApi);
		if (status != CombinedStatus.SERVICING) {
			throw new IllegalStateException("D10126: Master node status must be 'SERVICING'. (master=" + masterNode
					+ ", status=" + status + ")");
		}

		try {
			masterApi.postClusterStop();
		} catch (GridStoreWebAPIException e) {
			throw new GridStoreCommandException(
					"D10127:An error occurred while stopping cluster. : msg=[" + e.getMessage() + "]", e);
		}

		return new StatusWatcher(masterApi, CombinedStatus.STARTED);
	}

	public static <T> List<Watcher> stopCluster(GSCluster<T> cluster, String userId, String password)
			throws GridStoreCommandException {

		List<Watcher> watcherList = new ArrayList<Watcher>();

		if (!getStatCluster(cluster, userId, password)) {
			throw new GridStoreCommandException(
					"D10149: Current cluster configuration is mismatched with cluster definition.");
		}
		GSNode masterNode = cluster.getStat().getMasterNode();
		if (masterNode == null) {
			for (T obj : cluster.getNodes()) {
				GSNode node = (GSNode) obj;
				GridStoreWebAPI nodeApi = new GridStoreWebAPI(node, userId, password);
				CombinedStatus status = getCombinedStatus(nodeApi);
				if ((status == CombinedStatus.SERVICING) || (status == CombinedStatus.WAIT)) {
					try {
						nodeApi.postNodeLeave();
					} catch (GridStoreWebAPIException e) {
						throw new GridStoreCommandException(
								"D10144:An error occurred while stopping cluster. : msg=[" + e.getMessage() + "]", e);
					}
					watcherList.add(new StatusWatcher(nodeApi, CombinedStatus.STARTED));
				}
			}

		} else {
			final GridStoreWebAPI masterApi = new GridStoreWebAPI(masterNode, userId, password);
			try {
				masterApi.postClusterStop();
			} catch (GridStoreWebAPIException e) {
				throw new GridStoreCommandException(
						"D10145:An error occurred while stopping cluster. : msg=[" + e.getMessage() + "]", e);
			}
			watcherList.add(new StatusWatcher(masterApi, CombinedStatus.STARTED));
		}

		return watcherList;
	}

	public static JsonNode getStat(GSNode node, String userId, String password) throws GridStoreCommandException {
		try {
			return new GridStoreWebAPI(node, userId, password).getNodeStat();
		} catch (GridStoreWebAPIException e) {
			throw new GridStoreCommandException(
					"D10129: An error occurred while getting status info. : msg=[" + e.getMessage() + "]", e);
		}
	}

	public static <T> boolean getStatCluster(GSCluster<T> cluster, final String userId, final String password)
			throws GridStoreCommandException {
		GSClusterStat clStat = new GSClusterStat();
		cluster.setStat(clStat);

		List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
		if (cluster.getNodes().size() == 0) {
			return true;
		}
		if (cluster.getNodes().size() == 1) {
			GridStoreCommandUtils.getNodeStat((GSNode) cluster.getNodes().get(0), userId, password);
		} else {
			int threadCount = THREAD_COUNT;
			if (cluster.getNodes().size() < THREAD_COUNT) {
				threadCount = cluster.getNodes().size();
			}
			ExecutorService pool = Executors.newFixedThreadPool(threadCount);
			try {
				for (final T obj : cluster.getNodes()) {
					final GSNode node = (GSNode) obj;
					futures.add(pool.submit(new Callable<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							try {
								GridStoreCommandUtils.getNodeStat(node, userId, password);
								return true;
							} catch (Exception e) {
								throw e;
							}
						}
					}));
				}
				pool.shutdown();
				pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				pool.shutdownNow();
			}
		}

		String errMessage = "";
		for (Future<Boolean> future : futures) {
			try {
				future.get();
			} catch (InterruptedException e) {
				assert false;
			} catch (ExecutionException e) {
				if (e.getCause() != null) {
					errMessage += e.getCause().getMessage() + "\n";
				} else {
					errMessage += e.getMessage() + "\n";
				}
			}
		}
		if (!errMessage.isEmpty()) {
			throw new GridStoreCommandException(errMessage);
		}

		int countWait = 0;
		int countService = 0;
		boolean result = true;
		boolean initFlag = true;

		for (T obj : cluster.getNodes()) {
			GSNode node = (GSNode) obj;
			GSNodeStat stat = node.getStat();

			if ((stat.getCombinedStatus() == CombinedStatus.WAIT)
					|| (stat.getCombinedStatus() == CombinedStatus.SERVICING)) {
				if (cluster.getNodes().size() != stat.getDesignatedCount()) {
					result = false;
					continue;
				}
				if (!cluster.getName().equals(stat.getClusterName())) {
					result = false;
					continue;
				}
				if (stat.getCombinedStatus() == CombinedStatus.SERVICING) {
					countService++;
				} else if (stat.getCombinedStatus() == CombinedStatus.WAIT) {
					countWait++;
				}
				if (!stat.getInitClusterFlag()) {
					initFlag = false;
				}
			}

			if ((stat.getNodeRole() == NodeRole.MASTER) || (stat.getNodeRole() == NodeRole.SUB_MASTER)) {
				JsonNode nodeHostJson = getConfig(node, userId, password);
				JsonNode followers = nodeHostJson.path("follower");
				NodeKey[] followerKeys = new ObjectMapper()
						.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
						.convertValue(followers, NodeKey[].class);

				for (NodeKey nkey : followerKeys) {
					if (cluster.getNode(nkey) == null) {
						List<GSNode> undefNodes = clStat.getUndefinedNodes();
						if (undefNodes == null) {
							undefNodes = new ArrayList<GSNode>();
							clStat.setUndefinedNodes(undefNodes);
						}
						GSNode undefNode = new GSNode(nkey, GSNode.DEFAULT_SSH_PORT);
						GridStoreCommandUtils.getNodeStat(undefNode, userId, password);
						undefNodes.add(undefNode);
						if (undefNode.getStat().getCombinedStatus() == CombinedStatus.SERVICING) {
							countService++;
						} else if (undefNode.getStat().getCombinedStatus() == CombinedStatus.WAIT) {
							countWait++;
						}
						result = false;
					}
				}
			}

			if (stat.getNodeRole() == NodeRole.MASTER) {
				clStat.setMasterNode(node);
			}
		}
		clStat.setServiceNodeCount(countService);
		clStat.setWaitCount(countWait);

		if (cluster.getNodes().size() == countService) {
			clStat.setClusterStatus(ClusterStatus.SERVICE_STABLE);
		} else if (!initFlag && (countService > (cluster.getNodes().size() / 2))) {
			clStat.setClusterStatus(ClusterStatus.SERVICE_UNSTABLE);
		} else if ((countService > 0) || (countWait > 0)) {
			if (initFlag) {
				clStat.setClusterStatus(ClusterStatus.INIT_WAIT);
			} else {
				clStat.setClusterStatus(ClusterStatus.WAIT);
			}
		} else {
			clStat.setClusterStatus(ClusterStatus.STOP);
		}

		return result;
	}

	public static JsonNode getConfig(GSNode node, String userId, String password) throws GridStoreCommandException {
		try {
			return new GridStoreWebAPI(node, userId, password).getNodeHost();
		} catch (GridStoreWebAPIException e) {
			throw new GridStoreCommandException(
					"D10130: An error occurred while getting cluster configuration info. : msg=[" + e.getMessage()
							+ "]",
					e);
		}
	}

	public static JsonNode getParamConf(GSNode node, String userId, String password) throws GridStoreCommandException {
		try {
			return new GridStoreWebAPI(node, userId, password).getNodeConfig();
		} catch (GridStoreWebAPIException e) {
			throw new GridStoreCommandException(
					"D10138: An error occurred while getting node parameter info. : msg=[" + e.getMessage() + "]", e);
		}
	}

	public static String[] getLogs(GSNode node, String userId, String password) throws GridStoreCommandException {
		try {
			return new GridStoreWebAPI(node, userId, password).getNodeLog();
		} catch (GridStoreWebAPIException e) {
			throw new GridStoreCommandException(
					"D10131: An error occurred while getting log. : msg=[" + e.getMessage() + "]", e);
		}
	}

	public static Map<String, String> getLogConf(GSNode node, String userId, String password)
			throws GridStoreCommandException {
		try {
			return new GridStoreWebAPI(node, userId, password).getNodeTrace();
		} catch (GridStoreWebAPIException e) {
			throw new GridStoreCommandException(
					"D10132: An error occurred while getting log conf. : msg=[" + e.getMessage() + "]", e);
		}
	}

	public static Map<String, String> getLogConf(GSNode node, String userId, String password, String category)
			throws GridStoreCommandException {
		try {
			return new GridStoreWebAPI(node, userId, password).getNodeTrace(category);
		} catch (GridStoreWebAPIException e) {
			throw new GridStoreCommandException(
					"D10133: An error occurred while getting log conf. : msg=[" + e.getMessage() + "]", e);
		}
	}

	public static void setLogConf(GSNode node, String userId, String password, String category, String level)
			throws GridStoreCommandException {
		try {
			new GridStoreWebAPI(node, userId, password).postNodeTrace(category, level);
		} catch (GridStoreWebAPIException e) {
			throw new GridStoreCommandException(
					"D10134: An error occurred while setting log conf.: msg=[" + e.getMessage() + "]", e);
		}
	}

	public static String getNodeStatus(JsonNode stats) {
		return stats.path("cluster").path("nodeStatus").textValue();
	}

	public static String getClusterStatus(JsonNode stats) {
		return stats.path("cluster").path("clusterStatus").textValue();
	}

	public static String getClusterName(JsonNode stats) {
		return stats.path("cluster").path("clusterName").textValue();
	}

	public static int getDesinatedCount(JsonNode stats) {
		return stats.path("cluster").path("designatedCount").intValue();
	}

	public static int getActiveCount(JsonNode stats) {
		return stats.path("cluster").path("activeCount").intValue();
	}

	public static CombinedStatus getCombinedStatus(JsonNode stats) {
		String nodeStatus = stats.path("cluster").path("nodeStatus").textValue();

		if (nodeStatus.equals("INACTIVE") || nodeStatus.equals("DEACTIVCATING")) {
			double recoveryProgress = stats.path("recovery").path("progressRate").asDouble(1);
			return (recoveryProgress != 1) ? CombinedStatus.STARTING : CombinedStatus.STARTED;

		} else if (nodeStatus.equals("ACTIVE") || nodeStatus.equals("ACTIVATING")) {
			String clusterStatus = stats.path("cluster").path("clusterStatus").textValue();
			return (clusterStatus.equals("MASTER") || clusterStatus.equals("FOLLOWER")) ? CombinedStatus.SERVICING
					: CombinedStatus.WAIT;

		} else if (nodeStatus.equals("ABNORMAL")) {
			return CombinedStatus.ABNORMAL;

		} else if (nodeStatus.equals("NORMAL_SHUTDOWN")) {
			return CombinedStatus.STOPPING;

		} else {
			return CombinedStatus.UNKNOWN;
		}
	}

	public static CombinedStatus getCombinedStatus(GridStoreWebAPI webapi) throws GridStoreCommandException {
		try {
			JsonNode stats = webapi.getNodeStat();
			return getCombinedStatus(stats);

		} catch (GridStoreWebAPIException e1) {
			if (e1.getErrorCode() == GridStoreWebAPIException.CODE_API_CONNECT_ERROR) {
				return CombinedStatus.STOPPED;
			}
			throw new GridStoreCommandException(e1.getMessage(), e1);
		}
	}

	private static GSNodeStat getNodeStat(GSNode node, String userId, String password)
			throws GridStoreCommandException {

		GSNodeStat nodeStat = new GSNodeStat();
		node.setStat(nodeStat);

		try {
			JsonNode stats = null;
			try {
				stats = new GridStoreWebAPI(node, userId, password).getNodeStat();
			} catch (GridStoreWebAPIException e1) {
				if (e1.getErrorCode() == GridStoreWebAPIException.CODE_API_CONNECT_ERROR) {
					nodeStat.setCombinedStatus(CombinedStatus.STOPPED);
					return nodeStat;
				}
				throw e1;
			}

			nodeStat.setCombinedStatus(getCombinedStatus(stats));

			JsonNode init = stats.path("cluster").path("initialCluster");
			if (init.isMissingNode()) {
				nodeStat.setInitClusterFlag(false);
			} else {
				nodeStat.setInitClusterFlag(true);
			}

			String clusterStatus = getClusterStatus(stats);
			if (clusterStatus != null && !clusterStatus.isEmpty()) {
				if (clusterStatus.equalsIgnoreCase("MASTER")) {
					nodeStat.setNodeRole(NodeRole.MASTER);
				} else if (clusterStatus.equalsIgnoreCase("FOLLOWER")) {
					nodeStat.setNodeRole(NodeRole.FOLLOWER);
				} else if (clusterStatus.equalsIgnoreCase("SUB_CLUSTER")
						&& (getCombinedStatus(stats) == CombinedStatus.WAIT) && !nodeStat.getInitClusterFlag()) {
					nodeStat.setNodeRole(NodeRole.SUB_MASTER);
				} else if (clusterStatus.equalsIgnoreCase("SUB_FOLLOWER")) {
					nodeStat.setNodeRole(NodeRole.SUB_FOLLOWER);
				}
			}

			String clusterName = getClusterName(stats);
			if (clusterName != null && !clusterName.isEmpty()) {
				nodeStat.setClusterName(clusterName);
			}

			nodeStat.setDesignatedCount(getDesinatedCount(stats));
			nodeStat.setActiveCount(getActiveCount(stats));

		} catch (Exception e) {
			throw new GridStoreCommandException("D10137: Failed to get node status. (" + e.getMessage() + ")", e);
		}

		return nodeStat;
	}

	static int THREAD_COUNT = 8;

	public static <T> GSNode findMasterNode(GSCluster<T> cluster, String userId, String password)
			throws GridStoreCommandException {

		for (T obj : cluster.getNodes()) {
			GSNode node = (GSNode) obj;
			GridStoreWebAPI webapi = new GridStoreWebAPI(node, userId, password);

			JsonNode stats;
			try {
				stats = webapi.getNodeStat(AddressType.SYSTEM);
			} catch (GridStoreWebAPIException e) {
				if (e.getErrorCode() != GridStoreWebAPIException.CODE_API_CONNECT_ERROR) {
					throw new GridStoreCommandException("D10135: Failed to check node status. (" + e.getMessage() + ")",
							e);
				}
				continue;
			}
			JsonNode clusterStats = stats.path("cluster");
			String clusterName = clusterStats.path("clusterName").textValue();
			if (clusterName == null || clusterName.isEmpty()) {
				continue;
			} else if (!cluster.getName().equals(clusterName)) {
				throw new GridStoreCommandException("D10136: The node has already joined the other cluster \""
						+ clusterName + "\". (node=" + node + ")");
			}

			if ("MASTER".equals(getClusterStatus(clusterStats))) {
				return node;
			}

			JsonNode master = clusterStats.path("master");
			if (master.isMissingNode()) {
				return null;
			}
			String address = master.path("address").textValue();
			int port = master.path("port").intValue();

			GSNode masterNode = cluster.getNode(new NodeKey(address, port));
			if (masterNode == null) {
				masterNode = new GSNode(address, port);
			}

			JsonNode json = null;
			try {
				json = new GridStoreWebAPI(masterNode, userId, password).getNodeStat();
			} catch (GridStoreWebAPIException e) {
				continue;
			}
			if ("MASTER".equals(getClusterStatus(json))) {
				return masterNode;
			}
		}
		return null;
	}

}
