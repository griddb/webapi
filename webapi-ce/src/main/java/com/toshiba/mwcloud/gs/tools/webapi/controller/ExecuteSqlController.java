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

import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWSQLInput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWSQLOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.SqlDdlDclOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.SqlUpdateOutput;
import java.sql.SQLException;
import java.util.List;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.toshiba.mwcloud.gs.tools.webapi.service.ExecuteSqlService;

/** Controller handles request for Sql update service. */
@RestController
@RequestMapping("${basePath}" + "/" + "${version}")
public class ExecuteSqlController {

  /** Object to process functions of web API. */
  @Autowired private ExecuteSqlService executeSqlService;

  /**
   * [SE14] Execute multiple update-SQLs. <br>
   * <br>
   * <b>Processing flow:</b>
   *
   * <ol>
   *   <li>Execute SQLs by calling ExecuteSqlService.ExecuteSqlUpdate(String, String, String,
       List) function.
 </ol>
   *
   * @param authorization basic authentication
   * @param cluster name of cluster
   * @param database name of database
   * @param listSqlInput a {@link List} of {@link GWSQLInput}
   * @return a {@link ResponseEntity} object with body is a {@link List} of {@link SqlUpdateOutput}
   *     and status {@link HttpStatus#OK}
   * @throws GSException internal server exception {@link HttpStatus#INTERNAL_SERVER_ERROR}
   * @throws SQLException a {@link SQLException}
   */
  @RequestMapping(
      value = "{cluster}/dbs/{database}/sql/update",
      method = RequestMethod.POST,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<?> executeSqls(
      @RequestHeader(name = "Authorization", required = false) String authorization,
      @PathVariable("cluster") String cluster,
      @PathVariable("database") String database,
      @RequestBody List<GWSQLInput> listSqlInput)
      throws GSException, SQLException {

    List<SqlUpdateOutput> output =
        executeSqlService.executeSqlUpdate(
            authorization, cluster, database, listSqlInput);
    return new ResponseEntity<>(output, HttpStatus.OK);
  }

  /**
   * [SE16] Execute-DDLs. <br>
   * <br>
   * <b>Processing flow:</b>
   *
   * <ol>
   *   <li>Execute DDLs by calling ExecuteSqlService.ExecuteDDL(String, String, String,
   *       List) function.
   * </ol>
   *
   * @param authorization basic authentication
   * @param cluster name of cluster
   * @param database name of database
   * @param listSqlInput a {@link List} of {@link GWSQLInput}
   * @return a {@link ResponseEntity} object with body is a {@link List} of {@link SqlDdlDclOutput}
   *     and status {@link HttpStatus#OK}
   * @throws GSException internal server exception {@link HttpStatus#INTERNAL_SERVER_ERROR}
   * @throws SQLException a {@link SQLException}
   */
  @RequestMapping(
      value = "{cluster}/dbs/{database}/sql/ddl",
      method = RequestMethod.POST,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<?> executeDDLs(
      @RequestHeader(name = "Authorization", required = false) String authorization,
      @PathVariable("cluster") String cluster,
      @PathVariable("database") String database,
      @RequestBody List<GWSQLInput> listSqlInput)
      throws GSException, SQLException {

    List<SqlDdlDclOutput> output =
        executeSqlService.executeDDL(
            authorization, cluster, database, listSqlInput);
    return new ResponseEntity<>(output, HttpStatus.OK);
  }
  
  /**
   * [SE17] Execute-DCLs. <br>
   * <br>
   * <b>Processing flow:</b>
   *
   * <ol>
   *   <li>Execute DCLs by calling ExecuteSqlService.ExecuteDCL(String, String, String,
   *       List) function.
   * </ol>
   *
   * @param authorization basic authentication
   * @param cluster name of cluster
   * @param database name of database
   * @param listSqlInput a {@link List} of {@link GWSQLInput}
   * @return a {@link ResponseEntity} object with body is a {@link List} of {@link SqlDdlDclOutput}
   *     and status {@link HttpStatus#OK}
   * @throws GSException internal server exception {@link HttpStatus#INTERNAL_SERVER_ERROR}
   * @throws SQLException a {@link SQLException}
   */
  @RequestMapping(
      value = "{cluster}/dbs/{database}/sql/dcl",
      method = RequestMethod.POST,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<?> executeDCLs(
      @RequestHeader(name = "Authorization", required = false) String authorization,
      @PathVariable("cluster") String cluster,
      @PathVariable("database") String database,
      @RequestBody List<GWSQLInput> listSqlInput)
      throws GSException, SQLException {

    List<SqlDdlDclOutput> output =
        executeSqlService.executeDCL(
            authorization, cluster, database, listSqlInput);
    return new ResponseEntity<>(output, HttpStatus.OK);
  }
  
  /**
   * [SE18] Execute DML Update. <br>
   * <br>
   * <b>Processing flow:</b>
   *
   * <ol>
   *   <li>Execute DML update statements by calling ExecuteSqlService.ExecuteDML(String, String, String,
   *       List) function.
   * </ol>
   *
   * @param authorization basic authentication
   * @param cluster name of cluster
   * @param database name of database
   * @param listSqlInput a {@link List} of {@link GWSQLInput}
   * @return a {@link ResponseEntity} object with body is a {@link List} of {@link SqlUpdateOutput}
   *     and status {@link HttpStatus#OK}
   * @throws GSException internal server exception {@link HttpStatus#INTERNAL_SERVER_ERROR}
   * @throws SQLException a {@link SQLException}
   */
  @RequestMapping(
      value = "{cluster}/dbs/{database}/sql/dml/update",
      method = RequestMethod.POST,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<?> executeDmlUpdate(
      @RequestHeader(name = "Authorization", required = false) String authorization,
      @PathVariable("cluster") String cluster,
      @PathVariable("database") String database,
      @RequestBody List<GWSQLInput> listSqlInput)
      throws GSException, SQLException {

    List<SqlUpdateOutput> output =
        executeSqlService.executeDmlUpdate(
            authorization, cluster, database, listSqlInput);
    return new ResponseEntity<>(output, HttpStatus.OK);
  }

  /**
   * [SE19] Execute DML Query. <br>
   * <br>
   * <b>Processing flow:</b>
   *
   * <ol>
   *   <li>Execute DMLs select by calling webApiServiceImpl.executeSQLs(String, String,
   *       String, List) function.
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
      value = "{cluster}/dbs/{database}/sql/dml/query",
      method = RequestMethod.POST,
      produces = "application/json; charset=UTF-8")
  public ResponseEntity<?> executeDmlQuery(
      @RequestHeader(name = "Authorization", required = false) String authorization,
      @PathVariable("cluster") String cluster,
      @PathVariable("database") String database,
      @RequestBody List<GWSQLInput> listSqlInput)
      throws GSException, SQLException, UnsupportedEncodingException {

    List<GWSQLOutput> gwOutput =
        executeSqlService.executeDmlQuery(
            authorization, cluster, database, listSqlInput);
    return new ResponseEntity<>(gwOutput, HttpStatus.OK);
  }

}
