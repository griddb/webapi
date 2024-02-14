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

import java.util.List;

public class GWBulkMultipleContainerInput {

  /** Name of container. */
  private String name;

  /** Start key value. */
  private Object startKeyValue;

  /** Finish key value. */
  private Object finishKeyValue;

  /** List of key value. */
  private List<Object> keyValues;

  /** Offset value with the default value is 0. */
  private int offset = 0;

  /** Limit value with the default value is 10000. */
  private int limit = 10000;

  /**
   * Get name of container.
   *
   * @return name of container
   */
  public String getName() {
    return name;
  }

  /**
   * Set name container.
   *
   * @param name name of container
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get start key value.
   *
   * @return start key value
   */
  public Object getStartKeyValue() {
    return startKeyValue;
  }

  /**
   * Set start key value.
   *
   * @param startKeyValue start key value
   */
  public void setStartKeyValue(Object startKeyValue) {
    this.startKeyValue = startKeyValue;
  }

  /**
   * Set end key value.
   *
   * @param endKeyValue end key value
   */
  public void setFinishKeyValue(Object endKeyValue) {
    this.finishKeyValue = endKeyValue;
  }

  /**
   * Get end key value.
   *
   * @return end key value
   */
  public Object getFinishKeyValue() {
    return finishKeyValue;
  }

  /**
   * Get list of predicate key.
   *
   * @return a {@link List} of {@link Object}
   */
  public List<Object> getKeyValues() {
    return keyValues;
  }

  /**
   * Set the list of predicate key.
   *
   * @param keyValues a {@link List} of {@link Object}
   */
  public void setKeyValues(List<Object> keyValues) {
    this.keyValues = keyValues;
  }

  /**
   * Get offset.
   *
   * @return offset
   */
  public int getOffset() {
    return offset;
  }

  /**
   * Set offset.
   *
   * @param offset offset
   */
  public void setOffset(int offset) {
    this.offset = offset;
  }

  /**
   * Get limit.
   *
   * @return limit
   */
  public int getLimit() {
    return limit;
  }

  /**
   * Set limit.
   *
   * @param limit limit
   */
  public void setLimit(int limit) {
    this.limit = limit;
  }
}
