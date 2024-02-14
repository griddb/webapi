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

import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.experimental.ExperimentalTool;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWPutRowOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWQueryOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWQueryParams;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWTQLOutput;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWBadRequestException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWNotFoundException;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GWUser;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GridStoreUtils;
import com.toshiba.mwcloud.gs.tools.webapi.utils.Validation;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/** Service for put and get rows with BLOB type. */
public interface BlobHandleService {

  /**
   * Put data into database. <br>
   * <br>
   * <b>Processing flow:</b>
   *
   * <ol>
   *   <li>Check authorization
   *   <li>Call function {@link GWUser#getUserfromAuthorization(String)} to get {@link GWUser} from
   *       authorization
   *   <li>Call function {@link GridStoreUtils#getGridStore(String, String, String, String)} to get
   *       the information of the target cluster
   *   <li>If container info is null, throw a {@link GWNotFoundException} exception
   *   <li>For each list of object in {@code input}, call function setRowValue() to put it into a
   *       {@link List} of Row
   *   <li>Call function {@link Container#put(Object)} to put data into database
   * </ol>
   *
   * @param authorization basic authentication
   * @param cluster name of cluster
   * @param database name of database
   * @param container name of container
   * @param rows a {@link List} of {@link List} of {@link Object}
   * @param file MultipartFile that store blob data
   * @return number of input rows
   * @throws GSException internal server exception
   * @throws UnsupportedEncodingException exception when encoding data type {@link String}
   */
  GWPutRowOutput putRows(
      String authorization,
      String cluster,
      String database,
      String container,
      List<List<Object>> rows,
      MultipartFile file)
      throws GSException, UnsupportedEncodingException;

  /**
   * Get rows for BLOB data with limit, offset, condition, sort and column name. <br>
   * <br>
   * <b>Processing flow:</b>
   *
   * <ol>
   *   <li>Check authorization
   *   <li>Call function {@link GWUser#getUserfromAuthorization(String)} to get {@link GWUser} from
   *       authorization
   *   <li>Call function {@link Validation#validateInputParams(GWQueryParams)} to validate input
   *       parameters
   *   <li>Call function {@link GridStoreUtils#getGridStore(String, String, String, String)} to get
   *       the information of the target cluster
   *   <li>Call function {@link ExperimentalTool#getExtendedContainerInfo(GridStore, String)} to get
   *       the extended container information
   *   <li>Call function {@link GridStore#getContainerInfo(String)} to get the container information
   *   <li>If container information is null, throw a {@link GWNotFoundException} exception
   *   <li>If container is partition container, throw a {@link GWBadRequestException} exception
   *   <li>Call function Container.query(String) to execute query
   *   <li>Call function {@code rowSetToTqlResult(RowSet, ContainerInfo)} set the result of query to
   *       {@link GWTQLOutput}
   *   <li>Return the TQL result
   * </ol>
   *
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
  GWQueryOutput getRowsTypeBlob(
      String authorization,
      String cluster,
      String database,
      String container,
      GWQueryParams queryParams)
      throws GSException, GWException, UnsupportedEncodingException, SQLException, IOException;
}
