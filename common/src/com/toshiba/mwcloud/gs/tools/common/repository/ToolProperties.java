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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

public class ToolProperties {

	private static int fetchSize = 1000;

	public static int getFetchSize() {
		return fetchSize;
	}

	private static final String INIT_PROP_ADMIN_HOME_DIR = "adminHome";
	private static final String INIT_PROP_PROPERTY_FILE_PATH = "propertyFilePath";

	private static final String PROPERTY_FILE_PATH = "conf/gs_admin.properties";

	private static String m_homeDir;

	private static String m_propertyFilePath;

	private static volatile Properties m_properties;


	public static void readInitPropertyFile(String adminHome, String propertyFilePath) {
		readPathPropertyFile(adminHome, propertyFilePath);

		readPropertyFile();
	}

	
	private static void readPathPropertyFile(String path, String contextPath) {

		m_homeDir = path;
		m_propertyFilePath = contextPath;
	}

	private static void readPropertyFile() {
		readPropertyFile(m_homeDir, m_propertyFilePath);
	}

	private static void readPropertyFile(String dir, String filePath) {
		InputStream is = null;
		File file = new File(dir, filePath);
		try {
			is = new FileInputStream(file);
			Properties confNew = new Properties();
			confNew.load(is);
			m_properties = confNew;

		} catch (IOException e) {
			System.err.println("Cannot open " + file.getAbsolutePath() + ".");
			e.printStackTrace();
			throw new Error(e);

		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String getMessage(String key) {
		return m_properties.getProperty(key);
	}

	public static String getHomeDir() {
		return m_homeDir;
	}
}
