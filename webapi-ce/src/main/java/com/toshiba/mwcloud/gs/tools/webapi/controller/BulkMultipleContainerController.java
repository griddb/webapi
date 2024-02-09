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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWBulkMultipleContainerInput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWBulkMultipleContainerOuput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWBulkPutRow;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWBulkPutRowOutput;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWException;
import com.toshiba.mwcloud.gs.tools.webapi.service.BulkMultipleContainerService;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/** Controller handles request for Bulk Multiple Container Service. */
@RestController
@RequestMapping("${basePath}" + "/" + "${version}")
public class BulkMultipleContainerController {
  /** Bulk Multiple Container Service. */
  @Autowired private BulkMultipleContainerService bulkMultipleContainerServiceImpl;

  /**
   * [SE10] Get Bulk Multiple Container Service. <br>
   * <br>
   * <b>Processing flow:</b>
   *
   * <ol>
   *   <li>Get Bulk Multiple Container Service by calling {@link
   *       BulkMultipleContainerService#getRowsMultipleContainers(String, String, String, List)}
   *       function.
   * </ol>
   *
   * @param authorization basic authentication
   * @param cluster name of cluster
   * @param database name of database
   * @param bulkMultipleContainer a {@link List} of {@link GWBulkMultipleContainerInput}
   * @return a {@link ResponseEntity} object with status {@link HttpStatus#OK}
   * @throws GSException internal GridStore GridDB exception
   * @throws GWException internal GridDB exception
   * @throws UnsupportedEncodingException internal unsupported encoding exception
   * @throws SQLException internal SQL exception
   * @throws ParseException internal parse exception
   */
  @RequestMapping(
      value = "{cluster}/dbs/{database}/containers/rows",
      method = RequestMethod.POST,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<?> getRowsMultipleContainers(
      @RequestHeader(name = "Authorization", required = false) String authorization,
      @PathVariable("cluster") String cluster,
      @PathVariable("database") String database,
      @RequestBody List<GWBulkMultipleContainerInput> bulkMultipleContainer)
      throws GSException, GWException, UnsupportedEncodingException, SQLException, ParseException {

    List<GWBulkMultipleContainerOuput> output =
        bulkMultipleContainerServiceImpl.getRowsMultipleContainers(
            authorization, cluster, database, bulkMultipleContainer);
    return new ResponseEntity<>(output, HttpStatus.OK);
  }

  /**
   * [SE11] Put rows to multiple containers. <br>
   * <br>
   * <b>Processing flow:</b>
   *
   * <ol>
   *   <li>Put rows by calling {@link BulkMultipleContainerService#putRowsMultipleContainers(String,
   *       String, String, List)} function.
   * </ol>
   *
   * @param authorization basic authentication
   * @param cluster name of cluster
   * @param database name of database
   * @param input a {@link GWBulkPutRow}
   * @return a {@link ResponseEntity} object with body is a {@link GWBulkPutRowOutput} object and
   *     status {@link HttpStatus#OK}
   * @throws IOException an {@link IOException}
   * @throws JsonMappingException mapping JSON failed
   * @throws JsonParseException parse JSON failed
   */
  @RequestMapping(
      value = "{cluster}/dbs/{database}/containers/rows",
      method = RequestMethod.PUT,
      consumes = "application/json; charset=UTF-8",
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<?> putRows(
      @RequestHeader(name = "Authorization", required = false) String authorization,
      @PathVariable("cluster") String cluster,
      @PathVariable("database") String database,
      @RequestBody List<GWBulkPutRow> input)
      throws JsonParseException, JsonMappingException, IOException {

    List<GWBulkPutRowOutput> output =
        bulkMultipleContainerServiceImpl.putRowsMultipleContainers(
            authorization, cluster, database, input);
    return new ResponseEntity<>(output, HttpStatus.OK);
  }
}
