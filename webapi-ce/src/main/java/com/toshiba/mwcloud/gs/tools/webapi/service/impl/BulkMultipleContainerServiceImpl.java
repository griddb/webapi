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
import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.RowKeyPredicate;
import com.toshiba.mwcloud.gs.TimeUnit;
import com.toshiba.mwcloud.gs.experimental.ExperimentalTool;
import com.toshiba.mwcloud.gs.experimental.ExtendedContainerInfo;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWBulkMultipleContainerInput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWBulkMultipleContainerOuput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWBulkPutRow;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWBulkPutRowOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWTQLColumnInfo;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWBadRequestException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWNotFoundException;
import com.toshiba.mwcloud.gs.tools.webapi.service.BulkMultipleContainerService;
import com.toshiba.mwcloud.gs.tools.webapi.utils.ConversionUtils;
import com.toshiba.mwcloud.gs.tools.webapi.utils.DateFormatUtils;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GWSettingInfo;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GWUser;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GridStoreUtils;
import com.toshiba.mwcloud.gs.tools.webapi.utils.Messages;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BulkMultipleContainerServiceImpl implements BulkMultipleContainerService {

  private static final Logger logger =
      (Logger) LoggerFactory.getLogger(BulkMultipleContainerServiceImpl.class);

  @Autowired private WebAPIServiceImpl webApiServiceImpl;

  @Override
  public List<GWBulkMultipleContainerOuput> getRowsMultipleContainers(
      String authorization,
      String cluster,
      String database,
      List<GWBulkMultipleContainerInput> bulkMultipleContainerInput)
      throws GSException, GWException, UnsupportedEncodingException, SQLException, ParseException {
    GWUser user = GWUser.getUserfromAuthorization(authorization);
    long start = System.nanoTime();
    try (GridStore gridStore =
        GridStoreUtils.getGridStore(cluster, database, user.getUsername(), user.getPassword()); ) {

      Map<String, RowKeyPredicate<?>> predMap = new HashMap<String, RowKeyPredicate<?>>();
      setRowKeyPredicate(bulkMultipleContainerInput, gridStore, predMap);
      List<GWBulkMultipleContainerOuput> results = new ArrayList<>();
      Map<String, List<Row>> outMap = gridStore.multiGet(predMap);
      getResultRowsMultipleContainers(bulkMultipleContainerInput, gridStore, results, outMap);
      return results;
    } catch (GSException gsException) {
      logger.error(gsException.getMessage(), gsException);
      throw gsException;
    } finally {
      long end = System.nanoTime();
      logger.debug("getRows : time=" + (end - start) / 1000000f);
    }
  }

  private void getResultRowsMultipleContainers(
      List<GWBulkMultipleContainerInput> bulkMultipleContainerInput,
      GridStore gridStore,
      List<GWBulkMultipleContainerOuput> results,
      Map<String, List<Row>> outMap)
      throws GSException, SQLException, UnsupportedEncodingException {

    long rowMaxSize = GWSettingInfo.getMaxGetRowSize();
    long totalSize = 0;
    for (Map.Entry<String, List<Row>> entry : outMap.entrySet()) {
      long rowSize = 0;
      String containerName = entry.getKey();

      // Find last element match filter
      Collections.reverse(bulkMultipleContainerInput);
      GWBulkMultipleContainerInput bulkMultipleContainer =
          bulkMultipleContainerInput
              .stream()
              .filter(bulk -> containerName.equals(bulk.getName()))
              .findAny()
              .orElse(null);

      int offset = bulkMultipleContainer.getOffset();
      int limit = Math.min(bulkMultipleContainer.getLimit(), GWSettingInfo.getMaxLimit());
      int total = entry.getValue().size();

      GWBulkMultipleContainerOuput containerOuput =
          initContainerOuput(containerName, total, limit, offset);

      ContainerInfo containerInfo = gridStore.getContainerInfo(containerName);
      List<GWTQLColumnInfo> columnInfoAll = getColumnInfo(containerInfo);

      List<List<Object>> rowsResult = new ArrayList<>();
      if (offset >= total) {
        containerOuput.setResults(rowsResult);
        containerOuput.setColumns(columnInfoAll);
        results.add(containerOuput);
        continue;
      }

      int rowNumber = 0;

      for (Row row : entry.getValue()) {
        int columnCount = row.getSchema().getColumnCount();
        List<Object> list = new ArrayList<Object>(columnCount);
        if ((rowNumber >= offset) && (rowNumber < offset + limit)) {
          for (int colNo = 0; colNo < columnCount; ++colNo) {
            long size = 0;
            GSType type = containerInfo.getColumnInfo(colNo).getType();
            if (type == GSType.TIMESTAMP) {
              size = webApiServiceImpl.stringifyTimestamp(row, colNo, list);
            } else {
              size =
                webApiServiceImpl.stringify(
                    list,
                    row.getValue(colNo),
                    type,
                    false,
                    null,
                    null,
                    null,
                    null);
            }
            rowSize += size;
            totalSize += size;
          }
          if (rowSize > rowMaxSize || totalSize > GWSettingInfo.getMaxTotalResponseSize()) {
            throw new GWBadRequestException(Messages.GET_BULK_ROWS_CONTAINER_TOO_MANY);
          }
          rowsResult.add(list);
        }
        ++rowNumber;
      }

      containerOuput.setResults(rowsResult);
      containerOuput.setColumns(columnInfoAll);
      results.add(containerOuput);
    }
  }

  private GWBulkMultipleContainerOuput initContainerOuput(
      String containerName, int total, int limit, int offset) {
    GWBulkMultipleContainerOuput containerOuput = new GWBulkMultipleContainerOuput();
    containerOuput.setContainer(containerName);
    containerOuput.setTotal(total);
    containerOuput.setLimit(limit);
    containerOuput.setOffset(offset);
    return containerOuput;
  }

  private List<GWTQLColumnInfo> getColumnInfo(ContainerInfo containerInfo) {
    List<GWTQLColumnInfo> columnInfoAll = new ArrayList<>();
    for (int i = 0; i < containerInfo.getColumnCount(); ++i) {
      GWTQLColumnInfo columnInfo = new GWTQLColumnInfo();
      String columnName = containerInfo.getColumnInfo(i).getName();
      GSType columnType = containerInfo.getColumnInfo(i).getType();
      TimeUnit timeUnit = containerInfo.getColumnInfo(i).getTimePrecision();
      columnInfo.setName(columnName);
      columnInfo.setType(columnType);
      columnInfo.setTimePrecision(timeUnit);
      columnInfoAll.add(columnInfo);
    }
    return columnInfoAll;
  }

  private void setRowKeyPredicate(
      List<GWBulkMultipleContainerInput> bulkMultipleContainerInput,
      GridStore gridStore,
      Map<String, RowKeyPredicate<?>> predMap)
      throws GSException, ParseException {
    ContainerInfo containerInfo;
    for (GWBulkMultipleContainerInput bulkMultipleContainer : bulkMultipleContainerInput) {
      String containerName = bulkMultipleContainer.getName();
      validateBulkMultipleContainerInput(bulkMultipleContainer);
      containerInfo = gridStore.getContainerInfo(containerName);
      checkContainerExist(gridStore, containerName, containerInfo);
      List<Integer> listKeyColumn = containerInfo.getRowKeyColumnList();
      if (listKeyColumn.size() != 1) {
        RowKeyPredicate<?> predicate = RowKeyPredicate.create(containerInfo);
        predMap.put(containerName, predicate);
      } else {
        GSType columnType = containerInfo.getColumnInfo(0).getType();
        setStartFinishKeyValue(bulkMultipleContainer, columnType, predMap);
        setKeyValues(bulkMultipleContainer, columnType, predMap);
      }
    }
  }

  private void setKeyValues(
      GWBulkMultipleContainerInput bulkMultipleContainer,
      GSType columnType,
      Map<String, RowKeyPredicate<?>> predMap)
      throws GSException, ParseException {
    List<Object> keyValues = bulkMultipleContainer.getKeyValues();
    if (keyValues == null || keyValues.size() == 0) {
      return;
    }
    if (columnType == GSType.LONG) {
      RowKeyPredicate<Long> predicate = RowKeyPredicate.create(Long.class);
      for (Object key : keyValues) {
        predicate.add(ConversionUtils.convertToLong(key));
      }
      predMap.put(bulkMultipleContainer.getName(), predicate);
    } else if (columnType == GSType.INTEGER) {
      RowKeyPredicate<Integer> predicate = RowKeyPredicate.create(Integer.class);
      for (Object key : keyValues) {
        predicate.add(ConversionUtils.convertToInt(key));
      }
      predMap.put(bulkMultipleContainer.getName(), predicate);
    } else if (columnType == GSType.TIMESTAMP) {
      RowKeyPredicate<Date> predicate = RowKeyPredicate.create(Date.class);
      for (Object key : keyValues) {
        if (key != null) {
          predicate.add(DateFormatUtils.parse((String) key));
        }
      }
      predMap.put(bulkMultipleContainer.getName(), predicate);
    } else if (columnType == GSType.STRING) {
      RowKeyPredicate<String> predicate = RowKeyPredicate.create(String.class);
      for (Object key : keyValues) {
        if (key != null) {
          predicate.add((String) key);
        }
      }
      predMap.put(bulkMultipleContainer.getName(), predicate);
    }
  }

  private void setStartFinishKeyValue(
      GWBulkMultipleContainerInput bulkMultipleContainer,
      GSType columnType,
      Map<String, RowKeyPredicate<?>> predMap)
      throws GSException, ParseException {

    Object startKeyValue = bulkMultipleContainer.getStartKeyValue();
    Object endKeyValue = bulkMultipleContainer.getFinishKeyValue();

    if (columnType == GSType.LONG) {
      RowKeyPredicate<Long> predicate = RowKeyPredicate.create(Long.class);
      if (startKeyValue != null) {
        predicate.setStart(ConversionUtils.convertToLong(startKeyValue));
      }
      if (endKeyValue != null) {
        predicate.setFinish(ConversionUtils.convertToLong(endKeyValue));
      }
      predMap.put(bulkMultipleContainer.getName(), predicate);
    } else if (columnType == GSType.INTEGER) {
      RowKeyPredicate<Integer> predicate = RowKeyPredicate.create(Integer.class);
      if (startKeyValue != null) {
        predicate.setStart(ConversionUtils.convertToInt(startKeyValue));
      }
      if (endKeyValue != null) {
        predicate.setFinish(ConversionUtils.convertToInt(endKeyValue));
      }
      predMap.put(bulkMultipleContainer.getName(), predicate);
    } else if (columnType == GSType.TIMESTAMP) {
      RowKeyPredicate<Date> predicate = RowKeyPredicate.create(Date.class);
      if (startKeyValue != null) {
        predicate.setStart(DateFormatUtils.parse((String) startKeyValue));
      }
      if (endKeyValue != null) {
        predicate.setFinish(DateFormatUtils.parse((String) endKeyValue));
      }
      predMap.put(bulkMultipleContainer.getName(), predicate);
    } else {
      RowKeyPredicate<String> predicate = RowKeyPredicate.create(String.class);
      predMap.put(bulkMultipleContainer.getName(), predicate);
    }
  }

  private void checkContainerExist(
      GridStore gridStore, String container, ContainerInfo containerInfo) throws GSException {
    ExtendedContainerInfo extendContainerInfo =
        ExperimentalTool.getExtendedContainerInfo(gridStore, container);
    if (extendContainerInfo == null || containerInfo == null) {
      throw new GWNotFoundException(Messages.CONTAINER_NOT_EXISTED);
    }
  }

  @Override
  public List<GWBulkPutRowOutput> putRowsMultipleContainers(
      String authorization, String cluster, String database, List<GWBulkPutRow> input)
      throws GSException, UnsupportedEncodingException {

    logger.info("putRows : cluster=" + cluster + " database=" + database + " input=" + input);

    GWUser user = GWUser.getUserfromAuthorization(authorization);
    try (GridStore gridStore =
        GridStoreUtils.getGridStore(cluster, database, user.getUsername(), user.getPassword()); ) {

      Map<String, List<Row>> paramMap = new HashMap<String, List<Row>>();

      List<GWBulkPutRowOutput> output = new ArrayList<GWBulkPutRowOutput>();

      long rowsize = 0;
      long rowMaxSize = GWSettingInfo.getMaxPutRowSize();
      for (GWBulkPutRow inputContainer : input) {
        validatePutRows(inputContainer);
        int rowNumber = 1;
        String containerName = inputContainer.getContainerName();

        ContainerInfo containerInfo = gridStore.getContainerInfo(containerName);
        if (null == containerInfo) {
          throw new GWNotFoundException(Messages.CONTAINER_NOT_EXISTED);
        }
        output.add(new GWBulkPutRowOutput(containerName, inputContainer.getRows().size()));
        try (Container<Object, Row> cont = gridStore.getContainer(containerName); ) {
          List<Row> listRows = new ArrayList<Row>(inputContainer.getRows().size());

          for (List<Object> rows : inputContainer.getRows()) {
            rowsize =
                webApiServiceImpl.setRowValue(
                    rows, listRows, cont, containerInfo, rowNumber, rowsize, rowMaxSize);
            rowNumber++;
          }
          paramMap.put(containerName, listRows);
        }
      }
      gridStore.multiPut(paramMap);
      return output;

    } catch (GSException gsException) {
      logger.error(gsException.getMessage(), gsException);
      throw gsException;
    }
  }

  private void validatePutRows(GWBulkPutRow inputContainer) {
    String container = inputContainer.getContainerName();
    if (container == null || container.trim().isEmpty()) {
      throw new GWBadRequestException(Messages.CONTAINER_FIELD_INVALID);
    }
    if (inputContainer.getRows() == null || inputContainer.getRows().isEmpty()) {
      throw new GWBadRequestException(Messages.ROW_FIELD_INVALID);
    }
  }

  private void validateBulkMultipleContainerInput(GWBulkMultipleContainerInput bulkInput) {
    String container = bulkInput.getName();
    if (container == null || container.trim().isEmpty()) {
      throw new GWBadRequestException(Messages.CONTAINER_FIELD_INVALID);
    }
    if (bulkInput.getLimit() < 0) {
      throw new GWBadRequestException(Messages.LIMIT_FIELD_INVALID);
    }
    if (bulkInput.getOffset() < 0) {
      throw new GWBadRequestException(Messages.OFFSET_FIELD_INVALID);
    }
  }
}
