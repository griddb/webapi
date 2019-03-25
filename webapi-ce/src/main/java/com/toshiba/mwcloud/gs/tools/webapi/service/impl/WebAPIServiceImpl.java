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

import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.toshiba.mwcloud.gs.AggregationResult;
import com.toshiba.mwcloud.gs.ColumnInfo;
import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.ContainerType;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.Geometry;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.IndexType;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.RowSet;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWContainerInfo;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWContainerListOuput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWPutRowOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWQueryOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWQueryParams;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWSortCondition;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWTQLColumnInfo;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWTQLInput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWTQLOutput;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWTQLOutputAggregation;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWBadRequestException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWNotFoundException;
import com.toshiba.mwcloud.gs.tools.webapi.exception.GWResourceConflictedException;
import com.toshiba.mwcloud.gs.tools.webapi.service.WebAPIService;
import com.toshiba.mwcloud.gs.tools.webapi.utils.ConnectionThread;
import com.toshiba.mwcloud.gs.tools.webapi.utils.Constants;
import com.toshiba.mwcloud.gs.tools.webapi.utils.ConversionUtils;
import com.toshiba.mwcloud.gs.tools.webapi.utils.ConversionUtils.TQLStatementType;
import com.toshiba.mwcloud.gs.tools.webapi.utils.DateFormatUtils;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GWSettingInfo;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GWUser;
import com.toshiba.mwcloud.gs.tools.webapi.utils.GridStoreUtils;
import com.toshiba.mwcloud.gs.tools.webapi.utils.Validation;

import ch.qos.logback.classic.Logger;

@Service
public class WebAPIServiceImpl implements WebAPIService {

	private static final Logger logger = (Logger) LoggerFactory.getLogger(WebAPIServiceImpl.class);

	@Override
	public void testConnection(String authorization, String cluster, String database, Long timeout) throws GSException {

		if (GWSettingInfo.getLogger().isInfoEnabled()) {
			logger.info("testConnection : cluster=" + cluster + " database=" + database + " timeout=" + timeout);
		}
		long start = System.nanoTime();

		GWUser user = GWUser.getUserfromAuthorization(authorization);
		GridStore gridStore = null;
		try {
			gridStore = GridStoreUtils.getGridStore(cluster, database, user.getUsername(), user.getPassword());
			if (timeout != null) {
				if (timeout <= 0) {
					throw new GWBadRequestException("Timeout must be greater than 0");
				}
				if (timeout > Constants.MAX_TIME_OUT) {
					throw new GWBadRequestException("Cannot wait that long");
				}
				ConnectionThread connectionThread = new ConnectionThread(gridStore);
				Thread thread = new Thread(connectionThread);
				thread.setDaemon(true);
				thread.start();
				try {
					Thread.sleep(timeout);
				} catch (InterruptedException e) {
					throw new GWException(e.getMessage());
				}
				if (thread.isAlive()) {
					throw new GWException("Exceeded timeout");
				} else {
					Object result = connectionThread.getResult();
					if (result instanceof GSException) {
						GSException gsException = (GSException) result;
						throw gsException;
					}
				}
			} else {
				gridStore.getPartitionController().getPartitionCount();
			}
		} catch (GSException gsException) {
			throw gsException;
		} finally {
			if (gridStore != null) {
				try {
					gridStore.close();
				} catch (GSException exception) {
					exception.printStackTrace();
				}
			}

			long end = System.nanoTime();
			if (GWSettingInfo.getLogger().isDebugEnabled()) {
				logger.debug("testConnection : time=" + (end - start) / 1000000f);
			}
		}

	}

	@Override
	public GWContainerListOuput getListContainers(String authorization, String cluster, String database,
			ContainerType type, int limit, Integer offset, GWSortCondition sort) throws GSException {

		if (GWSettingInfo.getLogger().isInfoEnabled()) {
			logger.info("getListContainers : cluster=" + cluster + " database=" + database + " containertype=" + type
					+ " limit=" + limit + " offset=" + offset + " sort=" + sort);
		}
		long start = System.nanoTime();

		GWUser user = GWUser.getUserfromAuthorization(authorization);
		GridStore gridStore = null;

		try {
			gridStore = GridStoreUtils.getGridStore(cluster, database, user.getUsername(), user.getPassword());
			GWContainerListOuput containerListOutput = new GWContainerListOuput();
			if (offset == null) {
				offset = 0;
			}
			if (offset < 0) {
				throw new GWBadRequestException("'offset' is invalid");
			}
			if (limit < 0) {
				throw new GWBadRequestException("'limit' is invalid");
			}

			int partitionCount = gridStore.getPartitionController().getPartitionCount();
			List<String> listContainer = new ArrayList<>();

			for (int i = 0; i < partitionCount; i++) {
				List<String> temp = new ArrayList<String>();
				temp = gridStore.getPartitionController().getContainerNames(i, 0L, null);
				if (null == type) {
					for (int j = 0; j < temp.size(); j++) {
						listContainer.add(temp.get(j));
					}
				} else {
					for (int j = 0; j < temp.size(); j++) {
						if (gridStore.getContainerInfo(temp.get(j)).getType() == type) {
							listContainer.add(temp.get(j));
						}
					}
				}
			}
			int total = listContainer.size();
			containerListOutput.setTotal(total);
			containerListOutput.setLimit(Math.min(limit, GWSettingInfo.getMaxLimit()));
			containerListOutput.setOffset(offset);
			Collections.sort(listContainer);
			if (sort == GWSortCondition.DESC) {
				Collections.reverse(listContainer);
			}

			List<String> limitedListContainer = null;
			if (offset > 0) {
				if (offset > listContainer.size()) {
					limitedListContainer = new ArrayList<String>();
				} else {
					limitedListContainer = listContainer.subList(offset,
							Math.min(listContainer.size(), offset + limit));

				}
			} else {
				limitedListContainer = listContainer.subList(0, Math.min(limit, listContainer.size()));
			}
			long resSize = countResponseSize(limitedListContainer);

			if (resSize > GWSettingInfo.getMaxGetRowSize()) {
				throw new GWBadRequestException("Response size is too large. Please try to use 'limit' option.");
			}
			containerListOutput.setNames(limitedListContainer);

			return containerListOutput;
		} catch (GSException gsException) {
			throw gsException;
		} finally {
			if (gridStore != null) {
				try {
					gridStore.close();
				} catch (GSException exception) {
					exception.printStackTrace();
				}
			}

			long end = System.nanoTime();
			if (GWSettingInfo.getLogger().isDebugEnabled()) {
				logger.debug("getListContainers : time=" + (end - start) / 1000000f);
			}
		}
	}

