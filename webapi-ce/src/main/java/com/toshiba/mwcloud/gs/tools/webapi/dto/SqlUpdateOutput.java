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

public class SqlUpdateOutput {

  /** Number of rows insert or updated. */
  int updatedRows;

  /** Status of query: 1 is success, 0 is fail. */
  int status;

  /** error message if fail. */
  String message;

  /** Statement of SQL. */
  private String stmt;

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
   * Set status.
   *
   * @param status Status of query: 1 is success, 0 is fail
   */
  public void setStatus(int status) {
    this.status = status;
  }

  /**
   * Get status.
   *
   * @return Status of query: 1 is success, 0 is fail
   */
  public int getStatus() {
    return status;
  }

  /**
   * Set message.
   *
   * @param message error message if fail.
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Get message.
   *
   * @return error message if fail.
   */
  public String getmessage() {
    return message;
  }

  /**
   * Get the statement of SQL.
   *
   * @return statement of SQL
   */
  public String getStmt() {
    return stmt;
  }

  /**
   * Set the statement for SQL.
   *
   * @param stmt statement of SQL
   */
  public void setStmt(String stmt) {
    this.stmt = stmt;
  }
}
