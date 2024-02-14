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

import com.toshiba.mwcloud.gs.tools.webapi.dto.GWSQLInput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.SqlUpdateOutput;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWBadRequestException;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GWUser;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GridStoreUtils;
import java.sql.SQLException;
import java.util.List;

public interface ExecuteSqlUpdateService {

  /**
   * Execute multiple update SQLs. <br>
   * <br>
   * <b>Processing flow:</b>
   *
   * <ol>
   *   <li>Check authorization
   *   <li>Call function {@link GWUser#getUserfromAuthorization(String)} to get {@link GWUser} from
   *       authorization
   *   <li>Call function {@link GridStoreUtils#getGridStore(String, String, String, String)} to get
   *       the information of the target cluster
   *   <li>Call function checkAuthentication(GridStore) to check the authentication
   *   <li>If the size of {@code listSQLInput} is larger than the maximum of number of SQLs that can
   *       be executed, throw a {@link GWBadRequestException}
   *   <li>For each {@link GWSQLInput} in the {@code listSQLInput}, call function {@code
   *       executeSQL(Statement, GWSQLInput)} to execute each SQL
   *   <li>Return a list of {@link SqlUpdateOutput}
   * </ol>
   *
   * @param authorization basic authentication
   * @param cluster name of cluster
   * @param database name of database
   * @param listSqlInput a {@link List} of {@link GWSQLInput}
   * @return a {@link List} of {@link SqlUpdateOutput}
   * @throws SQLException a {@link SQLException}
   */
  public List<SqlUpdateOutput> executeSqlUpdate(
      String authorization, String cluster, String database, List<GWSQLInput> listSqlInput)
      throws SQLException;
}
