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

package com.toshiba.mwcloud.gs.tools.webapi.controller;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("application.properties")
public class WebAPICustomizePort {

	@Value("${adminHome}")
	private String adminHome;

	@Value("${propertyFilePath}")
	private String propertyFilePath;

	@Value(value = "${server.ssl.key-store:}")
	private String sslKeyStore;

	@Value("${server.port}")
	int httpsPort;

	/**
	 * Create a Tomcat Servlet Web Server bean.
	 *
	 * @return Tomcat Servlet Web Server
	 */
	@Bean
	@ConditionalOnProperty("${server.ssl.enabled:true}")
	public ServletWebServerFactory servletContainer() {
		boolean needRedirectToHttps = sslKeyStore != null && !sslKeyStore.isEmpty();

		TomcatServletWebServerFactory tomcat = null;

		if (!needRedirectToHttps) {
			tomcat = new TomcatServletWebServerFactory();
			return tomcat;
		}

		tomcat =
				new TomcatServletWebServerFactory() {

					@Override
					protected void postProcessContext(Context context) {
						SecurityConstraint securityConstraint = new SecurityConstraint();
						securityConstraint.setUserConstraint("CONFIDENTIAL");
						SecurityCollection collection = new SecurityCollection();
						collection.addPattern("/*");
						securityConstraint.addCollection(collection);
						context.addConstraint(securityConstraint);
					}
				};
		tomcat.addAdditionalTomcatConnectors(redirectConnector());
		return tomcat;
	}

	private Connector redirectConnector() {
		Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
		connector.setScheme("https");
		connector.setPort(httpsPort);
		return connector;
	}
}
