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


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GWContextListener;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GWJsonParser;

@Configuration
public class WebAPIConfiguration {

	/**
	 * Create a bean to execute {@link GWContextListener}
	 * 
	 * @return a {@link GWContextListener}
	 */
	@Bean
	public GWContextListener executeGWContextListener() {
	   return new GWContextListener();
	}
	
	/**
	 * Create a bean to map data
	 * 
	 * @return a {@link ObjectMapper}
	 */
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		GWJsonParser parser = new GWJsonParser();
		SimpleModule simpleModule = new SimpleModule("GWJsonParser");
		simpleModule.addDeserializer(Object[][].class, parser);
		mapper.registerModule(simpleModule);
		return mapper;
	}
	
}
