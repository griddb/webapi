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

public class GWBulkPutRowOutput {

  /**
   * GWBulkPutRowOutput constructor.
   *
   * @param containerName - String: the container name
   * @param updatedRows - Int: number of rows updated
   */
  public GWBulkPutRowOutput(String containerName, int updatedRows) {
    this.containerName = containerName;
    this.updatedRows = updatedRows;
  }

  /** name of container. */
  private String containerName;

  /** Number of rows insert or updated. */
  int updatedRows;

  /**
   * set row updated.
   *
   * @param updatedRows Number of rows insert or updated.
   */
  public void setUpdatedRows(int updatedRows) {
    this.updatedRows = updatedRows;
  }

  /**
   * Get row updated.
   *
   * @return rowUpdated Number of rows insert or updated.
   */
  public int getUpdatedRows() {
    return updatedRows;
  }

  /**
   * Get container name.
   *
   * @return container name
   */
  public String getContainerName() {
    return containerName;
  }

  /**
   * Set container name.
   *
   * @param containerName container name
   */
  public void setContainerName(String containerName) {
    this.containerName = containerName;
  }
}
