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

package com.toshiba.mwcloud.gs.tools.webapi.service.impl;

import ch.qos.logback.classic.Logger;

import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWSQLInput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWSQLOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.SqlDdlDclOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.SqlUpdateOutput;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWBadRequestException;
import com.toshiba.mwcloud.gs.tools.webapi.utils.ConnectionUtils;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GWSettingInfo;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GWUser;
import com.toshiba.mwcloud.gs.tools.webapi.utils.Messages;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.toshiba.mwcloud.gs.tools.webapi.service.ExecuteSqlService;

@Service
public class ExecuteSqlServiceImpl implements ExecuteSqlService {

  private static final Logger logger =
      (Logger) LoggerFactory.getLogger(ExecuteSqlServiceImpl.class);
  @Value("${sql.jdbc.statement.timeout:0}")
  private int sqlTimeOut;
  private static final int SUCCESS = 1;
  private static final int FAILED = 0;
  private static final String DDL_REGEX = "(create|alter|drop).*";
  private static final String DCL_REGEX = "(grant|revoke|set +password).*";
  private static final String DML_UPDATE_REGEX = "(update|delete|insert|replace).*";
  private static final String DML_QUERY_REGEX = "(select|explain|explain +analyze).*";

  @Autowired
  private WebAPIServiceImpl webAPIServiceImpl;


  /**
   * {@inheritDoc}
   *
   * @throws SQLException {@inheritDoc}
   */
  @Override
  public List<SqlUpdateOutput> executeSqlUpdate(
      String authorization, String cluster, String database, List<GWSQLInput> listSqlInput)
      throws SQLException {

    logger.info("executeUpdateSQLs : cluster=" + cluster + " database=" + database);

    GWUser user = GWUser.getUserfromAuthorization(authorization);
    
    validateInput(listSqlInput);

    List<SqlUpdateOutput> result = new ArrayList<>();

    long start = System.nanoTime();
    try (Connection connection =
            ConnectionUtils.getConnection(
                cluster, database, user.getUsername(), user.getPassword());
        Statement statement = connection.createStatement(); ) {
      if (sqlTimeOut != 0) {
        statement.setQueryTimeout(sqlTimeOut);
      }

      for (GWSQLInput sqlInput : listSqlInput) {
        result.add(executeSql(statement, sqlInput));
      }
    }
    catch (SQLException e) {
      throw e;
    }

    long end = System.nanoTime();
    logger.debug("executeSQLs : time=" + (end - start) / 1000000f);

    return result;
  }

  private void validateInput(List<GWSQLInput> listSqlInput) {
    if (listSqlInput == null || listSqlInput.size() == 0) {
      throw new GWBadRequestException(Messages.LIST_SQL_EMPTY);
    }

    if (listSqlInput.size() > GWSettingInfo.getMaxQueryNum()) {
      throw new GWBadRequestException(Messages.SQL_EXCEED_MAXIMUM);
    }

    for (GWSQLInput sqlInput : listSqlInput) {
      if (sqlInput == null) {
        throw new GWBadRequestException(Messages.SQL_INVALID);
      }

      if (null == sqlInput.getStmt() || sqlInput.getStmt().trim().length() == 0) {
        throw new GWBadRequestException(Messages.STATEMENT_INVALID);
      }
    }
  }

  private SqlUpdateOutput executeSql(Statement statement, GWSQLInput input) {
    SqlUpdateOutput result = new SqlUpdateOutput();

    try {
      result.setStmt(input.getStmt());
      result.setUpdatedRows(statement.executeUpdate(input.getStmt()));
      result.setStatus(SUCCESS);
    } catch (SQLException e) {
      result.setStatus(FAILED);
      result.setMessage(e.getMessage());
    }
    return result;
  }

