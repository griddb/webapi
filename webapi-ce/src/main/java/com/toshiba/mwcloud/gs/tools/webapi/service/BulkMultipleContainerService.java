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

import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWBulkMultipleContainerInput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWBulkMultipleContainerOuput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWBulkPutRow;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWBulkPutRowOutput;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWNotFoundException;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GWUser;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GridStoreUtils;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

public interface BulkMultipleContainerService {
  /**
   * Get Bulk Multiple Container Service. <br>
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
   *   <li>Call function setRowKeyPredicate() and getResultRowsMultipleContainers() to get Bulk
   *       Multiple Container Service
   * </ol>
   *
   * @param authorization basic authentication
   * @param cluster name of cluster
   * @param database name of database
   * @param bulkMultipleContainerInput a {@link List} of {@link GWBulkMultipleContainerInput}
   * @return {@link List} of {@link GWBulkMultipleContainerOuput}
   * @throws GSException internal GridStore GridDB exception
   * @throws GWException internal GridDB exception
   * @throws UnsupportedEncodingException internal unsupported encoding exception
   * @throws SQLException internal SQL exception
   * @throws ParseException internal parse exception
   */
  public List<GWBulkMultipleContainerOuput> getRowsMultipleContainers(
      String authorization,
      String cluster,
      String database,
      List<GWBulkMultipleContainerInput> bulkMultipleContainerInput)
      throws GSException, GWException, UnsupportedEncodingException, SQLException, ParseException;

  /**
   * Put data into multiple container in database. <br>
   * <br>
   * <b>Processing flow:</b>
   *
   * <ol>
   *   <li>Check authorization
   *   <li>Call function {@link GWUser#getUserfromAuthorization(String)} to get {@link GWUser} from
   *       authorization
   *   <li>Call function {@link GridStoreUtils#getGridStore(String, String, String, String)} to get
   *       the information of the target cluster
   *   <li>For each container in list input container
   *   <li>If container info is null, throw a {@link GWNotFoundException} exception
   *   <li>For each column in the container info, if its type is BLOB, throw a {@link GWException}
   *       exception
   *   <li>For each list of object in {@code input}, call function WebAPIServiceImpl.setRowValue()
   *       to put it into a {@link List} of Row
   *   <li>Call function {@link GridStore#multiPut(java.util.Map)} to put data into
   *       database
   * </ol>
   *
   * @param authorization basic authentication
   * @param cluster name of cluster
   * @param database name of database
   * @param input a {@link List} of {@link GWBulkPutRow}
   * @return {@link List} of {@link GWBulkPutRowOutput}
   * @throws GSException internal server exception
   * @throws UnsupportedEncodingException exception when encoding data type {@link String}
   */
  public List<GWBulkPutRowOutput> putRowsMultipleContainers(
      String authorization, String cluster, String database, List<GWBulkPutRow> input)
      throws GSException, UnsupportedEncodingException;
}
