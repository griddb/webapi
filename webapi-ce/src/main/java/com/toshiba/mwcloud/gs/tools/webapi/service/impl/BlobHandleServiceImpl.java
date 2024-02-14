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
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.RowSet;
import com.toshiba.mwcloud.gs.experimental.ExperimentalTool;
import com.toshiba.mwcloud.gs.experimental.ExtendedContainerInfo;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWPutRowOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWQueryOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWQueryParams;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWTQLColumnInfo;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWBadRequestException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWNotFoundException;
import com.toshiba.mwcloud.gs.tools.webapi.service.BlobHandleService;
import com.toshiba.mwcloud.gs.tools.webapi.utils.BlobUtils;
import com.toshiba.mwcloud.gs.tools.webapi.utils.Constants;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GWSettingInfo;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GWUser;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GridStoreUtils;
import com.toshiba.mwcloud.gs.tools.webapi.utils.Messages;
import com.toshiba.mwcloud.gs.tools.webapi.utils.Validation;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BlobHandleServiceImpl implements BlobHandleService {

  @Autowired private WebAPIServiceImpl webApiServiceImpl;

  private static final Logger logger =
      (Logger) LoggerFactory.getLogger(BlobHandleServiceImpl.class);
  private static final String rootPath = GWSettingInfo.getBlobPath();
  private static final int ONE_HOUR = 1;

  /**
   * Get rows with BLOB data.
   *
   * @param authorization login token
   * @param cluster cluster name
   * @param database database name in cluster
   * @param container container name in database
   * @param queryParams a {@link GWQueryParams} object
   * @return result a {@link GWQueryOutput} object
   * @throws IOException IO exception
   * @throws GWException GW exception
   * @throws SQLException SQL exception
   * @throws GSException internal server exception
   * @throws UnsupportedEncodingException exception when encoding data type {@link String}
   */
  @Override
  public GWQueryOutput getRowsTypeBlob(
      String authorization,
      String cluster,
      String database,
      String container,
      GWQueryParams queryParams)
      throws GSException, GWException, UnsupportedEncodingException, SQLException, IOException {

    logger.info(
        "getRows : cluster="
            + cluster
            + " database="
            + database
            + " container="
            + container
            + " queryParams: "
            + "limit="
            + queryParams.getLimit()
            + ",offset="
            + queryParams.getOffset()
            + ",sort="
            + queryParams.getSort()
            + ",condition="
            + queryParams.getCondition());

    long start = System.nanoTime();
    String sourcePath = BlobUtils.createSource();
    GWUser user = GWUser.getUserfromAuthorization(authorization);
    Validation.validateInputParams(queryParams);
    GWQueryOutput result = new GWQueryOutput();

    if (queryParams.getFileNameCol() != null) {
      if (!queryParams.getFileNameCol().trim().isEmpty()) {
        queryParams.setFileNameCol(queryParams.getFileNameCol().toLowerCase());
      }
    }

    try (GridStore gridStore =
        GridStoreUtils.getGridStore(cluster, database, user.getUsername(), user.getPassword())) {
      ExtendedContainerInfo extendContainerInfo =
          ExperimentalTool.getExtendedContainerInfo(gridStore, container);
      ContainerInfo containerInfo = gridStore.getContainerInfo(container);
      if (extendContainerInfo == null || containerInfo == null) {
        throw new GWNotFoundException(Messages.CONTAINER_NOT_EXISTED);
      }
      Container<Object, Row> cont;
      switch (extendContainerInfo.getAttribute()) {
        case SINGLE:
          cont = gridStore.getContainer(container);
          if (null == cont) {
            throw new GWNotFoundException(Messages.CONTAINER_NOT_EXISTED);
          }
          Query<Row> query = cont.query(webApiServiceImpl.buildQueryString(container, queryParams));
          RowSet<Row> rowSet = query.fetch();
          List<List<Object>> rows =
              rowSetToTqlResult(
                  rowSet, containerInfo, queryParams.getFileNameCol(), sourcePath);
          result.setRows(rows);

          List<GWTQLColumnInfo> columns = new ArrayList<>(containerInfo.getColumnCount());
          for (int i = 0; i < containerInfo.getColumnCount(); i++) {
            GWTQLColumnInfo gwTqlColumnInfo = new GWTQLColumnInfo();
            gwTqlColumnInfo.setName(containerInfo.getColumnInfo(i).getName());
            columns.add(gwTqlColumnInfo);
            gwTqlColumnInfo.setType(containerInfo.getColumnInfo(i).getType());
          }

          result.setColumns(columns);
          break;
        case LARGE:
          throw new GWBadRequestException(Messages.UNSUPPORTED_PARTITION_TABLE_TYPE);
        default:
          throw new GWNotFoundException(Messages.CONTAINER_NOT_EXISTED);
      }

      result.setLimit(Math.min(queryParams.getLimit(), GWSettingInfo.getMaxLimit()));
      result.setOffset(queryParams.getOffset());
      result.setTotal(
          webApiServiceImpl.getRowsCount(
              cont,
              webApiServiceImpl.buildQueryStringWithoutLimitAndOffset(container, queryParams)));
      result.setBlobPath(sourcePath);
      BlobUtils.deleteDirectory(sourcePath);
      return result;
    } catch (GSException gsException) {
      throw gsException;
    } finally {
      BlobUtils.deleteHistoryFile(ONE_HOUR);
      long end = System.nanoTime();
      logger.debug("getRows : time=" + (end - start) / 1000000f);
    }
  }

  private List<List<Object>> rowSetToTqlResult(
      RowSet<Row> rowSet, ContainerInfo containerInfo, String columnName, String sourcePath)
      throws GWException, SQLException, IOException {

    List<List<Object>> rows = new ArrayList<>();
    if (rowSet == null) {
      return rows;
    }
    List<String> columnNames = new ArrayList<>(containerInfo.getColumnCount());
    for (int i = 0; i < containerInfo.getColumnCount(); i++) {
      columnNames.add(containerInfo.getColumnInfo(i).getName().toLowerCase());
    }


    long rowMaxSize = GWSettingInfo.getMaxGetRowSize();
    long rowsize = 0;
    while (rowSet.hasNext()) {
      Row row = rowSet.next();
      int columnCount = containerInfo.getColumnCount();
      List<Object> list = new ArrayList<>(columnCount);
      for (int colNo = 0; colNo < columnCount; ++colNo) {
        long size =
            webApiServiceImpl.stringify(
                list,
                row.getValue(colNo),
                containerInfo.getColumnInfo(colNo).getType(),
                true,
                columnName,
                row,
                columnNames,
                sourcePath);
        rowsize += size;
      }
      if (rowsize > rowMaxSize) {
        throw new GWBadRequestException(Messages.TOO_MANY_RESULTS);
      }
      rows.add(list);
    }

    BlobUtils.makeDirectory(sourcePath);
    BlobUtils.pack(sourcePath);
    double fileSize = BlobUtils.getFileSize(sourcePath + Constants.ZIP_FILE_EXT);
    rowsize += fileSize;
    if (rowsize > rowMaxSize) {
      BlobUtils.deleteDirectory(sourcePath);
      throw new GWBadRequestException(Messages.TOO_MANY_RESULTS);
    }
    return rows;
  }

  /**
   * {@inheritDoc}
   *
   * @throws GSException {@inheritDoc}
   * @throws UnsupportedEncodingException {@inheritDoc}
   */
  @Override
  public GWPutRowOutput putRows(
      String authorization,
      String cluster,
      String database,
      String container,
      List<List<Object>> input,
      MultipartFile file)
      throws GSException, UnsupportedEncodingException {

    long start = System.nanoTime();
    
    String name = file.getOriginalFilename();
    if (!Objects.requireNonNull(name).contains(Constants.ZIP_FILE_EXT)) {
      logger.debug("File type is invalid");
      throw new GWBadRequestException(Messages.FILE_TYPE_INVALID);
    }

    String filename = UUID.randomUUID().toString();
    String filePath = rootPath + File.separator + filename;
    unZipFile(file, filePath);


    GWUser user = GWUser.getUserfromAuthorization(authorization);
    Validation.validatePutRowsInput(input);
    Container<Object, Row> cont;
    int rowNumber = 0;
    try (GridStore gridStore =
        GridStoreUtils.getGridStore(cluster, database, user.getUsername(), user.getPassword())) {

      ContainerInfo containerInfo = gridStore.getContainerInfo(container);
      if (null == containerInfo) {
        throw new GWNotFoundException(Messages.CONTAINER_NOT_EXISTED);
      }

      cont = gridStore.getContainer(container);
      List<Row> listRows = new ArrayList<>(input.size());
      long rowsize = 0;
      long rowMaxSize = GWSettingInfo.getMaxPutRowSize();
      for (List<Object> rows : input) {
        rowsize = setRowValue(rows, listRows, cont, containerInfo, rowsize, rowMaxSize, filePath);
        rowNumber++;
      }

      cont.put(listRows);
      GWPutRowOutput output = new GWPutRowOutput();
      output.setCount((rowNumber));
      return output;

    } catch (GSException gsException) {
      throw gsException;
    } finally {
      BlobUtils.deleteData(filename);
      long end = System.nanoTime();
      if (GWSettingInfo.getLogger().isDebugEnabled()) {
        logger.debug("putRows : time=" + (end - start) / 1000000f);
      }
    }
  }

  private void unZipFile(MultipartFile file, String filePath) {
    File serverFile = new File(filePath + Constants.ZIP_FILE_EXT);
    try (BufferedOutputStream stream =
        new BufferedOutputStream(new FileOutputStream(serverFile))) {
      stream.write(file.getBytes());

      // extract file
      BlobUtils.unzip(filePath + Constants.ZIP_FILE_EXT, filePath);
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      throw new GWException(Messages.EXTRACT_FILE_INVALID);
    }
  }

  /**
   * Set value for row.
   *
   * @param values list of value
   * @param rows list of row need set value
   * @param container container contain rows
   * @param containerInfo container infoRmation
   * @param rowSize size of row
   * @param rowMaxSize max size of rows
   * @return size of rows
   * @throws GSException internal server exception
   * @throws UnsupportedEncodingException exception when encoding data type {@link String}
   */
  private long setRowValue(
      List<Object> values,
      List<Row> rows,
      Container<Object, Row> container,
      ContainerInfo containerInfo,
      long rowSize,
      long rowMaxSize,
      String filePath)
      throws GSException {

    if (values == null || values.size() != containerInfo.getColumnCount()) {
      throw new GWBadRequestException(Messages.ROW_FIELD_INVALID);
    }
    Row row = container.createRow();
    int i = 0;
    for (Object value : values) {
      try {
        rowSize +=
            webApiServiceImpl.setRowValue(
                container,
                row,
                containerInfo.getColumnInfo(i).getType(),
                value,
                i++,
                true,
                filePath);
      } catch (Exception e) {
        throw new GWBadRequestException(e.getMessage());
      }
    }
    if (rowSize > rowMaxSize) {
      throw new GWBadRequestException(Messages.ROWS_EXCEED_MAXIMUM);
    }
    rows.add(row);
    return rowSize;
  }
}
