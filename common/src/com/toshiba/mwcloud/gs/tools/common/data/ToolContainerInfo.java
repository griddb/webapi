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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.toshiba.mwcloud.gs.ColumnInfo;
import com.toshiba.mwcloud.gs.CompressionMethod;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.ContainerType;
import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.IndexInfo;
import com.toshiba.mwcloud.gs.IndexType;
import com.toshiba.mwcloud.gs.TimeSeriesProperties;
import com.toshiba.mwcloud.gs.TimeUnit;
import com.toshiba.mwcloud.gs.TriggerInfo;
import com.toshiba.mwcloud.gs.experimental.ExtendedContainerInfo;
import com.toshiba.mwcloud.gs.tools.common.GridStoreCommandException;
import com.toshiba.mwcloud.gs.tools.common.data.ToolConstants.RowFileType;

public class ToolContainerInfo {

	private ContainerInfo m_conInfo;

	private List<ColumnInfo> m_columnInfoList;

	private List<IndexInfo> m_indexInfoList;

	private List<TriggerInfo> m_triggerInfoList;

	private TimeSeriesProperties m_timeSeriesProperties;

	private int partitionNo = 0;

	private String m_dbName;

	private boolean m_compatibleOption;

	private String m_version;

	private List<TablePartitionProperty> m_tablePartitionProperties;

	private ExpirationInfo m_expirationInfo = null;

	private String m_attribute = "SINGLE";

	private RowFileType containerFileType;

	private List<String> containerFileList;

	private String containerInternalFileName;

	private String fileBaseName;

	private String filterCondition = null;

	private StringBuilder m_msg;

	public ToolContainerInfo() {
		m_indexInfoList = new ArrayList<IndexInfo>();
		m_columnInfoList = new ArrayList<ColumnInfo>();
		m_triggerInfoList = new ArrayList<TriggerInfo>();
		m_conInfo = new ExtendedContainerInfo(null, null, m_columnInfoList, false, null);

		m_tablePartitionProperties = new ArrayList<TablePartitionProperty>();

	}

	public ToolContainerInfo(ExtendedContainerInfo conInfo) {
		updateContainerInfo(conInfo);

		containerFileList = new ArrayList<String>();
	}

	public void copyObject(ToolContainerInfo tcInfo) {

		m_indexInfoList = tcInfo.getIndexInfoList();
		m_columnInfoList = tcInfo.getColumnInfoList();
		m_triggerInfoList = tcInfo.getTriggerInfoList();
		m_timeSeriesProperties = tcInfo.getTimeSeriesProperties();
		m_tablePartitionProperties = tcInfo.getTablePartitionProperties();
		m_expirationInfo = tcInfo.getExpirationInfo();

		partitionNo = tcInfo.getPartitionNo();
		m_dbName = tcInfo.getDbName();
		m_version = tcInfo.getVersion();
		containerFileType = tcInfo.getContainerFileType();
		containerFileList = tcInfo.getContainerFileList();
		containerInternalFileName = tcInfo.getContainerInternalFileName();
		fileBaseName = tcInfo.getFileBaseName();
		filterCondition = tcInfo.getFilterCondition();

		ContainerInfo cInfo = tcInfo.getContainerInfo();
		m_conInfo.setColumnInfoList(tcInfo.getColumnInfoList());
		m_conInfo.setDataAffinity(cInfo.getDataAffinity());
		m_conInfo.setName(cInfo.getName());
		m_conInfo.setRowKeyAssigned(cInfo.isRowKeyAssigned());
		m_conInfo.setTimeSeriesProperties(cInfo.getTimeSeriesProperties());
		m_conInfo.setTriggerInfoList(cInfo.getTriggerInfoList());
		m_conInfo.setType(cInfo.getType());

	}

	public ContainerInfo getContainerInfo() {
		m_conInfo.setColumnInfoList(m_columnInfoList);
		m_conInfo.setTriggerInfoList(m_triggerInfoList);
		m_conInfo.setTimeSeriesProperties(m_timeSeriesProperties);

		return m_conInfo;
	}

	public void setContainerInfo(ContainerInfo contInfo) {
		updateContainerInfo(contInfo);
	}

	private void updateContainerInfo(ContainerInfo contInfo) {

		m_conInfo = contInfo;
		m_indexInfoList = contInfo.getIndexInfoList();
		m_columnInfoList = new ArrayList<ColumnInfo>();

		if (m_conInfo != null) {

			for (int i = 0; i < m_conInfo.getColumnCount(); i++) {
				ColumnInfo columnInfo = m_conInfo.getColumnInfo(i);
				m_columnInfoList.add(columnInfo);
			}

			m_triggerInfoList = m_conInfo.getTriggerInfoList();
			m_timeSeriesProperties = m_conInfo.getTimeSeriesProperties();
		}
	}

	public void setVersion(String version) {
		m_version = version;
	}

	public String getVersion() {
		return m_version;
	}

	public void setName(String name) {
		m_conInfo.setName(name);
	}

	public String getName() {
		return m_conInfo.getName();
	}

	public String getFullName() {
		if ((m_dbName == null) || (m_dbName.length() == 0)) {
			return m_conInfo.getName();
		} else {
			return m_dbName + ToolConstants.DB_DELIMITER + m_conInfo.getName();
		}
	}

	public void setType(ContainerType type) {
		m_conInfo.setType(type);
	}

	public void setType(String type) throws GridStoreCommandException {
		if (type == null || type.length() == 0) {

			throw new GridStoreCommandException(
					"Invalid value '" + ToolConstants.JSON_META_CONTAINER_TYPE + "'. : value=[" + type + "]");
		}
		if (type.equalsIgnoreCase(ContainerType.COLLECTION.toString())) {
			m_conInfo.setType(ContainerType.COLLECTION);

		} else if (type.equalsIgnoreCase(ContainerType.TIME_SERIES.toString())) {
			m_conInfo.setType(ContainerType.TIME_SERIES);

		} else {
			throw new GridStoreCommandException(
					"Invalid value '" + ToolConstants.JSON_META_CONTAINER_TYPE + "'. : value=[" + type + "]");
		}
	}

	public ContainerType getType() {
		return m_conInfo.getType();
	}

	public void setDataAffinity(String dataAffinity) throws GridStoreCommandException {
		if (m_compatibleOption) {
			return;
		}
		if (dataAffinity == null || dataAffinity.length() == 0) {

			return;
		}
		dataAffinity = dataAffinity.trim();
		if (dataAffinity.length() > 8) {
			String msg = "DataAffinity name is over 8 characters" + "'. : dataAffinityName=[" + dataAffinity + "]";
			throw new GridStoreCommandException(msg);
		}
		m_conInfo.setDataAffinity(dataAffinity);
	}

