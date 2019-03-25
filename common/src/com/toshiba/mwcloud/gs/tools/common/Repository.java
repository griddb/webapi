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

public class Repository {
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	public static class Header {
		private String lastModified;
		private String version;
		public String getLastModified() {
			return lastModified;
		}
		public void setLastModefied(String lastModified) {
			this.lastModified = lastModified;
		}
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
	}

	private Header header;
	private List<GSCluster<GSNode>> clusters = new ArrayList<GSCluster<GSNode>>();
	private List<GSNode> nodes = new ArrayList<GSNode>();

	public Header getHeader() {
		return header;
	}
	public void setHeader(Header header) {
		this.header = header;
	}
	public List<GSCluster<GSNode>> getClusters() {
		return clusters;
	}
	public void setClusters(List<GSCluster<GSNode>> clusters) {
		this.clusters = clusters;
	}
	public List<GSNode> getNodes() {
		return nodes;
	}
	public void setNodes(List<GSNode> nodes) {
		this.nodes = nodes;
	}
}