  @Override
  public List<SqlDdlDclOutput> executeDDL(String authorization, String cluster, String database,
      List<GWSQLInput> listDdlInput) throws SQLException {

    logger.info("executeDDLs : cluster=" + cluster + " database=" + database);

    GWUser user = GWUser.getUserfromAuthorization(authorization);
    
    validateInput(listDdlInput);
    validateSqlByType(listDdlInput,DDL_REGEX);
    List<SqlDdlDclOutput> result = new ArrayList<>();

    long start = System.nanoTime();
    try (Connection connection =
            ConnectionUtils.getConnection(
                cluster, database, user.getUsername(), user.getPassword());
        Statement statement = connection.createStatement();) {

      for (GWSQLInput sqlInput : listDdlInput) {
        result.add(executeDdlAndDcl(statement, sqlInput));
      }
    }

    long end = System.nanoTime();
    logger.debug("executeDDLs : time=" + (end - start) / 1000000f);

    return result;
  }
  
  private void validateSqlByType(List<GWSQLInput> listSqlInput, String regex) {
    for (GWSQLInput sqlInput : listSqlInput) {
      if (!sqlInput.getStmt().toLowerCase().trim()
          .matches(regex)) {
        throw new GWBadRequestException(Messages.STATEMENT_INVALID);
      }
    }
  }
  
  private SqlDdlDclOutput executeDdlAndDcl(Statement statement, GWSQLInput input) {
    SqlDdlDclOutput result = new SqlDdlDclOutput();

    try {
      result.setStmt(input.getStmt());
      statement.execute(input.getStmt());
      result.setStatus(SUCCESS);
    } catch (SQLException e) {
      result.setStatus(FAILED);
      result.setMessage(e.getMessage());
    }
    return result;
  }

  @Override
  public List<SqlDdlDclOutput> executeDCL(String authorization, String cluster, String database,
      List<GWSQLInput> listDclInput) throws SQLException {

    logger.info("executeDCLs : cluster=" + cluster + " database=" + database);
    GWUser user = GWUser.getUserfromAuthorization(authorization);
    validateInput(listDclInput);
    validateSqlByType(listDclInput,DCL_REGEX);

    List<SqlDdlDclOutput> result = new ArrayList<>();

    long start = System.nanoTime();
    try (Connection connection =
            ConnectionUtils.getConnection(
                cluster, database, user.getUsername(), user.getPassword());
        Statement statement = connection.createStatement(); ) {

      for (GWSQLInput sqlInput : listDclInput) {
        result.add(executeDdlAndDcl(statement, sqlInput));
      }
    }

    long end = System.nanoTime();
    logger.debug("executeDCLs : time=" + (end - start) / 1000000f);
    return result;
  }

  @Override
  public List<SqlUpdateOutput> executeDmlUpdate(String authorization, String cluster, String database,
      List<GWSQLInput> listDmlInput) throws SQLException {
    logger.info("executeDmlUpdate : cluster=" + cluster + " database=" + database);
    GWUser user = GWUser.getUserfromAuthorization(authorization);
    validateInput(listDmlInput);
    validateSqlByType(listDmlInput,DML_UPDATE_REGEX);

    List<SqlUpdateOutput> result = new ArrayList<>();

    long start = System.nanoTime();
    try (Connection connection =
            ConnectionUtils.getConnection(
                cluster, database, user.getUsername(), user.getPassword());
        Statement statement = connection.createStatement();) {
      for (GWSQLInput sqlInput : listDmlInput) {
        result.add(executeSql(statement, sqlInput));
      }
    }

    long end = System.nanoTime();
    logger.debug("executeDmlUpdate : time=" + (end - start) / 1000000f);
    return result;
  }

  @Override
  public List<GWSQLOutput> executeDmlQuery(String authorization, String cluster, String database,
      List<GWSQLInput> listSQLInput) throws GSException, SQLException, UnsupportedEncodingException {
    validateInput(listSQLInput);
    validateSqlByType(listSQLInput,DML_QUERY_REGEX);
    return webAPIServiceImpl.executeSQLs(authorization, cluster, database, listSQLInput);
  }

}
