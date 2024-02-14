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
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWPutRowOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWQueryOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWQueryParams;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWException;
import com.toshiba.mwcloud.gs.tools.webapi.service.impl.BlobHandleServiceImpl;
import com.toshiba.mwcloud.gs.tools.webapi.utils.Constants;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Controller handles request for Blob data Service. */
@RestController
@RequestMapping("${basePath}" + "/" + "${version}")
public class BlobHandleController {

  /** BLob handle Services. */
  private final BlobHandleServiceImpl blobServiceImpl;

  public BlobHandleController(BlobHandleServiceImpl blobServiceImpl) {
    this.blobServiceImpl = blobServiceImpl;
  }

  /**
   * [SE13] put rows with type BLOB to container. <br>
   * <b>Processing flow:</b>
   *
   * <ol>
   *   <li>Put rows by calling {@link BlobHandleServiceImpl#putRows(String, String, String, String,
   *       List, MultipartFile)} function.
   * </ol>
   *
   * @param authorization basic authentication
   * @param cluster name of cluster
   * @param database name of database
   * @param container name of container
   * @param rows array of row data to put
   * @param file contains all file to upload to BLOB columns
   * @return a {@link ResponseEntity} object with status {@link HttpStatus#OK}
   * @throws GSException internal GridStore GridDB exception
   * @throws UnsupportedEncodingException unsupported encoding exception
   */
  @RequestMapping(
      value = "{cluster}/dbs/{database}/containers/{container}/rows/blob",
      method = RequestMethod.PUT)
  @ResponseBody
  public ResponseEntity<?> putRowsBlob(
      @RequestHeader(name = "Authorization", required = false) String authorization,
      @PathVariable("cluster") String cluster,
      @PathVariable("database") String database,
      @PathVariable("container") String container,
      @RequestPart("rows") List<List<Object>> rows,
      @RequestPart("file") MultipartFile file)
      throws GSException, UnsupportedEncodingException {
    GWPutRowOutput output =
        blobServiceImpl.putRows(authorization, cluster, database, container, rows, file);
    return new ResponseEntity<>(output, HttpStatus.OK);
  }

  /**
   * [SE12] Get row type with blob. <br>
   * <b>Processing flow:</b>
   *
   * <ol>
   *   <li>Get rows by calling {@link BlobHandleServiceImpl#getRowsTypeBlob(String, String,String,
   *       String, GWQueryParams)} function.
   * </ol>
   *
   * @param authorization basic authentication
   * @param cluster name of cluster
   * @param database name of database
   * @param container name of container
   * @param queryParams request for get blob data
   * @param httpResponse response for get blob data
   * @return a {@link ResponseEntity} object with status {@link HttpStatus#OK}
   * @throws GSException internal GridStore GridDB exception
   * @throws SQLException internal Sql exception
   * @throws UnsupportedEncodingException unsupported encoding exception
   */
  @RequestMapping(
      value = "{cluster}/dbs/{database}/containers/{container}/rows/blob",
      method = {RequestMethod.POST})
  @ResponseBody
  public ResponseEntity<MultiValueMap<String, Object>> getRowsBlobMultipart(
      @RequestHeader(name = "Authorization", required = false) String authorization,
      @PathVariable("cluster") String cluster,
      @PathVariable("database") String database,
      @PathVariable("container") String container,
      @RequestBody GWQueryParams queryParams,
      HttpServletResponse httpResponse)
      throws GWException, SQLException, IOException {

    MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
    GWQueryOutput output =
        blobServiceImpl.getRowsTypeBlob(authorization, cluster, database, container, queryParams);

    String serverLocation = output.getBlobPath();
    File file = new File(serverLocation + Constants.ZIP_FILE_EXT);
    Resource resource = new FileSystemResource(file);
    output.setBlobPath(null);
    form.add("rows", output);
    form.add("file", resource);
    httpResponse.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
    MediaType multiPart = MediaType.parseMediaType(MediaType.MULTIPART_FORM_DATA_VALUE);
    return ResponseEntity.ok().contentType(multiPart).body(form);
  }
}
