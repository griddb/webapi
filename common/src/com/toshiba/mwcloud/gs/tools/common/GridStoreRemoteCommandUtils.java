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
import java.io.InputStream;
import java.net.UnknownHostException;

import org.apache.commons.io.IOUtils;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

public class GridStoreRemoteCommandUtils {
	private static final String GSADM_USER = "gsadm";

	public static class RemoteCommandResult {
		private final String stdout;
		private final String stderr;
		private final int exitStatus;

		public RemoteCommandResult(String stdout, String stderr, int exitStatus) {
			this.stdout = stdout;
			this.stderr = stderr;
			this.exitStatus = exitStatus;
		}

		public String getStdout() {
			return stdout;
		}
		public String getStderr() {
			return stderr;
		}
		public int getExitStatus() {
			return exitStatus;
		}
	}

	public static RemoteCommandResult executeRemoteCommand(GSNode node, String osPassword, String... commands)
			throws GridStoreCommandException {
		Connection conn = new Connection(node.getNodeKey().getAddress(), node.getSshPort());
		Session session = null;
		try {
			conn.connect();
			if (!conn.authenticateWithPassword(GSADM_USER, osPassword)) {
				throw new GridStoreCommandException("D10201: SSH Authentication Error (" + node+")");
			}

			String command = "source .bash_profile" + joinString(" ; ", commands);

			session = conn.openSession();
			session.execCommand(command);
			InputStream stdout = new StreamGobbler(session.getStdout());
			InputStream stderr = new StreamGobbler(session.getStderr());
			String stdoutOut = IOUtils.toString(stdout, "UTF-8");
			String stderrOut = IOUtils.toString(stderr, "UTF-8");
			Integer exitStatus = session.getExitStatus();
			while ( exitStatus == null ){
				try {
					Thread.sleep(100);
				} catch ( InterruptedException e){}
				exitStatus = session.getExitStatus();
			}
			return new RemoteCommandResult(stdoutOut, stderrOut, exitStatus);

		} catch (IOException e) {
			Throwable t = e.getCause();
			if ( t != null && (t instanceof UnknownHostException) ){
				throw new GridStoreCommandException("D10202: SSH Connect Error ("+node+","+t.getMessage()+")", e);
			} else {
				throw new GridStoreCommandException("D10203: SSH Connect Error ("+node+","+e.getMessage()+")", e);
			}
		} finally {
			if (session != null) {
				session.close();
			}
			conn.close();
		}
	}

	private static String joinString(String joint, String[] strings) {
		StringBuilder result = new StringBuilder();
		for (String s : strings) {
			result.append(joint).append(s);
		}
		return result.toString();
	}

}
