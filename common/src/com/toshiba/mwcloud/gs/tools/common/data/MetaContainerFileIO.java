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

package com.toshiba.mwcloud.gs.tools.common.data;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.toshiba.mwcloud.gs.ColumnInfo;
import com.toshiba.mwcloud.gs.ContainerType;
import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.IndexInfo;
import com.toshiba.mwcloud.gs.IndexType;
import com.toshiba.mwcloud.gs.TimeSeriesProperties;
import com.toshiba.mwcloud.gs.TimeUnit;
import com.toshiba.mwcloud.gs.TriggerInfo;
import com.toshiba.mwcloud.gs.tools.common.GridStoreCommandException;

public class MetaContainerFileIO {

	private String m_metaFilePath;

	private File m_metaFile;

	private JsonParser jp;

	private PrintWriter m_outMetaFile;

	public void writeStart(boolean multi) {
	}

	public void writeEnd() {
		if (m_metaFile != null) {
			m_outMetaFile.print("]");
			m_outMetaFile.close();
			m_outMetaFile = null;
			m_metaFile = null;
		}
	}

	public void writeMetaFile(ToolContainerInfo contInfo, String dir, boolean multi) throws GridStoreCommandException {
		List<ToolContainerInfo> contInfoList = new ArrayList<ToolContainerInfo>();
		contInfoList.add(contInfo);
		writeMetaFile(contInfoList, dir, multi);
	}

