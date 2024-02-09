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


public class ToolConstants {

	public static String META_FILE_VERSION = "5.5.0";

	public static enum RowFileType { CSV, BINARY, AVRO, ARCHIVE_CSV, RDB }

	public static final String ENCODING_JSON			= "UTF-8";

	public static final String FILE_EXT_METAINFO		= "_properties.json";

	public static final String PUBLIC_DB				= "public";

	public static final String DB_DELIMITER				= ".";

	public static final String JSON_META_VERSION				= "version";
	public static final String JSON_META_DBNAME					= "database";
	public static final String JSON_META_CONTAINER				= "container";
	public static final String JSON_META_CONTAINER_TYPE			= "containerType";
	public static final String JSON_META_CONTAINER_ATTRIBUTE	= "attribute";
	public static final String JSON_META_CONTAINER_FILE			= "containerFile";
	public static final String JSON_META_CONTAINER_INTERNAL_FILE	= "containerInternalFile";
	public static final String JSON_META_CONTAINER_FILE_TYPE	= "containerFileType";
	public static final String JSON_META_DATA_AFFINITY			= "dataAffinity";
	public static final String JSON_META_ROW_KEY				= "rowKeyAssigned";
	public static final String JSON_META_PARTITION_NO			= "partitionNo";

    public static final String JSON_META_EXPIRATION_TYPE            = "expirationType";
    public static final String JSON_META_EXPIRATION_TIME            = "expirationTime";
    public static final String JSON_META_EXPIRATION_TIME_UNIT       = "expirationTimeUnit";
    
	public static final String JSON_META_ARCHIVE_INFO			= "archiveInfo";
	public static final String JSON_META_NODE_ADDR			= "nodeAddr";
	public static final String JSON_META_NODE_PORT			= "nodePort";
	public static final String JSON_META_DATABASE_ID			= "databaseId";
	public static final String JSON_META_CONTAINER_ID			= "containerId";
	public static final String JSON_META_DATAPARTITION_ID		= "dataPartitionId";
	public static final String JSON_META_ROW_INDEX_OID			= "rowIndexOID";
	public static final String JSON_META_MVCC_INDEX_OID			= "mvccIndexOID";
	public static final String JSON_META_INIT_SCHEMA_STATUS		= "initSchemaStatus";
	public static final String JSON_META_SCHEMA_VERSION			= "schemaVersion";
	public static final String JSON_META_START_TIME			= "startTime";
	public static final String JSON_META_END_TIME			= "endTime";
	public static final String JSON_META_EXPIRED_TIME			= "expiredTime";
	public static final String JSON_META_ERASABLE_TIME		= "erasableTime";
	public static final String JSON_META_SCHEMA_INFORMATION	= "schemaInformation";

	public static final String[] JSON_META_GROUP_CONTAINER ={
		JSON_META_DBNAME, JSON_META_CONTAINER, JSON_META_CONTAINER_TYPE, JSON_META_CONTAINER_ATTRIBUTE,
		JSON_META_CONTAINER_FILE, JSON_META_CONTAINER_FILE_TYPE, JSON_META_DATA_AFFINITY,
		JSON_META_ROW_KEY, JSON_META_PARTITION_NO, JSON_META_CONTAINER_INTERNAL_FILE,
		JSON_META_EXPIRATION_TYPE, JSON_META_EXPIRATION_TIME, JSON_META_EXPIRATION_TIME_UNIT,
		JSON_META_ARCHIVE_INFO, JSON_META_NODE_ADDR, JSON_META_NODE_PORT,
		JSON_META_DATABASE_ID, JSON_META_CONTAINER_ID, JSON_META_DATAPARTITION_ID,
		JSON_META_ROW_INDEX_OID, JSON_META_MVCC_INDEX_OID, JSON_META_INIT_SCHEMA_STATUS,
		JSON_META_SCHEMA_VERSION, JSON_META_START_TIME, JSON_META_END_TIME,
		JSON_META_EXPIRED_TIME, JSON_META_ERASABLE_TIME, JSON_META_SCHEMA_INFORMATION
	};

