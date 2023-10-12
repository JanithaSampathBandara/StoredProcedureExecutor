# StoredProcedureExecutor for WSO2 Micro Integrator/Enterprise Integrator

This is a sample custom Class Mediator implementation which can be used to read external Datasource configurations and execute MYSQL Stored Procedures which returns multiple Resultset objects. 

## How it works.

* We should provide the JNDI **name of the Datasource** and the **name of the Stored Procedure** to be executed via Class Mediator properties [1].
* Then this will read the external Datasource configurations using its JNDI name and execute the Stored Procedure based on the name which is provided as a Class Mediator property.
* Finally, this will aggregate the multiple result sets into a single JSON object and build the JSON payload into Axis2 Message Context.

## Steps to engage with the Class Mediator.

1. Clone the project and build using **"mvn clean install"**
2. Place the built .jar file into **MI_HOME/lib** directory.
3. Download the JDBC Driver and copy the JAR (mysql-connector-java-x.x.xx-bin.jar) into **MI_HOME/lib** directory.
4. Deploy an external Datasource [2] into Micro Integrator runtime. <br />
**eg :** 
>     <datasource>
>       <name>MySQLConnection</name>
>       <description>MySQL Connection</description>
>       <jndiConfig useDataSourceFactory="false">
>         <name>MysqlConJNDI1</name>
>       </jndiConfig>
>      <definition type="RDBMS">
>         <configuration>
>           <driverClassName>com.mysql.jdbc.Driver</driverClassName>
>           <url>jdbc:mysql://localhost:3306/<DatabaseName></url>
>           <username><DBUsrname></username>
>           <password><DBPassword></password>
>         </configuration>
>      </definition>
>     </datasource>

5. Create a sample mediation sequence which includes a Class Mediator to engage the implementation and deploy it into the Micro Integrator runtime.
We need to define the fully qualified classname **com.wso2.sample.StoredProcedureExecutor** within the Class Mediator's **name** property.<br />
**eg :** 
>       <?xml version="1.0" encoding="UTF-8"?>
>       <api context="/test" name="TestAPI" xmlns="http://ws.apache.org/ns/synapse">
>        <resource methods="GET" uri-template="/posts/{id}">
>          <inSequence>
>            <class name="com.wso2.sample.StoredProcedureExecutor">
>              <property name="jndiName" value="MysqlConJNDI1"/>
>              <property name="storedProcedure" value="CALL GetTwoResultSets()"/>
>            </class>
>            <log level="full"/>
>            <respond/>
>          </inSequence>
>          <outSequence/>
>          <faultSequence/>
>        </resource>
>       </api>

6. Invoke the API resource : `curl GET 'http://localhost:8290/test/posts/1' -kv`
    
7. Navigate to **[MI_HOME]/repository/logs/wso2carbon.log** file. You will observe the below output which includes the aggregated JSON result.
>     INFO {LogMediator} - {api:TestAPI} To: /test/posts/1, MessageID: urn:uuid:e1d238c4-3881-4261-ab13-b893a7836c36, correlation_id: e1d238c4-3881-  4261-ab13-b893a7836c36, Direction: request, Payload: {"resultset1":[{"eid":1,"ename":"Janitha"},{"eid":2,"ename":"Sampath"},{"eid":3,"ename":"Bandara"}],"resultset2":[{"cname":"WSO2","cid":10},{"cname":"Virtusa","cid":20}]}
<br />
Thanks & Regards,<br />
Janitha Bandara<br />
[1]https://ei.docs.wso2.com/en/latest/micro-integrator/references/mediators/class-Mediator/#syntax
[2]https://ei.docs.wso2.com/en/latest/micro-integrator/develop/creating-artifacts/data-services/creating-datasources/

## References
1. https://learn.microsoft.com/en-us/sql/connect/jdbc/handling-complex-statements?view=sql-server-ver16
2. https://medium.com/think-integration/developing-and-debugging-custom-mediators-using-wso2-integration-studio-a3d974d1a2cf
3. https://athiththan11.medium.com/custom-datasource-class-mediators-607a60e6d6da
4. https://github.com/wso2-support/micro-integrator/tree/v4.1.0.86
5. https://www.baeldung.com/java-jdbc-convert-resultset-to-json
6. https://gist.github.com/marlonlom/5310a52bf09a46c345d9
7. https://github.com/wso2/wso2-synapse/blob/master/modules/commons/src/main/java/org/apache/synapse/commons/json/JsonUtil.java
8. https://ei.docs.wso2.com/en/latest/micro-integrator/references/mediators/class-Mediator/
