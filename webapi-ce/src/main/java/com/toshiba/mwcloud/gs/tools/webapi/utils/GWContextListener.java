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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import com.toshiba.mwcloud.gs.tools.common.repository.RepositoryUtils;
import com.toshiba.mwcloud.gs.tools.common.repository.ToolProperties;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWException;

@PropertySource("application.properties")
public class GWContextListener implements ServletContextListener {

	@Value("${adminHome}")
	private String adminHome;

	@Value("${propertyFilePath}")
	private String propertyFilePath;

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
	}

	/**
	 * Initialize web API connection
	 */
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {

		ToolProperties.readInitPropertyFile(adminHome, propertyFilePath);
		try {
			RepositoryUtils.init();
			RepositoryUtils.readRepository();
		} catch (Exception e) {
			throw new GWException("Failed to initialize repository");
		}
		try {
			GWSettingInfo.init();
		} catch (Exception e) {
			if (e instanceof GWException) {
				throw new GWException("Failed to read properties file: " + ((GWException) e).getMessage());
			} else {
				throw new GWException("Failed to read properties file");
			}
		}

	}
}