	private long countResponseSize(List<String> limitedListContainer) throws GSException {
		long coutSize = 0;

		for (String containerNam : limitedListContainer) {
			try {
				coutSize += containerNam.getBytes(Constants.ENCODING).length;
			} catch (UnsupportedEncodingException e) {
				throw new GSException("Not support character in container name");
			}
		}
		return coutSize;
	}

	@Override
	public GWContainerInfo getContainerInfo(String authorization, String cluster, String database, String container)
			throws GSException {

		if (GWSettingInfo.getLogger().isInfoEnabled()) {
			logger.info("getContainerInfo : cluster=" + cluster + " database=" + database + " container=" + container);
		}
		long start = System.nanoTime();

		GWUser user = GWUser.getUserfromAuthorization(authorization);
		GridStore gridStore = null;
		try {
			gridStore = GridStoreUtils.getGridStore(cluster, database, user.getUsername(), user.getPassword());
			ContainerInfo containerInfo = gridStore.getContainerInfo(container);
			if (null == containerInfo) {
				throw new GWNotFoundException("Container not existed");
			}
			GWContainerInfo gwContainerInfo = ConversionUtils.convertToGWContainerInfo(containerInfo);
			gridStore.close();
			return gwContainerInfo;
		} catch (GSException gsException) {
			throw gsException;
		} finally {
			if (gridStore != null) {
				try {
					gridStore.close();
				} catch (GSException exception) {
					exception.printStackTrace();
				}
			}

			long end = System.nanoTime();
			if (GWSettingInfo.getLogger().isDebugEnabled()) {
				logger.debug("getContainerInfo : time=" + (end - start) / 1000000f);
			}
		}
	}

	@Override
	public List<Object> executeTQLs(String authorization, String cluster, String database, List<GWTQLInput> listTQLs)
			throws GSException, GWException, UnsupportedEncodingException, SQLException {

		if (GWSettingInfo.getLogger().isInfoEnabled()) {
			logger.info("executeTQLs : cluster=" + cluster + " database=" + database);
		}
		long start = System.nanoTime();

		GWUser user = GWUser.getUserfromAuthorization(authorization);
		if (listTQLs == null || listTQLs.size() == 0) {
			throw new GWBadRequestException("List of TQL is empty");
		}
		if (listTQLs.size() > GWSettingInfo.getMaxQueryNum()) {
			throw new GWBadRequestException("Exceed maximum of TQLs that can be executed");
		}

		// Validate list TQLs
		for (int i = 0; i < listTQLs.size(); i++) {
			Validation.validateGWTQLInput(listTQLs.get(i));
		}

		GridStore gridStore = null;
		try {
			gridStore = GridStoreUtils.getGridStore(cluster, database, user.getUsername(), user.getPassword());
			// Execute TQL
			List<Object> result = new ArrayList<>();
			for (int i = 0; i < listTQLs.size(); i++) {
				String container = listTQLs.get(i).getName();
				String statement = listTQLs.get(i).getStmt();
				ArrayList<String> selectedFields = listTQLs.get(i).getColumns();
				Object gwTQLOutput = executeTQL(gridStore, container, statement, selectedFields);
				result.add(gwTQLOutput);
			}
			return result;
		} catch (GSException gsException) {
			throw gsException;
		} finally {
			if (gridStore != null) {
				try {
					gridStore.close();
				} catch (GSException exception) {
					exception.printStackTrace();
				}
			}

			long end = System.nanoTime();
			if (GWSettingInfo.getLogger().isDebugEnabled()) {
				logger.debug("executeTQLs : time=" + (end - start) / 1000000f);
			}
		}
	}

