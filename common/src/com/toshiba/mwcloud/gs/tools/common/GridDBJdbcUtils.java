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

package com.toshiba.mwcloud.gs.tools.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.toshiba.mwcloud.gs.TimeUnit;
import com.toshiba.mwcloud.gs.tools.common.data.ExpirationInfo;
import com.toshiba.mwcloud.gs.tools.common.data.TablePartitionProperty;
import com.toshiba.mwcloud.gs.tools.common.data.ToolConstants;

public class GridDBJdbcUtils {

	public static List<TablePartitionProperty> getTablePartitionProperties(Connection conn, String name) throws GridDBJdbcException {
		
		TablePartitionProperty partProp = null;
		TablePartitionProperty subProp = null;

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(String.format(ToolConstants.PSTMT_SELECT_META_TABLES_WITH_TABLE, name));
			rs = pstmt.executeQuery();
			if ( !rs.next() ){
				throw new GridDBJdbcException("Container not found. name=["+name+"]");
			}
			
			String partType = rs.getString(ToolConstants.META_TABLES_PARTITION_TYPE);
			String partColumn = rs.getString(ToolConstants.META_TABLES_PARTITION_COLUMN);

			if (partType != null) {
				if (partType.equals(ToolConstants.TABLE_PARTITION_TYPE_HASH)) {
					partProp = new TablePartitionProperty(partType, partColumn,
							rs.getInt(ToolConstants.META_TABLES_PARTITION_DIVISION_COUNT));
				} else if (partType.equals(ToolConstants.TABLE_PARTITION_TYPE_INTERVAL)) {
					partProp = new TablePartitionProperty(partType, partColumn,
							rs.getString(ToolConstants.META_TABLES_PARTITION_INTERVAL_VALUE),
							rs.getString(ToolConstants.META_TABLES_PARTITION_INTERVAL_UNIT));
				}
			}

			String subPartType = rs.getString(ToolConstants.META_TABLES_SUBPARTITION_TYPE);
			String subPartColumn = rs.getString(ToolConstants.META_TABLES_SUBPARTITION_COLUMN);

			if (subPartType != null) {
				if (subPartType.equals(ToolConstants.TABLE_PARTITION_TYPE_HASH)) {
					subProp = new TablePartitionProperty(subPartType, subPartColumn,
							rs.getInt(ToolConstants.META_TABLES_SUBPARTITION_DIVISION_COUNT));
				} else if (subPartType.equals(ToolConstants.TABLE_PARTITION_TYPE_INTERVAL)) {
					subProp = new TablePartitionProperty(subPartType, subPartColumn,
							rs.getString(ToolConstants.META_TABLES_SUBPARTITION_INTERVAL_VALUE),
							rs.getString(ToolConstants.META_TABLES_SUBPARTITION_INTERVAL_UNIT));
				}
			}

		} catch (Exception e) {
			throw new GridDBJdbcException("Failed to get table partitioning information. name=[" + name + "]", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {}
		}

		if (partProp == null) {
			return null;
		}

		List<TablePartitionProperty> props = new ArrayList<TablePartitionProperty>();
		props.add(partProp);
		if (subProp != null) {
			props.add(subProp);
		}
		return props;
	}
	
	public static ExpirationInfo getExpirationInfo(Connection conn, String name) throws GridDBJdbcException {
		
		ExpirationInfo expInfo = null;

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(String.format(ToolConstants.PSTMT_SELECT_META_TABLES_WITH_TABLE, name));
			rs = pstmt.executeQuery();
			if ( !rs.next() ){
				throw new GridDBJdbcException("Container not found. name=["+name+"]");
			}
			
			String expType = rs.getString(ToolConstants.META_TABLES_EXPIRATION_TYPE);
			int expTime = rs.getInt(ToolConstants.META_TABLES_EXPIRATION_TIME);
			String expTimeUnitStr = rs.getString(ToolConstants.META_TABLES_EXPIRATION_TIME_UNIT);
			
			if (expType != null && !expType.equalsIgnoreCase(ToolConstants.EXPIRATION_TYPE_ROW)) {
				expInfo = new ExpirationInfo(expType, expTime, TimeUnit.valueOf(expTimeUnitStr));
			}

		} catch (Exception e) {
			throw new GridDBJdbcException("Failed to get expiration information. name=[" + name + "]", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {}
		}

		if (expInfo == null) {
			return null;
		}

		return expInfo;
	}	
	
	public static void executePragma(Connection conn, String key, String value) throws GridDBJdbcException {
		Statement stmt = null;
		String sql = createPragmaStatement(key, value);
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			throw new GridDBJdbcException("Failed to execute internal statement. key=[" + key + "], value=[" + value + "]", e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {}
		}
	}

	private static String createPragmaStatement(String key, String value) {
		StringBuilder builder = new StringBuilder();
		builder.append("pragma ");
		builder.append(key);
		builder.append("=");
		builder.append(value);
		return builder.toString();
	}
}