	public String getDataAffinity() {
		if (m_compatibleOption) {
			return null;
		}
		return m_conInfo.getDataAffinity();
	}

	public void setRowKeyAssigned(boolean assigned) {
		m_conInfo.setRowKeyAssigned(assigned);
	}

	public boolean getRowKeyAssigned() {
		return m_conInfo.isRowKeyAssigned();
	}

	public void setPartitionNo(int no) {
		partitionNo = no;
	}

	public int getPartitionNo() {
		return partitionNo;
	}

	public void setDbName(String dbName) {
		m_dbName = dbName;
	}

	public String getDbName() {
		return m_dbName;
	}

	public void addColumnInfo(ColumnInfo columnInfo) {
		m_columnInfoList.add(columnInfo);
	}

	public void addColumnInfo(String columnName, GSType columnType) {
		m_columnInfoList.add(new ColumnInfo(columnName, columnType));
	}

	public void setColumnInfoList(List<ColumnInfo> columnInfoList) {
		m_columnInfoList = columnInfoList;
	}

	public ColumnInfo getColumnInfo(int index) {
		return m_columnInfoList.get(index);
	}

	public List<ColumnInfo> getColumnInfoList() {
		return m_columnInfoList;
	}

	public int getColumnCount() {
		return m_columnInfoList.size();
	}

	public void addTriggerInfo(TriggerInfo triggerInfo) {
		if (m_triggerInfoList == null) {
			m_triggerInfoList = new ArrayList<TriggerInfo>();
		}
		m_triggerInfoList.add(triggerInfo);
	}

	public void setTriggerInfoList(List<TriggerInfo> triggerInfoList) {
		m_triggerInfoList = triggerInfoList;
	}

	public List<TriggerInfo> getTriggerInfoList() {
		return m_triggerInfoList;
	}

	public void addIndexInfo(String columnName, IndexType indexType, String indexName) {
		if (m_indexInfoList == null) {
			m_indexInfoList = new ArrayList<IndexInfo>();
		}
		IndexInfo index = IndexInfo.createByColumn(columnName, indexType);
		if (indexName != null && !indexName.isEmpty()) {
			index.setName(indexName);
		}
		m_indexInfoList.add(index);
	}

	public List<IndexInfo> getIndexInfoList() {
		return m_indexInfoList;
	}

	public void setTimeSeriesProperties(TimeSeriesProperties timeProp) {
		m_timeSeriesProperties = timeProp;
	}

	public TimeSeriesProperties getTimeSeriesProperties() {
		return m_timeSeriesProperties;
	}

	public void setCompressionMethod(CompressionMethod compressionMethod) {
		if (m_timeSeriesProperties == null) {
			m_timeSeriesProperties = new TimeSeriesProperties();
		}
		m_timeSeriesProperties.setCompressionMethod(compressionMethod);
	}

	public CompressionMethod getCompressionMethod() {
		return m_timeSeriesProperties.getCompressionMethod();
	}

	public void setCompressionMethod(String compressionMethodString) throws GridStoreCommandException {
		if (m_timeSeriesProperties == null) {
			m_timeSeriesProperties = new TimeSeriesProperties();
		}
		try {
			m_timeSeriesProperties
					.setCompressionMethod(CompressionMethod.valueOf(compressionMethodString.toUpperCase()));
		} catch (IllegalArgumentException e) {
			throw new GridStoreCommandException(
					"'" + ToolConstants.JSON_META_TIME_COMP + "' is invalid. value=[" + compressionMethodString + "]",
					e);
		}
	}

	public void setExpirationDivisionCount(int count) {
		if (!m_compatibleOption) {
			if (m_timeSeriesProperties == null) {
				m_timeSeriesProperties = new TimeSeriesProperties();
			}
			if (count != -1) {
				m_timeSeriesProperties.setExpirationDivisionCount(count);
			}
		}
	}

	public void setRowExpiration(int elapsedTime, TimeUnit timeUnit) throws GridStoreCommandException {
		if (m_timeSeriesProperties == null) {
			m_timeSeriesProperties = new TimeSeriesProperties();
		}
		if (timeUnit == null) {
			throw new GridStoreCommandException("RowExpirationTimeUnit must not be null.");
		}
		m_timeSeriesProperties.setRowExpiration(elapsedTime, timeUnit);
	}

	public void setCompressionWindowSize(int compressionWindowSize, TimeUnit compressionWindowSizeUnit)
			throws GridStoreCommandException {
		if (m_timeSeriesProperties == null) {
			m_timeSeriesProperties = new TimeSeriesProperties();
		}
		if (compressionWindowSizeUnit == null) {
			throw new GridStoreCommandException("compressionWindowSizeUnit must not be null.");
		}
		m_timeSeriesProperties.setCompressionWindowSize(compressionWindowSize, compressionWindowSizeUnit);
	}

	public void setRelativeHiCompression(String column, double rate, double span) throws GridStoreCommandException {
		if (m_timeSeriesProperties == null) {
			m_timeSeriesProperties = new TimeSeriesProperties();
		}
		if (getCompressionMethod() != CompressionMethod.HI) {
			throw new GridStoreCommandException(
					"CompressionMethod must be 'HI' when ReletiveHiCompression is specified. method=["
							+ getCompressionMethod() + "]");
		}
		if (!(0 <= rate && rate <= 1)) {
			throw new GridStoreCommandException("The value of Rate for compression must be '0 <= and <=1'. name=["
					+ getName() + "] column=[" + column + "]");
		}
		m_timeSeriesProperties.setRelativeHiCompression(column, rate, span);
	}

	public void setAbsoluteHiCompression(String column, double width) throws GridStoreCommandException {
		if (m_timeSeriesProperties == null) {
			m_timeSeriesProperties = new TimeSeriesProperties();
		}
		if (getCompressionMethod() != CompressionMethod.HI) {
			throw new GridStoreCommandException(
					"CompressionMethod must be 'HI' when AbsoluteHiCompression is specified. method=["
							+ getCompressionMethod() + "]");
		}
		m_timeSeriesProperties.setAbsoluteHiCompression(column, width);
	}

