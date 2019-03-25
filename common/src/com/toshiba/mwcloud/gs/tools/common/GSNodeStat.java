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

public class GSNodeStat {
	
	private CombinedStatus status = CombinedStatus.STOPPED;

	private NodeRole role = NodeRole.NONE;

	private int activeCount;

	private int designatedCount;

	private String clusterName;

	private boolean initClusterFlag = true;

	public void setCombinedStatus(CombinedStatus status){
		this.status = status;
	}

	public CombinedStatus getCombinedStatus(){
		return status;
	}

	public void setNodeRole(NodeRole role){
		this.role = role;
	}

	public NodeRole getNodeRole(){
		return role;
	}

	public void setActiveCount(int activeCount){
		this.activeCount = activeCount;
	}

	public int getActiveCount(){
		return activeCount;
	}

	public void setDesignatedCount(int designatedCount){
		this.designatedCount = designatedCount;
	}

	public int getDesignatedCount(){
		return designatedCount;
	}

	public void setClusterName(String clusterName){
		this.clusterName = clusterName;
	}

	public String getClusterName(){
		return clusterName;
	}

	public void setInitClusterFlag(boolean initClusterFlag){
		this.initClusterFlag = initClusterFlag;
	}

	public boolean getInitClusterFlag(){
		return initClusterFlag;
	}
}