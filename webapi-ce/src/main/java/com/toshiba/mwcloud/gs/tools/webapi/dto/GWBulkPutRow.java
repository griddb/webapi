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

package com.toshiba.mwcloud.gs.tools.webapi.dto;

import com.toshiba.mwcloud.gs.ColumnInfo;
import java.util.List;

public class GWBulkPutRow {

  /** Name of container. */
  String containerName;

  /** List of column information. */
  private List<ColumnInfo> columns;

  /** List of rows. */
  List<List<Object>> rows;

  /** Get container name.
   *  
   * @return containerName - container name
   * 
   * */
  public String getContainerName() {
    return containerName;
  }

  /**
   * Set container name.
   * 
   * @param containerName - container name
   */
  public void setContainerName(String containerName) {
    this.containerName = containerName;
  }

  /**
   * Get list of column information.
   *
   * @return a {@link List} of {@link ColumnInfo}
   */
  public List<ColumnInfo> getColumns() {
    return columns;
  }

  /**
   * Set the list of column information.
   *
   * @param columns a {@link List} of {@link ColumnInfo}
   */
  public void setColumns(List<ColumnInfo> columns) {
    this.columns = columns;
  }

  /**
   * Get list of rows.
   *
   * @return a {@link List} of {@link List} of {@link Object}
   */
  public List<List<Object>> getRows() {
    return rows;
  }

  /**
   * Set list of rows.
   *
   * @param rows a {@link List} of {@link List} of {@link Object}
   */
  public void setRows(List<List<Object>> rows) {
    this.rows = rows;
  }
}
