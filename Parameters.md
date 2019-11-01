# Parameters

## Repository file

File path : ${GS_WEBAPI_HOME}/conf/repository.json

|Name|Type|Description|Value|
|---|---|---|---|
|name|string|cluster name||
|mode|string|connection type|MULTICAST,FIXED_LIST or PROVIDER|
|address|string|(mode=MULTICAST) multicast address||
|port|number|(mode=MULTICAST)multicast port||
|transactionMember|string|(mode=FIXED_LIST) fixed list of GridDB nodes||
|providerUrl|string|(mode=PROVIDER) provider URL||

## Web API property file

File path : ${GS_WEBAPI_HOME}/conf/griddb_webapi.properties

|Name|Description|Default|
|---|---|---|
|faioverTimeout|Failover timeout seconds to retry from occurring node failure in access from Web API to GridDB|5|
|transactionTimeout|Timeout seconds of transactions|30|
|containerCasheSize|Number of cache of container information|100|
|maxResponseSize|Upper limit [MB] of a response size|20|
|maxRequestSize|Upper limit [MB] of a request size|20|
|maxQueryNum|Max number of tql in each API request|10|
|maxLimit|Upper bound and default value of limit|1000000|

