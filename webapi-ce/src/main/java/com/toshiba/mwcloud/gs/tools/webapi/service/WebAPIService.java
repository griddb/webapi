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

package com.toshiba.mwcloud.gs.tools.webapi.service;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.ContainerType;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWContainerInfo;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWContainerListOuput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWPutRowOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWQueryOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWQueryParams;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWSQLInput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWSQLOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWSortCondition;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWTQLInput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWTQLOutput;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWBadRequestException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWNotFoundException;
import com.toshiba.mwcloud.gs.tools.webapi.utils.ConversionUtils;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GWUser;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GridStoreUtils;
import com.toshiba.mwcloud.gs.tools.webapi.utils.Validation;

public interface WebAPIService {

	/**
	 * Check the connection to database.
	 * 
	 * <br><br>
	 * <b>Processing flow:</b>
	 * <ol>
	 *  <li>Check authorization</li>
	 *  <li>Call function {@link GWUser#getUserfromAuthorization(String)} to get {@link GWUser} from authorization</li>
	 *  <li>Call function {@link GridStoreUtils#getGridStore(String, String, String, String)} to get the information of the target cluster</li>
	 *  <li>If timeout is specified, create a thread to wait for the connection and listen the result from that thread</li>
	 *  <li>If timeout is not specified, call {@link GridStore#getPartitionController()}  check the connection</li>
	 * </ol>
	 * @param authorization basic authentication
	 * @param cluster name of cluster
	 * @param database name of database
	 * @param timeout the connection timeout
	 * @throws GSException internal server exception
	 */
	public void testConnection(String authorization, String cluster, String database, Long timeout) throws GSException;

	/**
	 * Get list of the containers of a database.
	 * 
	 * <br><br>
	 * <b>Processing flow:</b>
	 * <ol>
	 *  <li>Check authorization</li>
	 *  <li>Call function {@link GWUser#getUserfromAuthorization(String)} to get {@link GWUser} from authorization</li>
	 *  <li>Call function {@link GridStoreUtils#getGridStore(String, String, String, String)} to get the information of the target cluster</li>
	 *  <li>Check parameters: {@code offset}, {@code limit}. If they are not valid, throw {@link GWBadRequestException}</li>
	 *  <li>Get the partition count</li>
	 *  <li>For each partition, get all container names from containers that theirs type is specified</li>
	 *  <li>If the list of container names obtained is empty, return an empty list</li>
	 *  <li>Sort the list if {@code sort} is not null. If it is, sort ASCENDING</li>
	 *  <li>If offset and/or limit is specified, get the list with offset and limit</li>
	 *  <li>Return list of containers</li>
	 * </ol>
	 * @param authorization basic authentication
	 * @param cluster name of cluster
	 * @param database name of database
	 * @param type type of container
	 * @param limit limit number of the list of containers
	 * @param offset offset of the list of containers
	 * @param sort type of sorting the list of containers
	 * @return A {@link List} of container name
	 * @throws GSException unable to get list of containers
	 */
	public GWContainerListOuput getListContainers(String authorization, String cluster, String database, ContainerType type,
			int limit, Integer offset, GWSortCondition sort) throws GSException;

	/**
	 * Get container information.
	 * 
	 * <br><br>
	 * <b>Processing flow:</b>
	 * <ol>
	 *  <li>Check authorization</li>
	 *  <li>Call function {@link GWUser#getUserfromAuthorization(String)} to get {@link GWUser} from authorization</li>
	 *  <li>Call function {@link GridStoreUtils#getGridStore(String, String, String, String)} to get the information of the target cluster</li>
	 *  <li>Call function {@link GridStore#getContainerInfo(String)} to check the container information</li>
	 *  <li>Call function {@link ConversionUtils#convertToGWContainerInfo(ContainerInfo)} to convert the container information obtained above to {@link GWContainerInfo}</li>
	 *  <li>Return the container information</li>
	 * </ol>
	 * @param authorization basic authentication
	 * @param cluster name of cluster
	 * @param database name of database
	 * @param container name of container
	 * @return {@link GWContainerInfo} of a container
	 * @throws GSException unable to get container information
	 */
	public GWContainerInfo getContainerInfo(String authorization, String cluster, String database, String container) throws GSException;

