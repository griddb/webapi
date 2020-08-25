GridDB WebAPI

## Overview

GridDB WebAPI is developed using GridDB Java Client and [Spring Boot](https://spring.io/projects/spring-boot).  

## Operating environment

Building of the library and execution of the sample programs have been checked in the following environment.
- OS: CentOS 7.5(x64)
- [GridDB Server](https://github.com/griddb/griddb): 4.5

## QuickStart

### Build and Run 
        
1. Execute the command on project directory.

        $ cd webapi-ce
        $ ./gradlew build

    The following jar file is created.
    - webapi-ce/build/libs/griddb-webapi-ce-X.X.X.jar

2. Set the GS_WEBAPI_HOME variable for GridDB WebAPI home directory .

        $ export GS_WEBAPI_HOME=$PWD/..

    Default is /var/lib/gridstore/webapi.

3. Edit parameter files

        Edit the following files on the ${GS_WEBAPI_HOME}/conf directory
        - repository.json
        - griddb_webapi.properties (if necessary)

    Please refer to [Parameter](Parameters.md).

4. Run with Jar

        java -jar ./build/libs/griddb-webapi-ce-X.X.X.jar

### Execution Example

GridDB Server need to be started in advance.

1. Create a container

        #Request  http://[host]:[port]/griddb/v2/[clusterName]/dbs/public/containers 
        $ curl -X POST --basic -u admin:admin -H "Content-type:application/json" -d 
        '{"container_name":"test", "container_type":"COLLECTION", "rowkey":true, 
        "columns":[{"name":"col1", "type":"STRING", "index":["TREE"]}, 
          {"name":"col2", "type":"INTEGER"}, {"name":"col3", "type":"BOOL"}]}' 
        http://127.0.0.1:8010/griddb/v2/mycluster/dbs/public/containers

2. Append a row data

        #Request  http://[host]:[port]/griddb/v2/[clusterName]/dbs/public/containers/[containerName]/rows 
        $ curl -X PUT --basic -u admin:admin -H "Content-type:application/json" -d 
        '[["value", 1, true]]' 
        http://127.0.0.1:8010/griddb/v2/mycluster/dbs/public/containers/test/rows 

3. Get a row data

        #Request  http://[host]:[port]/griddb/v2/[clusterName]/dbs/public/containers/[containerName]/rows 
        $ curl -X POST --basic -u admin:admin -H "Content-type:application/json" -d 
        '{"limit":1000}'
        http://127.0.0.1:8010/griddb/v2/mycluster/dbs/public/containers/test/rows 
        --> {"columns":[{"name":"col1","type":"STRING"},{"name":"col2","type":"INTEGER"}, 
              {"name":"col3","type":"BOOL"}],"rows":[["value",1,true]],"offset":0,"limit":1000,"total":1}

4. Query with TQL

        #Request  http://[host]:[port]/griddb/v2/[clusterName]/dbs/public/tql 
        curl -X POST --basic -u admin:admin -H "Content-type:application/json" -d 
        '[{"name":"test", "stmt":"select *", "columns":[]}]' 
        http://127.0.0.1:8010/griddb/v2/mycluster/dbs/public/tql 
        --> [{"columns":[{"name":"col1","type":"STRING"},{"name":"col2","type":"INTEGER"}, 
               {"name":"col3","type":"BOOL"}],"results":[["value",1,true]],"offset":0,"limit":1000000,"total":1}]

5. Query with SQL

        #Request  http://[host]:[port]/griddb/v2/[clusterName]/dbs/public/sql 
        curl -X POST -u admin:admin -H "Content-type:application/json; charset=UTF-8" 
        -d '[{"type":"sql-select","stmt":"select * from test"}]' 
        http://127.0.0.1:8010/griddb/v2/mycluster/dbs/public/sql 
        --> [{"columns":[{"name":"col1","type":"STRING"},{"name":"col2","type":"INTEGER"}, 
               {"name":"col3","type":"BOOL"}],"results":[["value",1,true]]}]

Please refer to [WebAPI Specification](WebAPISpecification.md).

## Community

  * Issues  
    Use the GitHub issue function if you have any requests, questions, or bug reports. 
  * PullRequest  
    Use the GitHub pull request function if you want to contribute code.
    You'll need to agree GridDB Contributor License Agreement(CLA_rev1.1.pdf).
    By using the GitHub pull request function, you shall be deemed to have agreed to GridDB Contributor License Agreement.

## License
  
  GridDB WebAPI source license is Apache License, version 2.0.
