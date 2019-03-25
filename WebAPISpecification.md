# WebAPI Specification

## List of APIs

Base URL : http(s)://(host):(port)/griddb/v2/

Port number is a parameter set at griddb_webapi.properties.

|Name|Method|URL|
|---|---|---|
|A) get rows|POST|/:cluster/dbs/public/containers/:container/rows|
|B) put rows|PUT|/:cluster/dbs/public/containers/:container/rows|
|C) get container list|GET|/:cluster/dbs/public/containers|
|D) get container information|GET|/:cluster/dbs/public/containers/:container/info|
|E) execute multiple TQLs|POST|/:cluster/dbs/public/tql|
|F) delete rows|DELETE|/:cluster/dbs/public/containers/:container/rows|
|G) create container|POST|/:cluster/dbs/public/containers|
|H) delete containers|DELETE|/:cluster/dbs/public/containers|

The :cluster (the :container) means cluster name (container name).

### A) Body for getting rows

Body:
condition of get rows

Example:

        {
            "offset": 10, 
            "limit": 100, 
            "condition": "id >= 50",
            "sort": "id DESC"
        }

|Name|Type|Description|Value|
|---|---|---|---|
|offset|number|offset of acquiring rows||
|limit|number|number of acquiring rows||
|condition|string|condition of acquiring rows||
|sort|string|sort order of result rows| (column) [ASC\|DESC] [, (column) [ASC\|DESC]]*|

### B) Body for putting rows

Body:
Input rows

Example:

        [
            ["2016-01-16T10:25:00.253Z", 100.5, "normal"],
            ["2016-01-16T10:35:00.691Z", 173.9, "normal"],
            ["2016-01-16T10:45:00.032Z", 173.9, null]
        ]

### E) Body for executing multiple TQLs

Body:
request of TQLs

Example:

        [
            {"name": "container1", "stmt": "select * limit 100", "columns": null},
            {"name": "container2", "stmt": "select * where column1>=0", "columns": ["column1"]},
            {"name": "container3", "stmt": "select SUM(*) order by column1 desc", "columns": null}
        ]

|Name|Type|Description|Value|
|---|---|---|---|
|name|string|container name to execute TQL||
|stmt|number|TQL statement||
|columns|array|columns of the container to get from APIs||

### F) Body for deleting rows

Body:
rowkeys

Example:

        [
            "key1",
            "key2",
            "key3"
        ]

### G) Body for creating container

Body:
container information

Example:

        {
            "container_name": "container1",
            "container_type": "COLLECTION",
            "rowkey": true,
            "columns" : [
                {"name": "date", "type": "TIMESTAMP", "index": ["TREE"]},
                {"name": "value", "type": "DOUBLE", "index": []},
                {"name": "str", "type": "STRING", "index": []}
            ]
        }

|Name|Type|Description|Value|
|---|---|---|---|
|container_name|string|container name||
|container_type|string|container type|COLLECTION or TIME_SERIES|
|rowkey|boolean|existence of rowkey||
|columns|array|columns of the container||
|name|string|column name||
|type|string|column type||
|index|array|array of index type of the column|array of TREE or HASH|

Column type consists of the following:
- BOOL
- STRING
- BYTE
- SHORT
- INTEGER
- LONG
- FLOAT
- DOUBLE
- TIMESTAMP
- GEOMETRY
- BLOB
- BOOL_ARRAY
- STRING_ARRAY
- BYTE_ARRAY
- SHORT_ARRAY
- INTEGER_ARRAY
- LONG_ARRAY
- FLOAT_ARRAY
- DOUBLE_ARRAY
- TIMESTAMP_ARRAY

### H) Body for deleting container

Body:
container names

Example:

        [
            "container1",
            "container2",
            "container3"
        ]