	/**
	 * Execute multiple TQLs.
	 * 
	 * <br><br>
	 * <b>Processing flow:</b>
	 * <ol>
	 *  <li>Check authorization</li>
	 *  <li>Call function {@link GWUser#getUserfromAuthorization(String)} to get {@link GWUser} from authorization</li>
	 *  <li>Call function {@link GridStoreUtils#getGridStore(String, String, String, String)} to get the information of the target cluster</li>
	 *  <li>If the size of {@code listTQLs} is larger than the maximum of number of TQLs that can be executed, throw a {@link GWBadRequestException}</li>
	 *  <li>Call function {@link GridStore#getContainerInfo(String)} to check the container information</li>
	 *  <li>For each {@link GWTQLInput}, get the container name, statement, selected fields</li>
	 *  <li>Call function executeTQL(String, String, String, String, String, String, ArrayList) to execute each query and add result to a {@link List} of {@link GWTQLOutput}, if size of response is over the limitation or there is error syntax in TQLs, throw a {@link GWBadRequestException}</li>
	 *  <li>Return a list of {@link GWTQLOutput}</li>
	 * </ol>
	 * @param authorization basic authentication
	 * @param cluster name of cluster
	 * @param database name of database
	 * @param listTQLs a {@link List} of {@link GWTQLInput}
	 * @return a {@link List} of {@link GWTQLOutput}
	 * @throws GSException internal server exception
	 * @throws GWException internal server exception
	 * @throws UnsupportedEncodingException exception when encoding data type {@link String}
	 * @throws SQLException exception when getting length of data with {@link GSType} is BLOB
	 */
	public List<Object> executeTQLs(String authorization, String cluster, String database,
			List<GWTQLInput> listTQLs) throws GSException, GWException, UnsupportedEncodingException, SQLException;

	/**
	 * Get rows with limit, offset, condition and sort.
	 * 
	 * <br><br>
	 * <b>Processing flow:</b>
	 * <ol>
	 *  <li>Check authorization</li>
	 *  <li>Call function {@link GWUser#getUserfromAuthorization(String)} to get {@link GWUser} from authorization</li>
	 *  <li>Call function {@link Validation#validateInputParams(GWQueryParams)} to validate input parameters</li>
	 *  <li>Call function {@link GridStoreUtils#getGridStore(String, String, String, String)} to get the information of the target cluster</li>
	 *  <li>Call function {@link GridStore#getContainerInfo(String)} to get the container information</li>
	 *  <li>If container information is null, throw a {@link GWNotFoundException} exception</li>
	 *  <li>Call function Container.query(String) to execute query</li>
	 *  <li>Call function {@code rowSetToTqlResult(RowSet, ContainerInfo)} set the result of query to {@link GWTQLOutput}</li>
	 *  <li>Return the TQL result</li>
	 * </ol>
	 * @param authorization basic authentication
	 * @param cluster name of cluster
	 * @param database name of database
	 * @param container name of container
	 * @param queryParams a {@link GWQueryParams} object
	 * @return a {@link GWQueryOutput} object
	 * @throws GSException internal server exception
	 * @throws GWException internal server exception
	 * @throws UnsupportedEncodingException exception when encoding data type {@link String}
	 * @throws SQLException exception when getting length of data with {@link GSType} is BLOB
	 */
	public GWQueryOutput getRows(String authorization, String cluster, String database, String container,
			GWQueryParams queryParams) throws GSException, GWException, UnsupportedEncodingException, SQLException;


	/**
	 * Put data into database.
	 * 
	 * <br><br>
	 * <b>Processing flow:</b>
	 * <ol>
	 *  <li>Check authorization</li>
	 *  <li>Call function {@link GWUser#getUserfromAuthorization(String)} to get {@link GWUser} from authorization</li>
	 *  <li>Call function {@link GridStoreUtils#getGridStore(String, String, String, String)} to get the information of the target cluster</li>
	 *  <li>If container info is null, throw a {@link GWNotFoundException} exception</li>
	 *  <li>For each column in the container info, if its type is BLOB, throw a {@link GWException} exception</li>
	 *  <li>For each list of object in {@code input}, call function setRowValue() to put it into a {@link List} of Row</li>
	 *  <li>Call function {@link Container#put(Object)} to put data into database</li>
	 * </ol>
	 * @param authorization basic authentication
	 * @param cluster name of cluster
	 * @param database name of database
	 * @param container name of container
	 * @param input a {@link List} of {@link List} of {@link Object}
	 * @return number of input rows
	 * @throws GSException internal server exception
	 * @throws UnsupportedEncodingException exception when encoding data type {@link String}
	 */
	public GWPutRowOutput putRows(String authorization, String cluster, String database, String container,
			List<List<Object>> input) throws GSException, UnsupportedEncodingException;

