Web API
=======

Overview
----

GridDB provides a Web API to register rows, acquire rows and execute TQL and SQL statements for the GridDB cluster.  The Web API is configured as a web application.

The functions of GridDB Web API are shown below.

- Row acquisition
  - Acquires rows from a container.
  - Acquires rows from multiple containers.
- Row registration
  - Registers rows in a container.
  - Registers rows in multiple containers.
- Database connection confirmation
  - Checks connection to a database.
- Container list acquisition
  - Acquires a container list from a database.
- Container information acquisition
  - Acquires container information (column etc.) from a container.
- TQL statement execution
  - Executes multiple SQL statements and acquires the results.
- Row deletion
  - Deletes a row from a container.
- Container creation
  - Creates a new container for a database.
- Container deletion
  - Deletes a container from a database.
- SQL SELECT execution
  - Executes a SQL SELECT statement.
- SQL UPDATE execution
  - Executes a SQL UPDATE statement.

Using Web API
---------------------

To use Web API, Java must be installed beforehand. The compatible versions are:

- Oracle Java 8
- Oracle Java 11
- RedHat OpenJDK 8
- RedHat OpenJDK 11

The procedure to configure the Web API is as follows:

1.  Setting of the destination cluster
2.  Setting of the Web API operation (optional)
3.  Setting of the log output destination (optional)

### Setting of the destination cluster

Specify the information of the cluster to be connected from the Web API in the cluster information definition file (`${GS_WEBAPI_HOME}/conf/repository.json`).

Based on the value of the cluster definition file (gs_cluster.json) of the cluster to be connected, specify the cluster configuration method to "mode", and the address information corresponding to the method.

| Parameter          | JSON data type | Description                                                 |
|-----------------------------|----------|------------------------------------------------------|
| clusters           | array          | An array of cluster information                                   |
|   name               | string         | Cluster name                                           |
|   mode               | string         | Connection method (MULTICAST／FIXED_LIST／PROVIDER ) |
| (mode=MULTICAST)            |          |                                                      |
|   address            | string         | Multicast address for row registration and acquisition               |
|   port               | integer            | Multicast port for row registration and acquisition                               |
|   jdbcAddress        | string         | Multicast address for SQL execution                 |
|   jdbcPort           | integer            | Multicast port for SQL execution                             |
| (mode=FIXED_LIST) |          |                                                      |
|   transactionMember  | string         | address and port for row registration and acquisition                     |
|   sqlMember          | string         | address and port for SQL execution                       |
| (mode=PROVIDER)             |          |                                                      |
|   providerUrl        | string         | Provider URL for all functions                            |

Example for the multicast method

```
{
  "clusters" : [
    {
      "name" : "myCluster",
      "mode" : "MULTICAST",
      "address" : "239.0.0.111",
      "port" : 31999
    }
  ]
}
```

[Memo]
- If the contents of repository.json are invalid (e.g., format error, mandatory parameter not defined, etc.), a Web API startup will result in an error.

### Setting of Web API behavior (optional)

Set Web API behavior in the configuration file (`${GS_WEBAPI_HOME}/conf/griddb_webapi.properties`).

Even without changing the initial settings, Web API can work while all the parameters remain at the default values. Change the values as required by the system.

#### GridDB configuration

| Field              | Description                                                                                                       | Default value |
|--------------------|--------------------------------------------------------------------------------|--------------|
| failoverTimeout    | The failover time (in seconds) during which the Web API retries after detecting a node failure in accessing GridDB.  | 5            |
| transactionTimeout | The maximum amount of time between the start and end of a transaction (in seconds).                    | 30           |
| containerCacheSize | The maximum number of the container information to be cached.                                    | 100          |

#### Web API configuration  

| Field              | Description                                                                                                       | Default value |
|--------------------|--------------------------------------------------------------------------------|--------------|
| port | Web API service port (integer from 1 to 65535) | 8081           |
| maxResponseSize | The upper limit of the size for row acquisition, SQL execution, and TQL execution (MB) (integer from 1 to 2048) | 20           |
| maxRequestSize | The upper size limit for row registration (MB) (1-2048) | 20           |
| loginTimeout | The connection timeout time (in seconds) for SQL execution<br>(Specify an integer value for this field. If the value is less than or equal to zero, an SQL statement cannot be executed.)  | 5            |
| maxQueryNum | Maximum number of queries that can be included in one request (integer from 0 to 100) | 10           |
| maxLimit | Maximum number of rows that can be acquired at a time when executing SQL or TQL (integer of 1 or more) | 10000           |
| timeZone   | Specify the time (in ±hh:mm or ±hhmm), time zone ID (only "Z" is supported), or AUTO (to take over the JavaVM environment) as a time zone to be used in the offset calculation to retrieve time information in SQL and TQL.  | Z           |
| notificationInterfaceAddress  | To configure the cluster network in multicast mode when multiple network interfaces are available, specify the IP address of the interface to receive the multicast packets from. | OS-dependent           |
| blobPath | Directory used to store temporary data in processing BLOB data as a zip file | - |

[Memo]
- The Web API needs to be restarted to reflect the environment settings.

### Setting of the log output destination (optional)

The Web API logs are output to the following directory by default.

``` example
/var/lib/gridstore/webapi/log
```

To change the output directory, edit the following file:

``` example
${GS_WEBAPI_HOME}/conf/logback.xml
```

Common functions (HTTP request/response)
-----------------------------------

This chapter describes the HTTP request/response part common to each function.

### URI

The URI to be accessed when using the Web API.

`http://(Web API server IP) :(port)/griddb/v2/(command path)`

[Memo]
- For "(command path)" above, see the section on each function.
- Operations on clusters, databases and containers whose name contains any special characters ('-', '.', '/', '=') cannot be performed in the Web API.

<a id="request_header"></a>
### Request header

Specify the following headers to the request header (common to all APIs):

| Field         | Description                                                                                            | Required                       |
|-------------------|--------------------------------------------------|----------------------------|
| Content-Type  | "application/json; charset=UTF-8"                                                                      | ✓ |
| Authorization | Specify the user and password to access GridDB in the user: password format (Basic authentication) | ✓ |

### Request body

Specify the request body in JSON format. Please refer to the JSON format of each functions.

[Memo]
- Write JSON format data in UTF-8.

- Write the date in UTC using the following format:

  - YYYY-MM-DDThh:mm:ss.SSSZ
  
- Otherwise, an error will occur.
  - Example:
    - 2016/01/17T14: 32: 33.888Z ... Error due to a year, month and date separator error
    - 2016-01-18 ... Error because time is not specified

- The maximum size of JSON data in the request body can be set in ` griddb_webapi.properties` as below (unit: MB):

  ```
  maxRequestSize=20
  ```