	public static final String JSON_META_COLUMN_SET				= "columnSet";
	public static final String JSON_META_COLUMN_NAME			= "columnName";
	public static final String JSON_META_COLUMN_TYPE			= "type";
	public static final String JSON_META_COLUMN_CSTR_NOTNULL	= "notNull";

	public static final String JSON_META_TIME_PROP				= "timeSeriesProperties";
	public static final String JSON_META_TIME_COMP				= "compressionMethod";
	public static final String JSON_META_TIME_WINDOW			= "compressionWindowSize";
	public static final String JSON_META_TIME_WINDOW_UNIT		= "compressionWindowSizeUnit";
	public static final String JSON_META_TIME_EXPIRATION_DIV_COUNT	= "expirationDivisionCount";
	public static final String JSON_META_TIME_EXPIRATION		= "rowExpirationElapsedTime";
	public static final String JSON_META_TIME_EXPIRATION_UNIT	= "rowExpirationTimeUnit";
	public static final String JSON_META_TIME_UNIT_NULL			= "null";

	public static final String[] JSON_META_GROUP_TIME	= {JSON_META_TIME_COMP, JSON_META_TIME_WINDOW,
		JSON_META_TIME_WINDOW_UNIT, JSON_META_TIME_EXPIRATION_DIV_COUNT ,
		JSON_META_TIME_EXPIRATION, JSON_META_TIME_EXPIRATION_UNIT};

	public static final String JSON_META_INDEX_SET				= "indexSet";
	public static final String JSON_META_INDEX_NAME				= "columnName";
	public static final String JSON_META_INDEX_TYPE1			= "type";
	public static final String JSON_META_INDEX_TYPE2			= "indexType";
	public static final String JSON_META_INDEX_INDEXNAME		= "indexName";

	public static final String JSON_META_TRIGGER_SET			= "triggerInfoSet";
	public static final String JSON_META_TRIGGER_EVENTNAME		= "eventName";
	public static final String JSON_META_TRIGGER_TYPE			= "notificationType";
	public static final String JSON_META_TRIGGER_TARGET			= "targetEvents";
	public static final String JSON_META_TRIGGER_COLUMN			= "targetColumnNames";
	public static final String JSON_META_TRIGGER_URI			= "notificationURI";
	public static final String JSON_META_TRIGGER_JMS_TYPE		= "JmsDestinationType";
	public static final String JSON_META_TRIGGER_JMS_NAME		= "JmsDestinationName";
	public static final String JSON_META_TRIGGER_JMS_USER		= "JmsUser";
	public static final String JSON_META_TRIGGER_JMS_PASS		= "JmsPassword";

	public static final String[] JSON_META_GROUP_TRIGGER =
		{JSON_META_TRIGGER_EVENTNAME, JSON_META_TRIGGER_TYPE, JSON_META_TRIGGER_TARGET,
		JSON_META_TRIGGER_COLUMN, JSON_META_TRIGGER_URI, JSON_META_TRIGGER_JMS_TYPE,
		JSON_META_TRIGGER_JMS_NAME, JSON_META_TRIGGER_JMS_USER,
		JSON_META_TRIGGER_JMS_PASS};

	public static final String JSON_META_CMP_SET				= "compressionInfoSet";
	public static final String JSON_META_CMP_NAME				= "columnName";
	public static final String JSON_META_CMP_TYPE				= "compressionType";
	public static final String JSON_META_CMP_RATE				= "rate";
	public static final String JSON_META_CMP_SPAN				= "span";
	public static final String JSON_META_CMP_WIDTH				= "width";

	public static final String[] JSON_META_GROUP_CMP = {
		JSON_META_CMP_NAME, JSON_META_CMP_TYPE, JSON_META_CMP_RATE, JSON_META_CMP_SPAN,
		JSON_META_CMP_WIDTH
	};

	public static final String JSON_META_TP_PROPS				= "tablePartitionInfo";
	public static final String JSON_META_TP_TYPE				= "type";
	public static final String JSON_META_TP_COLUMN				= "column";
	public static final String JSON_META_TP_DIV_COUNT			= "divisionCount";
	public static final String JSON_META_TP_ITV_VALUE			= "intervalValue";
	public static final String JSON_META_TP_ITV_UNIT			= "intervalUnit";

