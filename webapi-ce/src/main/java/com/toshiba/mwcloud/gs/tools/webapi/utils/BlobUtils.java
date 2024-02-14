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

package com.toshiba.mwcloud.gs.tools.webapi.utils;

import ch.qos.logback.classic.Logger;
import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.Row;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

/** Utils for get and put rows for BLOB type. */
public class BlobUtils {

  private static final String SOURCE_PATH = GWSettingInfo.getBlobPath();
  private static final int BUFFER_SIZE = 4096;
  private static final Logger logger = (Logger) LoggerFactory.getLogger(BlobUtils.class);

  /**
   * Read file to Blob type.
   *
   * @param fileName File name read to blob
   * @param container Container to store blob
   * @return blob blob type data
   * @throws SQLException a {@link SQLException}
   * @throws IOException a {@link IOException}
   */
  public static Blob readBlob(String fileName, Container<?, Row> container)
      throws SQLException, IOException {
    FileInputStream blobFile = new FileInputStream(fileName);
    Blob blob = container.createBlob();
    OutputStream blobBuffer = blob.setBinaryStream(1);
    int len;
    while ((len = blobFile.read()) > -1) {
      blobBuffer.write(len);
    }
    blobBuffer.flush();
    blobFile.close();
    return blob;
  }

  /**
   * Write blob data to file.
   *
   * @param blob blob data
   * @param filename blob file name
   * @param sourcePath blob file path
   * @return file name
   * @throws IOException a {@link IOException}
   * @throws SQLException a {@link SQLException}
   */
  public static String writeBlob(Blob blob, String filename, String sourcePath)
      throws IOException, SQLException {
    String outputFileName;
    if (filename == null || filename.trim().isEmpty() || isValidFilename(filename) == false) {
      outputFileName = UUID.randomUUID().toString();
    } else {
      outputFileName = filename;
    }
    Files.createDirectories(Paths.get(sourcePath));
    String savePath = sourcePath + outputFileName;
    byte[] byteArray = blob.getBytes(1, (int) blob.length());
    FileOutputStream outPutStream = new FileOutputStream(savePath);
    outPutStream.write(byteArray);
    outPutStream.close();
    return outputFileName;
  }