	/**
	 * Delete rows by row keys.
	 * 
	 * <br><br>
	 * <b>Processing flow:</b>
	 * <ol>
	 *  <li>Check authorization</li>
	 *  <li>Call function {@link GWUser#getUserfromAuthorization(String)} to get {@link GWUser} from authorization</li>
	 *  <li>Call function {@link GridStoreUtils#getGridStore(String, String, String, String)} to get the information of the target cluster</li>
	 *  <li>For each row key in {@code listRowKeys}, call function GridStore.getContainer(String).remove(String) to delete corresponding row</li>
	 * </ol>
	 * @param authorization basic authentication
	 * @param cluster name of cluster
	 * @param database name of database
	 * @param container name of container
	 * @param listRowKeys a {@link List} of row keys
	 * @throws GSException internal server exception
	 * @throws ParseException when parsing row key failed
	 */
	public void deleteRows(String authorization, String cluster, String database, String container,
			List<Object> listRowKeys) throws GSException, ParseException;

	/**
	 * Delete containers.
	 * 
	 * <br><br>
	 * <b>Processing flow:</b>
	 * <ol>
	 *  <li>Check authorization</li>
	 *  <li>Call function {@link GWUser#getUserfromAuthorization(String)} to get {@link GWUser} from authorization</li>
	 *  <li>Call function {@link GridStoreUtils#getGridStore(String, String, String, String)} to get the information of the target cluster</li>
	 *  <li>For each container name in list containers, call function {@link GridStore#dropContainer(String)} to delete corresponding container</li>
	 * </ol>
	 * @param authorization basic authentication
	 * @param cluster name of cluster
	 * @param database name of database
	 * @param listContainers list of containers
	 * @throws GSException internal server exception
	 */
	public void deleteContainers(String authorization, String cluster, String database, List<String> listContainers) throws GSException;

	/**
	 * Create a container.
	 * 
	 * <br><br>
	 * <b>Processing flow:</b>
	 * <ol>
	 *  <li>Check authorization</li>
	 *  <li>Call function {@link GWUser#getUserfromAuthorization(String)} to get {@link GWUser} from authorization</li>
	 *  <li>Call function {@link GridStoreUtils#getGridStore(String, String, String, String)} to get the information of the target cluster</li>
	 *  <li>Call function {@link GridStore#getContainerInfo(String)} to get the container information</li>
	 *  <li>If container information is not null, throw a {@link GWBadRequestException} exception</li>
	 *  <li>Call function {@code ConversionUtils#convertToContainerInfo(GWContainerInfo)} to convert {@code gwContainerInfo} to {@link ContainerInfo}</li>
	 *  <li>Call function {@link GridStore#putContainer(String, ContainerInfo, boolean)} to create a containers</li>
	 * </ol>
	 * @param authorization basic authentication
	 * @param cluster name of cluster
	 * @param database name of database
	 * @param gwContainerInfo a {@link GWContainerInfo} object
	 * @throws GSException list of containers
	 */
	public void createContainer(String authorization, String cluster, String database, GWContainerInfo gwContainerInfo) throws GSException;
	
	/**
	 * Execute multiple SQLs.
	 *
	 * <br><br>
	 * <b>Processing flow:</b>
	 * <ol>
	 *  <li>Check authorization</li>
	 *  <li>Call function {@link GWUser#getUserfromAuthorization(String)} to get {@link GWUser} from authorization</li>
	 *  <li>Call function {@link GridStoreUtils#getGridStore(String, String, String, String)} to get the information of the target cluster</li>
	 *  <li>Call function checkAuthentication(GridStore) to check the authentication</li>
	 *  <li>If the size of {@code listSQLInput} is larger than the maximum of number of SQLs that can be executed, throw a {@link GWBadRequestException}</li>
	 *  <li>For each {@link GWSQLInput} in the {@code listSQLInput}, call function {@code executeSQL(String, String, String, String, GWSQLOutput)} to execute each SQL</li>
	 *  <li>Return a list of {@link GWSQLOutput}</li>
	 * </ol>
	 * @param authorization basic authentication
	 * @param cluster name of cluster
	 * @param database name of database
	 * @param listSQLInput a {@link List} of {@link GWSQLInput}
	 * @return a {@link List} of {@link GWSQLOutput}
	 * @throws GSException internal server exception
	 * @throws SQLException a {@link SQLException}
	 * @throws UnsupportedEncodingException a {@link UnsupportedEncodingException}
	 */
	public List<GWSQLOutput> executeSQLs(String authorization, String cluster, String database, List<GWSQLInput> listSQLInput) throws GSException, SQLException, UnsupportedEncodingException;
}