	private Object executeTQL(GridStore gridStore, String container, String statement, ArrayList<String> selectedFields)
			throws GSException, GWException, UnsupportedEncodingException, SQLException {
		GWTQLOutput result = new GWTQLOutput();
		ContainerInfo containerInfo = gridStore.getContainerInfo(container);
		if (null == containerInfo) {
			throw new GWNotFoundException("Container not existed");
		}

		ArrayList<Integer> selectedColumns = new ArrayList<Integer>();
		ArrayList<GWTQLColumnInfo> columns = new ArrayList<GWTQLColumnInfo>();
		ArrayList<String> listColumnNames = new ArrayList<>();
		int columnCount = containerInfo.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			listColumnNames.add(containerInfo.getColumnInfo(i).getName());
		}

		// Check selected columns
		if (selectedFields != null && selectedFields.size() > 0) {
			for (int i = 0; i < selectedFields.size(); i++) {
				GWTQLColumnInfo gwTQLColumnInfo = new GWTQLColumnInfo();
				String selectedField = selectedFields.get(i);
				if (listColumnNames.contains(selectedField)) {
					gwTQLColumnInfo.setName(selectedField);
					int index = listColumnNames.indexOf(selectedField);
					gwTQLColumnInfo.setType(containerInfo.getColumnInfo(index).getType());
					columns.add(gwTQLColumnInfo);
					selectedColumns.add(index);
				} else {
					throw new GWBadRequestException("Column '" + selectedField + "' not existed");
				}
			}
		} else {
			for (int i = 0; i < columnCount; i++) {
				GWTQLColumnInfo gwTQLColumnInfo = new GWTQLColumnInfo();
				gwTQLColumnInfo.setName(containerInfo.getColumnInfo(i).getName());
				gwTQLColumnInfo.setType(containerInfo.getColumnInfo(i).getType());
				columns.add(gwTQLColumnInfo);
			}
		}
		result.setColumns(columns);

		// Check container existence
		Container<?, ?> cont = gridStore.getContainer(container);
		if (null == cont) {
			throw new GWNotFoundException("Container not existed");
		}

		// Remove unexpected characters in statement
		statement = statement.replace("\n", " ").replace("\r", " ");

		String newStatement = "";
		List<Integer> limitAndoffset = ConversionUtils.getLimitAndOffsetFromStmt(statement);
		Integer limit = limitAndoffset.get(0);
		if (limit != null) {
			if (limit > GWSettingInfo.getMaxLimit()) {
				result.setLimit(GWSettingInfo.getMaxLimit());
				newStatement = ConversionUtils.modifyLimitofStatement(statement, GWSettingInfo.getMaxLimit());
			} else {
				result.setLimit(limit);
				newStatement = statement;
			}
		} else {
			result.setLimit(GWSettingInfo.getMaxLimit());
			newStatement = statement + " limit " + GWSettingInfo.getMaxLimit();
		}
		Integer offset = limitAndoffset.get(1);
		if (offset != null) {
			result.setOffset(offset);
		} else {
			result.setOffset(0);
		}

		// Set total
		result.setTotal(countTotalOfBaseTQL(cont, statement));

		// Execute query
		Query<?> query = cont.query(newStatement, null);
		RowSet<?> rowSet = query.fetch();
		List<Object> fetchResult = new ArrayList<>();
		while (rowSet.hasNext()) {
			fetchResult.add(rowSet.next());
		}

		// In case of aggregation result
		if (fetchResult.size() > 0) {
			Object object = fetchResult.get(0);
			if (object instanceof AggregationResult) {
				GWTQLOutputAggregation resultAggregation = new GWTQLOutputAggregation();
				List<List<Object>> rows = new ArrayList<>();
				GSType type = null;
				if (((AggregationResult) object).getDouble() != null) {
					Object row = ((AggregationResult) object).getDouble();
					List<Object> listObject = new ArrayList<>();
					listObject.add(row);
					rows.add(listObject);
					type = GSType.DOUBLE;
				} else if (((AggregationResult) object).getTimestamp() != null) {
					Object row = ((AggregationResult) object).getTimestamp();
					List<Object> listObject = new ArrayList<>();
					listObject.add(row);
					rows.add(listObject);
					type = GSType.TIMESTAMP;
				}
				resultAggregation.setResults(rows);
				GWTQLColumnInfo colInf = new GWTQLColumnInfo();
				colInf.setName("aggregationResult");
				colInf.setType(type);
				List<GWTQLColumnInfo> colInfList = new ArrayList<>();
				colInfList.add(colInf);
				resultAggregation.setColumns(colInfList);
				return resultAggregation;
			}
		}

