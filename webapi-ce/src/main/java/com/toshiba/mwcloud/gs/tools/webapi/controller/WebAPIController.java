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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toshiba.mwcloud.gs.ContainerType;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWContainerInfo;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWContainerListOuput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWPutRowOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWQueryOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWQueryParams;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWSortCondition;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWTQLInput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWSQLInput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWSQLOutput;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWBadRequestException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWException;
import com.toshiba.mwcloud.gs.tools.webapi.service.WebAPIService;

/**
 * Controller handles request.
 * 
 */

@RestController
@RequestMapping("${basePath}" + "/" + "${version}")
public class WebAPIController {

	/**
	 * Object to process functions of web API
	 */
	@Autowired
	private WebAPIService webAPIServiceImpl;

	/**
	 * Object to map data
	 */
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * [SE3] Check connection to database.
	 * 
	 * <br>
	 * <br>
	 * <b>Processing flow:</b>
	 * <ol>
	 * <li>Check connection by calling
	 * {@link WebAPIService#testConnection(String, String, String, Long)}
	 * function.</li>
	 * </ol>
	 * 
	 * @param authorization
	 *            basic authentication
	 * @param cluster
	 *            name of cluster
	 * @param database
	 *            name of database
	 * @param timeout
	 *            limit time to get the connection to database (optional)
	 * @return a {@link ResponseEntity} object with status {@link HttpStatus#OK}
	 * @throws GSException
	 *             internal GridDB exception
	 */
	@RequestMapping(value = "{cluster}/dbs/{database}/checkConnection", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
	public ResponseEntity<?> testConnection(
			@RequestHeader(name = "Authorization", required = false) String authorization,
			@PathVariable("cluster") String cluster, @PathVariable("database") String database,
			@PathParam("timeout") Long timeout) throws GSException {

		webAPIServiceImpl.testConnection(authorization, cluster, database, timeout);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * [SE4] Get list of the containers in a database.
	 * 
	 * <br>
	 * <br>
	 * <b>Processing flow:</b>
	 * <ol>
	 * <li>Get list of the containers in a database by calling
	 * WebAPIService#getListContainers(String, String, String, ContainerType)
	 * function.</li>
	 * </ol>
	 * 
	 * @param authorization
	 *            basic authentication
	 * @param cluster
	 *            name of cluster
	 * @param database
	 *            name of database
	 * @param type
	 *            type of {@link ContainerType} (optional)
	 * @param limit
	 *            number of containers (required)
	 * @param offset
	 *            offset to get list containers (optional)
	 * @param sort
	 *            order to sort the list of containers (optional)
	 * @return a {@link ResponseEntity} object with body is a {@link List} of
	 *         container names and status {@link HttpStatus#OK}
	 * @throws GSException
	 *             internal GridDB exception
	 */
	@RequestMapping(value = "{cluster}/dbs/{database}/containers", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
	public ResponseEntity<?> getContainerList(
			@RequestHeader(name = "Authorization", required = false) String authorization,
			@PathVariable("cluster") String cluster, @PathVariable("database") String database,
			@PathParam("type") ContainerType type, @PathParam("limit") Integer limit,
			@PathParam("offset") Integer offset, @PathParam("sort") GWSortCondition sort) throws GSException {

		if (null == limit) {
			throw new GWBadRequestException("'limit' is required");
		}
		GWContainerListOuput listContainers = webAPIServiceImpl.getListContainers(authorization, cluster, database,
				type, limit, offset, sort);
		return new ResponseEntity<>(listContainers, HttpStatus.OK);
	}

	/**
	 * [SE5] Get information of a container.
	 * 
	 * <br>
	 * <br>
	 * <b>Processing flow:</b>
	 * <ol>
	 * <li>Get information of the container by calling
	 * {@link WebAPIService#getContainerInfo(String, String, String, String)}
	 * function.</li>
	 * </ol>
	 * 
	 * @param authorization
	 *            basic authentication
	 * @param cluster
	 *            name of cluster
	 * @param database
	 *            name of database
	 * @param container
	 *            name of container
	 * @return a {@link ResponseEntity} object with body is a
	 *         {@link GWContainerInfo} and status {@link HttpStatus#OK}
	 * @throws GSException
	 *             internal GridDB exception
	 */
	@RequestMapping(value = "{cluster}/dbs/{database}/containers/{container}/info", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
	public ResponseEntity<?> getContainerInfo(
			@RequestHeader(name = "Authorization", required = false) String authorization,
			@PathVariable("cluster") String cluster, @PathVariable("database") String database,
			@PathVariable("container") String container) throws GSException {

		GWContainerInfo gwContainerInfo = webAPIServiceImpl.getContainerInfo(authorization, cluster, database,
				container);
		return new ResponseEntity<>(gwContainerInfo, HttpStatus.OK);
	}

	/**
	 * [SE6] Execute multiple TQLs.
	 * 
	 * <br>
	 * <br>
	 * <b>Processing flow:</b>
	 * <ol>
	 * <li>Execute multiple TQLs by calling WebAPIService.executeTQLs(String,
	 * String, String, List) function.</li>
	 * </ol>
	 * 
	 * @param authorization
	 *            basic authentication
	 * @param cluster
	 *            name of cluster
	 * @param database
	 *            name of database
	 * @param listTQL
	 *            a {@link List} of {@link GWTQLInput}
	 * @return a {@link ResponseEntity} object with body is a {@link List} of
	 *         {@link Object} and status {@link HttpStatus#OK}
	 * @throws GSException
	 *             internal GridDB exception
	 * @throws GWException
	 *             internal server exception
	 *             {@link HttpStatus#INTERNAL_SERVER_ERROR}
	 * @throws UnsupportedEncodingException
	 *             exception when encoding data type {@link String}
	 *             {@link HttpStatus#INTERNAL_SERVER_ERROR}
	 * @throws SQLException
	 *             exception when getting length of data with {@link GSType} is
	 *             BLOB {@link HttpStatus#INTERNAL_SERVER_ERROR}
	 */
	@RequestMapping(value = "{cluster}/dbs/{database}/tql", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public ResponseEntity<?> executeTQLs(@RequestHeader(name = "Authorization", required = false) String authorization,
			@PathVariable("cluster") String cluster, @PathVariable("database") String database,
			@RequestBody List<GWTQLInput> listTQL)
			throws GSException, GWException, UnsupportedEncodingException, SQLException {

		List<Object> gwTQLOutput = webAPIServiceImpl.executeTQLs(authorization, cluster, database, listTQL);
		return new ResponseEntity<>(gwTQLOutput, HttpStatus.OK);
	}

	/**
	 * [SE1] Get rows.
	 * 
	 * <br>
	 * <br>
	 * <b>Processing flow:</b>
	 * <ol>
	 * <li>Get rows by calling
	 * {@link WebAPIService#executeTQLs(String, String, String, List)} function.
	 * </li>
	 * </ol>
	 * 
	 * @param authorization
	 *            basic authentication
	 * @param cluster
	 *            name of cluster
	 * @param database
	 *            name of database
	 * @param container
	 *            name of container
	 * @param queryParams
	 *            a {@link GWQueryParams} object
	 * @return a {@link ResponseEntity} object with body is a
	 *         {@link GWQueryOutput} object and status {@link HttpStatus#OK}
	 * @throws GSException
	 *             internal GridDB exception
	 * @throws GWException
	 *             internal server exception
	 *             {@link HttpStatus#INTERNAL_SERVER_ERROR}
	 * @throws UnsupportedEncodingException
	 *             exception when encoding data type {@link String}
	 *             {@link HttpStatus#INTERNAL_SERVER_ERROR}
	 * @throws SQLException
	 *             exception when if there is an error accessing the length of
	 *             the BLOB with {@link HttpStatus#INTERNAL_SERVER_ERROR}
	 */
	@RequestMapping(value = "{cluster}/dbs/{database}/containers/{container}/rows", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public ResponseEntity<?> getRows(@RequestHeader(name = "Authorization", required = false) String authorization,
			@PathVariable("cluster") String cluster, @PathVariable("database") String database,
			@PathVariable("container") String container, @RequestBody GWQueryParams queryParams)
			throws GSException, GWException, UnsupportedEncodingException, SQLException {

		GWQueryOutput output = webAPIServiceImpl.getRows(authorization, cluster, database, container, queryParams);
		return new ResponseEntity<>(output, HttpStatus.OK);
	}

	/**
	 * [SE2] Put rows.
	 * 
	 * <br>
	 * <br>
	 * <b>Processing flow:</b>
	 * <ol>
	 * <li>Put rows by calling
	 * {@link WebAPIService#putRows(String, String, String, String, List)}
	 * function.</li>
	 * </ol>
	 * 
	 * @param authorization
	 *            basic authentication
	 * @param cluster
	 *            name of cluster
	 * @param database
	 *            name of database
	 * @param container
	 *            name of container
	 * @param data
	 *            a {@link String}
	 * @return a {@link ResponseEntity} object with body is a
	 *         {@link GWPutRowOutput} object and status {@link HttpStatus#OK}
	 * @throws IOException
	 *             an {@link IOException}
	 * @throws JsonMappingException
	 *             mapping JSON failed
	 * @throws JsonParseException
	 *             parse JSON failed
	 */
	@RequestMapping(value = "{cluster}/dbs/{database}/containers/{container}/rows", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8", produces = "application/json; charset=UTF-8")
	public ResponseEntity<?> putRows(@RequestHeader(name = "Authorization", required = false) String authorization,
			@PathVariable("cluster") String cluster, @PathVariable("database") String database,
			@PathVariable("container") String container, @RequestBody String data)
			throws JsonParseException, JsonMappingException, IOException {

		Object[][] _input = objectMapper.readValue(data, Object[][].class);
		List<List<Object>> input = new ArrayList<List<Object>>();
		for (Object[] row : _input) {
			input.add(Arrays.asList(row));
		}
		GWPutRowOutput output = webAPIServiceImpl.putRows(authorization, cluster, database, container, input);
		return new ResponseEntity<>(output, HttpStatus.OK);
	}

	/**
	 * [SE7] Delete rows.
	 * 
	 * <br>
	 * <br>
	 * <b>Processing flow:</b>
	 * <ol>
	 * <li>Delete rows by calling
	 * {@link WebAPIService#deleteRows(String, String, String, String, List)}
	 * function.</li>
	 * </ol>
	 * 
	 * @param authorization
	 *            basic authentication
	 * @param cluster
	 *            name of cluster
	 * @param database
	 *            name of database
	 * @param container
	 *            name of container
	 * @param listRowKeys
	 *            a {@link List} of row keys that's going to be deleted
	 * @return a {@link ResponseEntity} object with status
	 *         {@link HttpStatus#NO_CONTENT}
	 * @throws GSException
	 *             internal GridDB exception
	 * @throws ParseException
	 *             exception when converting {@link Object} to rowkey type
	 */
	@RequestMapping(value = "{cluster}/dbs/{database}/containers/{container}/rows", method = RequestMethod.DELETE, produces = "application/json; charset=UTF-8")
	public ResponseEntity<?> deleteRows(@RequestHeader(name = "Authorization", required = false) String authorization,
			@PathVariable("cluster") String cluster, @PathVariable("database") String database,
			@PathVariable("container") String container, @RequestBody List<Object> listRowKeys)
			throws GSException, ParseException {

		webAPIServiceImpl.deleteRows(authorization, cluster, database, container, listRowKeys);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * [SE8] Create a container.
	 * 
	 * <br>
	 * <br>
	 * <b>Processing flow:</b>
	 * <ol>
	 * <li>Create a container by calling
	 * {@link WebAPIService#createContainer(String, String, String, GWContainerInfo)}
	 * function.</li>
	 * </ol>
	 * 
	 * @param authorization
	 *            basic authentication
	 * @param cluster
	 *            name of cluster
	 * @param database
	 *            name of database
	 * @param gwContainerInfo
	 *            a {@link GWContainerInfo} object
	 * @return a {@link ResponseEntity} object with status
	 *         {@link HttpStatus#CREATED}
	 * @throws GSException
	 *             internal GridDB exception
	 */
	@RequestMapping(value = "{cluster}/dbs/{database}/containers", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public ResponseEntity<?> createContainer(
			@RequestHeader(name = "Authorization", required = false) String authorization,
			@PathVariable("cluster") String cluster, @PathVariable("database") String database,
			@RequestBody GWContainerInfo gwContainerInfo) throws GSException {

		webAPIServiceImpl.createContainer(authorization, cluster, database, gwContainerInfo);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	/**
	 * [SE9] Delete containers.
	 * 
	 * <br>
	 * <br>
	 * <b>Processing flow:</b>
	 * <ol>
	 * <li>Delete containers by calling
	 * {@link WebAPIService#deleteContainers(String, String, String, List)}
	 * function.</li>
	 * </ol>
	 * 
	 * @param authorization
	 *            basic authentication
	 * @param cluster
	 *            name of cluster
	 * @param database
	 *            name of database
	 * @param listContainers
	 *            a {@link String} of container name
	 * @return a {@link ResponseEntity} object with status
	 *         {@link HttpStatus#NO_CONTENT}
	 * @throws GSException
	 *             internal GridDB exception
	 */
	@RequestMapping(value = "{cluster}/dbs/{database}/containers", method = RequestMethod.DELETE, produces = "application/json; charset=UTF-8")
	public ResponseEntity<?> deleteContainers(
			@RequestHeader(name = "Authorization", required = false) String authorization,
			@PathVariable("cluster") String cluster, @PathVariable("database") String database,
			@RequestBody List<String> listContainers) throws GSException {

		webAPIServiceImpl.deleteContainers(authorization, cluster, database, listContainers);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * [CE] Execute multiple select-SQLs.
	 *
	 * <br>
	 * <br>
	 * <b>Processing flow:</b>
	 * <ol>
	 * <li>Execute SQLs by calling WebAPIService.executeSQLs(String, String,
	 * String, List) function.</li>
	 * </ol>
	 *
	 * @param authorization
	 *            basic authentication
	 * @param cluster
	 *            name of cluster
	 * @param database
	 *            name of database
	 * @param listSQLInput
	 *            a {@link List} of {@link GWSQLInput}
	 * @return a {@link ResponseEntity} object with body is a {@link List} of
	 *         {@link GWSQLOutput} and status {@link HttpStatus#OK}
	 * @throws GSException
	 *             internal server exception
	 *             {@link HttpStatus#INTERNAL_SERVER_ERROR}
	 * @throws SQLException a {@link SQLException}
	 * @throws UnsupportedEncodingException a {@link UnsupportedEncodingException}
	 */
	@RequestMapping(value = "{cluster}/dbs/{database}/sql", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public ResponseEntity<?> executeSQLs(@RequestHeader(name = "Authorization", required = false) String authorization,
			@PathVariable("cluster") String cluster, @PathVariable("database") String database,
			@RequestBody List<GWSQLInput> listSQLInput) throws GSException, SQLException, UnsupportedEncodingException {

		List<GWSQLOutput> gwOutput = webAPIServiceImpl.executeSQLs(authorization, cluster, database, listSQLInput);
		return new ResponseEntity<>(gwOutput, HttpStatus.OK);
	}

	/**
	 * [SE15] Execute SQL select. <br>
	 * <br>
	 * <b>Processing flow:</b>
	 *
	 * <ol>
	 *   <li>Execute SQLs select by calling webApiServiceImpl.executeSQLs(String, String, String, List) function.</li>
	 * </ol>
	 *
	 * @param authorization basic authentication
	 * @param cluster name of cluster
	 * @param database name of database
	 * @param listSqlInput a {@link List} of {@link GWSQLInput}
	 * @return a {@link ResponseEntity} object with body is a {@link List} of {@link GWSQLOutput} and
	 *     status {@link HttpStatus#OK}
	 * @throws GSException internal server exception {@link HttpStatus#INTERNAL_SERVER_ERROR}
	 * @throws SQLException a {@link SQLException}
	 * @throws UnsupportedEncodingException a {@link UnsupportedEncodingException}
	 */
	@RequestMapping(
			value = "{cluster}/dbs/{database}/sql/select",
			method = RequestMethod.POST,
			produces = "application/json; charset=UTF-8")
	public ResponseEntity<?> executeSqlSelect(
			@RequestHeader(name = "Authorization", required = false) String authorization,
			@PathVariable("cluster") String cluster,
			@PathVariable("database") String database,
			@RequestBody List<GWSQLInput> listSqlInput) throws GSException, SQLException, UnsupportedEncodingException {

		List<GWSQLOutput> gwOutput = webAPIServiceImpl.executeSQLs(authorization, cluster, database, listSqlInput);
		return new ResponseEntity<>(gwOutput, HttpStatus.OK);
	}

}