	public void setContainerFileType(String fileType) throws GridStoreCommandException {
		if (fileType.toUpperCase().equalsIgnoreCase(RowFileType.CSV.toString())) {
			containerFileType = RowFileType.CSV;

		} else if (fileType.toUpperCase().equalsIgnoreCase(RowFileType.BINARY.toString())) {
			containerFileType = RowFileType.BINARY;

		} else if (fileType.toUpperCase().equalsIgnoreCase(RowFileType.AVRO.toString())) {
			containerFileType = RowFileType.AVRO;

		} else if (fileType.toUpperCase().equalsIgnoreCase(RowFileType.ARCHIVE_CSV.toString())) {
			containerFileType = RowFileType.ARCHIVE_CSV;

		} else {
			String msg = "Invalid value was specified as '" + ToolConstants.JSON_META_CONTAINER_FILE_TYPE
					+ "'. : value=[" + fileType + "]";
			throw new GridStoreCommandException(msg);
		}
	}

	public void setContainerFileType(RowFileType fileType) {
		containerFileType = fileType;
	}

	public void setContainerFile(List<String> list) {
		containerFileList = list;
	}

	public void setContainerInternalFileName(String fileName) {
		containerInternalFileName = fileName;
	}

	public void addContainerFile(String fileName) throws GridStoreCommandException {
		addContainerFile(fileName, null);
	}

	public void addContainerFile(String fileName, String dirPath) throws GridStoreCommandException {
		if (fileName == null || fileName.length() == 0) {
			throw new GridStoreCommandException("'" + ToolConstants.JSON_META_CONTAINER_FILE + "' is required.");
		}
		if (dirPath != null) {
			File dataFile = new File(dirPath, fileName);
			if (!dataFile.exists()) {
				throw new GridStoreCommandException(
						"Data File not found.: dataFile=[" + dataFile.getAbsolutePath() + "]");
			}
		}

		if (containerFileList == null) {
			containerFileList = new ArrayList<String>();
		}
		containerFileList.add(fileName);

	}

	public void setFileBaseName(String arg) {
		fileBaseName = arg;
	}

	public String getFileBaseName() {
		return fileBaseName;
	}

	public void setFilterCondition(String str) {
		filterCondition = str;
	}

	public String getFilterCondition() {
		return filterCondition;
	}

	public RowFileType getContainerFileType() {
		return containerFileType;
	}

	public String getContainerFile() {
		if ((containerFileList != null) && (containerFileList.size() > 0)) {
			return containerFileList.get(0);
		} else {
			return null;
		}
	}

	public List<String> getContainerFileList() {
		return containerFileList;
	}

	public String getContainerInternalFileName() {
		return containerInternalFileName;
	}

	public String getAttribute() {
		return m_attribute;
	}

	public void setAttribute(String attribute) {
		m_attribute = attribute;
	}

	public List<TablePartitionProperty> getTablePartitionProperties() {
		return m_tablePartitionProperties;
	}

	public void setTablePartitionProperties(List<TablePartitionProperty> properties) {
		m_tablePartitionProperties = properties;
	}

	public ExpirationInfo getExpirationInfo() {
		return m_expirationInfo;
	}

	public void setExpirationInfo(ExpirationInfo info) throws GridStoreCommandException {

		if (info != null) {
			if (info.getType() == null) {
				throw new GridStoreCommandException("expirationType must not be null.");
			}
			if (info.getTimeUnit() == null) {
				throw new GridStoreCommandException("expirationTimeUnit must not be null.");
			}
		}
		this.m_expirationInfo = info;
	}

	public boolean isPartitioned() {
		return !m_tablePartitionProperties.isEmpty();
	}

	public boolean hasAdditionalProperty() {

		if (m_timeSeriesProperties != null) {
			return true;
		}

		if (m_expirationInfo != null) {
			return true;
		}

		return false;
	}

	public String buildCreateTableStatement() {
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE TABLE \"");
		builder.append(this.getName());
		builder.append("\" (\"");

		for (int i = 0; i < this.getColumnCount(); i++) {
			ColumnInfo col = this.getColumnInfo(i);
			if (i != 0) {
				builder.append(",\"");
			}
			builder.append(col.getName());
			builder.append("\" ");
			builder.append(col.getType());
			if (i == 0 && this.getRowKeyAssigned()) {
				builder.append(" PRIMARY KEY");
			} else if (col.getNullable() != null && !col.getNullable()) {
				builder.append(" NOT NULL");
			}
		}
		builder.append(") ");

		if (this.getType().equals(ContainerType.TIME_SERIES)) {
			builder.append("USING TIMESERIES ");
		}

		if (this.hasAdditionalProperty()) {
			StringBuilder timePropertyStr = new StringBuilder();
			boolean additional = false;

			if (this.getTimeSeriesProperties() != null) {
				TimeSeriesProperties tsProp = this.getTimeSeriesProperties();
				if (tsProp.getRowExpirationTime() != -1) {
					timePropertyStr.append("expiration_time=");
					timePropertyStr.append(tsProp.getRowExpirationTime());
					additional = true;
				}
				if (tsProp.getRowExpirationTimeUnit() != null) {
					if (additional)
						timePropertyStr.append(",");
					timePropertyStr.append("expiration_time_unit='");
					timePropertyStr.append(tsProp.getRowExpirationTimeUnit());
					timePropertyStr.append("'");
					additional = true;
				}
				if (tsProp.getExpirationDivisionCount() != -1) {
					if (additional)
						timePropertyStr.append(",");
					timePropertyStr.append("expiration_division_count=");
					timePropertyStr.append(tsProp.getExpirationDivisionCount());
					additional = true;
				}
				if (timePropertyStr.length() > 0) {
					builder.append("WITH(");
					builder.append(timePropertyStr);
					builder.append(")");
				}
			} else if (this.getExpirationInfo() != null) {

				ExpirationInfo expInfo = this.getExpirationInfo();
				builder.append(
						String.format("WITH(expiration_type='%s', expiration_time=%d, expiration_time_unit='%s') ",
								expInfo.getType(), expInfo.getTime(), expInfo.getTimeUnit().toString()));
			}
		}