### Response code

Refer to the section on each function for the response code.


<a id="response_body"></a>
### Response body

Refer to the section on each function for the response body when the processing succeeds.

If the processing fails, an error message is returned in the following format:

```
{
  "version":"v2",
  "errorCode":"Error code",
  "errorMessage":"Error message"
}
```

[Memo]

- The maximum size of JSON data in the response body can be set in ` griddb_webapi.properties` as below (unit: MB):

  ```
  maxResponseSize=20
  ```

  

Row acquisition
--------

### Row acquisition from a single container

This function acquires rows from a container (table). It is also possible to narrow down the rows to be acquired by specifying conditions.

**Path**

`/:cluster/dbs/:database/containers/:container/rows`

| Item       | Description                                                    |
|------------|---------------------------------------------------------|
| :cluster   | cluster name                                              |
| :database  | database name (Specify "public" for a public database)    |
| :container | container (table) name                                    |

**HTTP method**

POST

**Request header**

Refer to the [request header](#request_header).

**Request body**

| Item      | Description                                 | JSON data type    | Required |
|------------|--------------|-------------------|------|
| /offset    | Acquisition start position                  | an integer from 0 | \-    |
| /limit     | The number of rows to be acquired           | an integer from 1 | ✓    |
| /condition | Conditional expression (For details, see the "GridDB TQL Reference".)                        | string            | \-    |
| /sort      | Sorting condition (ascending or descending order of values of a specified column; specified as "column name asc" or "column name desc" ) | string            | \-    |

[Memo]
- If the value specified by limit is greater than the value of maxLimit in the configuration file, the value of maxLimit is used in the limit clause.

The example below acquires row data with a column "id" value of 50 or more, sorts it in descending order by the value of "id", and acquires 100 values from the 11th row.

```
{
  "offset" : 10,
  "limit"  : 100,
  "condition" : "id >= 50",
  "sort" : "id desc"
}
```

**Response code**

| Code | Description                           |
|--------|--------------------------------|
| 200  | Success                           |
| 400  | Incorrect request data         |
| 401  | An authentication error, a connection error         |
| 404  | The specified resource is not found  |
| 500  | An error occurs in Web API or GridDB   |

**Response body**

Acquired rows will be returned as the following JSON data:

| Item    | Description                                               | JSON data type |
|---------------|------------------|--------------|
| /columns | An array of column information                            | array         |
| /columns/name    | Column name                                               | string       |
| /columns/type    | JSON Data type                                            | string       |
| /columns/rows    | An array of rows                                      | array         |
| /total  | Number of rows acquired when offset and limit are ignored | number         |
| /offset  | Acquisition start position                                | number         |
| /limit   | Number of acquisitions applied                            | number         |

Example:

```
{
  "columns" : [
    {"name": "date", "type": "TIMESTAMP" },
    {"name": "value", "type": "DOUBLE" },
    {"name": "str", "type": "STRING" }
  ],
  "rows" : [
    ["2016-01-16T10:25:00.253Z", 100.5, "normal" ],
    ["2016-01-16T10:35:00.691Z", 173.9, "normal" ],
    ["2016-01-16T10:45:00.032Z", 173.9, null ]
  ],
  "total" : 100000,
  "offset" : 0,
  "limit" : 3
}
```

<a name="data-type-json-type"> Depending on the column data type, column values with the following JSON data type will be returned: </a>

| Classification | Data type |  | JSON data type | Example                                   |
|------------------|----------------|-------------------------|------------------------------|--------------------------------------|
| Primitive | Boolean type | BOOL | Boolean (true/false) | true                                 |
|                  | String type | STRING | String | "GridDB"                             |
|                  | Integer type | BYTE/SHORT/INTEGER/LONG | Number | 512                                  |
|                  | Floating point type | FLOAT/DOUBLE | Number | 593.5                                |
|                  | Date and time type | TIMESTAMP | Text string<br>・UTC<br>・format<br>YYYY-MM-DDThh:mm:ss.SSSZ | "2016-01-16T10:25:00.253Z"   |
|                  | Spatial-type | GEOMETRY | Text string (WKT representation) | POLYGON((0 0,10 0,10 10,0 10,0 0)) |
| | BLOB type | BLOB | string | "UEsDBAoAAAgAADS4PFIAAAAAAAAAAAA..." |
| Array | Boolean type | BOOL | Array of Boolean values | \[true, false, true]\                |
|                  | String type | STRING | Array of text string values | \["A","B","C"\]                      |
|                  | Integer type | BYTE/SHORT/INTEGER/LONG | Array of numbers | \[100, 39, 535\]                     |
|                  | Floating point type | FLOAT/DOUBLE | Array of numbers | \[3.52, 6.94, 1.83\]                 |
|                  | Date and time type | TIMESTAMP | Array of text string values<br>(The format is the same as the format for the primitive date and time type) | \["2016-01-16T10:25:00.253Z", "2016-01-17T01:42:53.038Z"\]          |

[Memo]
- BLOB data is returned in base64 format; thus, base64 data needs to be decoded after obtaining BLOB data.
- If the column value is NULL, null is returned for the column in JSON data.

### Row acquisition from a single container (BLOB data as a zip file)

This function acquires rows from a container (table). If the container has a BLOB column, BLOB data is returned as a zip file. It is also possible to narrow down the rows to be acquired by specifying conditions. 

**Path**

`/:cluster/dbs/:database/containers/:container/rows/blob`

| Item       | Description                                            |
| ---------- | ------------------------------------------------------ |
| :cluster   | cluster name                                           |
| :database  | database name (Specify "public" for a public database) |
| :container | container (table) name                                 |

**HTTP method**

POST

**Request header**

Refer to the [request header](#request_header).

**Request body**

| Item         | Description                                                  | JSON data type    | Required |
| ------------ | ------------------------------------------------------------ | ----------------- | -------- |
| /offset      | Acquisition start position                                   | an integer from 0 | \-       |
| /limit       | The number of rows to be acquired                            | an integer from 1 | ✓        |
| /condition   | Conditional expression (For details, see the "GridDB TQL Reference".)                                         | string            | \-       |
| /sort        | Sorting condition (ascending or descending order of values of a specified column; specified as "column name asc" or "column name desc" )                  | string            | \-       |
| /fileNameCol | The name of a column in the container whose value is used to name BLOB data files | string            | -        |

[Memo]

- If the value specified by limit is greater than the value of maxLimit in the configuration file, the value of maxLimit is used in the limit clause.
- By default, BLOB data for each row is saved to a file with a random name without a file extension. The Web API has an option to set the name of a BLOB data file by a value of another column. For example, if a table has the string column `file_name` that stores file names of BLOB data, by option, the values of this column can be used to set the name of BLOB data file of each row: `{"fileNameCol": "<column_name>"}`

The following example acquires row data with the column "id" value of 50 or more, sorts it in descending order by the value of "id", acquires 100 values from the 10th row, and uses the value in the column `file_name` to name BLOB data files.

```
{ 
  "offset" : 10, 
  "limit" : 100, 
  "condition" : "id >= 50", 
  "sort" : "id desc",
  "fileNameCol": "file_name"
}
```

**Response code**

| Code | Description                                 |
| ---- | ------------------------------------------- |
| 200  | Success                                     |
| 400  | Incorrect request data                      |
| 401  | An authentication error, a connection error |
| 404  | The specified resource is not found             |
| 500  | An error occurs in Web API or GridDB        |

**Response body**

The response body is returned as the following form data:

| Key  | Description                                                  | Content type     |
| ---- | ------------------------------------------------------------ | ---------------- |
| file | The zip file contains all BLOB data files.<br/>A random name generated as a name of the zip file is attached to a response. | application/zip  |
| rows | Data of a non-BLOB column                                      | application/json |

The value of `rows` is returned as the following JSON data:

| Item          | Description                                               | JSON data type |
| ------------- | --------------------------------------------------------- | -------------- |
| /columns      | An array of column information                            | array          |
| /columns/name | Column name                                               | string         |
| /columns/type | Column data type                                          | string         |
| /rows         | An array of rows                                      | array          |
| /total        | Number of rows acquired when offset and limit are ignored | number         |
| /offset       | Acquisition start position                                | number         |
| /limit        | Number of acquisitions applied                            | number         |

[Memo]

- IF the container does not have a BLOB column, or the query result has no BLOB data, this API returns a zip file with no file inside.
- If the container has two or more BLOB columns, the BLOB data files have the same name with a prefix number attached. e.g. 1_picture.jpg and 2_picture.jpg. 
- If the row value of `fileNameCol` has an invalid file name, the API server generates a random name.
- The value of a BLOB column in the JSON data is the same with the BLOB file name in a zip file, and both start with the prefix `(BLOB)`.
- The response data has two data types: JSON data and form data. They are separated by a string that starts with the `--` prefix. The JSON data is in the first part, and the form data is in the second part. To retrieve the zip file that contains all BLOB data files, copy the binary content (start with the `PK` prefix) and save it to a file with a `.zip` extension.

Example:

- Below is an example of a response body:

```
--zO7yOzRXhcrXUbD7heAB9rGzewWDfUt
Content-Disposition: form-data; name="rows"
Content-Type: application/json;charset=UTF-8

{"columns":[{"name":"key","type":"INTEGER"},{"name":"data","type":"BLOB"},{"name":"des","type":"STRING"}],"rows":[[1000,"(BLOB)d0f925b9-884a-420a-a7a8-91c53ed7b126","lombok"],[200,"(BLOB)dee59ebc-2bf9-4f36-9b86-0bf72ff16e29","anh2.png"]],"offset":0,"limit":2,"total":3}
--zO7yOzRXhcrXUbD7heAB9rGzewWDfUt
Content-Disposition: form-data; name="file"; filename="e17bba76-4315-4c32-9384-81fff84abc84.zip"
Content-Type: application/zip
Content-Length: 1780817

PKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
```

Depending on the column data type, column values with the following JSON data type will be returned:

| Classification | Data type      |                         | JSON data type                                               | Example                                                    |
| -------------- | ------------------- | ----------------------- | ------------------------------------------------------------ | ---------------------------------------------------------- |
| Primitive      | Boolean type        | BOOL                    | Boolean (true/false)                                         | true                                                       |
|                | String type         | STRING                  | String                                                       | "GridDB"                                                   |
|                | Integer type        | BYTE/SHORT/INTEGER/LONG | Number                                                       | 512                                                        |
|                | Floating point type | FLOAT/DOUBLE            | Number                                                       | 593.5                                                      |
|                | Date and time type  | TIMESTAMP               | Text string<br>・UTC<br>・format<br>YYYY-MM-DDThh:mm:ss.SSSZ | "2016-01-16T10:25:00.253Z"                                 |
|                | Spatial-type        | GEOMETRY                | Text string (WKT representation)                             | POLYGON((0 0,10 0,10 10,0 10,0 0))                         |
| Array          | Boolean type        | BOOL                    | Array of Boolean values                                      | \[true, false, true]\                                      |
|                | String type         | STRING                  | Array of text string values                                  | \["A","B","C"\]                                            |
|                | Integer type        | BYTE/SHORT/INTEGER/LONG | Array of numbers                                             | \[100, 39, 535\]                                           |
|                | Floating point type | FLOAT/DOUBLE            | Array of numbers                                             | \[3.52, 6.94, 1.83\]                                       |
|                | Date and time type  | TIMESTAMP               | Array of text string values<br>(The format is the same as the format for the primitive date and time type) | \["2016-01-16T10:25:00.253Z", "2016-01-17T01:42:53.038Z"\] |

[Memo]

- If the column value is NULL, null is returned for the column in JSON data.

### Row acquisition from multiple containers

This function acquires rows from multiple containers (tables). It is also possible to narrow down the rows to be acquired by specifying conditions. This function is only supported for a container with a row key.

**Path**

`/:cluster/dbs/:database/containers/rows`

| Item      | Description                                            |
| --------- | ------------------------------------------------------ |
| :cluster  | cluster name                                           |
| :database | database name (Specify "public" for a public database) |

**HTTP method**

POST

**Request header**

Refer to the [request header](#request_header).

**Request body**

| Item            | Description                                                  | JSON data type           | Required | Default value |
| --------------- | ------------------------------------------------------------ | ------------------------ | -------- | ------------- |
| /name           | Container name                                               | string                   | ✓        | -             |
| /startKeyValue  | - Row key value to specify the start position to obtain row data.<br />- This applies only for a container that has a single row key with the data type LONG, INTEGER, or TIMESTAMP.<br />- When the row key is TIMESTAMP, the start key value must be in the following format: `yyyy-MM-ddTHH:mm:ss.SSSZ` | string or number         | -        | -             |
| /finishKeyValue | - Row key value to specify the last position to obtain row data.<br />- This applies only for a container that has a single row key with the data type LONG, INTEGER, or TIMESTAMP.<br />- When the row key is TIMESTAMP, the finish key value must be in the following format: `yyyy-MM-ddTHH:mm:ss.SSSZ` | string or number           | -        | -             |
| /keyValues      | - List of row key values used as search conditions to obtain row data. The row data with a row key that matches with a value in this list will be returned.<br />- This applies only for a container that has a single row key with the data type STRING, LONG, INTEGER or TIMESTAMP.<br />- If `startKeyValue`, `finishKeyValue`, and `keyValues` are specified at the same time, API obtains rows using the `keyValues` condition and ignores `startKeyValue` and `finishKeyValue`.<br />- When the row key is TIMESTAMP, the key value must be in the following format: `yyyy-MM-ddTHH:mm:ss.SSSZ` | array of strings/numbers | -        | -             |
| /offset         | Acquisition start position. The offset is set for each container. | an integer from 0        | \-       | 0             |
| /limit          | The number of rows to be acquired. The limit is set for each container. | an integer from 1        | -        | 10000         |

[Memo]

- If the value specified by limit is greater than the value of maxLimit in the configuration file, the value of maxLimit is used in the limit clause.
- BLOB data is returned in base64 format; thus, base64 data needs to be decoded after obtaining BLOB data.
- The conditions `startKeyValue`, `finishKeyValue`, and `keyValues` are only supported for a container with a single row key.

The following example acquires row data from two containers. The first container acquires row data with a key value from 0 to 100, from which 10 values from the 2nd row are acquired. The second container only acquires row data with a key value 1, 3, or 5.

```
[
  {
    "name":"container1",
    "startKeyValue":0,
    "finishKeyValue":100,
    "limit":10,
    "offset":2
  },
  {
    "name":"container2",
    "keyValues":[1, 3, 5]
  }
]
```

**Response code**

| Code | Description                                 |
| ---- | ------------------------------------------- |
| 200  | Success                                     |
| 400  | Incorrect request data                      |
| 401  | An authentication error, a connection error |
| 404  | The specified resource is not found             |
| 500  | An error occurs in Web API or GridDB        |

**Response body**

Acquired rows will be returned as the following JSON data:

| Item          | Description                                               | JSON data type |
| ------------- | --------------------------------------------------------- | -------------- |
| /container    | Container name                                            | string         |
| /columns      | An array of column information                            | array          |
| /columns/name | Column name                                               | string         |
| /columns/type | JSON data type                                            | string         |
| /results      | An array of rows                                      | array          |
| /total        | Number of rows acquired when offset and limit are ignored | number         |
| /offset       | Acquisition start position                                | number         |
| /limit        | Number of acquisitions applied                            | number         |

Example:

```
[
  {
    "container":"container1",
    "columns": [
      { "name":"date", "type":"TIMESTAMP" },
      { "name":"value", "type":"DOUBLE" },
      { "name":"str", "type":"STRING" }
    ],
    "results": [
      ["2016-01-16T10:25:00.253Z", 100.5, "normal"],
      ["2016-01-16T10:35:00.691Z", 173.9, "normal"],
      ["2016-01-16T10:45:00.032Z", 173.9, null]
    ],
    "total":100,
    "offset":0,
    "limit":3
  },
  {
    "container":"container2",
    "columns": [
      { "name":"date", "type":"STRING" },
      { "name":"value", "type":"LONG" }
    ],
    "results": [
      ["string1", 100],
      ["string2", 173],
      ["string3", 200]
    ],
    "total":10000,
    "offset":0,
    "limit":10000
  }
]
```

Depending on the column data type, column values with the following JSON data type will be returned:
Refer to [this section](#data-type-json-type).

[Memo]

- If the column value is NULL, null is returned for the column in JSON data.

Row registration
--------

### Row registration in a single container

This function registers rows in a container.

Specify the rows to be registered in JSON format. Multiple rows can be registered in one container.

[Memo]
- The data of one row / multiple rows can be specified and registered in one container.
- The container to be registered must exist.
- If the container has a row key that already exists in the container, that row will be updated. If the container has a row key that does not exist in the container, a row will be newly created.
- If the container has no row key, all rows will be newly created.
- The Web API supports BLOB data in base64 format. Thus, base64 BLOB data needs to be encoded before specifying it to the request body.
- When an exception occurs during row registration, only some rows may be registered. Therefore, when retrying a request with an HTTP client during an exception, the same row data may be registered duplicately if a container has no row key.

**Path**

`/:cluster/dbs/:database/containers/:container/rows`

| Item       | Description                                                    |
|------------|---------------------------------------------------------|
| :cluster   | cluster name                                              |
| :database  | database name (Specify "public" for a public database)        |
| :container | container (table) name                                    |

**HTTP method**

PUT

**Request header**

Refer to the [request header](#request_header).

**Request body**

Specify the rows as the following JSON format:

| Item      | Description                                 | JSON data type    | Required |
|-----------|--------------------|---------------|------|
| /(row) | Row (an array of column values) | Array          | ✓    |

Example:

```
[
  ["2016-01-16T10:25:00.253Z", 100.5, "normal"],
  ["2016-01-16T10:35:00.691Z", 173.9, "normal"],
  ["2016-01-16T10:45:00.032Z", 173.9, null]
]
```

<a name="data-type-json-type-registration"> Depending on the column data type, describe a column value with the following JSON data type:</a>

| Classification | Data type |  | JSON data type | Example                                   |
|-----------------|----------------|-------------------------|-----------------------------------|--------------------------------------|
| Primitive | Boolean type | BOOL | Boolean value (true or false)<br>or character string ("true" or "false") | true   |
|                  | String type | STRING | String | "GridDB"                             |
|                  | Integer type | BYTE/SHORT/INTEGER/LONG | Number or text string | 512                                  |
|                  | Floating point type | FLOAT/DOUBLE | Number or text string | 593.5                                |
|                  | Date and time type | TIMESTAMP | Text string<br>・UTC<br>・format<br>YYYY-MM-DDThh:mm:ss.SSSZ | "2016-01-16T10:25:00.253Z" |
|                  | Spatial-type | GEOMETRY | Text string (WKT representation) | POLYGON((0 0,10 0,10 10,0 10,0 0)) |
| | BLOB type | BLOB | string | "UEsDBAoAAAgAADS4PFIAAAAAAAAAAAA..." |
| Array | Boolean type | BOOL | Boolean array or string array | \[true, false, true\]                |
|                  | String type | STRING | Array of text string values | \["A","B","C"\]                      |
|                  | Integer type | BYTE/SHORT/INTEGER/LONG | Numeric array or string array | \[100, 39, 535\]                     |
|                  | Floating point type | FLOAT/DOUBLE | Numeric array or string array | \[3.52, 6.94, 1.83\]                 |
|                  | Date and time type | TIMESTAMP | Array of text string values<br>(The format is the same as the format for the primitive date and time type) | \["2016-01-16T10:25:00.253Z", "2016-01-17T01:42:53.038Z"\]  |

[Memo]

- If a NULL value (JSON data type null) is specified as a column value, the Web API operates as follows:
  - If the NOT NULL constraint is specified for the column, a registration error occurs.
  - Otherwise, a NULL value is registered.

**Response code**

| Code | Description                           |
|--------|--------------------------------|
| 200  | Success                           |
| 400  | Incorrect request data         |
| 401  | An authentication error, a connection error         |
| 404  | The specified resource is not found  |
| 500  | An error occurs in Web API or GridDB   |

**Response body**

The response body will be returned as the following JSON data:

| Item   | Description                     | JSON data type |
| ------ | ------------------------------- | -------------- |
| /count | Number of updated/inserted rows | Number         |

Example:

```
{
  "count" : 2
}
```

### Row registration in a single container (BLOB data as a zip file)

This function registers the rows in a container with specified BLOB data as a zip file.

Specify the rows to be registered in form data. Multiple rows can be registered in one container.

[Memo]

- The data of one row or multiple rows can be specified and registered in one container.
- The container to be registered must exist.
- If the container has a row key that already exists in the container, that row will be updated. If the container has a row key that does not exist in the container, a row will be newly created.
- If the container has no row key, a row will be newly created.
- When an exception occurs during row registration, only some rows may be registered. Therefore, when an HTTP client retries a request during an exception, the same row data may be registered duplicately if the container has no row key.

**Path**

`/:cluster/dbs/:database/containers/:container/rows/blob`

| Item       | Description                                            |
| ---------- | ------------------------------------------------------ |
| :cluster   | cluster name                                           |
| :database  | database name (Specify "public" for a public database) |
| :container | container (table) name                                 |

**HTTP method**

PUT

**Request header**

Refer to the [request header](#request_header).

**Request body**

Specify the request body as the following form data:

| Key  | Description                                                  | Content type     |
| ---- | ------------------------------------------------------------ | ---------------- |
| file | The zip file contains all BLOB data files.                    | application/zip  |
| rows | A list of rows, each of which is a list of objects, that needs to be added to the container in JSON format | application/json |

Specify the `rows` as the following JSON data:

| Item   | Description                     | JSON data type | Required |
| ------ | ------------------------------- | -------------- | -------- |
| /(row) | Row (an array of column values) | Array          | ✓        |

[Memo]

- The value of BLOB columns must be of string type. This is the same with a file name (case-sensitive) that is in the zip file.

- If the value of BLOB columns is not found in the zip file, an error is returned.

- If the zip file has a sub directory, specify the sub directory in a BLOB column value.

- The maximum size of an upload file can be set in the application.properties file as below:

  ```xml
  # specifies the maximum size permitted for uploaded files. The default is 1MB
  spring.servlet.multipart.max-file-size=500MB

  # specifies the maximum size allowed for multipart/form-data requests. The default is 10MB.
  spring.servlet.multipart.max-request-size=500MB
  ```
  


Example:

- If the zip file has a structure as below:

  ```
  file.zip
      |----image1.png
      |----picture
              |----image2.png
  ```

  Then the JSON data of `rows` in the request body should be:

  ```
  [
     ["5","image1.png"],   
     ["6","picture/image2.png"]
  ] 
  ```

Depending on the column data type, describe a column value with the following JSON data type:

| Classification | Data type      |                         | JSON data type                                               | Example                                                    |
| -------------- | ------------------- | ----------------------- | ------------------------------------------------------------ | ---------------------------------------------------------- |
| Primitive      | Boolean type        | BOOL                    | Boolean value (true or false)<br>or character string ("true" or "false") | true                                                       |
|                | String type         | STRING                  | String                                                       | "GridDB"                                                   |
|                | Integer type        | BYTE/SHORT/INTEGER/LONG | Number or text string                                        | 512                                                        |
|                | Floating point type | FLOAT/DOUBLE            | Number or text string                                        | 593.5                                                      |
|                | Date and time type  | TIMESTAMP               | Text string<br>・UTC<br>・format<br>YYYY-MM-DDThh:mm:ss.SSSZ | "2016-01-16T10:25:00.253Z"                                 |
|                | Spatial-type        | GEOMETRY                | Text string (WKT representation)                             | POLYGON((0 0,10 0,10 10,0 10,0 0))                         |
| Array          | Boolean type        | BOOL                    | Boolean array or string array                                | \[true, false, true\]                                      |
|                | String type         | STRING                  | Array of text string values                                  | \["A","B","C"\]                                            |
|                | Integer type        | BYTE/SHORT/INTEGER/LONG | Numeric array or string array                                | \[100, 39, 535\]                                           |
|                | Floating point type | FLOAT/DOUBLE            | Numeric array or string array                                | \[3.52, 6.94, 1.83\]                                       |
|                | Date and time type  | TIMESTAMP               | Array of text string values<br>(The format is the same as the format for the primitive date and time type) | \["2016-01-16T10:25:00.253Z", "2016-01-17T01:42:53.038Z"\] |

[Memo]

- If a NULL value (JSON data type null) is specified as a column value, the Web API operates as follows:
  - If the NOT NULL constraint is specified for the column, a registration error will occur.
  - Otherwise, a NULL value will be registered.

**Response code**

| Code | Description                                 |
| ---- | ------------------------------------------- |
| 200  | Success                                     |
| 400  | Incorrect request data                      |
| 401  | An authentication error, a connection error |
| 404  | The specified resource is not found             |
| 500  | An error occurs in Web API or GridDB        |

**Response body**

If the process is successful, nothing is returned.

Please refer to [response body](#response_body) in case of failure.

### Row registration in multiple containers

This function registers rows in multiple containers.

Specify the rows to be registered in JSON format. Multiple rows can be registered in one container.

[Memo]

- The data of one row / multiple rows can be specified and registered in one container.
- The containers to be registered must exist.
- If the container has a row key that already exists in the container, that row will be updated. If the container has a row key that does not exist in the container, a row will be newly created.
- If the container has no row key, all rows will be newly created.
- The number of values in a row must be equal to the number of columns in a container. If the column is nullable, the value of that column cannot be omitted and must be specified as a `null` or empty value.
- The Web API supports BLOB data in base64 format. Thus, base64 BLOB data needs to be encoded before specifying it to the request body.
- When an exception occurs during row registration, only some rows may be registered. Therefore, when retrying a request with an HTTP client during an exception, the same row data may be registered duplicately if the container has no row key.

**Path**

`/:cluster/dbs/:database/containers/rows`

| Item      | Description                                            |
| --------- | ------------------------------------------------------ |
| :cluster  | cluster name                                           |
| :database | database name (Specify "public" for a public database) |

**HTTP method**

PUT

**Request header**

Refer to the [request header](#request_header).

**Request body**

Specify the rows as the following JSON format:

| Item           | Description                      | JSON data type | Required |
| -------------- | -------------------------------- | -------------- | -------- |
| /containerName | Container name                   | String         | ✓        |
| /rows          | Rows (an array of column values) | Array          | ✓        |

[Memo]

-  If the row key is TIMESTAMP, the row value must be in the following format: `yyyy-MM-ddTHH:mm:ss.SSSZ`

Example:

```
[
  {
    "containerName":"container2",
    "rows":[
      ["a3", "a4"],
      ["b3", "b4"]
    ]
  },
  {
    "containerName":"container4",
    "rows":[
      [3000, 4000],
      [5000, 6000]
    ]
  }
]
```

Depending on the column data type, describe a column value with the following JSON data type:

Refer to [this section](#data-type-json-type-registration).

[Memo]

- If a NULL value (JSON data type null) is specified as a column value, the Web API operates as follows:
  - If the NOT NULL constraint is specified for the column, a registration error occurs.
  - Otherwise, a NULL value is registered.

**Response code**

| Code | Description                                 |
| ---- | ------------------------------------------- |
| 200  | Success                                     |
| 400  | Incorrect request data                      |
| 401  | An authentication error, a connection error |
| 404  | The specified resource is not found             |
| 500  | An error occurs in Web API or GridDB        |

**Response body**

The response body is returned as the following JSON data:

| Item           | Description                     | JSON data type |
| -------------- | ------------------------------- | -------------- |
| /containerName | Container name                  | String         |
| /updatedRows   | Number of updated/inserted rows | Number         |

Example:

```
[
  {
    "containerName":"container2",
    "updatedRows" : 2
  },
  {
    "containerName":"container4",
    "updatedRows" : 2
  }
]
```

Database connection confirmation
--------

Check the connection to the specified database.

**Path**

`/:cluster/dbs/:database/checkConnection`

| Item       | Description                                                    |
|------------|---------------------------------------------------------|
| :cluster   | cluster name                                              |
| :database  | database name (Specify "public" for a public database)    |

**HTTP method**

GET

**Request header**

Refer to the [request header](#request_header).

**Request parameter**

| Item     | Description                               | Data type    | Required |
|------------|--------------|-------------------|------|
| /timeout | Timeout value used only for this API (in seconds) | an integer from 0 | \-    |

**Response code**

| Code | Description                           |
|--------|--------------------------------|
| 200  | Success                           |
| 400  | Incorrect request data         |
| 401  | An authentication error, a connection error         |
| 500  | An error occurs in Web API or GridDB   |

**Response body**

If the process is successful, nothing is returned.

Please refer to [response body](#response_body) in case of failure.

Container list acquisition
--------

Get a list of containers and tables. It is also possible to narrow down the containers to be acquired by specifying conditions.

**Path**

`/:cluster/dbs/:database/containers`

| Item       | Description                                                    |
|------------|---------------------------------------------------------|
| :cluster   | cluster name                                              |
| :database  | database name (Specify "public" for a public database)    |

**HTTP method**

GET

**Request header**

Refer to the [request header](#request_header).

**Request parameter**

| Item      | Description                                 | JSON data type    | Required |
|------------|----------------|------------------------------------|------|
| /type  | Type of containers to be acquired | text string（COLLECTION or TIME_SERIES） | \-    |
| /limit     | The number of rows to be acquired           | an integer from 0 | ✓    |
| /offset    | Acquisition start position                  | an integer from 0 | \-    |
| /sort   | Sorting expression                | string                                | \-    |

[Memo]
- offset can be used together with "limit".

**Response code**

| Code | Description                           |
|--------|--------------------------------|
| 200  | Success                           |
| 400  | Incorrect request data         |
| 401  | An authentication error, a connection error         |
| 500  | An error occurs in Web API or GridDB   |

**Response body**

Acquired rows will be returned as the following JSON data:

| Item    | Description                                               | JSON data type |
|---------------|------------------|--------------|
| /names | Array of container names                                 | Array         |
| /total | Number of acquisitions when offset and limit are ignored | Number         |
| /offset  | Acquisition start position                                | Number         |
| /limit   | Number of acquisitions applied                            | Number         |

Example:

```
{
  "names" : [
    "container1",
    "container2",
    "timeseries1"
  ],
  "total" : 100000,
  "offset" : 0,
  "limit" : 3
}
```

Container information acquisition
--------

Get the information on a container or a table.

**Path**

`/:cluster/dbs/:database/containers/:container/info`

| Item       | Description                                                    |
|------------|---------------------------------------------------------|
| :cluster   | cluster name                                              |
| :database  | database name (Specify "public" for a public database)    |
| :container | container (table) name                                    |

**HTTP method**

GET

**Request header**

Refer to the [request header](#request_header).

**Request body**

\-

**Response code**

| Code | Description                           |
|--------|--------------------------------|
| 200  | Success                           |
| 400  | Incorrect request data         |
| 401  | An authentication error, a connection error         |
| 404  | The specified resource is not found  |
| 500  | An error occurs in Web API or GridDB   |

**Response body**

| Item    | Description                                               | JSON data type |
|-----------------|-----------------|--------------|
| /container_name | Container name | string         |
| /container_type | Container type                    | string (COLLECTION or TIME_SERIES)       |
| /rowkey          | Existence of a row key                    | Boolean (true or false)      |
| /columns          | An array of column information         | array         |
| /columns/name    | Column name                                               | string       |
| /columns/type             | Data type of column            | string       |
| /columns/index   | Index                          | Array of strings (TREE or SPATIAL)         |

Example:

```
{
  "container_name" : "container1",
  "container_type" : "COLLECTION",
  "rowkey" : true,
  "columns" : [
    {"name": "date", "type": "TIMESTAMP", "index": ["TREE"]},
    {"name": "value", "type": "DOUBLE", "index": []},
    {"name": "str", "type": "STRING", "index": []}
  ]
}
```
[Memo]
- Containers with composite row keys and composite indexes are not supported. When the command is executed, IllegalStateException occurs.

TQL command execution
--------

Execute a TQL statement. Multiple TQL statements can be sent at once.
The value of specific columns can be set to acquire it in the execution result.

**Path**

`/:cluster/dbs/:database/tql`

| Item       | Description                                                    |
|------------|---------------------------------------------------------|
| :cluster   | cluster name                                              |
| :database  | database name (Specify "public" for a public database)    |

**HTTP method**

POST

**Request header**

Refer to the [request header](#request_header).

**Request body**

| Item      | Description                                 | JSON data type    | Required |
|------------|------------------|------------|------|
| /name   | Target container name             | string         | ✓    |
| /stmt    | TQL statement                     | string         | ✓    |
| /columns | Array of acquisition column names | array          | \-    |

[Memo]
- If the value specified by limit in TQL is greater than the value of maxLimit in the configuration file, the value of maxLimit is used in the limit clause.

Example:

```
[
  {"name" : "container1", "stmt" : "select * limit 100", "columns" : null},
  {"name" : "container2", "stmt" : "select * where column1>=0", "columns" : ["column1"]},
  {"name" : "container3", "stmt" : "select SUM(*) order by column1 desc", "columns" : null}
]
```

**Response code**

| Code | Description                           |
|--------|--------------------------------|
| 200  | Success                           |
| 400  | Incorrect request data         |
| 401  | An authentication error, a connection error         |
| 404  | The specified resource is not found  |
| 500  | An error occurs in Web API or GridDB   |


**Response body**

| Item    | Description                                               | JSON data type |
|---------------|------------------|--------------|
| /columns | An array of column information                            | Array         |
| /columns/name    | Column name                                               | string       |
| /columns/type    | JSON data type                                            | string       |
| /results | TQL execution results                                    | Array         |
| /total | Number of acquisitions when offset and limit are ignored | Number         |
| /offset  | Acquisition start position                                | Number         |
| /limit   | Number of acquisitions applied                            | Number         |

[Memo]
- If the TQL statement is an aggregate operation, total, offset, and limit are not included in the response body.

Example:

```
[
  {
    "columns" : [
      {"name": "date", "type": "TIMESTAMP"},
      {"name": "value", "type": "DOUBLE"},
      {"name": "str", "type": "STRING"}
    ],
    "results" : [
      ["2016-01-16T10:25:00.253Z", 100.5, "normal" ],
      ["2016-01-16T10:35:00.691Z", 173.9, "normal" ],
      ["2016-01-16T10:45:00.032Z", 173.9, null]
    ],
    "total" : 1000125,
    "offset" : 0,
    "limit" : 3
  },
  {
    "columns" : [
      {"name": "aggregationResult", "type": "LONG"}
    ],
    "results" : [
      [55]
    ]
  }
]
```

Row deletion
--------

This function deletes the rows from the container (table).

**Path**

`/:cluster/dbs/:database/containers/:container/rows`

| Item       | Description                                                    |
|------------|---------------------------------------------------------|
| :cluster   | cluster name                                              |
| :database  | database name (Specify "public" for a public database)    |
| :container | container (table) name                                    |

**HTTP method**

DELETE

**Request header**

Refer to the [request header](#request_header).

**Request body**

| Item      | Description                                 | JSON data type    | Required |
|------------|--------------|-------------------|------|
| /(key) | Row key     | Array          | ✓    |

Example:

```
[
  "key1",
  "key2",
  "key3"
]
```

**Response code**

| Code | Description                           |
|--------|--------------------------------|
| 204  | Success                           |
| 400  | Incorrect request data         |
| 401  | An authentication error, a connection error         |
| 404  | The specified resource is not found  |
| 500  | An error occurs in Web API or GridDB   |

**Response body**

If the process is successful, nothing is returned.

Please refer to [response body](#response_body) in case of failure.

[Memo]
- Containers with composite row keys are not supported. When the command is executed, IllegalStateException occurs.

Creating a container
--------

Create a container.

**Path**

`/:cluster/dbs/:database/containers`

| Item       | Description                                                    |
|------------|---------------------------------------------------------|
| :cluster   | cluster name                                              |
| :database  | database name (Specify "public" for a public database)    |

**HTTP method**

POST

**Request header**

Refer to the [request header](#request_header).

**Request body**

| Item    | Description                                               | JSON data type | Required |
|-----------------|-----------------|--------------|------|
| /container_name | Container name | string         | ✓    |
| /container_type | Container type                    | string (COLLECTION or TIME_SERIES)       | ✓    |
| /rowkey          | Existence of a row key                    | Boolean (true or false)      | ✓    |
| /columns          | An array of column information         | array         | ✓    |
| /columns/name    | Column name                                               | string       | ✓    |
| /columns/type             | Data type of column            | string       | ✓    |
| /columns/index   | Index                          | Array of strings (TREE or SPATIAL)         | \- |

Example:

```
{
  "container_name" : "container1",
  "container_type" : "COLLECTION",
  "rowkey" : true,
  "columns" : [
    {"name": "date", "type": "TIMESTAMP", "index": ["TREE"]},
    {"name": "value", "type": "DOUBLE", "index": []},
    {"name": "str", "type": "STRING", "index": []}
  ]
}
```

**Response code**

| Code | Description                           |
|--------|--------------------------------|
| 201  | Success                           |
| 400  | Incorrect request data         |
| 401  | An authentication error, a connection error         |
| 409  | The specified container already exists.  |
| 500  | An error occurs in Web API or GridDB   |

**Response body**

If the process is successful, nothing is returned.

Please refer to [response body](#response_body) in case of failure.

[Memo]
- Composite row keys and composite indexes are not supported.

Container deletion
--------

Delete a container

**Path**

`/:cluster/dbs/:database/containers`

| Item       | Description                                                    |
|------------|---------------------------------------------------------|
| :cluster   | cluster name                                              |
| :database  | database name (Specify "public" for a public database)    |

**HTTP method**

DELETE

**Request header**

Refer to the [request header](#request_header).

**Request body**

| Item      | Description                                 | JSON data type    | Required |
|------------|-----------|-------------|------|
| /(name) | \<Container name\> | Array          |  ✓   |

Example:

```
[
  "container1",
  "container2",
  "timeseries1"
]
```

**Response code**

| Code | Description                           |
|--------|--------------------------------|
| 204  | Success, no container to be deleted                           |
| 400  | Incorrect request data         |
| 401  | An authentication error, a connection error         |
| 500  | An error occurs in Web API or GridDB   |

**Response body**

If the process is successful, nothing is returned.

Please refer to [response body](#response_body) in case of failure.

SQL SELECT execution
----------------

This function executes one or more SQL SELECT statements in a GridDB database.

**Path**

`/:cluster/dbs/:database/sql/select`

| Item       | Description                                                    |
|-----------|---------------------------------------------------------|
| :cluster   | cluster name                                              |
| :database  | database name (Specify "public" for a public database)      |

**HTTP method**

POST

**Request header**

Refer to the [request header](#request_header).

**Request body**

Specify one or more SQL SELECT statements as the following JSON format:

| Item      | Description                                 | JSON data type    | Required   |
|-------|------------------------------------|--------------|--------|
| /stmt  | An SQL SELECT statement                                       | string         | ✓   |

Example:

```
[
  {"stmt" : "select * from container1"},
  {"stmt" : "select column1 from container2 where column1>=0"},
  {"stmt" : "select column2, column3 from container3 order by column1 desc"}
]
```

**Response code**

| Code | Description                           |
|--------|--------------------------------|
| 200  | Success                           |
| 400  | Incorrect request data         |
| 401  | An authentication error, a connection error         |
| 404  | The specified resource is not found  |
| 500  | An error occurs in Web API or GridDB   |

**Response body**

| Item    | Description                                               | JSON data type |
|---------------|-------------------|--------------|
| /columns | An array of column information                            | array         |
| /columns/name    | Column name                                               | string       |
| /columns/type    | JSON data type                                            | string       |
| /results    | SQL SELECT execution results   | array         |

Example:

```
[
  {
    "columns" : [
      {"name": "date", "type": "TIMESTAMP"},
      {"name": "value", "type": "DOUBLE"},
      {"name": "str", "type": "STRING"}
    ],
    "results" : [
      ["2016-01-16T10:25:00.253Z", 100.5, "normal" ],
      ["2016-01-16T10:35:00.691Z", 173.9, "normal" ],
      ["2016-01-16T10:45:00.032Z", 173.9, null]
    ]
  },
  {
    "columns" : [
      {"name": "date", "type": "TIMESTAMP"},
      {"name": "value", "type": "DOUBLE"},
      {"name": "str", "type": "STRING"}
    ],
    "results" : [
      ["2016-01-16T10:25:00.253Z", 100.5, "normal" ],
      ["2016-01-16T10:35:00.691Z", 173.9, "normal" ],
      ["2016-01-16T10:45:00.032Z", 173.9, null]
    ]
  }
]
```

Depending on the column data type, column values with the following JSON data type will be returned:

| Classification | Data type |  | JSON data type | Example                         |
|------------------|----------------|-------------------------|--------------------------|----------------------------|
| Primitive | Boolean type | BOOL | Boolean (true/false) | true                       |
|                  | String type | STRING | String | "GridDB"                   |
|                  | Integer type | BYTE/SHORT/INTEGER/LONG | Number | 512                        |
|                  | Floating point type | FLOAT/DOUBLE | Number | 593.5                      |
|                  | Date and time type | TIMESTAMP | Text string<br>・UTC<br>・format<br>YYYY-MM-DDThh:mm:ss.SSSZ | "2016-01-16T10:25:00.253Z" |

[Memo]
- If the column value is NULL, null is returned for the column in JSON data.
- GEOMETRY and array types are not supported in this function.
- For GEOMETRY and array columns, "UNKNOWN" will be returned as a data type, and null will be returned as a data value.
- When multiple SQL statements are specified and any one of them fails to execute, status code 400 is returned as an http response. In this case, SQL statements subsequent to the failed one are not executed.

SQL UPDATE execution
----------------

This function executes one or more SQL UPDATE statements in a GridDB database.

**Path**

`/:cluster/dbs/:database/sql/update`

| Item      | Description                                            |
| --------- | ------------------------------------------------------ |
| :cluster  | cluster name                                           |
| :database | database name (Specify "public" for a public database) |

**HTTP method**

POST

**Request header**

Refer to the [request header](#request_header).

**Request body**

Specify one or more SQL UPDATE statements as the following JSON format:

| Item  | Description            | JSON data type | Required |
| ----- | ---------------------- | -------------- | -------- |
| /stmt | An SQL UPDATE statement | string         | ✓        |

[Memo]

- SELECT, EXPLAIN, and ANALYZE in an SQL statement are not supported in this API.

Example:

```
[ 
  {"stmt" : "update container1 set col2 = 333 where col1 = 't3'"},
  {"stmt" : "insert into container1(col1, col2) values('t4', 4)"}
]
```

**Response code**

| Code | Description                                 |
| ---- | ------------------------------------------- |
| 200  | Success                                     |
| 400  | Incorrect request data                      |
| 401  | An authentication error, a connection error |
| 500  | An error occurs in Web API or GridDB        |

**Response body**

| Item         | Description                                                  | JSON data type |
| ------------ | ------------------------------------------------------------ | -------------- |
| /status      | Status of an SQL UPDATE statement. `1` and `0` denote success and failure, respectively. Even if a result contains `0`, the HTTP response status code is 200. | Number         |
| /message     | Error message displayed when executing the query | String         |
| /updatedRows | Number of updated or inserted rows                        | Number         |
| /stmt        | SQL UPDATE statement                                         | String         |

Example:

```
[ 
  {
    "status" : 1,
    "updatedRows" : 2,
    "stmt" : "update container1 set col2 = 333 where col1 = 't3'",
    "message": null 
  },
  {
    "status" : 0,
    "updatedRows" : 0,
    "stmt" : "insert into container1(col1, col2) values('t4', 4)" ,
    "message": "[240001:SQL_COMPILE_SYNTAX_ERROR] Specified insert column='col1' is not found on updating (sql=\"insert into container1(col1, col2) values('t4', 4)\") (db='public') (user='admin') (clientId='1dafa133-df4-43cb-85b3-3b17593d298c:2') (clientNd='{clientId=1450, address=10.116.41.133:58632}') (address=10.116.227.26:20001, partitionId=557)" 
  } 
]
```

Checking the operation
--------

To check the operation of the Web API, use the curl command in Linux or some other command.

- Example) Checking database connection

  ``` example
  curl -f -X GET -u "user:password" \
  http://host:port/griddb/v2/cluster/dbs/public/checkConnection
  ```

- Example) Retrieving a container list

  ``` example
  curl -f -X GET -u "user:password" \
  http://host:port/griddb/v2/cluster/dbs/public/containers?limit=100
  ```

- Example) Row acquisition

  ``` shell
  curl -f -X POST -u "user:password" \
  -H "Content-type:application/json; charset=UTF-8" -d '{"limit":5,"sort":"id asc"}' \
  http://host:port/griddb/v2/cluster/dbs/public/containers/test/rows
  ```

- Example) Row registration

  ``` shell
  curl -f -X PUT -u "user:password" \
  -H "Content-type:application/json; charset=UTF-8" -d '[[1,"value"],[2,"value"]]' \
  http://host:port/griddb/v2/cluster/dbs/public/containers/test/rows
  ```

- Example) SQL SELECT execution

  ``` shell
  curl -f -X POST -u "user:password" \
  -H "Content-type:application/json; charset=UTF-8" -d '[{"stmt":"select * from test"}]' \
  http://host:port/griddb/v2/cluster/dbs/public/sql/select
  ```

