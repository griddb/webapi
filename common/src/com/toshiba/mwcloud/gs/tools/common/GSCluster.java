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
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GSCluster<T>{

	private String name;

	private NotificationMode mode = NotificationMode.MULTICAST;

	private String address;

	private int port;

	private String jdbcAddress;

	private int jdbcPort;

	private String transactionMember;

	private String sqlMember;

	private String providerUrl;

	private List<T> nodes = new ArrayList<T>();

	private GSClusterStat stat;

	public GSCluster() {

	}

	public GSCluster(String clusterName, String multicastAddr, int port, List<T> nodes) {
		this.name = clusterName;
		this.address = multicastAddr;
		this.port = port;
		this.nodes = new ArrayList<T>(nodes);
	}

	public GSCluster(String clusterName, String multicastAddr, int port, T... nodes) {
		this(clusterName, multicastAddr, port, Arrays.asList(nodes));
	}

	public GSCluster(String clusterName, String jdbcAddr, int jdbcPort ){
		this.name = clusterName;
		this.jdbcAddress = jdbcAddr;
		this.jdbcPort = jdbcPort;
	}

	public GSCluster(String clusterName, String multicastAddr, int port,
						String jdbcAddr, int jdbcPort, List<T> nodes){
		this.name = clusterName;
		this.address = multicastAddr;
		this.port = port;
		this.jdbcAddress = jdbcAddr;
		this.jdbcPort = jdbcPort;
		this.nodes = new ArrayList<T>(nodes);
	}

	public GSCluster(String clusterName, String multicastAddr, int port,
						String jdbcAddr, int jdbcPort, T... nodes){
		this(clusterName, multicastAddr, port, jdbcAddr, jdbcPort, Arrays.asList(nodes));
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public NotificationMode getMode() {
		return mode;
	}
	public void setMode(NotificationMode mode) {
		this.mode = mode;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getJdbcAddress() {
		return jdbcAddress;
	}
	public void setJdbcAddress(String jdbcAddress) {
		this.jdbcAddress = jdbcAddress;
	}
	public int getJdbcPort() {
		return jdbcPort;
	}
	public void setJdbcPort(int jdbcPort) {
		this.jdbcPort = jdbcPort;
	}
	public String getTransactionMember() {
		return transactionMember;
	}
	public void setTransactionMember(String transactionMember) {
		this.transactionMember = transactionMember;
	}
	public String getSqlMember() {
		return sqlMember;
	}
	public void setSqlMember(String sqlMember) {
		this.sqlMember = sqlMember;
	}
	public String getProviderUrl() {
		return providerUrl;
	}
	public void setProviderUrl(String providerUrl) {
		this.providerUrl = providerUrl;
	}

	@JsonIgnore
	public List<T> getNodes() {
		return nodes;
	}
	public void setNodes(List<T> nodes){
		this.nodes = nodes;
	}

	@JsonIgnore
	public GSClusterStat getStat(){
		return stat;
	}
	public void setStat(GSClusterStat stat){
		this.stat = stat;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Cluster[name=").append(name);
		if ( address != null ){
			builder.append(",").append(address).append(":").append(port);
		}
		if ( jdbcAddress != null ){
			builder.append(",sql=").append(jdbcAddress).append(":").append(jdbcPort);
		}
		builder.append(",nodes=(");
		for (int i = 0; i < nodes.size(); i++) {
			if (i != 0) {
				builder.append(",");
			}
			builder.append(((GSNode) nodes.get(i)).getIdentifier());
		}
		builder.append(")]");
		return builder.toString();
	}

	public GSNode getNode(NodeKey nodeKey) {
		for (Object obj : nodes) {
			GSNode node = (GSNode)obj;
			if (node.getNodeKey().equals(nodeKey)) {
				return node;
			}
		}
		return null;
	}

	public void checkNodes() throws GridStoreCommandException {
		List<GSNode> nodeList = new ArrayList<GSNode>(nodes.size());

		for ( Object obj : nodes ){
			GSNode node = (GSNode)obj;
			if ( nodeList.contains(node) ){
				throw new GridStoreCommandException("D10302: Duplicate value exists in the node list. node=["+node.getIdentifier()
						+","+nodeList.get(nodeList.indexOf(node)).getIdentifier()+"]");
			}
			nodeList.add(node);
		}
	}

}