		if (this.isPartitioned()) {
			for (int i = 0; i < this.getTablePartitionProperties().size(); i++) {
				boolean isSub = (i == 1);
				TablePartitionProperty partProp = this.getTablePartitionProperties().get(i);
				if (isSub) {
					builder.append("SUB");
				}
				builder.append("PARTITION BY ");
				if (partProp.getType().equals(ToolConstants.TABLE_PARTITION_TYPE_HASH)) {
					builder.append(partProp.getType());
					builder.append("(\"");
					builder.append(partProp.getColumn());
					builder.append("\") ");
					if (isSub) {
						builder.append("SUB");
					}
					builder.append("PARTITIONS ");
					builder.append(partProp.getDivisionCount());
				} else if (partProp.getType().equals(ToolConstants.TABLE_PARTITION_TYPE_INTERVAL)) {
					builder.append(ToolConstants.TABLE_PARTITION_TYPE_RANGE);
					builder.append("(\"");
					builder.append(partProp.getColumn());
					builder.append("\") ");
					builder.append("EVERY(");
					builder.append(partProp.getIntervalValue());
					if (partProp.getIntervalUnit() != null) {
						builder.append(",");
						builder.append(partProp.getIntervalUnit());
						builder.append("");
					}
					builder.append(")");
				}
				builder.append(" ");
			}
		}
		builder.append(";");
		return builder.toString();
	}

	public Map<IndexInfo, String> buildCreateIndexStatements() {

		Map<IndexInfo, String> sqlMap = null;
		if (this.getIndexInfoList() != null) {
			sqlMap = new HashMap<IndexInfo, String>();

			for (IndexInfo indexInfo : this.getIndexInfoList()) {

				if (indexInfo.getColumnName().equalsIgnoreCase(m_columnInfoList.get(0).getName())
						&& (indexInfo.getName() == null) && (indexInfo.getType() == IndexType.TREE)) {
					continue;
				}

				StringBuilder builder = new StringBuilder();
				builder.append("CREATE INDEX \"");
				builder.append(indexInfo.getName());
				builder.append("\" ON ");
				builder.append(this.getName());
				builder.append(" ( \"");
				builder.append(indexInfo.getColumnName());
				builder.append("\")");
				sqlMap.put(indexInfo, builder.toString());
			}
		}
		return sqlMap;
	}

	public boolean compareContainerInfo(ContainerInfo anotherInfo) {

		boolean checkErrorFlag = false;
		m_msg = new StringBuilder();

		if (m_conInfo.getType() != anotherInfo.getType()) {
			addMessage("ContainerType", m_conInfo.getType(), anotherInfo.getType(), null);
			checkErrorFlag = true;
		}
		if (m_conInfo.isRowKeyAssigned() != anotherInfo.isRowKeyAssigned()) {
			addMessage("rowKeyAssigned", m_conInfo.isRowKeyAssigned(), anotherInfo.isRowKeyAssigned(), null);
			checkErrorFlag = true;
		}
		if (!m_compatibleOption) {
			String d1 = m_conInfo.getDataAffinity();
			String d2 = anotherInfo.getDataAffinity();
			if (((d1 == null) && (d1 != d2)) || ((d1 != null) && (!d1.equals(d2)))) {
				addMessage("dataAffinity", d1, d2, null);
				checkErrorFlag = true;
			}
		}

		if (!compareColumnInfo(anotherInfo)) {
			checkErrorFlag = true;
		}
		if (!compareIndexInfo(anotherInfo)) {
			checkErrorFlag = true;
		}
		if (!compareTriggerInfo(anotherInfo)) {
			checkErrorFlag = true;
		}
		if (!compareTimeSeriesProperties(anotherInfo)) {
			checkErrorFlag = true;
		}

		if (checkErrorFlag) {
			return false;
		} else {
			return true;
		}
	}

	public boolean compareContainerInfo(ToolContainerInfo anotherInfo) {
		if (!compareContainerInfo(anotherInfo.getContainerInfo())) {
			return false;
		}
		if (!compareTablePartitionProperties(anotherInfo.getTablePartitionProperties())) {
			return false;
		}
		if (!compareExpirationInfo(anotherInfo.getExpirationInfo())) {
			return false;
		}
		return true;
	}

	private boolean compareColumnInfo(ContainerInfo anotherInfo) {

		if (m_columnInfoList.size() != anotherInfo.getColumnCount()) {
			addMessage("ColumnCount", m_conInfo.getColumnCount(), anotherInfo.getColumnCount(), null);
			return false;
		}

		int error_count = 0;
		for (int i = 0; i < m_columnInfoList.size(); i++) {
			ColumnInfo localColumn = m_columnInfoList.get(i);
			ColumnInfo gsColumn = anotherInfo.getColumnInfo(i);

			if (!localColumn.getName().equalsIgnoreCase(gsColumn.getName())) {
				addMessage("ColumnName", localColumn.getName(), gsColumn.getName(), null);
				error_count++;
			}
			if (localColumn.getType() != gsColumn.getType()) {
				addMessage("ColumnType", localColumn.getType(), gsColumn.getType(),
						" columnName=[" + localColumn.getName() + "]");
				error_count++;
			}
			Boolean localNullable = localColumn.getNullable();
			if (localNullable == null) {
				if (i == 0 && m_conInfo.isRowKeyAssigned()) {
					localNullable = false;
				} else {
					localNullable = true;
				}
			}

			if (localNullable != gsColumn.getNullable()) {
				addMessage("notNull", !localNullable, !gsColumn.getNullable(),
						" columnName=[" + localColumn.getName() + "]");
				error_count++;
			}

		}
		if (error_count == 0) {
			return true;
		} else {
			return false;
		}
	}

	private boolean compareIndexInfo(ContainerInfo gsContInfo) {

		String rowKeyColumnName = null;
		if (gsContInfo.isRowKeyAssigned() && gsContInfo.getType() == ContainerType.COLLECTION) {
			rowKeyColumnName = m_columnInfoList.get(0).getName();
		}

		List<IndexInfo> virtualIndices = new ArrayList<IndexInfo>();
		boolean rowKeyIndexFound = false;
		for (IndexInfo metaIndex : m_indexInfoList) {
			virtualIndices.add(metaIndex);
			if (rowKeyColumnName != null && rowKeyColumnName.equals(metaIndex.getColumnName())
					&& metaIndex.getType() == IndexType.TREE) {
				rowKeyIndexFound = true;
			}
		}
		if (rowKeyColumnName != null && !rowKeyIndexFound) {
			virtualIndices.add(IndexInfo.createByColumn(rowKeyColumnName, IndexType.TREE));
		}

		if (virtualIndices.size() != gsContInfo.getIndexInfoList().size()) {
			addMessage("IndexCount", virtualIndices.size(), gsContInfo.getIndexInfoList().size(), null);
			return false;
		}

		int foundCount = 0;
		boolean match = false;
		for (IndexInfo metaIndex : virtualIndices) {
			for (IndexInfo gsIndex : gsContInfo.getIndexInfoList()) {
				if (metaIndex.getColumnName().equals(gsIndex.getColumnName())
						&& metaIndex.getType().equals(gsIndex.getType())) {
					if (metaIndex.getName() == null || gsIndex.getName() == null) {
						if (metaIndex.getName() == gsIndex.getName()) {
							foundCount++;
							match = true;
						}
					} else if (metaIndex.getName().equals(gsIndex.getName())) {
						foundCount++;
						match = true;
					}
				}
			}
			if (!match) {
				addMessage("IndexInfo", "column=" + metaIndex.getColumnName() + ",type=" + metaIndex.getType()
						+ ",name=" + metaIndex.getName(), "unmatch", null);
			}
			match = false;
		}

		if (foundCount == virtualIndices.size()) {
			return true;
		} else {
			return false;
		}
	}

	private boolean compareTriggerInfo(ContainerInfo gsContInfo) {

		try {
			List<TriggerInfo> triggerList = m_triggerInfoList;
			List<TriggerInfo> gsTriggerList = gsContInfo.getTriggerInfoList();

			if (triggerList.size() != gsTriggerList.size()) {
				addMessage("Trigger Count", triggerList.size(), gsTriggerList.size(), null);
				return false;
			}

			int error_count = 0;
			for (TriggerInfo localTrigger : triggerList) {
				boolean match = false;

				for (TriggerInfo gsTrigger : gsTriggerList) {

					if (!gsTrigger.getName().equalsIgnoreCase(localTrigger.getName())) {
						continue;
					}
					match = true;

					if (!localTrigger.getTargetColumns().equals(gsTrigger.getTargetColumns())) {
						addMessage("Trigger Columns", localTrigger.getTargetColumns(), gsTrigger.getTargetColumns(),
								" triggerName=[" + localTrigger.getName() + "]");
						error_count++;
					}
					if (!gsTrigger.getTargetEvents().equals(localTrigger.getTargetEvents())) {
						addMessage("Trigger Events", localTrigger.getTargetEvents(), gsTrigger.getTargetEvents(),
								" triggerName=[" + localTrigger.getName() + "]");
						error_count++;
					}
					if (gsTrigger.getType() != localTrigger.getType()) {
						addMessage("Trigger Type", localTrigger.getType(), gsTrigger.getType(),
								" triggerName=[" + localTrigger.getName() + "]");
						error_count++;
					}

					String local = localTrigger.getJMSDestinationName();
					String gs = gsTrigger.getJMSDestinationName();
					if (((local == null) || (local.length() == 0)) && ((gs == null) || (gs.length() == 0))) {
					} else if (((gs == null) && (gs != local)) || ((gs != null) && !gs.equalsIgnoreCase(local))) {
						addMessage("Trigger JmsDestinationName", local, gs,
								" triggerName=[" + localTrigger.getName() + "]");
						error_count++;
					}

					local = localTrigger.getJMSDestinationType();
					gs = gsTrigger.getJMSDestinationType();
					if (((local == null) || (local.length() == 0)) && ((gs == null) || (gs.length() == 0))) {
					} else if (((gs == null) && (gs != local)) || ((gs != null) && !gs.equalsIgnoreCase(local))) {
						addMessage("Trigger JmsDestinationType", localTrigger.getJMSDestinationType(),
								gsTrigger.getJMSDestinationType(), " triggerName=[" + localTrigger.getName() + "]");
						error_count++;
					}

					local = localTrigger.getUser();
					gs = gsTrigger.getUser();
					if (((local == null) || (local.length() == 0)) && ((gs == null) || (gs.length() == 0))) {
					} else if (((gs == null) && (gs != local)) || ((gs != null) && !gs.equalsIgnoreCase(local))) {
						addMessage("Trigger User", localTrigger.getUser(), gsTrigger.getUser(),
								" triggerName=[" + localTrigger.getName() + "]");
						error_count++;
					}

					local = localTrigger.getPassword();
					gs = gsTrigger.getPassword();
					if (((local == null) || (local.length() == 0)) && ((gs == null) || (gs.length() == 0))) {
					} else if (((gs == null) && (gs != local)) || ((gs != null) && !gs.equalsIgnoreCase(local))) {
						addMessage("Trigger Password", "***", "***", " triggerName=[" + localTrigger.getName() + "]");
						error_count++;
					}

					URI localURI = localTrigger.getURI();
					URI gsURI = gsTrigger.getURI();
					if (((gsURI == null) && (gsURI != localURI)) || ((gsURI != null) && !gsURI.equals(localURI))) {
						addMessage("Trigger URI", localTrigger.getURI(), gsTrigger.getURI(),
								" triggerName=[" + localTrigger.getName() + "]\n");
						error_count++;
					}
					break;
				}
				if (!match) {
					m_msg.append("Trigger Name \"" + localTrigger.getName() + "\" does not exist on another.\n");
					error_count++;
				}
			}

			if (error_count == 0) {
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			m_msg.append("compareColumnInfo():Columninfo Compare [false]");
			return false;
		}
	}

	private boolean compareTimeSeriesProperties(ContainerInfo gsContInfo) {

		try {
			int error_count = 0;

			TimeSeriesProperties localTimesereis = m_timeSeriesProperties;
			TimeSeriesProperties gsTimesereis = gsContInfo.getTimeSeriesProperties();

			if ((localTimesereis == null) && (gsTimesereis == null)) {
				return true;
			} else if (localTimesereis == null) {

				localTimesereis = new TimeSeriesProperties();
			} else if (gsTimesereis == null) {
				gsTimesereis = new TimeSeriesProperties();
			}

			if (!gsTimesereis.getCompressionMethod().equals(localTimesereis.getCompressionMethod())) {
				addMessage("CompressionMethod", localTimesereis.getCompressionMethod(),
						gsTimesereis.getCompressionMethod(), null);
				error_count++;
			}

			if (gsTimesereis.getCompressionWindowSize() != localTimesereis.getCompressionWindowSize()) {
				addMessage("CompressionWindowSize", localTimesereis.getCompressionWindowSize(),
						gsTimesereis.getCompressionWindowSize(), null);
				error_count++;
			}

			if (gsTimesereis.getCompressionWindowSizeUnit() != localTimesereis.getCompressionWindowSizeUnit()) {
				addMessage("CompressionWindowSizeUnit", localTimesereis.getCompressionWindowSizeUnit(),
						gsTimesereis.getCompressionWindowSizeUnit(), null);
				error_count++;
			}

			if (!m_compatibleOption) {
				if (gsTimesereis.getExpirationDivisionCount() != localTimesereis.getExpirationDivisionCount()) {
					if ((gsTimesereis.getExpirationDivisionCount() == 8)
							&& (localTimesereis.getExpirationDivisionCount() == -1)) {

					} else {
						addMessage("ExpirationDivisionCount", localTimesereis.getExpirationDivisionCount(),
								gsTimesereis.getExpirationDivisionCount(), null);
						error_count++;
					}
				}
			}

			if (gsTimesereis.getRowExpirationTime() != localTimesereis.getRowExpirationTime()) {
				addMessage("RowExpirationTime", localTimesereis.getRowExpirationTime(),
						gsTimesereis.getRowExpirationTime(), null);
				error_count++;
			}

			if (gsTimesereis.getRowExpirationTimeUnit() != localTimesereis.getRowExpirationTimeUnit()) {
				addMessage("RowExpirationTimeUnit", localTimesereis.getRowExpirationTimeUnit(),
						gsTimesereis.getRowExpirationTimeUnit(), null);
				error_count++;
			}

			Set<String> localColums = localTimesereis.getSpecifiedColumns();
			Set<String> gsColums = gsTimesereis.getSpecifiedColumns();

			if (gsColums.size() != localColums.size()) {
				addMessage("SpecifiedColumns(CompressionColumns) Count", localColums.size(), gsColums.size(), null);
				error_count++;
			}
			if (!gsColums.equals(localColums)) {
				addMessage("SpecifiedColumns(CompressionColumns)", localColums, gsColums, null);
				error_count++;
			}

			for (String column : gsColums) {

				if (!gsTimesereis.getCompressionRate(column).equals(localTimesereis.getCompressionRate(column))) {
					addMessage("Compression Rate", localTimesereis.getCompressionRate(column),
							gsTimesereis.getCompressionRate(column), " column=[" + column + "]");
					error_count++;
				}

				if (!gsTimesereis.getCompressionSpan(column).equals(localTimesereis.getCompressionSpan(column))) {
					addMessage("Compression Span", localTimesereis.getCompressionSpan(column),
							gsTimesereis.getCompressionSpan(column), " column=[" + column + "]");
					error_count++;
				}

				if (!gsTimesereis.getCompressionWidth(column).equals(localTimesereis.getCompressionWidth(column))) {
					addMessage("Compression Width", localTimesereis.getCompressionWidth(column),
							gsTimesereis.getCompressionWidth(column), " column=[" + column + "]");
					error_count++;
				}
			}

			if (error_count == 0) {
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {

			m_msg.append("compareTimeSeriesProperties():TimeSeriesProperties Compare [false]");
			return false;
		}
	}

	private boolean compareTablePartitionProperties(List<TablePartitionProperty> gsProperties) {
		if ((m_tablePartitionProperties == null) && (gsProperties == null)) {
			return true;
		}
		if ((m_tablePartitionProperties == null) && (gsProperties != null)
				|| (m_tablePartitionProperties != null) && (gsProperties == null)) {
			addMessage("TablePartitionInfo", (m_tablePartitionProperties == null) ? "Not Partitioned" : "Partitioned",
					(gsProperties == null) ? "Not Partitioned" : "Partitioned", null);
			return false;
		}
		if (m_tablePartitionProperties.size() != gsProperties.size()) {
			String t1 = m_tablePartitionProperties.get(0).getType()
					+ (m_tablePartitionProperties.size() == 2 ? "-" + m_tablePartitionProperties.get(1).getType() : "");
			String t2 = gsProperties.get(0).getType()
					+ (gsProperties.size() == 2 ? "-" + gsProperties.get(1).getType() : "");
			addMessage("TablePartitionInfo", t1, t2, null);
			return false;
		}

		int i = 0;
		for (TablePartitionProperty prop : m_tablePartitionProperties) {
			if (!compareTablePartitionProperty(prop, gsProperties.get(i++))) {
				return false;
			}
		}
		return true;
	}

	private boolean compareTablePartitionProperty(TablePartitionProperty localProp, TablePartitionProperty gsProp) {

		if (!localProp.getType().equals(gsProp.getType())) {
			addMessage("TablePartitionInfo type", localProp.getType(), gsProp.getType(), null);
			return false;
		}

		if (!localProp.getColumn().equals(gsProp.getColumn())) {
			addMessage("TablePartitionInfo column", localProp.getColumn(), gsProp.getColumn(), null);
			return false;
		}

		if (localProp.getType().equals(ToolConstants.TABLE_PARTITION_TYPE_HASH)) {

			if (localProp.getDivisionCount() != gsProp.getDivisionCount()) {
				addMessage("TablePartitionInfo divisionCount", localProp.getDivisionCount(), gsProp.getDivisionCount(),
						null);
				return false;
			} else {
				return true;
			}

		} else if (localProp.getType().equals(ToolConstants.TABLE_PARTITION_TYPE_INTERVAL)) {

			if (!localProp.getIntervalValue().equals(gsProp.getIntervalValue())) {
				addMessage("TablePartitionInfo intervalValue", localProp.getIntervalValue(), gsProp.getIntervalValue(),
						null);
				return false;

			} else if (((localProp.getIntervalUnit() == null) && (gsProp.getIntervalUnit() != null))
					|| ((localProp.getIntervalUnit() != null)
							&& !localProp.getIntervalUnit().equalsIgnoreCase(gsProp.getIntervalUnit()))) {
				addMessage("TablePartitionInfo intervalUnit", localProp.getIntervalUnit(), gsProp.getIntervalUnit(),
						null);
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	private boolean compareExpirationInfo(ExpirationInfo gsInfo) {
		ExpirationInfo localInfo = m_expirationInfo;

		if (localInfo == null && gsInfo == null) {
			return true;
		} else if (localInfo == null && gsInfo != null) {
			addMessage("Expiration", "disable", "enable", null);
			return false;
		} else if (localInfo != null && gsInfo == null) {
			addMessage("Expiration", "enable", "disable", null);
		}

		if (!localInfo.getType().equals(gsInfo.getType())) {
			addMessage("expirationType", localInfo.getType(), gsInfo.getType(), null);
			return false;
		} else if (localInfo.getTime() != gsInfo.getTime()) {
			addMessage("expirationTime", localInfo.getTime(), gsInfo.getTime(), null);
			return false;
		} else if (!localInfo.getTimeUnit().equals(gsInfo.getTimeUnit())) {
			addMessage("expirationTimeUnit", localInfo.getTimeUnit(), gsInfo.getTimeUnit(), null);
			return false;
		} else {
			return true;
		}
	}

	public String getMessage() {
		return m_msg.toString();
	}

	private void addMessage(String itemName, Object self, Object another, String additional) {
		m_msg.append("[Unmatch]");
		m_msg.append(itemName);
		m_msg.append(" : self=[");
		if (self == null) {
			m_msg.append("null");
		} else {
			m_msg.append(self.toString());
		}
		m_msg.append("] another=[");
		if (another == null) {
			m_msg.append("null");
		} else {
			m_msg.append(another.toString());
		}
		m_msg.append("]");
		if (additional != null) {
			m_msg.append(additional);
		}
		m_msg.append("\n");
	}

	public void checkContainerInfo() throws GridStoreCommandException {
		try {
			String conName = getName();
			ContainerType conType = getType();

			if ((conName == null) || (conName.length() == 0)) {
				throw new GridStoreCommandException("ContainerName is required.");
			}
			if (conType == null) {
				throw new GridStoreCommandException("ContainerType is required. name=[" + conName + "]");
			}

			if (getColumnCount() == 0) {

				throw new GridStoreCommandException("ColumnInfo is required. name=[" + conName + "]");
			}
			List<String> columnNameList = new ArrayList<String>();
			for (int i = 0; i < getColumnCount(); i++) {
				ColumnInfo columnInfo = getColumnInfo(i);

				String columnName = columnInfo.getName();
				if ((columnName == null) || (columnName.length() == 0)) {
					throw new GridStoreCommandException("ColumnName is required. name=[" + conName + "]");
				}

				GSType columnType = columnInfo.getType();
				if (columnType == null) {
					throw new GridStoreCommandException(
							"ColumnType is required. name=[" + conName + "] columnName=[" + columnName + "]");
				}

				if ((i == 0) && getRowKeyAssigned()) {
					switch (conType) {
					case COLLECTION:
						switch (columnType) {
						case INTEGER:
						case STRING:
						case LONG:
						case TIMESTAMP:
							break;
						default:
							throw new GridStoreCommandException("ColumnType of the rowkey is invalid in type \'"
									+ ContainerType.COLLECTION + "'. name=[" + conName + "] columnName=[" + columnName
									+ "] columnType=[" + columnType + "]");
						}
						break;
					case TIME_SERIES:
						if (columnType == GSType.TIMESTAMP) {
						} else {
							throw new GridStoreCommandException("ColumnType of the rowkey is invalid in type \'"
									+ ContainerType.TIME_SERIES + "\'. name=[" + conName + "] columnName=[" + columnName
									+ "] columnType=[" + columnType + "]");
						}
						break;
					}

					if ((columnInfo.getNullable() != null) && (columnInfo.getNullable() == true)) {
						throw new GridStoreCommandException("Row key cannot be null. name=[" + conName
								+ "] columnName=[" + columnName + "] columnType=[" + columnType + "]");
					}
				}

				columnNameList.add(columnName);
			}

			switch (conType) {
			case COLLECTION:
				if (m_timeSeriesProperties != null) {
					throw new GridStoreCommandException("TimeSeriesProperties is not required in type \'"
							+ ContainerType.COLLECTION + "'. name=[" + getName() + "]");
				}
				break;

			case TIME_SERIES:
				if (!getRowKeyAssigned()) {
					throw new GridStoreCommandException("RowKeyAssined is required in type \'"
							+ ContainerType.TIME_SERIES + "\'. name=[" + getName() + "]");
				}

				checkTimeSeriesProperties();

				break;
			}

			checkIndexInfo();

			for (TriggerInfo trigger : m_triggerInfoList) {
				if ((trigger.getName() == null) || (trigger.getName().length() == 0)) {
					throw new GridStoreCommandException("TriggerName is required. name=[" + getName() + "]");
				}
				if (trigger.getType() == null) {
					throw new GridStoreCommandException("TriggerType is required. name=[" + getName()
							+ "] triggerName=[" + trigger.getName() + "]");
				}
				if (trigger.getURI() == null) {
					throw new GridStoreCommandException(
							"URI is required. name=[" + getName() + "] triggerName=[" + trigger.getName() + "]");
				}
				if ((trigger.getTargetEvents() == null) || (trigger.getTargetEvents().size() == 0)) {
					throw new GridStoreCommandException("TargetEvents is required. name=[" + getName()
							+ "] triggerName=[" + trigger.getName() + "]");
				}
				for (String name : trigger.getTargetColumns()) {
					if (!columnNameList.contains(name)) {

						throw new GridStoreCommandException(
								"The columnName of triggerInfo does not exist in ColumnInfoList. name=[" + getName()
										+ "] triggerName=[" + trigger.getName() + "]");
					}
				}
			}

			if (isPartitioned()) {
				try {
					Class.forName("com.toshiba.mwcloud.gs.sql.Driver");
				} catch (Exception e) {
					throw new GridStoreCommandException(
							"The table partitioning function is not supported in Standard Edition. name=[" + getName()
									+ "]");
				}

				if (this.getDataAffinity() != null) {
					throw new GridStoreCommandException(
							"DataAffinity cannot be specified in a partitioned table. name=[" + getName() + "]");
				}

				if (!m_triggerInfoList.isEmpty()) {
					throw new GridStoreCommandException(
							"Trigger cannot be specified in a partitioned table. name=[" + getName() + "]");
				}

				for (int i = 0; i < getColumnCount(); i++) {
					ColumnInfo columnInfo = getColumnInfo(i);
					if (columnInfo.getType() == GSType.GEOMETRY) {
						throw new GridStoreCommandException(
								"Geometry type column cannot be specified in a partitioned table. name=[" + getName()
										+ "] columnName=" + columnInfo.getName() + "]");
					}
				}

				for (IndexInfo index : m_indexInfoList) {
					if ((index.getName() == null) || index.getName().isEmpty()) {
						if (!getRowKeyAssigned() || !index.getColumnName().equals(getColumnInfo(0).getName())) {
							throw new GridStoreCommandException(
									"The index name is required in a partitioned table. name=[" + getName()
											+ "] columnName=[" + index.getColumnName() + "] indexType=["
											+ index.getType() + "]");
						}
					}

					if (index.getType() != IndexType.TREE) {
						throw new GridStoreCommandException(
								"The type of indexes must be TREE in a partitioned table.: columnName=["
										+ index.getColumnName() + "] indexType=[" + index.getType() + "]");
					}
				}
			}

			ExpirationInfo expInfo = getExpirationInfo();
			if (expInfo != null) {
				try {
					Class.forName("com.toshiba.mwcloud.gs.sql.Driver");
				} catch (Exception e) {
					throw new GridStoreCommandException(
							"The partition expiration function is not supported in Standard Edition. name=[" + getName()
									+ "]");
				}

				String expType = expInfo.getType();

				if (expType.equalsIgnoreCase(ToolConstants.EXPIRATION_TYPE_PARTITION)) {
					if (m_timeSeriesProperties != null && m_timeSeriesProperties.getRowExpirationTime() != -1) {
						throw new GridStoreCommandException(
								"The partition expiration function can not be used with row expiration. name=["
										+ getName() + "]");
					}
				} else {
					throw new GridStoreCommandException("The expiration type is invalid. name=[" + getName()
							+ "] expirationType=[" + expType + "]");
				}
			}
		} catch (GridStoreCommandException e) {
			throw e;

		} catch (Exception e) {
			throw new GridStoreCommandException(
					"Error occurs in Check ContainerInfo." + ": name=[" + getName() + "] msg=[" + e.getMessage() + "]",
					e);
		}
	}

	private void checkTimeSeriesProperties() throws GridStoreCommandException {
		if (m_timeSeriesProperties == null) {
			return;
		}

		CompressionMethod cmType = m_timeSeriesProperties.getCompressionMethod();
		Set<String> specifiedColumnList = m_timeSeriesProperties.getSpecifiedColumns();
		List<String> columnNameList = new ArrayList<String>();

		if (this.isPartitioned() && (cmType != CompressionMethod.NO)) {
			throw new GridStoreCommandException(
					"CompressionMethod must be 'NO' in a partitioned table. name=[" + getName() + "]");
		}

		for (int i = 0; i < getColumnCount(); i++) {
			ColumnInfo columnInfo = getColumnInfo(i);
			String columnName = columnInfo.getName();
			columnNameList.add(columnName);

			if (!specifiedColumnList.contains(columnName)) {
				continue;
			}

			switch (cmType) {
			case HI:
				GSType type = columnInfo.getType();

				switch (type) {
				case BYTE:
				case SHORT:
				case INTEGER:
				case LONG:
				case FLOAT:
				case DOUBLE:
					break;
				default:
					throw new GridStoreCommandException(
							"The columnType cannot be specified for a compressed column. name=[" + getName()
									+ "] columnName=[" + columnName + "] columnType=[" + type + "]");
				}

				if (m_timeSeriesProperties.isCompressionRelative(columnName)) {
					if (m_timeSeriesProperties.getCompressionWidth(columnName) != null) {
						throw new GridStoreCommandException(
								"\"with\" cannot be specified as \"compressionType\":\"RELATIVE\". name=[" + getName()
										+ "] columnName=[" + columnName + "]");
					}
				} else {
					if (m_timeSeriesProperties.getCompressionRate(columnName) != null
							|| m_timeSeriesProperties.getCompressionSpan(columnName) != null) {
						throw new GridStoreCommandException(
								"\"rate\" and \"span\" cannot be specified as \"compressionType\":\"ABSOLUTE\". name=["
										+ getName() + "] columnName=[" + columnName + "]");
					}
				}

				if (m_timeSeriesProperties.getCompressionWidth(columnName) != null
						&& (m_timeSeriesProperties.getCompressionRate(columnName) != null
								|| m_timeSeriesProperties.getCompressionSpan(columnName) != null)) {
					throw new GridStoreCommandException(
							"\"with\" and \"rate/span\" cannot be specified at the same time. name=[" + getName()
									+ "] columnName=[" + columnName + "]");
				}
				break;

			case NO:
			case SS:

				throw new GridStoreCommandException("\"rate\" ,\"span\" and \"width\" cannot be specified "
						+ "as \"compressionMethod\":\"NO|SS\". name=[" + getName() + "] columnName=[" + columnName
						+ "]");
			}
		}

		for (String column2 : m_timeSeriesProperties.getSpecifiedColumns()) {
			boolean match = false;
			for (String column : columnNameList) {
				if (column2.equalsIgnoreCase(column)) {
					match = true;
					break;
				}
			}
			if (!match) {
				throw new GridStoreCommandException(
						"The name of compressed column does not exist in ColumnInfoList. name=[" + getName()
								+ "] compressedColumn=" + m_timeSeriesProperties.getSpecifiedColumns()
								+ " columnInfoList=" + columnNameList.toString());
			}
		}

	}

	private void checkIndexInfo() throws GridStoreCommandException {

		for (IndexInfo index : m_indexInfoList) {
			String currentColumnName = index.getColumnName();
			IndexType indexType = index.getType();

			if ((currentColumnName == null) || (currentColumnName.length() == 0)) {
				throw new GridStoreCommandException("ColumnName of index is required. name=[" + getName() + "]");
			}
			if (indexType == null) {
				throw new GridStoreCommandException(
						"IndexType is required. : name=[" + getName() + "] columnName=[" + currentColumnName + "]");
			}

			if (getType() == ContainerType.TIME_SERIES) {
				if ((indexType != null) && indexType != IndexType.TREE) {
					throw new GridStoreCommandException("IndexType is invalid in type 'TimeSeries'. : name=["
							+ getName() + "] columnName=[" + currentColumnName + "] indexType=[" + indexType + "]");
				}
			}

			boolean match = false;
			for (int i = 0; i < getColumnCount(); i++) {
				ColumnInfo columnInfo = getColumnInfo(i);

				if (currentColumnName.equalsIgnoreCase(columnInfo.getName())) {
					GSType dataType = columnInfo.getType();

					if ((i == 0) && (getType() == ContainerType.TIME_SERIES)) {
						throw new GridStoreCommandException("The first column of TimeSeries cannot index. : name=["
								+ getName() + "] columnName=[" + currentColumnName + "] indexType=[" + indexType + "]");
					}

					switch (indexType) {
					case HASH:
						switch (dataType) {
						case BOOL:
						case STRING:
						case BYTE:
						case SHORT:
						case INTEGER:
						case LONG:
						case FLOAT:
						case DOUBLE:
						case TIMESTAMP:

							break;
						default:
							throw new GridStoreCommandException(
									"The combination of ColumnType and IndexType is invalid. name=[" + getName()
											+ "] columnName=[" + currentColumnName + "] columnType=[" + dataType
											+ "] indexType=[" + indexType + "]");
						}
						break;

					case SPATIAL:
						if (dataType != GSType.GEOMETRY) {
							throw new GridStoreCommandException(
									"The combination of ColumnType and IndexType is invalid. name=[" + getName()
											+ "] columnName=[" + currentColumnName + "] columnType=[" + dataType
											+ "] indexType=[" + indexType + "]");
						}
						break;

					case TREE:
						switch (dataType) {
						case BOOL:
						case STRING:
						case BYTE:
						case SHORT:
						case INTEGER:
						case LONG:
						case FLOAT:
						case DOUBLE:
						case TIMESTAMP:

							break;
						default:
							throw new GridStoreCommandException(
									"The combination of ColumnType and IndexType is invalid. name=[" + getName()
											+ "] columnName=[" + currentColumnName + "] columnType=[" + dataType
											+ "] indexType=[" + indexType + "]");
						}
						break;
					}
					match = true;
					break;
				}
			}

			if (!match) {
				throw new GridStoreCommandException(
						"The columnName of IndexList does not exist in ColumnInfoList. : name=[" + getName()
								+ "] columnName=[" + currentColumnName + "]");
			}

		}
	}

	public boolean checkExpImpSetting() {

		for (String fileName : containerFileList) {
			new File(fileName);
		}

		return true;
	}

}
