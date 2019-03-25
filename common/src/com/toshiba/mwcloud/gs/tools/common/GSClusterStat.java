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

import java.util.List;

public class GSClusterStat {

	private ClusterStatus status;

	private int serviceNodeCount;

	private int waitNodeCount;

	private List<GSNode> unDefinedNodes;

	private GSNode masterNode;

	public ClusterStatus getClusterStatus(){
		return status;
	}

	public void setClusterStatus(ClusterStatus status){
		this.status = status;
	}

	public int getServiceNodeCount(){
		return serviceNodeCount;
	}

	public void setServiceNodeCount(int serviceNodeCount){
		this.serviceNodeCount = serviceNodeCount;
	}

	public int getWaitNodeCount(){
		return waitNodeCount;
	}

	public void setWaitCount(int waitNodeCount){
		this.waitNodeCount = waitNodeCount;
	}

	public List<GSNode> getUndefinedNodes(){
		return unDefinedNodes;
	}
	public void setUndefinedNodes(List<GSNode> unDefiGsNodes){
		this.unDefinedNodes = unDefiGsNodes;
	}

	public GSNode getMasterNode(){
		return masterNode;
	}
	public void setMasterNode(GSNode masterNode){
		this.masterNode = masterNode;
	}

}
