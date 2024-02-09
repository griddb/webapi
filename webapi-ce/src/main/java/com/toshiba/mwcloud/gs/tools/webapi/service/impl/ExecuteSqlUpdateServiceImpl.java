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
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWSQLInput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.SqlUpdateOutput;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWBadRequestException;
import com.toshiba.mwcloud.gs.tools.webapi.service.ExecuteSqlUpdateService;
import com.toshiba.mwcloud.gs.tools.webapi.utils.ConnectionUtils;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GWSettingInfo;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GWUser;
import com.toshiba.mwcloud.gs.tools.webapi.utils.Messages;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExecuteSqlUpdateServiceImpl implements ExecuteSqlUpdateService {

  private static final Logger logger =
      (Logger) LoggerFactory.getLogger(ExecuteSqlUpdateServiceImpl.class);

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

      for (GWSQLInput sqlInput : listSqlInput) {
        result.add(executeSql(statement, sqlInput));
      }
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
      result.setStatus(1);
    } catch (SQLException e) {
      result.setStatus(0);
      result.setMessage(e.getMessage());
    }
    return result;
  }
}
