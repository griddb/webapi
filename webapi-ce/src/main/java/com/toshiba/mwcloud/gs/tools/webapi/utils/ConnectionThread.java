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

import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;

public class ConnectionThread implements Runnable{

	private GridStore gridStore;
	
	private Object result;
	
	public ConnectionThread(GridStore gridStore) {
		this.gridStore = gridStore;
	}
	
	@Override
	public void run() {
		checkAuthentication(gridStore);
	}

	private void checkAuthentication(GridStore gridStore) {
		try {
			result = gridStore.getPartitionController().getPartitionCount();
		} catch (GSException ex) {
			result = ex;
		}
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

}