	public List<ToolContainerInfo> writeMetaFile(List<ToolContainerInfo> contInfoList, String dir, boolean multi)
			throws GridStoreCommandException {

		List<ToolContainerInfo> resultList = new ArrayList<ToolContainerInfo>();
		File metaFile = null;
		for (ToolContainerInfo cInfo : contInfoList) {
			try {
				metaFile = new File(dir, cInfo.getFileBaseName() + ToolConstants.FILE_EXT_METAINFO);

				StringWriter sw = buildJsonObjects(cInfo);

				if ((m_metaFile == null) || !m_metaFile.equals(metaFile)) {
					if (m_metaFile != null) {
						m_outMetaFile.print("]");
						m_outMetaFile.close();
						m_outMetaFile = null;
					}
					m_metaFile = metaFile;

					m_outMetaFile = new PrintWriter(new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(m_metaFile), ToolConstants.ENCODING_JSON)));
					if (multi) {
						m_outMetaFile.print("[");
					}
				} else {
					m_outMetaFile.println(",");
				}

				m_outMetaFile.write(sw.toString());
				m_outMetaFile.flush();

				if (multi) {
				} else {
					m_outMetaFile.close();
					m_outMetaFile = null;
					m_metaFile = null;
				}

				resultList.add(cInfo);

			} catch (Exception e) {
				String errMsg = "Failed to read Meta Information file." + ": containerName=["
						+ cInfo.getContainerInfo().getName() + "] msg=[" + e.getMessage() + "] path=["
						+ metaFile.getAbsolutePath() + "]";
				throw new GridStoreCommandException(errMsg, e);
			}
		}
		return resultList;
	}

	public ToolContainerInfo readMetaInfo(String filePath) throws GridStoreCommandException {

		try {
			if (jp != null) {
				jp.close();
				jp = null;
			}
			File file = new File(filePath);
			m_metaFilePath = file.getCanonicalPath();
			jp = Json.createParser(
					new InputStreamReader(skipBOM(new FileInputStream(file)), ToolConstants.ENCODING_JSON));

			if (!jp.hasNext())
				throw new GridStoreCommandException("The json format of metaFile is invalid.");
			Event e = jp.next();
			if (e == Event.START_ARRAY) {
				if (!jp.hasNext())
					throw new GridStoreCommandException("The json format of metaFile is invalid.");
				if (jp.next() != Event.START_OBJECT)
					throw new GridStoreCommandException("The json format of metaFile is invalid.");
			} else if (e == Event.START_OBJECT) {
			} else {
				throw new GridStoreCommandException("The json format of metaFile is invalid. : event=[" + e + "]");
			}

			ToolContainerInfo ci = readMetaFile(null, null);

			ToolContainerInfo nextContInfo = readMetaFile(null, null);
			if (nextContInfo != null) {
				throw new GridStoreCommandException("The number of ContainerInfo in metaFile must be 1.");
			}

			m_metaFilePath = null;
			jp.close();
			jp = null;

			return ci;

		} catch (Exception e) {
			throw new GridStoreCommandException(e.getMessage(), e);
		}
	}

	public ToolContainerInfo readMetaInfo(File file, String containerName, String dbName)
			throws GridStoreCommandException {

		try {
			boolean readNewFile = true;

			if (file.getCanonicalPath().equals(m_metaFilePath) && jp.hasNext()) {
				if (jp.next() != Event.START_OBJECT) {
					readNewFile = true;
				} else {
					readNewFile = false;
				}
			}

			ToolContainerInfo ci = null;

			for (int i = 0; i < 2; i++) {
				if (readNewFile) {
					if (jp != null)
						jp.close();
					m_metaFilePath = file.getCanonicalPath();
					jp = Json.createParser(
							new InputStreamReader(skipBOM(new FileInputStream(file)), ToolConstants.ENCODING_JSON));

					if (!jp.hasNext())
						throw new GridStoreCommandException("json invalid");
					Event e = jp.next();
					if (e == Event.START_ARRAY) {
						if (!jp.hasNext())
							throw new GridStoreCommandException("json invalid");
						if (jp.next() != Event.START_OBJECT)
							throw new GridStoreCommandException("Object format is required.");
					} else if (e == Event.START_OBJECT) {
					} else {
						throw new GridStoreCommandException("json invalid.: event=[" + e + "]");
					}
				}

				ci = readMetaFile(containerName, dbName);

				if (ci != null) {
					break;
				} else {
					if (readNewFile || (i == 1)) {
						throw new GridStoreCommandException("Container info does not exist in metaFile.");
					}
					readNewFile = true;
				}
			}

			return ci;

		} catch (GridStoreCommandException e) {
			throw new GridStoreCommandException("D00B01: Meta Information File Read failed." + ": containerName=["
					+ containerName + "] msg=[" + e.getMessage() + "] path=[" + m_metaFilePath + "]", e);

		} catch (Exception e) {
			throw new GridStoreCommandException("Error occured in read metaFile." + ": containerName=[" + containerName
					+ "] msg=[" + e.getMessage() + "] path=[" + m_metaFilePath + "]", e);

		} finally {
		}
	}

	private ToolContainerInfo readMetaFile(String containerName, String dbName) throws GridStoreCommandException {
		try {
			int depth = 1;
			boolean isContainer = false;
			boolean endFlag = false;
			int containerDepth = 0;
			String key = null;
			ToolContainerInfo ci = null;
			boolean typeFlag = false;
			String jsonDbName = ToolConstants.PUBLIC_DB;
			String version = null;
			String expirationType = null;
			int expirationTime = -1;
			TimeUnit expirationTimeUnit = null;

			while (jp.hasNext()) {
				Event event = jp.next();
				switch (event) {
				case KEY_NAME:
					key = jp.getString();

					if (isContainer) {
						if (key.equalsIgnoreCase(ToolConstants.JSON_META_COLUMN_SET)) {
							readColumnSet(jp, ci);

						} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_TIME_PROP)) {
							readTimeSeriesProperties(jp, ci);

						} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_INDEX_SET)) {
							readIndexSet(jp, ci);

						} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_TRIGGER_SET)) {
							readTriggerInfoSet(jp, ci);

						} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_CMP_SET)) {
							readCompressionInfoSet(jp, ci);

						} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_TP_PROPS)) {
							readTablePartitionProperties(jp, ci);

						} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_SCHEMA_INFORMATION)) {
							int schemaInformationDepth = depth;
							int tmpDepth = depth;

							boolean schemaInformationEndFlag = false;
							boolean firstEvent = true;
							while (jp.hasNext()) {
								Event readEvent = jp.next();
								if (firstEvent && readEvent != Event.START_OBJECT) {
									throw new GridStoreCommandException("The value of '"
											+ ToolConstants.JSON_META_SCHEMA_INFORMATION + "' must be object."
											+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
								}

								switch (readEvent) {
								case START_ARRAY:
									tmpDepth++;
									break;
								case END_ARRAY:
									tmpDepth--;
									break;
								case START_OBJECT:
									tmpDepth++;
									break;
								case END_OBJECT:
									tmpDepth--;
									if (tmpDepth == schemaInformationDepth) {
										schemaInformationEndFlag = true;
									}
									break;
								default:
									break;
								}
								firstEvent = false;

								if (schemaInformationEndFlag) {
									break;
								}
							}
						} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_ARCHIVE_INFO)) {
							int archiveInfoDepth = depth;
							int tmpDepth = depth;

							boolean archiveInfoEndFlag = false;
							boolean firstEvent = true;
							while (jp.hasNext()) {
								Event readEvent = jp.next();
								if (firstEvent && readEvent != Event.START_OBJECT) {
									throw new GridStoreCommandException("The value of '"
											+ ToolConstants.JSON_META_ARCHIVE_INFO + "' must be object."
											+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
								}

								switch (readEvent) {
								case START_ARRAY:
									tmpDepth++;
									break;
								case END_ARRAY:
									tmpDepth--;
									break;
								case START_OBJECT:
									tmpDepth++;
									break;
								case END_OBJECT:
									tmpDepth--;
									if (tmpDepth == archiveInfoDepth) {
										archiveInfoEndFlag = true;
									}
									break;
								default:
									break;
								}
								firstEvent = false;

								if (archiveInfoEndFlag) {
									break;
								}
							}
						} else {
							boolean match = false;
							for (int j = 0; j < ToolConstants.JSON_META_GROUP_CONTAINER.length; j++) {
								if (key.equalsIgnoreCase(ToolConstants.JSON_META_GROUP_CONTAINER[j])) {
									match = true;
									break;
								}
							}
							if (!match)
								throw new GridStoreCommandException("key '" + key + "' is invalid.: line(about)=["
										+ jp.getLocation().getLineNumber() + "]");
						}
					}
					break;

				case VALUE_STRING:
					String value = jp.getString();
					value = value.trim();

					if (key.equalsIgnoreCase(ToolConstants.JSON_META_CONTAINER) && (depth == 1)) {
						if ((containerName == null) || value.equalsIgnoreCase(containerName)) {
							if ((((dbName == null) || dbName.equalsIgnoreCase(ToolConstants.PUBLIC_DB))
									&& ((jsonDbName == null) || jsonDbName.equalsIgnoreCase(ToolConstants.PUBLIC_DB)))
									|| dbName.equalsIgnoreCase(jsonDbName)) {
								isContainer = true;
								typeFlag = false;
								containerDepth = depth;
								ci = new ToolContainerInfo();
								ci.setName(value);
								ci.setDbName(jsonDbName);
								ci.setVersion(version);
							}
						}
						jsonDbName = ToolConstants.PUBLIC_DB;

					} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_DBNAME) && (depth == 1)) {
						jsonDbName = value;
						if (jsonDbName.equalsIgnoreCase(ToolConstants.PUBLIC_DB))
							jsonDbName = ToolConstants.PUBLIC_DB;

					} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_VERSION)) {
						version = value;

					} else if (isContainer) {
						try {
							if (key.equalsIgnoreCase(ToolConstants.JSON_META_CONTAINER_TYPE)) {
								ci.setType(value);

							} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_CONTAINER_ATTRIBUTE)) {
								if (value != null) {
									ci.setAttribute(value);
								}

							} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_CONTAINER_FILE_TYPE)) {
								ci.setContainerFileType(value);

							} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_CONTAINER_FILE)) {
								if (containerName != null) {
									ci.addContainerFile(value, new File(m_metaFilePath).getParent());
								} else {
									ci.addContainerFile(value);
								}

							} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_CONTAINER_INTERNAL_FILE)) {
								ci.setContainerInternalFileName(value);

							} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_DATA_AFFINITY)) {
								ci.setDataAffinity(value);
							} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_EXPIRATION_TYPE)) {
								expirationType = value;
							} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_EXPIRATION_TIME_UNIT)) {
								if (!value.equalsIgnoreCase(ToolConstants.JSON_META_TIME_UNIT_NULL)) {
									try {
										expirationTimeUnit = TimeUnit.valueOf(value.toUpperCase());
									} catch (Exception e) {
										throw new GridStoreCommandException("'"
												+ ToolConstants.JSON_META_EXPIRATION_TIME_UNIT + "' is invalid. value=["
												+ value + "] line(about)=[" + jp.getLocation().getLineNumber() + "]",
												e);
									}
								}
							}
						} catch (GridStoreCommandException e) {
							throw new GridStoreCommandException(
									e.getMessage() + " line(about)=[" + jp.getLocation().getLineNumber() + "]", e);
						}
					}
					break;
				case VALUE_NUMBER:
					if (isContainer) {
						if (key.equalsIgnoreCase(ToolConstants.JSON_META_PARTITION_NO)) {
							ci.setPartitionNo(jp.getInt());
						} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_EXPIRATION_TIME)) {
							expirationTime = jp.getInt();
						}
					}
					break;

				case VALUE_TRUE:
					if (isContainer) {
						if (key.equalsIgnoreCase(ToolConstants.JSON_META_ROW_KEY)) {
							ci.setRowKeyAssigned(true);
							typeFlag = true;
						}
					}
					break;
				case VALUE_FALSE:
					if (isContainer) {
						if (key.equalsIgnoreCase(ToolConstants.JSON_META_ROW_KEY)) {
							ci.setRowKeyAssigned(false);
							typeFlag = true;
						}
					}
					break;
				case VALUE_NULL:
					break;
				case START_ARRAY:
					depth++;
					break;
				case END_ARRAY:
					depth--;
					break;
				case START_OBJECT:
					depth++;
					break;
				case END_OBJECT:
					if ((depth == containerDepth) && isContainer) {
						endFlag = true;
					}
					depth--;
					break;
				}
				if (endFlag) {
					break;
				}
			}

			if (expirationType != null) {
				if (expirationTime <= 0) {
					throw new GridStoreCommandException("'" + ToolConstants.JSON_META_EXPIRATION_TIME
							+ "' must be more than 0. value=[" + expirationTime + "]");
				}
				if (expirationTimeUnit == null) {
					throw new GridStoreCommandException(
							"'" + ToolConstants.JSON_META_EXPIRATION_TIME_UNIT + "' is required.");
				}
				ci.setExpirationInfo(new ExpirationInfo(expirationType, expirationTime, expirationTimeUnit));
			}

			if (ci != null) {
				if (!typeFlag) {
					throw new GridStoreCommandException("'" + ToolConstants.JSON_META_ROW_KEY + "' is required.");
				}
				ci.checkContainerInfo();

			}

			return ci;

		} catch (GridStoreCommandException e) {
			throw e;
		} catch (Exception e) {
			throw new GridStoreCommandException(
					e.getMessage() + ": line(about)=[" + jp.getLocation().getLineNumber() + "]", e);
		}
	}

	private void readColumnSet(JsonParser jp, ToolContainerInfo ci) throws GridStoreCommandException {
		if (!jp.hasNext()) {
			throw new GridStoreCommandException("'" + ToolConstants.JSON_META_COLUMN_SET + "' is invalid."
					+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
		}
		if (jp.next() != Event.START_ARRAY) {
			throw new GridStoreCommandException("'" + ToolConstants.JSON_META_COLUMN_SET + "' must be array."
					+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
		}

		Event event = null;
		String columnKey = null;
		while (jp.hasNext()) {
			event = jp.next();
			if (event == Event.END_ARRAY)
				break;
			if (event != Event.START_OBJECT) {
				throw new GridStoreCommandException("The value of '" + ToolConstants.JSON_META_COLUMN_SET
						+ "' must be object." + ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
			}

			boolean typeFlag = false;

			String columnName = null;
			GSType columnType = null;
			Boolean nullable = null;
			for (int i = 0; i < 6; i++) {
				event = jp.next();
				if (event == Event.KEY_NAME) {
					if (jp.getString().equalsIgnoreCase(ToolConstants.JSON_META_COLUMN_NAME)
							|| jp.getString().equalsIgnoreCase(ToolConstants.JSON_META_COLUMN_TYPE)
							|| jp.getString().equalsIgnoreCase(ToolConstants.JSON_META_COLUMN_CSTR_NOTNULL)) {
						columnKey = jp.getString();
					} else {
						throw new GridStoreCommandException(
								"'" + jp.getString() + "' is not the key of '" + ToolConstants.JSON_META_COLUMN_SET
										+ "'." + ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
					}
				} else if (event == Event.VALUE_STRING) {
					String value = jp.getString();
					if (columnKey.equalsIgnoreCase(ToolConstants.JSON_META_COLUMN_NAME)) {
						if (value.length() == 0) {
							throw new GridStoreCommandException("'" + ToolConstants.JSON_META_COLUMN_NAME
									+ "' is required." + ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
						}
						columnName = jp.getString();
					} else if (columnKey.equalsIgnoreCase(ToolConstants.JSON_META_COLUMN_TYPE)) {
						columnType = convertStringToColumnType(jp.getString());
						typeFlag = true;
					} else if (columnKey.equalsIgnoreCase(ToolConstants.JSON_META_COLUMN_CSTR_NOTNULL)) {
						throw new GridStoreCommandException("The value of '"
								+ ToolConstants.JSON_META_COLUMN_CSTR_NOTNULL + "' must be boolean or null"
								+ "'.: line(about)=[" + jp.getLocation().getLineNumber() + "]");
					} else {
						throw new GridStoreCommandException(
								"'" + columnKey + "' is not the key of '" + ToolConstants.JSON_META_COLUMN_SET
										+ "'.: line(about)=[" + jp.getLocation().getLineNumber() + "]");
					}
				} else if (event == Event.VALUE_TRUE || event == Event.VALUE_FALSE || event == Event.VALUE_NULL) {
					if (columnKey.equalsIgnoreCase(ToolConstants.JSON_META_COLUMN_CSTR_NOTNULL)) {
						if (event == Event.VALUE_TRUE) {
							nullable = false;
						} else if (event == Event.VALUE_FALSE) {
							nullable = true;
						}
					} else {
						throw new GridStoreCommandException("The value of '" + columnKey + "' must be string"
								+ "'.: line(about)=[" + jp.getLocation().getLineNumber() + "]");
					}
				} else if (event == Event.END_OBJECT) {
					break;
				} else {
					throw new GridStoreCommandException("'" + ToolConstants.JSON_META_COLUMN_SET + "' is invalid."
							+ ": event=[" + event + "] line(about)=[" + jp.getLocation().getLineNumber() + "]");
				}
			}

			if (event != Event.END_OBJECT && jp.next() != Event.END_OBJECT) {
				throw new GridStoreCommandException("'" + ToolConstants.JSON_META_COLUMN_SET + "' is invalid."
						+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
			}

			if (columnName == null) {
				throw new GridStoreCommandException("'" + ToolConstants.JSON_META_COLUMN_NAME + "' is required."
						+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
			} else if (!typeFlag) {
				throw new GridStoreCommandException("'" + ToolConstants.JSON_META_COLUMN_TYPE + "' is required."
						+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
			}

			ColumnInfo columnInfo = new ColumnInfo(columnName, columnType, nullable, null);
			ci.addColumnInfo(columnInfo);
		}
	}

	public void readIndexSet(JsonParser jp, ToolContainerInfo ci) throws GridStoreCommandException {
		if (!jp.hasNext()) {
			throw new GridStoreCommandException("'" + ToolConstants.JSON_META_INDEX_SET + "' is invalid."
					+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
		}
		if (jp.next() != Event.START_ARRAY) {
			throw new GridStoreCommandException("'" + ToolConstants.JSON_META_INDEX_SET + "' must be array."
					+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
		}
		Event event = null;
		String key = null;
		while (jp.hasNext()) {
			event = jp.next();
			if (event == Event.END_ARRAY)
				break;
			if (event != Event.START_OBJECT) {
				throw new GridStoreCommandException("The value of '" + ToolConstants.JSON_META_INDEX_SET
						+ "' must be object." + ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
			}
			String columnName = null;
			IndexType indexType = null;
			boolean typeFlag = false;
			String indexName = null;

			for (int i = 0; i < 6; i++) {
				event = jp.next();
				if (event == Event.KEY_NAME) {
					if (jp.getString().equalsIgnoreCase(ToolConstants.JSON_META_INDEX_NAME)) {
						key = ToolConstants.JSON_META_INDEX_NAME;

					} else if (jp.getString().equalsIgnoreCase(ToolConstants.JSON_META_INDEX_TYPE1)
							|| jp.getString().equalsIgnoreCase(ToolConstants.JSON_META_INDEX_TYPE2)) {
						key = ToolConstants.JSON_META_INDEX_TYPE1;
					} else if (jp.getString().equalsIgnoreCase(ToolConstants.JSON_META_INDEX_INDEXNAME)) {
						key = ToolConstants.JSON_META_INDEX_INDEXNAME;
					} else {
						throw new GridStoreCommandException(
								"'" + jp.getString() + "' is not the key of '" + ToolConstants.JSON_META_INDEX_SET
										+ "'." + ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
					}
				} else if (event == Event.VALUE_STRING) {
					String value = jp.getString();
					if (key.equalsIgnoreCase(ToolConstants.JSON_META_INDEX_NAME)) {
						if (value.length() == 0) {
							throw new GridStoreCommandException("'" + ToolConstants.JSON_META_INDEX_NAME
									+ "' is required." + ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
						}
						columnName = value;
					} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_INDEX_TYPE1)) {
						try {
							indexType = IndexType.valueOf(value.toUpperCase());
						} catch (Exception e) {
							throw new GridStoreCommandException(
									"'" + ToolConstants.JSON_META_INDEX_TYPE1 + "' is invalid." + ": value=[" + value
											+ "] line(about)=[" + jp.getLocation().getLineNumber() + "]",
									e);
						}
						typeFlag = true;
					} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_INDEX_INDEXNAME)) {
						indexName = value;
					} else {
						throw new GridStoreCommandException("'" + ToolConstants.JSON_META_INDEX_SET + "' is invalid."
								+ ": key=[" + key + "] line(about)=[" + jp.getLocation().getLineNumber() + "]");
					}
				} else if (event == Event.VALUE_NULL) {
					if (key.equalsIgnoreCase(ToolConstants.JSON_META_INDEX_INDEXNAME)) {
						indexName = null;
					} else {
						throw new GridStoreCommandException("'" + ToolConstants.JSON_META_INDEX_SET + "' is invalid."
								+ ": event=[" + event + "] line(about)=[" + jp.getLocation().getLineNumber() + "]");
					}
				} else if (event == Event.END_OBJECT) {
					break;
				} else {
					throw new GridStoreCommandException("'" + ToolConstants.JSON_META_INDEX_SET + "' is invalid."
							+ ": event=[" + event + "] line(about)=[" + jp.getLocation().getLineNumber() + "]");
				}
			}

			if (event != Event.END_OBJECT && jp.next() != Event.END_OBJECT) {
				throw new GridStoreCommandException("'" + ToolConstants.JSON_META_INDEX_SET + "' is invalid."
						+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
			}

			if (columnName == null) {
				throw new GridStoreCommandException("'" + ToolConstants.JSON_META_INDEX_NAME + "' is required."
						+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
			} else if (!typeFlag) {
				throw new GridStoreCommandException("'" + ToolConstants.JSON_META_INDEX_TYPE1 + "' is required."
						+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
			}

			ci.addIndexInfo(columnName, indexType, indexName);
		}
	}

	public void readTriggerInfoSet(JsonParser jp, ToolContainerInfo ci) throws GridStoreCommandException {
		if (!jp.hasNext()) {
			throw new GridStoreCommandException("'" + ToolConstants.JSON_META_TRIGGER_SET + "' is invalid."
					+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
		}
		if (jp.next() != Event.START_ARRAY) {
			throw new GridStoreCommandException("'" + ToolConstants.JSON_META_TRIGGER_SET + "' must be array."
					+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
		}

		Event event = null;
		Set<String> nameSet = new HashSet<String>();
		List<TriggerInfo> triggerList = new ArrayList<TriggerInfo>();
		while (jp.hasNext()) {
			event = jp.next();
			if (event == Event.END_ARRAY)
				break;
			if (event != Event.START_OBJECT) {
				throw new GridStoreCommandException("The value of '" + ToolConstants.JSON_META_TRIGGER_SET
						+ "' must be object." + ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
			}

			TriggerInfo trigger = new TriggerInfo();
			String key = null;
			while (jp.hasNext()) {
				event = jp.next();
				if (event == Event.KEY_NAME) {
					key = null;
					for (int i = 0; i < ToolConstants.JSON_META_GROUP_TRIGGER.length; i++) {
						if (jp.getString().equalsIgnoreCase(ToolConstants.JSON_META_GROUP_TRIGGER[i])) {
							key = ToolConstants.JSON_META_GROUP_TRIGGER[i];
							break;
						}
					}
					if (key == null) {
						throw new GridStoreCommandException(
								"'" + jp.getString() + "' is not the key of " + ToolConstants.JSON_META_TRIGGER_SET
										+ "'." + ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
					}
				} else if (event == Event.VALUE_STRING) {
					String value = jp.getString();

					if (key == ToolConstants.JSON_META_TRIGGER_EVENTNAME) {
						if (nameSet.contains(value)) {
							throw new GridStoreCommandException(
									"key:'" + key + "' value:'" + value + "'  same value already exists."
											+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
						}
						trigger.setName(value);
						nameSet.add(value);

					} else if (key == ToolConstants.JSON_META_TRIGGER_COLUMN) {
						Set<String> targetColumns = new HashSet<String>(Arrays.asList(value.split(",", 0)));
						trigger.setTargetColumns(targetColumns);

					} else if (key == ToolConstants.JSON_META_TRIGGER_TARGET) {
						String[] list = value.split(",", 0);
						Set<TriggerInfo.EventType> triggerTypeList = new HashSet<TriggerInfo.EventType>(list.length);
						for (int j = 0; j < list.length; j++) {
							try {
								triggerTypeList.add(TriggerInfo.EventType.valueOf(list[j]));
							} catch (IllegalArgumentException e) {
								throw new GridStoreCommandException(
										"'" + ToolConstants.JSON_META_TRIGGER_TARGET + "' is invalid." + ": value=["
												+ list[j] + "] line(about)=[" + jp.getLocation().getLineNumber() + "]",
										e);
							}
						}
						trigger.setTargetEvents(triggerTypeList);

					} else if (key == ToolConstants.JSON_META_TRIGGER_TYPE) {
						try {
							trigger.setType(TriggerInfo.Type.valueOf(value));
						} catch (IllegalArgumentException e) {
							throw new GridStoreCommandException(
									"'" + ToolConstants.JSON_META_TRIGGER_TYPE + "' is invalid." + ": value=[" + value
											+ "] line(about)=[" + jp.getLocation().getLineNumber() + "]",
									e);
						}
					} else if (key == ToolConstants.JSON_META_TRIGGER_URI) {
						try {
							trigger.setURI(new URI(value));
						} catch (URISyntaxException e) {
							throw new GridStoreCommandException(
									"'" + ToolConstants.JSON_META_TRIGGER_URI + "' is invalid." + ": key=[" + key
											+ "] line(about)=[" + jp.getLocation().getLineNumber() + "]",
									e);
						}
					} else if (key == ToolConstants.JSON_META_TRIGGER_JMS_NAME) {
						trigger.setJMSDestinationName(value);

					} else if (key == ToolConstants.JSON_META_TRIGGER_JMS_TYPE) {
						trigger.setJMSDestinationType(value);

					} else if (key == ToolConstants.JSON_META_TRIGGER_JMS_USER) {
						trigger.setUser(value);

					} else if (key == ToolConstants.JSON_META_TRIGGER_JMS_PASS) {
						trigger.setPassword(value);

					} else {
						throw new GridStoreCommandException("'" + ToolConstants.JSON_META_TRIGGER_SET + "' is invalid."
								+ ": key=[" + key + "] line(about)=[" + jp.getLocation().getLineNumber() + "]");
					}

				} else if (event == Event.END_OBJECT) {
					break;
				} else {
					throw new GridStoreCommandException("'" + ToolConstants.JSON_META_TRIGGER_SET + "' is invalid."
							+ ": event=[" + event + "] line(about)=[" + jp.getLocation().getLineNumber() + "]");
				}
			}

			if ((trigger.getName() == null) || (trigger.getName().length() == 0)) {
				throw new GridStoreCommandException("'" + ToolConstants.JSON_META_TRIGGER_EVENTNAME + "' is required."
						+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");

			} else if (trigger.getType() == null) {
				throw new GridStoreCommandException("'" + ToolConstants.JSON_META_TRIGGER_TYPE + "' is required."
						+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
			}

			triggerList.add(trigger);
		}

		ci.setTriggerInfoList(triggerList);
	}

	public void readCompressionInfoSet(JsonParser jp, ToolContainerInfo ci) throws GridStoreCommandException {
		if (!jp.hasNext()) {
			throw new GridStoreCommandException("'" + ToolConstants.JSON_META_CMP_SET + "' is invalid."
					+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
		}
		if (jp.next() != Event.START_ARRAY) {
			throw new GridStoreCommandException("'" + ToolConstants.JSON_META_CMP_SET + "' must be array."
					+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
		}

		Event event = null;
		while (jp.hasNext()) {
			event = jp.next();
			if (event == Event.END_ARRAY)
				break;
			if (event != Event.START_OBJECT) {
				throw new GridStoreCommandException("The value of '" + ToolConstants.JSON_META_CMP_SET
						+ "' must be object." + ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
			}

			String key = null;
			String compressionType = null;
			String columnName = null;
			double rate = -1;
			double span = -1;
			double width = -1;
			while (jp.hasNext()) {
				event = jp.next();
				if (event == Event.KEY_NAME) {
					key = null;
					for (int j = 0; j < ToolConstants.JSON_META_GROUP_CMP.length; j++) {
						if (jp.getString().equalsIgnoreCase(ToolConstants.JSON_META_GROUP_CMP[j])) {
							key = jp.getString();
							break;
						}
					}
					if (key == null) {
						throw new GridStoreCommandException("key '" + jp.getString() + "' is invalid."
								+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
					}

				} else if (event == Event.VALUE_STRING) {
					String value = jp.getString();
					if (key.equalsIgnoreCase(ToolConstants.JSON_META_CMP_NAME)) {
						if (value.length() == 0) {
							throw new GridStoreCommandException("'" + ToolConstants.JSON_META_CMP_NAME
									+ "' is required." + ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
						}
						columnName = value;

					} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_CMP_TYPE)) {
						compressionType = value;
						if (value.equalsIgnoreCase(ToolConstants.COMPRESSION_TYPE_RELATIVE)) {
						} else if (value.equalsIgnoreCase(ToolConstants.COMPRESSION_TYPE_ABSOLUTE)) {
						} else {
							throw new GridStoreCommandException("'" + ToolConstants.JSON_META_CMP_TYPE + "' is invalid."
									+ ": key=[" + key + "] value=[" + value + "] line(about)=["
									+ jp.getLocation().getLineNumber() + "]");
						}
					} else {
						throw new GridStoreCommandException("'" + ToolConstants.JSON_META_CMP_SET + "' is invalid."
								+ ": key=[" + key + "] line(about)=[" + jp.getLocation().getLineNumber() + "]");
					}

				} else if (event == Event.VALUE_NUMBER) {
					if (key.equalsIgnoreCase(ToolConstants.JSON_META_CMP_RATE)) {
						rate = jp.getBigDecimal().doubleValue();
					} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_CMP_SPAN)) {
						span = jp.getBigDecimal().doubleValue();
					} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_CMP_WIDTH)) {
						width = jp.getBigDecimal().doubleValue();
					}

				} else if (event == Event.END_OBJECT) {
					break;
				} else {
					throw new GridStoreCommandException("'" + ToolConstants.JSON_META_CMP_SET + "' is invalid."
							+ ": event=[" + event + "] line(about)=[" + jp.getLocation().getLineNumber() + "]");
				}
			}

			if (compressionType.equalsIgnoreCase(ToolConstants.COMPRESSION_TYPE_RELATIVE)) {
				if (width != -1) {
					throw new GridStoreCommandException("'" + ToolConstants.JSON_META_CMP_SET + "' is invalid."
							+ ": event=[" + event + "] line(about)=[" + jp.getLocation().getLineNumber() + "]");
				}
				ci.setRelativeHiCompression(columnName, rate, span);
			} else {
				if (rate != -1 || span != -1) {
					throw new GridStoreCommandException("'" + ToolConstants.JSON_META_CMP_SET + "' is invalid."
							+ ": event=[" + event + "] line(about)=[" + jp.getLocation().getLineNumber() + "]");
				}
				ci.setAbsoluteHiCompression(columnName, width);
			}
		}
	}

	public void readTimeSeriesProperties(JsonParser jp, ToolContainerInfo ci) throws GridStoreCommandException {
		if (!jp.hasNext()) {
			throw new GridStoreCommandException("'" + ToolConstants.JSON_META_TIME_PROP + "' is invalid."
					+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
		}
		if (jp.next() != Event.START_OBJECT) {
			throw new GridStoreCommandException("'" + ToolConstants.JSON_META_TIME_PROP + "' is invalid."
					+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
		}

		Event event = null;
		String key = null;
		int windowSize = -1;
		int rowExpirationTime = -1;
		TimeUnit windowUnit = null;
		TimeUnit rowExpirationUnit = null;

		while (jp.hasNext()) {
			event = jp.next();
			if (event == Event.KEY_NAME) {
				key = null;
				for (int i = 0; i < ToolConstants.JSON_META_GROUP_TIME.length; i++) {
					if (jp.getString().equalsIgnoreCase(ToolConstants.JSON_META_GROUP_TIME[i])) {
						key = ToolConstants.JSON_META_GROUP_TIME[i];
					}
				}
				if (key == null) {
					throw new GridStoreCommandException("'" + ToolConstants.JSON_META_TIME_PROP + "' is invalid."
							+ ": key=[" + jp.getString() + "] line(about)=[" + jp.getLocation().getLineNumber() + "]");
				}
			} else if (event == Event.VALUE_STRING) {
				String value = jp.getString();
				if (key.equalsIgnoreCase(ToolConstants.JSON_META_TIME_COMP)) {
					ci.setCompressionMethod(value);

				} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_TIME_WINDOW_UNIT)) {
					if (!value.equalsIgnoreCase(ToolConstants.JSON_META_TIME_UNIT_NULL)) {
						try {
							windowUnit = TimeUnit.valueOf(value.toUpperCase());
						} catch (Exception e) {
							throw new GridStoreCommandException(
									"'" + ToolConstants.JSON_META_TIME_WINDOW_UNIT + "' is invalid. value=[" + value
											+ "] line(about)=[" + jp.getLocation().getLineNumber() + "]",
									e);
						}
					}
				} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_TIME_EXPIRATION_UNIT)) {
					if (!value.equalsIgnoreCase(ToolConstants.JSON_META_TIME_UNIT_NULL)) {
						try {
							rowExpirationUnit = TimeUnit.valueOf(value.toUpperCase());
						} catch (Exception e) {
							throw new GridStoreCommandException(
									"'" + ToolConstants.JSON_META_TIME_EXPIRATION_UNIT + "' is invalid. value=[" + value
											+ "] line(about)=[" + jp.getLocation().getLineNumber() + "]",
									e);
						}
					}
				} else {
					throw new GridStoreCommandException("'" + ToolConstants.JSON_META_TIME_PROP + "' is invalid."
							+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
				}
				key = null;
			} else if (event == Event.VALUE_NUMBER) {
				if (key.equalsIgnoreCase(ToolConstants.JSON_META_TIME_WINDOW)) {
					windowSize = jp.getInt();

				} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_TIME_EXPIRATION)) {
					rowExpirationTime = jp.getInt();

				} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_TIME_EXPIRATION_DIV_COUNT)) {
					ci.setExpirationDivisionCount(jp.getInt());

				} else {
					throw new GridStoreCommandException("'" + ToolConstants.JSON_META_TIME_PROP + "' is invalid."
							+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
				}
			} else if (event == Event.END_OBJECT) {
				break;
			} else {
				throw new GridStoreCommandException("'" + ToolConstants.JSON_META_TIME_PROP + "' is invalid."
						+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
			}
		}
		if (windowSize != -1) {
			ci.setCompressionWindowSize(windowSize, windowUnit);
		}
		if (rowExpirationTime != -1) {
			ci.setRowExpiration(rowExpirationTime, rowExpirationUnit);
		}
	}

	public void readTablePartitionProperties(JsonParser jp, ToolContainerInfo ci) throws GridStoreCommandException {

		if (!jp.hasNext()) {
			throw new GridStoreCommandException("'" + ToolConstants.JSON_META_TP_PROPS + "' is invalid."
					+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
		}

		Event event = jp.next();
		if ((event != Event.START_ARRAY) && (event != Event.START_OBJECT)) {
			throw new GridStoreCommandException("'" + ToolConstants.JSON_META_TP_PROPS + "' is invalid."
					+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]" + event);
		}

		List<TablePartitionProperty> properties = new ArrayList<TablePartitionProperty>();

		if (event == Event.START_OBJECT) {
			TablePartitionProperty tpp = readTablePartitionPropertiesObject(jp, ci);
			properties.add(tpp);

		} else if (event == Event.START_ARRAY) {
			while (jp.hasNext()) {
				event = jp.next();
				if (event == Event.START_OBJECT) {
					if (properties.size() == 2) {
						throw new GridStoreCommandException("The array length of '" + ToolConstants.JSON_META_TP_PROPS
								+ "' must be 2 or less." + ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
					}

					TablePartitionProperty tpp = readTablePartitionPropertiesObject(jp, ci);
					properties.add(tpp);

				} else if (event == Event.END_ARRAY) {
					break;

				} else {
					throw new GridStoreCommandException("Elements of the array of '" + ToolConstants.JSON_META_TP_PROPS
							+ "' must be object." + ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
				}
			}
		}

		if (properties.size() == 0) {
			throw new GridStoreCommandException("'" + ToolConstants.JSON_META_TP_PROPS + "' is invalid."
					+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
		}

		if (properties.size() == 2) {
			String type0 = properties.get(0).getType();
			String type1 = properties.get(1).getType();

			if (!(type0.equalsIgnoreCase(ToolConstants.TABLE_PARTITION_TYPE_INTERVAL)
					&& type1.equalsIgnoreCase(ToolConstants.TABLE_PARTITION_TYPE_HASH))) {
				throw new GridStoreCommandException(
						type0 + "-" + type1 + " is not supported for composite partitioning.");
			}
		}

		ci.setTablePartitionProperties(properties);
	}

	private TablePartitionProperty readTablePartitionPropertiesObject(JsonParser jp, ToolContainerInfo ci)
			throws GridStoreCommandException {

		TablePartitionProperty partProp = null;
		String type = null;
		String column = null;
		String intervalValue = null;
		String intervalUnit = null;
		Integer divisionCount = null;

		Event event = null;
		String key = null;
		while (jp.hasNext()) {
			event = jp.next();

			switch (event) {
			case END_OBJECT:
				break;

			case KEY_NAME:
				if (jp.getString().equalsIgnoreCase(ToolConstants.JSON_META_TP_TYPE)
						|| jp.getString().equalsIgnoreCase(ToolConstants.JSON_META_TP_COLUMN)
						|| jp.getString().equalsIgnoreCase(ToolConstants.JSON_META_TP_ITV_VALUE)
						|| jp.getString().equalsIgnoreCase(ToolConstants.JSON_META_TP_ITV_UNIT)
						|| jp.getString().equalsIgnoreCase(ToolConstants.JSON_META_TP_DIV_COUNT)) {
					key = jp.getString();
				} else {
					throw new GridStoreCommandException(
							"'" + jp.getString() + "' is not the key of '" + ToolConstants.JSON_META_TP_PROPS + "'."
									+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
				}
				break;

			case VALUE_STRING:
				String value = jp.getString();

				if (key.equalsIgnoreCase(ToolConstants.JSON_META_TP_TYPE)) {
					if (value.equalsIgnoreCase(ToolConstants.TABLE_PARTITION_TYPE_HASH)) {
						type = ToolConstants.TABLE_PARTITION_TYPE_HASH;
					} else if (value.equalsIgnoreCase(ToolConstants.TABLE_PARTITION_TYPE_INTERVAL)) {
						type = ToolConstants.TABLE_PARTITION_TYPE_INTERVAL;
					} else {
						throw new GridStoreCommandException("The value of '" + ToolConstants.JSON_META_TP_TYPE
								+ "' must be \"" + ToolConstants.TABLE_PARTITION_TYPE_HASH + "\" or \""
								+ ToolConstants.TABLE_PARTITION_TYPE_INTERVAL + "\".: line(about)=["
								+ jp.getLocation().getLineNumber() + "]");
					}

				} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_TP_COLUMN)) {
					if (!value.isEmpty()) {
						column = value;
					}
				} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_TP_ITV_VALUE)) {
					if (!value.isEmpty()) {
						intervalValue = value;
					}
				} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_TP_ITV_UNIT)) {
					if (!value.isEmpty()) {
						intervalUnit = value;
					}
				} else {
					if ((type == null) || type.equalsIgnoreCase(ToolConstants.TABLE_PARTITION_TYPE_HASH)) {
						throw new GridStoreCommandException("The value of '" + key + "' is invalid." + ": line(about)=["
								+ jp.getLocation().getLineNumber() + "]");
					}
				}
				break;

			case VALUE_NUMBER:
				if (key.equalsIgnoreCase(ToolConstants.JSON_META_TP_ITV_VALUE)) {
					intervalValue = Long.toString(jp.getLong());
				} else if (key.equalsIgnoreCase(ToolConstants.JSON_META_TP_DIV_COUNT)) {
					divisionCount = jp.getInt();
				} else {
					throw new GridStoreCommandException("The type of '" + key + "' must be string." + ": line(about)=["
							+ jp.getLocation().getLineNumber() + "]");
				}
				break;

			case VALUE_TRUE:
			case VALUE_FALSE:
				if (key.equalsIgnoreCase(ToolConstants.JSON_META_TP_TYPE)
						|| key.equalsIgnoreCase(ToolConstants.JSON_META_TP_COLUMN)) {
					throw new GridStoreCommandException("The type of '" + key + "' must be string." + ": line(about)=["
							+ jp.getLocation().getLineNumber() + "]");
				}
				break;

			case VALUE_NULL:
				break;
			default:
				throw new GridStoreCommandException("'" + ToolConstants.JSON_META_TP_PROPS + "' is invalid."
						+ ": event=[" + event + "] line(about)=[" + jp.getLocation().getLineNumber() + "]");
			}

			if (event == Event.END_OBJECT)
				break;
		}

		if (type == null) {
			throw new GridStoreCommandException(
					"'" + ToolConstants.JSON_META_TP_TYPE + "' is required in '" + ToolConstants.JSON_META_TP_PROPS
							+ "'." + ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
		}
		if (column == null) {
			throw new GridStoreCommandException(
					"'" + ToolConstants.JSON_META_TP_COLUMN + "' is required in '" + ToolConstants.JSON_META_TP_PROPS
							+ "'." + ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
		}

		if (type.equalsIgnoreCase(ToolConstants.TABLE_PARTITION_TYPE_HASH)) {
			if (divisionCount == null) {
				throw new GridStoreCommandException(
						"'" + ToolConstants.JSON_META_TP_DIV_COUNT + "' is required when 'type' is 'HASH'."
								+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
			}
			partProp = new TablePartitionProperty(type, column, divisionCount);

		} else if (type.equalsIgnoreCase(ToolConstants.TABLE_PARTITION_TYPE_INTERVAL)) {
			if (intervalValue == null) {
				throw new GridStoreCommandException(
						"'" + ToolConstants.JSON_META_TP_ITV_VALUE + "' is required when 'type' is 'INTERVAL'."
								+ ": line(about)=[" + jp.getLocation().getLineNumber() + "]");
			}
			partProp = new TablePartitionProperty(type, column, intervalValue, intervalUnit);
		}

		return partProp;
	}

	private StringWriter buildJsonObjects(ToolContainerInfo cInfo) throws Exception {

		StringWriter sw = new StringWriter();
		Map<String, Object> properties = new HashMap<String, Object>(1);
		properties.put(JsonGenerator.PRETTY_PRINTING, true);
		JsonGeneratorFactory factory = Json.createGeneratorFactory(properties);
		JsonGenerator gen = factory.createGenerator(sw);

		buildJson(cInfo, gen);

		gen.close();
		return sw;

	}

	private JsonGenerator buildJson(ToolContainerInfo cInfo, JsonGenerator gen) throws Exception {

		gen.writeStartObject();
		gen.write("version", ToolConstants.META_FILE_VERSION);
		if (cInfo.getDbName() != null) {
			gen.write(ToolConstants.JSON_META_DBNAME, cInfo.getDbName());
		}
		gen.write(ToolConstants.JSON_META_CONTAINER, cInfo.getName());
		gen.write(ToolConstants.JSON_META_CONTAINER_TYPE, cInfo.getType().toString());
		gen.write(ToolConstants.JSON_META_CONTAINER_FILE_TYPE, cInfo.getContainerFileType().toString().toLowerCase());
		if (cInfo.getContainerFileList() == null) {
		} else if (cInfo.getContainerFileList().size() == 1) {
			gen.write(ToolConstants.JSON_META_CONTAINER_FILE, cInfo.getContainerFile());
		} else {
			gen.writeStartArray(ToolConstants.JSON_META_CONTAINER_FILE);
			for (String fileName : cInfo.getContainerFileList()) {
				gen.write(fileName);
			}
			gen.writeEnd();
		}
		if (cInfo.getContainerInternalFileName() != null) {
			gen.write(ToolConstants.JSON_META_CONTAINER_INTERNAL_FILE, cInfo.getContainerInternalFileName());
		}
		if (cInfo.getDataAffinity() != null) {
			gen.write(ToolConstants.JSON_META_DATA_AFFINITY, cInfo.getDataAffinity());
		}
		gen.write(ToolConstants.JSON_META_ROW_KEY, cInfo.getRowKeyAssigned());
		gen.write(ToolConstants.JSON_META_PARTITION_NO, cInfo.getPartitionNo());

		if (cInfo.getColumnCount() > 0) {
			gen.writeStartArray(ToolConstants.JSON_META_COLUMN_SET);
			for (int j = 0; j < cInfo.getColumnCount(); j++) {
				gen.writeStartObject();
				gen.write(ToolConstants.JSON_META_COLUMN_NAME, cInfo.getColumnInfo(j).getName());
				gen.write(ToolConstants.JSON_META_COLUMN_TYPE, convertColumnType(cInfo.getColumnInfo(j).getType()));
				Boolean nullable = cInfo.getColumnInfo(j).getNullable();
				if (nullable == null) {
					gen.writeNull(ToolConstants.JSON_META_COLUMN_CSTR_NOTNULL);
				} else {
					gen.write(ToolConstants.JSON_META_COLUMN_CSTR_NOTNULL, !nullable);
				}
				gen.writeEnd();
			}
			gen.writeEnd();
		}

		int indexCount = 0;
		for (IndexInfo index : cInfo.getIndexInfoList()) {
			if (indexCount == 0) {
				gen.writeStartArray(ToolConstants.JSON_META_INDEX_SET);
			}
			gen.writeStartObject();
			gen.write(ToolConstants.JSON_META_INDEX_NAME, index.getColumnName());
			gen.write(ToolConstants.JSON_META_INDEX_TYPE1, index.getType().toString());
			String indexName = index.getName();
			if (indexName == null) {
				gen.writeNull(ToolConstants.JSON_META_INDEX_INDEXNAME);
			} else {
				gen.write(ToolConstants.JSON_META_INDEX_INDEXNAME, indexName);
			}
			gen.writeEnd();
			indexCount++;
		}
		if (indexCount > 0)
			gen.writeEnd();

		if (cInfo.getTriggerInfoList().size() > 0) {
			gen.writeStartArray(ToolConstants.JSON_META_TRIGGER_SET);
			for (TriggerInfo event : cInfo.getTriggerInfoList()) {
				gen.writeStartObject();
				gen.write(ToolConstants.JSON_META_TRIGGER_EVENTNAME, event.getName());
				gen.write(ToolConstants.JSON_META_TRIGGER_TYPE, event.getType().toString());
				StringBuilder sb_targetEvent = new StringBuilder();
				for (TriggerInfo.EventType ev : event.getTargetEvents()) {
					sb_targetEvent.append(ev.toString() + ",");
				}
				if (sb_targetEvent.length() > 2)
					sb_targetEvent.deleteCharAt(sb_targetEvent.length() - 1);
				gen.write(ToolConstants.JSON_META_TRIGGER_TARGET, sb_targetEvent.toString());
				StringBuilder sb_TargetColumnNames = new StringBuilder();
				for (String s : event.getTargetColumns()) {
					sb_TargetColumnNames.append(s + ",");
				}
				if (sb_TargetColumnNames.length() > 2)
					sb_TargetColumnNames.deleteCharAt(sb_TargetColumnNames.length() - 1);
				gen.write(ToolConstants.JSON_META_TRIGGER_COLUMN, sb_TargetColumnNames.toString());
				gen.write(ToolConstants.JSON_META_TRIGGER_URI, event.getURI().toString());
				if (event.getType().equals(TriggerInfo.Type.JMS)) {
					gen.write(ToolConstants.JSON_META_TRIGGER_JMS_TYPE, event.getJMSDestinationType());
					gen.write(ToolConstants.JSON_META_TRIGGER_JMS_NAME, event.getJMSDestinationName());
					gen.write(ToolConstants.JSON_META_TRIGGER_JMS_USER, event.getUser());
					gen.write(ToolConstants.JSON_META_TRIGGER_JMS_PASS, event.getPassword());
				}
				gen.writeEnd();
			}
			gen.writeEnd();
		}

		if (cInfo.getType().equals(ContainerType.TIME_SERIES)) {
			TimeSeriesProperties timeProp = cInfo.getTimeSeriesProperties();
			gen.writeStartObject(ToolConstants.JSON_META_TIME_PROP);
			gen.write(ToolConstants.JSON_META_TIME_COMP, timeProp.getCompressionMethod().toString());
			gen.write(ToolConstants.JSON_META_TIME_WINDOW, timeProp.getCompressionWindowSize());
			if (timeProp.getCompressionWindowSizeUnit() == null) {
				gen.write(ToolConstants.JSON_META_TIME_WINDOW_UNIT, "null");
			} else {
				gen.write(ToolConstants.JSON_META_TIME_WINDOW_UNIT, timeProp.getCompressionWindowSizeUnit().toString());
			}
			gen.write(ToolConstants.JSON_META_TIME_EXPIRATION_DIV_COUNT, timeProp.getExpirationDivisionCount());
			gen.write(ToolConstants.JSON_META_TIME_EXPIRATION, timeProp.getRowExpirationTime());
			if (timeProp.getRowExpirationTimeUnit() == null) {
				gen.write(ToolConstants.JSON_META_TIME_EXPIRATION_UNIT, "null");
			} else {
				gen.write(ToolConstants.JSON_META_TIME_EXPIRATION_UNIT, timeProp.getRowExpirationTimeUnit().toString());
			}
			gen.writeEnd();

			gen.writeStartArray(ToolConstants.JSON_META_CMP_SET);
			for (String compColmnName : timeProp.getSpecifiedColumns()) {
				gen.writeStartObject();
				gen.write(ToolConstants.JSON_META_CMP_NAME, compColmnName);
				if (timeProp.isCompressionRelative(compColmnName)) {
					gen.write(ToolConstants.JSON_META_CMP_TYPE, ToolConstants.COMPRESSION_TYPE_RELATIVE);
					gen.write(ToolConstants.JSON_META_CMP_RATE, timeProp.getCompressionRate(compColmnName));
					gen.write(ToolConstants.JSON_META_CMP_SPAN, timeProp.getCompressionSpan(compColmnName));
				} else {
					gen.write(ToolConstants.JSON_META_CMP_TYPE, ToolConstants.COMPRESSION_TYPE_ABSOLUTE);
					gen.write(ToolConstants.JSON_META_CMP_WIDTH, timeProp.getCompressionWidth(compColmnName));
				}
				gen.writeEnd();
			}
			gen.writeEnd();
		}

		if (cInfo.getTablePartitionProperties().size() > 0) {
			if (cInfo.getTablePartitionProperties().size() != 1) {
				gen.writeStartArray(ToolConstants.JSON_META_TP_PROPS);
			}
			for (TablePartitionProperty prop : cInfo.getTablePartitionProperties()) {
				if (cInfo.getTablePartitionProperties().size() == 1) {
					gen.writeStartObject(ToolConstants.JSON_META_TP_PROPS);
				} else {
					gen.writeStartObject();
				}
				gen.write(ToolConstants.JSON_META_TP_TYPE, prop.getType());
				gen.write(ToolConstants.JSON_META_TP_COLUMN, prop.getColumn());
				if (prop.getType().equalsIgnoreCase(ToolConstants.TABLE_PARTITION_TYPE_HASH)) {
					gen.write(ToolConstants.JSON_META_TP_DIV_COUNT, prop.getDivisionCount());
				} else if (prop.getType().equalsIgnoreCase(ToolConstants.TABLE_PARTITION_TYPE_INTERVAL)) {
					gen.write(ToolConstants.JSON_META_TP_ITV_VALUE, prop.getIntervalValue());
					if (prop.getIntervalUnit() != null && !prop.getIntervalUnit().isEmpty()) {
						gen.write(ToolConstants.JSON_META_TP_ITV_UNIT, prop.getIntervalUnit());
					}
				}
				gen.writeEnd();
			}
			if (cInfo.getTablePartitionProperties().size() != 1) {
				gen.writeEnd();
			}
		}

		ExpirationInfo expInfo = cInfo.getExpirationInfo();
		if (expInfo != null) {
			gen.write(ToolConstants.JSON_META_EXPIRATION_TYPE, expInfo.getType());
			gen.write(ToolConstants.JSON_META_EXPIRATION_TIME, expInfo.getTime());
			gen.write(ToolConstants.JSON_META_EXPIRATION_TIME_UNIT, expInfo.getTimeUnit().toString());
		}

		gen.writeEnd();

		return gen;
	}

	public static GSType convertStringToColumnType(String type) throws GridStoreCommandException {
		try {
			type = type.trim();

			if (type.equalsIgnoreCase(ToolConstants.COLUMN_TYPE_STRING_ARRAY)) {
				return GSType.STRING_ARRAY;
			} else if (type.equalsIgnoreCase(ToolConstants.COLUMN_TYPE_BOOL_ARRAY)) {
				return GSType.BOOL_ARRAY;
			} else if (type.equalsIgnoreCase(ToolConstants.COLUMN_TYPE_BYTE_ARRAY)) {
				return GSType.BYTE_ARRAY;
			} else if (type.equalsIgnoreCase(ToolConstants.COLUMN_TYPE_SHORT_ARRAY)) {
				return GSType.SHORT_ARRAY;
			} else if (type.equalsIgnoreCase(ToolConstants.COLUMN_TYPE_INTEGER_ARRAY)) {
				return GSType.INTEGER_ARRAY;
			} else if (type.equalsIgnoreCase(ToolConstants.COLUMN_TYPE_LONG_ARRAY)) {
				return GSType.LONG_ARRAY;
			} else if (type.equalsIgnoreCase(ToolConstants.COLUMN_TYPE_FLOAT_ARRAY)) {
				return GSType.FLOAT_ARRAY;
			} else if (type.equalsIgnoreCase(ToolConstants.COLUMN_TYPE_DOUBLE_ARRAY)) {
				return GSType.DOUBLE_ARRAY;
			} else if (type.equalsIgnoreCase(ToolConstants.COLUMN_TYPE_TIMESTAMP_ARRAY)) {
				return GSType.TIMESTAMP_ARRAY;
			} else if (type.equalsIgnoreCase(ToolConstants.COLUMN_TYPE_BOOL)) {
				return GSType.BOOL;
			}
			return GSType.valueOf(type.toUpperCase().trim());

		} catch (Exception e) {
			throw new GridStoreCommandException(
					"Error occurded in convert to type" + ": type=[" + type + "] msg=[" + e.getMessage() + "]", e);
		}
	}

	private String convertColumnType(GSType type) throws GridStoreCommandException {
		try {
			if (GSType.STRING_ARRAY.equals(type)) {
				return ToolConstants.COLUMN_TYPE_STRING_ARRAY;
			} else if (GSType.BOOL_ARRAY.equals(type)) {
				return ToolConstants.COLUMN_TYPE_BOOL_ARRAY;
			} else if (GSType.BYTE_ARRAY.equals(type)) {
				return ToolConstants.COLUMN_TYPE_BYTE_ARRAY;
			} else if (GSType.SHORT_ARRAY.equals(type)) {
				return ToolConstants.COLUMN_TYPE_SHORT_ARRAY;
			} else if (GSType.INTEGER_ARRAY.equals(type)) {
				return ToolConstants.COLUMN_TYPE_INTEGER_ARRAY;
			} else if (GSType.LONG_ARRAY.equals(type)) {
				return ToolConstants.COLUMN_TYPE_LONG_ARRAY;
			} else if (GSType.FLOAT_ARRAY.equals(type)) {
				return ToolConstants.COLUMN_TYPE_FLOAT_ARRAY;
			} else if (GSType.DOUBLE_ARRAY.equals(type)) {
				return ToolConstants.COLUMN_TYPE_DOUBLE_ARRAY;
			} else if (GSType.TIMESTAMP_ARRAY.equals(type)) {
				return ToolConstants.COLUMN_TYPE_TIMESTAMP_ARRAY;
			} else if (GSType.BOOL.equals(type)) {
				return ToolConstants.COLUMN_TYPE_BOOL;
			} else {
				return type.toString().toLowerCase();
			}
		} catch (Exception e) {
			throw new GridStoreCommandException(
					"Error occurded in convert to type" + ": type=[" + type + "] msg=[" + e.getMessage() + "]", e);
		}
	}

	/**
	 * Check if a column is precise timestamp
	 *
	 * @param columnInfo the information of column
	 * @return true if the given column is precise timestamp
	 */
	public static boolean isPreciseColumn(ColumnInfo columnInfo) {
		TimeUnit unit = columnInfo.getTimePrecision();
		return  unit == TimeUnit.MICROSECOND || unit == TimeUnit.NANOSECOND;
	}

	public static InputStream skipBOM(InputStream in) throws Exception {
		if (!in.markSupported()) {
			in = new BufferedInputStream(in);
		}
		in.mark(3);
		if (in.available() >= 3) {
			byte b[] = { 0, 0, 0 };
			in.read(b, 0, 3);
			if (b[0] != (byte) 0xEF || b[1] != (byte) 0xBB || b[2] != (byte) 0xBF) {
				in.reset();
			}
		}
		return in;
	}

}