  /**
   * Zip a directory.
   *
   * @param sourceDirPath directory's path
   * @throws IOException a {@link IOException}
   */
  public static void pack(String sourceDirPath) throws IOException {
    Path dirPath = Paths.get(sourceDirPath);
    Path zipFilePath =
        Files.createFile(Paths.get(SOURCE_PATH + dirPath.getFileName() + Constants.ZIP_FILE_EXT));
    try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
      Files.walk(dirPath)
          .forEach(
              path -> {
                ZipEntry zipEntry = new ZipEntry(dirPath.relativize(path).toString());
                if (!Files.isDirectory(dirPath.relativize(path))) {
                  try {
                    zs.putNextEntry(zipEntry);
                    Files.copy(path, zs);
                    zs.closeEntry();
                  } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                  }
                }
              });
    }
  }

  /**
   * Delete a directory.
   *
   * @param directoryFilePath - String: directory's path
   * @throws IOException a {@link IOException}
   */
  public static void deleteDirectory(String directoryFilePath) throws IOException {
    File file = new File(directoryFilePath);
    Path directory = Paths.get(directoryFilePath);
    if (Files.exists(directory)) {
      FileUtils.deleteDirectory(file);
    }
  }

  /**
   * Check file existed.
   *
   * @param filename file need to check
   * @param sourcePath parent folder path
   * @return true if file existed
   */
  public static boolean fileExists(String filename, String sourcePath) {
    return new File(sourcePath, filename).exists();
  }

  /**
   * Make directory in case not has blob data.
   *
   * @param path - String: directory's path
   */
  public static void makeDirectory(String path) {
    new File(path).mkdir();
  }

  /**
   * Create source directory to save BLOB data file.
   *
   * @return a directory path
   */
  public static String createSource() {
    return SOURCE_PATH + UUID.randomUUID().toString();
  }

  /**
   * Delete history file before a period of time.
   *
   * @param hours time setting
   */
  public static void deleteHistoryFile(double hours) {

    File folder = new File(SOURCE_PATH);
    if (folder.exists()) {
      File[] listFiles = folder.listFiles();
      double eligibleForDeletion = System.currentTimeMillis() - (hours * 60 * 60 * 1000);
      for (File listFile : listFiles != null ? listFiles : new File[0]) {
        if (listFile.lastModified() < eligibleForDeletion) {
          listFile.delete();
        }
      }
    }
  }

  /**
   * get size of file.
   *
   * @param filename file need to check size
   * @return size of file - bytes
   */
  public static double getFileSize(String filename) {
    File file = new File(filename);
    return file.length();
  }

  /**
   * Check file name valid.
   *
   * @param file file need to check
   * @return true if file name is valid
   */
  private static boolean isValidFilename(String file) {
    File f = new File(file);
    try {
      f.getCanonicalPath();
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * delete files.
   *
   * @param filename file need to delete
   */
  public static void deleteData(String filename) {
    String folderPath = SOURCE_PATH + File.separator + filename;
    String fileZipPath = folderPath + Constants.ZIP_FILE_EXT;

    File fileZip = new File(fileZipPath);
    if (fileZip.exists()) {
      fileZip.delete();
    }

    try {
      FileUtils.forceDelete(new File(folderPath));
    } catch (IOException e) {
      logger.error(e.toString());
    }
  }

  /**
   * Extracts a zip entry (file entry).
   *
   * @param zipIn zip input stream
   * @param filePath zip file Path
   * @throws IOException IO exception
   */
  private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
    byte[] bytesIn = new byte[BUFFER_SIZE];
    int read;
    while ((read = zipIn.read(bytesIn)) != -1) {
      bos.write(bytesIn, 0, read);
    }
    bos.close();
  }

  /**
   * Extracts a zip file specified by the zipFilePath to a directory specified by destDirectory.
   * (will be created if does not exists)
   *
   * @param zipFilePath input zip file
   * @param destDirectory destination folder
   * @throws IOException IO exception
   */
  public static void unzip(String zipFilePath, String destDirectory) throws IOException {
    File destDir = new File(destDirectory);
    if (!destDir.exists()) {
      destDir.mkdir();
    }
    Charset cp886 = Charset.forName("CP866");
    ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath), cp886);
    ZipEntry entry = zipIn.getNextEntry();
    // iterates over entries in the zip file
    while (entry != null) {

      String filePath = destDirectory + File.separator + entry.getName();
      File destFile = FileUtils.getFile(filePath);
      File destinationParent = destFile.getParentFile();

      // create the parent directory structure if needed
      if (!destinationParent.exists()) {
        Boolean isCreated = destinationParent.mkdirs();
        if (!isCreated) {
          logger.error("can not create folder in zip file");
        }
      }

      if (!entry.isDirectory()) {
        // if the entry is a file, extracts it
        extractFile(zipIn, filePath);
      } else {
        // if the entry is a directory, make the directory
        File dir = new File(filePath);
        dir.mkdirs();
      }
      zipIn.closeEntry();
      entry = zipIn.getNextEntry();
    }
    zipIn.close();
  }

  /**
   * Convert String base 64 to blob type.
   *
   * @param stringBase64 input string base 64
   * @return blob type data
   * @throws SerialException SerialException
   * @throws SQLException SQLException
   */
  public static Blob toBlob(String stringBase64) throws SerialException, SQLException {
    byte[] decodedByte = Base64.decodeBase64(stringBase64);
    Blob blob = new SerialBlob(decodedByte);
    return blob;
  }

  /**
   * Convert Blob data to String base 64.
   *
   * @param blob blob type data
   * @return string base64 data
   * @throws SQLException SQLException
   */
  public static String toBase64String(Blob blob) throws SQLException {
    byte[] imageByte = blob.getBytes(1, (int) blob.length());
    String imgString = Base64.encodeBase64String(imageByte);
    return imgString;
  }
}