	public static final double COMPRESSION_WIDTH_INIT = -1;
	public static final double COMPRESSION_RATE_INIT  = -1;
	public static final double COMPRESSION_SPAN_INIT  = -1;

	public static final String COMPRESSION_TYPE_INIT  = "";
	public static final String COMPRESSION_TYPE_RELATIVE = "RELATIVE";
	public static final String COMPRESSION_TYPE_ABSOLUTE = "ABSOLUTE";

	public static final String COLUMN_TYPE_STRING_ARRAY		= "string[]";
	public static final String COLUMN_TYPE_BOOL_ARRAY		= "boolean[]";
	public static final String COLUMN_TYPE_BYTE_ARRAY		= "byte[]";
	public static final String COLUMN_TYPE_SHORT_ARRAY		= "short[]";
	public static final String COLUMN_TYPE_INTEGER_ARRAY	= "integer[]";
	public static final String COLUMN_TYPE_LONG_ARRAY		= "long[]";
	public static final String COLUMN_TYPE_FLOAT_ARRAY		= "float[]";
	public static final String COLUMN_TYPE_DOUBLE_ARRAY		= "double[]";
	public static final String COLUMN_TYPE_TIMESTAMP_ARRAY	= "timestamp[]";
	public static final String COLUMN_TYPE_BOOL				= "boolean";

	public static final String EXP_TOOL_ATTRIBUTE_BASE = "BASE";
	public static final String EXP_TOOL_ATTRIBUTE_SINGLE = "SINGLE";
	public static final String EXP_TOOL_ATTRIBUTE_LARGE = "LARGE";
	public static final String EXP_TOOL_ATTRIBUTE_SUB = "SUB";
	public static final String EXP_TOOL_ATTRIBUTE_SINGLE_SYSTEM = "SINGLE_SYSTEM";

	public static final String TABLE_PARTITION_TYPE_HASH		= "HASH";
	public static final String TABLE_PARTITION_TYPE_INTERVAL	= "INTERVAL";
	public static final String TABLE_PARTITION_TYPE_RANGE		= "RANGE";
	public static final String TABLE_PARTITION_ITV_UNIT_DAY		= "DAY";

	public static final String EXPIRATION_TYPE_PARTITION = "partition";
	public static final String EXPIRATION_TYPE_ROW = "row";
	public static final String META_TABLES 				= "#tables";
	public static final String META_TABLE_PARTITIONS	= "#table_partitions";
	public static final String META_COLUMNS				= "#columns";
	public static final String META_INDEX_INFO			= "#index_info";
	public static final String META_PRIMARY_KEYS		= "#primary_keys";
	public static final String META_EVENT_TRIGGERS		= "#event_triggers";

	private static final String STMT_SELECT_ANY_FROM = "SELECT * FROM ";

	public static final String STMT_SELECT_META_TABLES = STMT_SELECT_ANY_FROM + "\"" + META_TABLES + "\"";

	public static final String STMT_SELECT_META_TABLE_PARTITIONS = STMT_SELECT_ANY_FROM + "\"" + META_TABLE_PARTITIONS + "\"";

	public static final String STMT_SELECT_META_COLUMNS = STMT_SELECT_ANY_FROM + "\"" + META_COLUMNS + "\"";

	public static final String STMT_SELECT_META_INDEX_INFO = STMT_SELECT_ANY_FROM + "\"" + META_INDEX_INFO + "\"";

	public static final String STMT_SELECT_META_PRIMARY_KEYS = STMT_SELECT_ANY_FROM + "\"" + META_PRIMARY_KEYS + "\"";

	public static final String STMT_SELECT_META_EVENT_TRIGGERS = STMT_SELECT_ANY_FROM + "\"" + META_EVENT_TRIGGERS + "\"";

	public static final String STMT_SELECT_META_TABLES_PATITIONNAMES = STMT_SELECT_META_TABLES + " WHERE PARTITION_TYPE IS NOT NULL";

	private static final String PSTMT_WHERE_TABLE_NAME = " WHERE TABLE_NAME='%s';";

	public static final String PSTMT_DROP_TABLE = "DROP TABLE \"%s\";";

