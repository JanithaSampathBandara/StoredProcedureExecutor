package com.wso2.sample;

import org.apache.axis2.AxisFault;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.micro.integrator.security.user.core.util.DatabaseUtil;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;

public class StoredProcedureExecutor extends AbstractMediator{

    // jndiName and storedProcedure values are getting assigned via the Getter and Setter methods defined below.
    private String jndiName = "";
    private String storedProcedure = "";
    public StoredProcedureExecutor(){}

    @Override
    public boolean mediate(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext)messageContext).getAxis2MessageContext();

        // SQL connection variables
        Connection connection = null;
        CallableStatement callableStatement = null;
        ResultSet resultSet = null;

        try {

            // Retrieving Datasource configurations using JNDI name of the Datasource (JNDI name can be configured when creating the Datasource. eg: via Integration Studio)
            Object jndiObject;
            jndiObject = new InitialContext().lookup(jndiName);
            DataSource externalDatasource = (DataSource) jndiObject;
            connection = DatabaseUtil.getDBConnection(externalDatasource);

            // Creating a CallableStatement object for calling the Stored Procedure.
            callableStatement = connection.prepareCall(storedProcedure);

            // true if the first result is a ResultSet object; false if the first result is an update count or there is no result.
            boolean results = callableStatement.execute();
            JSONObject jsonObject = new JSONObject();
            int count = 1;

                do {
                    if (results) {
                        // Retrieve the first ResultSet
                        resultSet = callableStatement.getResultSet();
                        JSONArray jsonArray = new JSONArray();

                        // Iterate through the result set and create separate JSON objects per ROW
                        if (resultSet != null) {
                            while (resultSet.next()) {
                                ResultSetMetaData metaData = resultSet.getMetaData();
                                int totalColumns = metaData.getColumnCount();
                                JSONObject obj = new JSONObject();
                                for (int i = 0; i < totalColumns; i++) {
                                    String colName = metaData.getColumnLabel(i + 1).toLowerCase();
                                    Object colValue = resultSet.getObject(i + 1);
                                    obj.put(colName, colValue);
                                }
                                // Store separately created JSON objects into a JSON Array
                                jsonArray.put(obj);
                            }
                        }
                        // Store the whole JSON Array into the final JSON object
                        jsonObject.put("resultset" + count, jsonArray);
                        count++;
                    }
                    // Check whether any other result sets are available.
                    // If so, do the same as above. (Iterate through the result set, create array of JSON objects for all the rows and store into the final JSON object).
                    results = callableStatement.getMoreResults();

                } while (results);

           // Builds and returns a new JSON payload for a message context with a stream of JSON content.
           // This is the recommended way to build a JSON payload into an Axis2 message context.
           JsonUtil.getNewJsonPayload(axis2MessageContext,jsonObject.toString(),  true, true);
           return true;

        } catch (NamingException e) {
            log.error("Error while getting datasource configurations", e);
            e.printStackTrace();
        } catch (SQLException e) {
            log.error("Error while accessing DB connection", e);
            e.printStackTrace();
        } catch (AxisFault e) {
            log.error("Cannot parse stream. MessageID: " + messageContext.getMessageID() + ". Error>>> " + e.getLocalizedMessage(), e);
            e.printStackTrace();
        } finally {
            DatabaseUtil.closeAllConnections(connection, resultSet, callableStatement);
        }
        return false;
    }

    // Getter and Setter methods to retrieve and assign jndiName and storedProcedure values from the properties defined in the Class Mediator. (Check the sample API sequence)
    public void setJndiName(String jndiName){
        this.jndiName = jndiName;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setStoredProcedure(String storedProcedure){
        this.storedProcedure = storedProcedure;
    }

    public String getStoredProcedure() {
        return storedProcedure;
    }

}
