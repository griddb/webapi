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

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GSNode {
	public static final int DEFAULT_SSH_PORT = 22;

	private String address;
	private int port;
	private int sshPort;
	private String clusterName;
	private String osPassword;

	private GSNodeStat stat;

	public GSNode() {
		
	}

	public GSNode(NodeKey nodeKey, int sshPort) {
		this(nodeKey.getAddress(), nodeKey.getPort(), sshPort);
	}

	public GSNode(String address, int port, int sshPort) {
		this.address = address;
		this.port = port;
		this.sshPort = sshPort;
	}

	public GSNode(String addr, int port) {
		this(addr, port, DEFAULT_SSH_PORT);
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
	public int getSshPort() {
		return sshPort;
	}
	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	@JsonIgnore
	public String getOsPassword() {
		return osPassword;
	}
	public void setOsPassword(String osPassword) {
		this.osPassword = osPassword;
	}
	@JsonIgnore
	public NodeKey getNodeKey() {
		return new NodeKey(address, port);
	}
	@JsonIgnore
	public GSNodeStat getStat(){
		return stat;
	}
	public void setStat(GSNodeStat stat){
		this.stat = stat;
	}

	@JsonIgnore
	public String getIdentifier(){
		return address + ":" + port;
	}

	@Override
	public String toString() {
		return "Node[" + address + ":" + port + ",ssh=" + sshPort + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;

		int tmp = 1;
		tmp = prime * tmp + ((address == null) ? 0 : address.hashCode());
		tmp = prime * tmp + port;

		int result = 1;
		result = prime * result + ((address == null) ? 0 : tmp);

		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GSNode other = (GSNode) obj;
		if (address == null) {
			if (other.getAddress() != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		return true;
	}
}