	public static final String PSTMT_SELECT_META_TABLES_WITH_TABLE = STMT_SELECT_META_TABLES + PSTMT_WHERE_TABLE_NAME;

	public static final String PSTMT_SELECT_META_TABLE_PARTITIONS_WITH_TABLE = STMT_SELECT_META_TABLE_PARTITIONS + PSTMT_WHERE_TABLE_NAME;

	public static final String PSTMT_SELECT_META_COLUMNS_WITH_TABLE = STMT_SELECT_META_COLUMNS + PSTMT_WHERE_TABLE_NAME;

	public static final String PSTMT_SELECT_META_INDEX_INFO_WITH_TABLE = STMT_SELECT_META_INDEX_INFO + PSTMT_WHERE_TABLE_NAME;

	public static final String PSTMT_SELECT_META_PRIMARY_KEYS_WITH_TABLE = STMT_SELECT_META_PRIMARY_KEYS + PSTMT_WHERE_TABLE_NAME;

	public static final String PSTMT_SELECT_META_EVENT_TRIGGERS_WITH_TABLE = STMT_SELECT_META_EVENT_TRIGGERS + PSTMT_WHERE_TABLE_NAME;

	public static final String PRAMGMA_INTERNAL_META_TABLE_VISIBLE = "internal.compiler.meta_table_visible";

	public static final String META_TABLES_DATABASE_NAME 				= "DATABASE_NAME";
	public static final String META_TABLES_TABLE_NAME 					= "TABLE_NAME";

	public static final String META_TABLES_EXPIRATION_TYPE				= "EXPIRATION_TYPE";
	public static final String META_TABLES_EXPIRATION_TIME 				= "EXPIRATION_TIME";
	public static final String META_TABLES_EXPIRATION_TIME_UNIT 		= "EXPIRATION_TIME_UNIT";
	public static final String META_TABLES_PARTITION_TYPE 				= "PARTITION_TYPE";
	public static final String META_TABLES_PARTITION_COLUMN 			= "PARTITION_COLUMN";
	public static final String META_TABLES_PARTITION_INTERVAL_VALUE 	= "PARTITION_INTERVAL_VALUE";
	public static final String META_TABLES_PARTITION_INTERVAL_UNIT 		= "PARTITION_INTERVAL_UNIT";
	public static final String META_TABLES_PARTITION_DIVISION_COUNT 	= "PARTITION_DIVISION_COUNT";
	public static final String META_TABLES_SUBPARTITION_TYPE 			= "SUBPARTITION_TYPE";
	public static final String META_TABLES_SUBPARTITION_COLUMN 			= "SUBPARTITION_COLUMN";
	public static final String META_TABLES_SUBPARTITION_INTERVAL_VALUE 	= "SUBPARTITION_INTERVAL_VALUE";
	public static final String META_TABLES_SUBPARTITION_INTERVAL_UNIT 	= "SUBPARTITION_INTERVAL_UNIT";
	public static final String META_TABLES_SUBPARTITION_DIVISION_COUNT 	= "SUBPARTITION_DIVISION_COUNT";

	public static final String META_TABLE_PARTITIONS_DATABASE_NAME 					= "DATABASE_NAME";
	public static final String META_TABLE_PARTITIONS_TABLE_NAME 					= "TABLE_NAME";
	public static final String META_TABLE_PARTITIONS_PARTITION_SEQ 					= "PARTITION_SEQ";
	public static final String META_TABLE_PARTITIONS_PARTITION_NAME 				= "PARTITION_NAME";
	public static final String META_TABLE_PARTITIONS_PARTITION_BOUNDARY_VALUE 		= "PARTITION_BOUNDARY_VALUE";
	public static final String META_TABLE_PARTITIONS_SUBPARTITION_BOUNDARY_VALUE 	= "SUBPARTITION_BOUNDARY_VALUE";
	public static final String META_TABLE_PARTITIONS_PARTITION_NODE_AFFINITY 		= "PARTITION_NODE_AFFINITY";
	public static final String META_TABLE_PARTITIONS_CLUSTER_PARTITION_INDEX 		= "CLUSTER_PARTITION_INDEX";

}