		// Convert TQL result to List<List<Object>>
		if (selectedFields != null && selectedFields.size() > 0 && selectedColumns.size() == 0) {
			result.setColumns(null);
		} else if (selectedFields != null && selectedFields.size() > 0 && selectedColumns.size() != 0) {
			result.setResults(getSelectedColumnsByTQL(fetchResult, selectedColumns, containerInfo));
		} else {
			result.setResults(rowSetToTqlResult(fetchResult, containerInfo));
		}
		return result;
	}

	/**
	 * Count the total result from TQL without limit and offset option
	 * 
	 * @param cont
	 * @param statement
	 * @return
	 * @throws GSException
	 */
	private long countTotalOfBaseTQL(Container<?, ?> cont, String statement) throws GSException {
		long total = 0;
		TQLStatementType stmtType = ConversionUtils.checkStatementType(statement);
		switch (stmtType) {
		case SELECT_ALL:
			String noLimitStmt = ConversionUtils.removeTQLLimitOffSet(statement);
			String totalStmt = ConversionUtils.convertToCountTQL(noLimitStmt);
			total = getRowsCount(cont, totalStmt);
			break;
		case AGGREGATION:
			total = 1;
			break;
		case TIME_SERIES_INTERPOLATION:
		default:
			total = -1;
		}

		return total;
	}

	private List<List<Object>> getSelectedColumnsByTQL(List<Object> fetchResult, ArrayList<Integer> selectedColumns,
			ContainerInfo containerInfo) throws GSException, GWException, UnsupportedEncodingException, SQLException {
		if (null == fetchResult) {
			return null;
		}
		List<List<Object>> rows = new ArrayList<List<Object>>();
		long rowMaxSize = GWSettingInfo.getMaxGetRowSize();
		long rowsize = 0;
		for (Object object : fetchResult) {
			Row row = (Row) object;
			List<Object> listObject = new ArrayList<Object>();
			for (int colNo : selectedColumns) {
				long size = stringify(listObject, row.getValue(colNo), containerInfo.getColumnInfo(colNo).getType());
				rowsize += size;
			}
			if (rowsize > rowMaxSize) {
				throw new GWBadRequestException("Too many result");
			}
			rows.add(listObject);
		}
		return rows;
	}

	@Override
	public GWQueryOutput getRows(String authorization, String cluster, String database, String container,
			GWQueryParams queryParams) throws GSException, GWException, UnsupportedEncodingException, SQLException {

		if (GWSettingInfo.getLogger().isInfoEnabled()) {
			logger.info("getRows : cluster=" + cluster + " database=" + database + " container=" + container
					+ " queryParams: " + "limit=" + queryParams.getLimit() + ",offset=" + queryParams.getOffset()
					+ ",sort=" + queryParams.getSort() + ",condition=" + queryParams.getCondition());
		}
		long start = System.nanoTime();

		GWUser user = GWUser.getUserfromAuthorization(authorization);
		Validation.validateInputParams(queryParams);
		GWQueryOutput result = new GWQueryOutput();
		GridStore gridStore = null;
		try {
			gridStore = GridStoreUtils.getGridStore(cluster, database, user.getUsername(), user.getPassword());
			ContainerInfo containerInfo = gridStore.getContainerInfo(container);
			if (containerInfo == null) {
				throw new GWNotFoundException("Container not existed");
			}
			Container<Object, Row> cont = null;
			cont = gridStore.getContainer(container);
			if (null == cont) {
				throw new GWNotFoundException("Container not existed");
			}

			Query<Row> query = cont.query(buildQueryString(container, queryParams));
			RowSet<Row> rowSet = query.fetch();
			List<List<Object>> rows = rowSetToTqlResult(rowSet, containerInfo);
			result.setRows(rows);

			List<GWTQLColumnInfo> columns = new ArrayList<GWTQLColumnInfo>(containerInfo.getColumnCount());
			for (int i = 0; i < containerInfo.getColumnCount(); i++) {
				GWTQLColumnInfo gwTQLColumnInfo = new GWTQLColumnInfo();
				gwTQLColumnInfo.setName(containerInfo.getColumnInfo(i).getName());
				gwTQLColumnInfo.setType(containerInfo.getColumnInfo(i).getType());
				columns.add(gwTQLColumnInfo);
			}

			result.setColumns(columns);

			result.setLimit(Math.min(queryParams.getLimit(), GWSettingInfo.getMaxLimit()));
			result.setOffset(queryParams.getOffset());
			result.setTotal(getRowsCount(cont, buildQueryStringWithoutLimitAndOffset(container, queryParams)));
			return result;
		} catch (GSException gsException) {
			throw gsException;
		} finally {
			if (gridStore != null) {
				try {
					gridStore.close();
				} catch (GSException exception) {
					exception.printStackTrace();
				}
			}

			long end = System.nanoTime();
			if (GWSettingInfo.getLogger().isDebugEnabled()) {
				logger.debug("getRows : time=" + (end - start) / 1000000f);
			}
		}
	}

	private List<List<Object>> rowSetToTqlResult(List<Object> fetchResult, ContainerInfo containerInfo)
			throws GSException, GWException, UnsupportedEncodingException, SQLException {
		if (fetchResult == null) {
			return null;
		}
		List<List<Object>> rows = new ArrayList<List<Object>>();
		long rowMaxSize = GWSettingInfo.getMaxGetRowSize();
		long rowsize = 0;
		for (Object object : fetchResult) {
			Row row = (Row) object;
			int columnCount = containerInfo.getColumnCount();
			List<Object> list = new ArrayList<Object>(columnCount);
			for (int colNo = 0; colNo < columnCount; ++colNo) {
				long size = stringify(list, row.getValue(colNo), containerInfo.getColumnInfo(colNo).getType());
				rowsize += size;
			}
			if (rowsize > rowMaxSize) {
				throw new GWBadRequestException("Too many result");
			}
			rows.add(list);
		}
		return rows;
	}

	private List<List<Object>> rowSetToTqlResult(RowSet<Row> rowSet, ContainerInfo containerInfo)
			throws GSException, GWException, UnsupportedEncodingException, SQLException {
		if (rowSet == null) {
			return null;
		}
		List<List<Object>> rows = new ArrayList<List<Object>>();
		long rowMaxSize = GWSettingInfo.getMaxGetRowSize();
		long rowsize = 0;
		while(rowSet.hasNext()) {
			Row row = rowSet.next();
			int columnCount = containerInfo.getColumnCount();
			List<Object> list = new ArrayList<Object>(columnCount);
			for (int colNo = 0; colNo < columnCount; ++colNo) {
				long size = stringify(list, row.getValue(colNo), containerInfo.getColumnInfo(colNo).getType());
				rowsize += size;
			}
			if (rowsize > rowMaxSize) {
				throw new GWBadRequestException("Too many result");
			}
			rows.add(list);
		}
		return rows;
	}
	
	private long stringify(List<Object> list, Object data, GSType type)
			throws GWException, SQLException, UnsupportedEncodingException {

		Object reObj = data;
		if (reObj == null) {
			list.add(null);
			return 0;
		}
		long size = 0;
		switch (type) {
		case TIMESTAMP:
			reObj = DateFormatUtils.format((Date) data);
			size = Constants.SIZE_TIMESTAMP;
			break;
		case GEOMETRY:
			String tmp1 = ((Geometry) data).toString();
			size = tmp1.length();
			reObj = tmp1;
			break;

		case BLOB:
			Blob blob = (Blob) data;
			if (blob == null) {
				reObj = null;
			} else if (blob.length() == 0) {
				reObj = "";
			} else {
				reObj = "";
			}
			break;

		case BYTE_ARRAY:
			Byte[] byte_array = new Byte[((byte[]) data).length];
			int n = 0;
			for (byte b : (byte[]) data) {
				byte_array[n++] = Byte.valueOf(b);
			}
			reObj = byte_array;
			size = Byte.SIZE * n;
			break;

		case TIMESTAMP_ARRAY:
			Date[] dateArray = (Date[]) data;
			String[] tmp = new String[dateArray.length];
			int k = 0;
			for (; k < dateArray.length; ++k) {
				tmp[k] = DateFormatUtils.format(dateArray[k]);
			}
			reObj = tmp;
			size = Constants.SIZE_TIMESTAMP * k;
			break;

		case BOOL:
			size = 1;
			break;
		case STRING:
			size = ((String) data).getBytes(Constants.ENCODING).length;
			break;
		case BYTE:
			size = Byte.SIZE;
			break;
		case SHORT:
			size = (Short.SIZE / Byte.SIZE);
			break;
		case INTEGER:
			size = (Integer.SIZE / Byte.SIZE);
			break;
		case LONG:
			size = (Long.SIZE / Byte.SIZE);
			break;
		case FLOAT:
			size = (Float.SIZE / Byte.SIZE);
			break;
		case DOUBLE:
			size = (Double.SIZE / Byte.SIZE);
			break;
		case BOOL_ARRAY:
			size = ((boolean[]) data).length * 1;
			break;
		case STRING_ARRAY:
			String[] string_array = ((String[]) data);
			if (string_array.length > 0) {
				size = (string_array[0].getBytes(Constants.ENCODING)).length * string_array.length;
			}
			break;
		case SHORT_ARRAY:
			size = ((short[]) data).length * (Short.SIZE / Byte.SIZE);
			break;
		case INTEGER_ARRAY:
			size = ((int[]) data).length * (Integer.SIZE / Byte.SIZE);
			break;
		case LONG_ARRAY:
			size = ((long[]) data).length * (Long.SIZE / Byte.SIZE);
			break;
		case FLOAT_ARRAY:
			size = ((float[]) data).length * (Float.SIZE / Byte.SIZE);
			break;
		case DOUBLE_ARRAY:
			size = ((double[]) data).length * (Double.SIZE / Byte.SIZE);
			break;
		default:
			throw new GWException("Type is invalid");
		}

		list.add(reObj);
		return size;
	}

	private String buildQueryString(String containerName, GWQueryParams queryParams) {
		String query = "select * from " + containerName;

		if (queryParams != null) {
			if (queryParams.getCondition() != null && queryParams.getCondition() != "") {
				query += " where " + queryParams.getCondition();
			}
			if (queryParams.getSort() != null && queryParams.getSort() != "") {
				query += " order by " + queryParams.getSort();
			}

			if (queryParams.getLimit() > 0) {

				// Limit number of returned rows
				if (queryParams.getLimit() > GWSettingInfo.getMaxLimit()) {
					query += " limit " + GWSettingInfo.getMaxLimit();
				} else {
					query += " limit " + queryParams.getLimit();
				}
				if (queryParams.getOffset() > 0) {
					query += " offset " + queryParams.getOffset();
				}
			} else {
				if (queryParams.getOffset() > 0) {
					throw new GWBadRequestException("'offset' and 'limit' are invalid");
				}
			}
		}
		return query;
	}

	private String buildQueryStringWithoutLimitAndOffset(String containerName, GWQueryParams queryParams) {
		String query = "select count(*) from " + containerName;
		if (queryParams != null) {
			if (queryParams.getCondition() != null && queryParams.getCondition() != "") {
				query += " where " + queryParams.getCondition();
			}
		}
		return query;
	}

	@Override
	public GWPutRowOutput putRows(String authorization, String cluster, String database, String container,
			List<List<Object>> input) throws GSException, UnsupportedEncodingException {

		if (GWSettingInfo.getLogger().isInfoEnabled()) {
			logger.info("putRows : cluster=" + cluster + " database=" + database + " container=" + container + " input="
					+ input);
		}
		long start = System.nanoTime();

		GWUser user = GWUser.getUserfromAuthorization(authorization);
		Validation.validatePutRowsInput(input);
		Container<Object, Row> cont = null;
		int rowNumber = 1;
		GridStore gridStore = null;
		try {
			gridStore = GridStoreUtils.getGridStore(cluster, database, user.getUsername(), user.getPassword());
			ContainerInfo containerInfo = gridStore.getContainerInfo(container);
			if (null == containerInfo) {
				throw new GWNotFoundException("Container not existed");
			}

			for (int i = 0; i < containerInfo.getColumnCount(); i++) {
				ColumnInfo columnInfo = containerInfo.getColumnInfo(i);
				switch (columnInfo.getType()) {
				case BLOB:
					throw new GWBadRequestException("Column type BLOB is not supported");
				default:
					break;
				}
			}

			cont = gridStore.getContainer(container);
			List<Row> listRows = new ArrayList<Row>(input.size());
			long rowsize = 0;
			long rowMaxSize = GWSettingInfo.getMaxPutRowSize();
			for (List<Object> rows : input) {
				rowsize = setRowValue(rows, listRows, cont, containerInfo, rowNumber, rowsize, rowMaxSize);
				rowNumber++;
			}

			cont.put(listRows);
			GWPutRowOutput output = new GWPutRowOutput();
			output.setCount((rowNumber - 1));
			return output;
		} catch (GSException gsException) {
			throw gsException;
		} finally {
			if (gridStore != null) {
				try {
					gridStore.close();
				} catch (GSException exception) {
					exception.printStackTrace();
				}
			}

			long end = System.nanoTime();
			if (GWSettingInfo.getLogger().isDebugEnabled()) {
				logger.debug("putRows : time=" + (end - start) / 1000000f);
			}
		}

	}

	private long setRowValue(List<Object> values, List<Row> rows, Container<Object, Row> container,
			ContainerInfo containerInfo, int rowNumber, long rowSize, long rowMaxSize)
			throws GSException, UnsupportedEncodingException {

		if (values == null || values.size() != containerInfo.getColumnCount()) {
			throw new GWBadRequestException("Row data is invalid");
		}
		Row row = container.createRow();
		int i = 0;
		for (Object value : values) {
			try {
				rowSize += setRowValue(container, row, containerInfo.getColumnInfo(i).getType(), value, i++);
			} catch (Exception e) {
				throw new GWBadRequestException(e.getMessage());
			}
		}
		if (rowSize > rowMaxSize) {
			throw new GWBadRequestException("Too many rows data");
		}
		rows.add(row);
		return rowSize;
	}

	private long setRowValue(Container<?, Row> container, Row row, GSType columnType, Object value, int columnNum)
			throws Exception {
		long size = 0;
		if (value == null) {
			row.setNull(columnNum);
			return 0;
		}

		switch (columnType) {
		case BOOL:
			row.setBool(columnNum, ConversionUtils.convertToBoolean(value));
			size = 1;
			break;

		case STRING:
			String str = ConversionUtils.convertToString(value);
			row.setString(columnNum, str);
			size = str.getBytes(Constants.ENCODING).length;
			break;

		case BYTE:
			row.setByte(columnNum, ConversionUtils.convertToByte(value));
			size = Byte.SIZE;
			break;

		case SHORT:
			row.setShort(columnNum, ConversionUtils.convertToShort(value));
			size = (Short.SIZE / Byte.SIZE);
			break;

		case INTEGER:
			row.setInteger(columnNum, ConversionUtils.convertToInt(value));
			size = (Integer.SIZE / Byte.SIZE);
			break;

		case LONG:
			row.setLong(columnNum, ConversionUtils.convertToLong(value));
			size = (Long.SIZE / Byte.SIZE);
			break;

		case FLOAT:
			row.setFloat(columnNum, ConversionUtils.convertToFloat(value));
			size = (Float.SIZE / Byte.SIZE);
			break;

		case DOUBLE:
			row.setDouble(columnNum, ConversionUtils.convertToDouble(value));
			size = (Double.SIZE / Byte.SIZE);
			break;

		case TIMESTAMP:
			if (value instanceof String) {
				try {
					Date tmp = DateFormatUtils.parse((String) value);
					row.setTimestamp(columnNum, tmp);
				} catch (Exception e) {
					throw new GWException("The specified data cannot be converted to TIMESTAMP type.");
				}
			} else {
				throw new GWException("The specified data cannot be converted to TIMESTAMP type.");
			}
			size = Constants.SIZE_TIMESTAMP;
			break;

		case GEOMETRY:
			if (value instanceof String) {
				try {
					row.setGeometry(columnNum, Geometry.valueOf((String) value));
					size = ((String) value).length();
				} catch (Exception e) {
					throw new GWException("The specified data cannot be converted to GEOMETRY type.");
				}
			} else {
				throw new GWException("The specified data cannot be converted to GEOMETRY type.");
			}
			break;

		case BLOB:
			throw new GWException("Unsupport binary.");

		case BOOL_ARRAY:
			if (value instanceof List<?>) {
				List<?> list = (List<?>) value;
				boolean[] bool_array = new boolean[list.size()];
				int n = 0;
				for (Object obj : list) {
					bool_array[n++] = ConversionUtils.convertToBoolean(obj);
				}
				row.setBoolArray(columnNum, bool_array);
				size = 1 * n;
			} else {
				throw new GWException("The specified data cannot be converted to BOOL_ARRAY type.");
			}
			break;

		case STRING_ARRAY:
			if (value instanceof List<?>) {
				List<?> list = (List<?>) value;
				String[] string_array = new String[list.size()];
				int n = 0;
				for (Object obj : list) {
					string_array[n] = ConversionUtils.convertToString(obj);
					size += (string_array[n].getBytes(Constants.ENCODING)).length;
					n++;
				}
				row.setStringArray(columnNum, string_array);
			} else {
				throw new GWException("The specified data cannot be converted to STRING_ARRAY type.");
			}
			break;

		case BYTE_ARRAY:
			if (value instanceof List<?>) {
				List<?> list = (List<?>) value;
				byte[] byte_array = new byte[list.size()];
				int n = 0;
				for (Object obj : list) {
					byte_array[n++] = ConversionUtils.convertToByte(obj);
				}
				size = Byte.SIZE * n;
				row.setByteArray(columnNum, byte_array);
			} else {
				throw new GWException("The specified data cannot be converted to BYTE_ARRAY type.");
			}
			break;

		case SHORT_ARRAY:
			if (value instanceof List<?>) {
				List<?> list = (List<?>) value;
				short[] short_array = new short[list.size()];
				int n = 0;
				for (Object obj : list) {
					short_array[n++] = ConversionUtils.convertToShort(obj);
				}
				row.setShortArray(columnNum, short_array);
				size = (Short.SIZE / Byte.SIZE) * n;
			} else {
				throw new GWException("The specified data cannot be converted to SHORT_ARRAY type.");
			}
			break;

		case INTEGER_ARRAY:
			if (value instanceof List<?>) {
				List<?> list = (List<?>) value;
				int[] int_array = new int[list.size()];
				int n = 0;
				for (Object obj : list) {
					int_array[n++] = ConversionUtils.convertToInt(obj);
				}
				row.setIntegerArray(columnNum, int_array);
				size = (Integer.SIZE / Byte.SIZE) * n;
			} else {
				throw new GWException("The specified data cannot be converted to INTEGER_ARRAY type.");
			}
			break;

		case LONG_ARRAY:
			if (value instanceof List<?>) {
				List<?> list = (List<?>) value;
				long[] long_array = new long[list.size()];
				int n = 0;
				for (Object obj : list) {
					long_array[n++] = ConversionUtils.convertToLong(obj);
				}
				row.setLongArray(columnNum, long_array);
				size = (Long.SIZE / Byte.SIZE) * n;
			} else {
				throw new GWException("The specified data cannot be converted to LONG_ARRAY type.");
			}
			break;

		case FLOAT_ARRAY:
			if (value instanceof List<?>) {
				List<?> list = (List<?>) value;
				float[] float_array = new float[list.size()];
				int n = 0;
				for (Object obj : list) {
					float_array[n++] = ConversionUtils.convertToFloat(obj);
				}
				row.setFloatArray(columnNum, float_array);
				size = (Float.SIZE / Byte.SIZE) * n;
			} else {
				throw new GWException("The specified data cannot be converted to FLOAT_ARRAY type.");
			}
			break;

		case DOUBLE_ARRAY:
			if (value instanceof List<?>) {
				List<?> list = (List<?>) value;
				double[] double_array = new double[list.size()];
				int n = 0;
				for (Object obj : list) {
					double_array[n++] = ConversionUtils.convertToDouble(obj);
				}
				row.setDoubleArray(columnNum, double_array);
				size = (Double.SIZE / Byte.SIZE) * n;
			} else {
				throw new GWException("The specified data cannot be converted to DOUBLE_ARRAY type.");
			}
			break;

		case TIMESTAMP_ARRAY:
			if (value instanceof List<?>) {
				List<?> list = (List<?>) value;
				Date[] date_array = new Date[list.size()];
				int n = 0;
				for (Object obj : list) {
					if (obj instanceof String) {
						try {
							date_array[n++] = DateFormatUtils.parse((String) obj);
						} catch (ParseException e) {
							throw new GWException("The specified data cannot be converted to TIMESTAMP_ARRAY type.");
						}
					} else {
						throw new GWException("The specified data cannot be converted to TIMESTAMP_ARRAY type.");
					}
				}
				row.setTimestampArray(columnNum, date_array);
				size = Constants.SIZE_TIMESTAMP * n;
			} else {
				throw new GWException("The specified data cannot be converted to TIMESTAMP_ARRAY type.");
			}
			break;
		}
		return size;

	}

	@Override
	public void deleteContainers(String authorization, String cluster, String database, List<String> listContainers)
			throws GSException {

		if (GWSettingInfo.getLogger().isInfoEnabled()) {
			logger.info("deleteContainers : cluster=" + cluster + " database=" + database + " listContainers="
					+ listContainers);
		}
		long start = System.nanoTime();

		GWUser user = GWUser.getUserfromAuthorization(authorization);
		GridStore gridStore = null;
		try {
			gridStore = GridStoreUtils.getGridStore(cluster, database, user.getUsername(), user.getPassword());
			// Check authorization
			gridStore.getPartitionController().getPartitionCount();
			for (String container : listContainers) {
				try {
					gridStore.dropContainer(container);
				} catch (GSException ex) {
					ex.printStackTrace();
				}
			}
		} catch (GSException gsException) {
			throw gsException;
		} finally {
			if (gridStore != null) {
				try {
					gridStore.close();
				} catch (GSException exception) {
					exception.printStackTrace();
				}
			}

			long end = System.nanoTime();
			if (GWSettingInfo.getLogger().isDebugEnabled()) {
				logger.debug("deleteContainers : time=" + (end - start) / 1000000f);
			}
		}
	}

	@Override
	public void deleteRows(String authorization, String cluster, String database, String container,
			List<Object> listRowKeys) throws GSException, ParseException {

		if (GWSettingInfo.getLogger().isInfoEnabled()) {
			logger.info("deleteRows : cluster=" + cluster + " database=" + database + " listRowKeys=" + listRowKeys);
		}
		long start = System.nanoTime();

		GWUser user = GWUser.getUserfromAuthorization(authorization);
		GridStore gridStore = null;
		try {
			gridStore = GridStoreUtils.getGridStore(cluster, database, user.getUsername(), user.getPassword());
			// Check existence of container
			ContainerInfo containerInfo = gridStore.getContainerInfo(container);
			if (null == containerInfo) {
				throw new GWNotFoundException("Container not existed");
			}
			if (!containerInfo.isRowKeyAssigned()) {
				throw new GWBadRequestException("Row key does not exist");
			}

			GSType columnType = gridStore.getContainerInfo(container).getColumnInfo(0).getType();
			if (columnType == GSType.LONG) {
				for (Object object : listRowKeys) {
					if (object instanceof Integer) {
						try {
							gridStore.getContainer(container).remove(((Integer) object).longValue());
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						try {
							gridStore.getContainer(container).remove(object);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} else if (columnType == GSType.TIMESTAMP) {
				for (Object object : listRowKeys) {
					try {
						gridStore.getContainer(container).remove(DateFormatUtils.parse((String) object));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				for (Object object : listRowKeys) {
					try {
						gridStore.getContainer(container).remove(object);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (GSException gsException) {
			throw gsException;
		} finally {
			if (gridStore != null) {
				try {
					gridStore.close();
				} catch (GSException exception) {
					exception.printStackTrace();
				}
			}

			long end = System.nanoTime();
			if (GWSettingInfo.getLogger().isDebugEnabled()) {
				logger.debug("deleteRows : time=" + (end - start) / 1000000f);
			}
		}
	}

	@Override
	public void createContainer(String authorization, String cluster, String database, GWContainerInfo gwContainerInfo)
			throws GSException {

		if (GWSettingInfo.getLogger().isInfoEnabled()) {
			logger.info("createContainer : cluster=" + cluster + " database=" + database);
		}
		long start = System.nanoTime();

		GWUser user = GWUser.getUserfromAuthorization(authorization);
		Validation.validateGWContainerInfo(gwContainerInfo);
		GridStore gridStore = null;
		try {
			gridStore = GridStoreUtils.getGridStore(cluster, database, user.getUsername(), user.getPassword());
			// Check if container name is already existed
			String containername = gwContainerInfo.getContainer_name();
			ContainerInfo containerInfo = gridStore.getContainerInfo(containername);
			if (null != containerInfo) {
				throw new GWResourceConflictedException("Container already existed");
			} else {
				ContainerInfo contInfo = ConversionUtils.convertToContainerInfo(gwContainerInfo);
				Container<Object, Row> container = gridStore.putContainer(containername, contInfo, true);
				// Create column index
				try {
					for (int i = 0; i < contInfo.getColumnCount(); i++) {
						ColumnInfo colInfo = contInfo.getColumnInfo(i);
						if (colInfo.getIndexTypes() != null) {
							for (int j = 0; j < colInfo.getIndexTypes().size(); j++) {
								container.createIndex(colInfo.getName(),
										(IndexType) colInfo.getIndexTypes().toArray()[j]);
							}
						}
					}
				} catch (GSException e) {
					gridStore.dropContainer((contInfo.getName()));
					throw e;
				}
			}
		} catch (GSException gsException) {
			throw gsException;
		} finally {
			if (gridStore != null) {
				try {
					gridStore.close();
				} catch (GSException exception) {
					exception.printStackTrace();
				}
			}

			long end = System.nanoTime();
			if (GWSettingInfo.getLogger().isDebugEnabled()) {
				logger.debug("createContainer : time=" + (end - start) / 1000000f);
			}
		}
	}

	/**
	 * Get total rows
	 * 
	 * @param cont
	 * @param statement
	 *            a 'select count(*)' statement
	 * @return the number of rows
	 * @throws GSException
	 */
	private long getRowsCount(Container<?, ?> cont, String statement) throws GSException {
		long rowsCount = 0;
		RowSet<AggregationResult> rowSet = cont.query(statement, AggregationResult.class).fetch();
		while (rowSet.hasNext()) {
			rowsCount = rowSet.next().getLong();
		}
		return rowsCount;
	}

}
