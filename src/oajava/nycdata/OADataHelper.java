package oajava.nycdata;

import javafx.scene.control.Tab;
import oajava.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;





/**
 * Created by sbobba on 8/25/2017.
 */
public class OADataHelper {

    private String baseURL = null;
    private TableInfo tableInfo = null;
    RESTHelper restHelper = null;

    public OADataHelper(String baseURL, TableInfo tableInfo)
    {
        this.baseURL = baseURL;
        this.tableInfo = tableInfo;
        restHelper = new RESTHelper();
    }

    int fetchAll_OR_TOP_N(long dam_hstmt, int iStmtType, xo_long piNumResRows, HashMap<String, Long> columnHandleMap, int topRows, String schema, String table, long m_tmHandle){
        int retCode = -9;



        if (fetchAndAddDataToOpenAccess(dam_hstmt, iStmtType, piNumResRows, columnHandleMap, table, m_tmHandle, baseURL))
            return ip.DAM_FAILURE;

        return retCode;
    }


    //Push Down filters to REST API
    int fetchFiltered(long dam_hstmt, int iStmtType, xo_long piNumResRows, HashMap<String, Long> columnHandleMap, int topRows, String schema, String table, long m_tmHandle, OAConditionProcessor oaConditionProcessor)
    {

        StringBuilder whereCondition =  new StringBuilder();
        HashMap<String, Object> eqlConditionsInUse = oaConditionProcessor.getEqlConditionsInUse();
        HashMap<String, Object> grtConditionsInUse = oaConditionProcessor.getGrtConditionsInUse();

        whereCondition.append("?$where=");
        int conditionCount = 0;


        //Scan if there are any equal conditions, and build your URL according to it, to Push down the filters
        if(eqlConditionsInUse.size() > 0)
        {

            for(String column: eqlConditionsInUse.keySet())
            {
                if(conditionCount != 0)
                {
                    whereCondition.append(" and ");
                }
                whereCondition = whereCondition.append(""+ column.toLowerCase() +"=\"" + eqlConditionsInUse.get(column).toString() + "\"");
                conditionCount++;
            }
        }

        //Scan if there are any Greater than conditions, and build your URL according to it, to Push down the filters
        if(grtConditionsInUse.size() > 0)
        {
            for(String column: grtConditionsInUse.keySet()) {
                if (conditionCount != 0) {
                    whereCondition.append(" and ");
                }

                whereCondition = whereCondition.append(""+ column.toLowerCase() +">\"" + grtConditionsInUse.get(column).toString() + "\"");
                conditionCount++;
            }
        }

        String url = baseURL + whereCondition.toString();

        //Get The data from REST API and Feed it to OpenAccess
        fetchAndAddDataToOpenAccess(dam_hstmt, iStmtType, piNumResRows, columnHandleMap, table, m_tmHandle, url);

        return ip.DAM_SUCCESS;
    }


    private boolean fetchAndAddDataToOpenAccess(long dam_hstmt, int iStmtType, xo_long piNumResRows, HashMap<String, Long> columnHandleMap, String table, long m_tmHandle, String url) {
        ArrayList<String> responseData = null;
        ArrayList<HashMap<String, String>> rowData = new ArrayList<>();

        //Get Data from REST API by making the GET call
        try {
            responseData = restHelper.getResponseFromAPI(url, m_tmHandle);
        }
        catch (MalformedURLException ex)
        {
            return true;
        }
        catch (Exception ex)
        {
            return true;
        }

        if(!responseData.get(0).equals("200"))
        {
            return true;
        }


        //Parse the data - Each row gets added to a unique HashMap where key is column name and value is column value. each of these HashMap is added to
        JSONArray jsonResponse = new JSONArray(responseData.get(1));
        for(int i= 0; i < jsonResponse.length(); i++)
        {
            JSONObject singleRowJSON = jsonResponse.getJSONObject(i);
            HashMap<String, String > singlerowmap = new HashMap<>();
            for(String columnName: columnHandleMap.keySet())
            {
                if(singleRowJSON.has(columnName.toLowerCase()))
                {
                    singlerowmap.put(columnName, singleRowJSON.getString(columnName.toLowerCase()));
                }else
                {
                    singlerowmap.put(columnName, null);
                }
            }

            rowData.add(singlerowmap);
        }

        feedDatatoOpenAccess(dam_hstmt, iStmtType, piNumResRows, columnHandleMap, table, rowData);
        return false;
    }



    boolean feedDatatoOpenAccess(long dam_hstmt, int iStmtType, xo_long piNumResRows, HashMap<String, Long> columnHandleMap, String requestedTable, ArrayList<HashMap<String, String>> rowData) {

        int retCode = 0;
        int num_rows = 0;
        HashMap<String, Integer> columnMeta = tableInfo.getColumnsinTable(requestedTable);

        for (int i = 0; i < rowData.size(); i++) {
            //Allocate memory for new row.
            //This method allocates a new row and returns its handle. The OpenAccess SDK SQL Engine allocates the memory required by the row
            long hrow_loop = 0;
            hrow_loop = jdam.dam_allocRow(dam_hstmt);

            HashMap<String, String> row = rowData.get(i);
            for (String columnName : columnMeta.keySet())
            {
                long columnHandle = columnHandleMap.get(columnName.toUpperCase());
                String data = row.get(columnName.toUpperCase());

                if (data == "null" || data == "" || data == null) {
                    data = null;
                }

                //Add data to Row based on the data type of column we are dealing using the column handles that we got earlier.
                //VARCHAR
                if (columnMeta.get(columnName) == 12 ) {
                    if (!(data == null)) {
                        retCode = jdam.dam_addCharValToRow(dam_hstmt, hrow_loop, columnHandle, data, ip.XO_NTS);
                    } else {
                        retCode = jdam.dam_addCharValToRow(dam_hstmt, hrow_loop, columnHandle, null, ip.XO_NULL_DATA);
                    }
                    //BIG INT
                }
                else if (columnMeta.get(columnName) == -5) {

                    retCode = jdam.dam_addBigIntValToRow(dam_hstmt, hrow_loop, columnHandle, Long.parseLong(data), (data != null) ? ip.XO_NTS : ip.XO_NULL_DATA);
                    //TIME STAMP
                }
                else if(columnMeta.get(columnName) == 93)
                {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS");
                    Date parseDate = null;
                    try {
                        parseDate = dateFormat.parse(data);
                    }
                    catch (Exception ex)
                    {
                        System.out.println("Exception :" + ex.getMessage());
                    }

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(parseDate);

                    xo_tm xo_timestamp = new xo_tm(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), calendar.get(Calendar.MILLISECOND));
                    retCode = jdam.dam_addTimeStampValToRow(dam_hstmt, hrow_loop, columnHandle, xo_timestamp, (data != null) ? ip.XO_NTS : ip.XO_NULL_DATA);
                }

                if(retCode == ip.DAM_FAILURE)
                {
                    return false;
                }

            }

            //Check if the Row that you are trying to add meets the SQL Query requirements - You shouldn't have any unwanted data at this point if you have made proper GET request in general.
            //This function can help you determine if the row meets query requirements and discards the row if it doesn't
            if(jdam.dam_isTargetRow(dam_hstmt, hrow_loop) == ip.DAM_TRUE)
            {

                //Add Row to the resultset and you are done. OpenAccess takes care of the rest.
                retCode =jdam.dam_addRowToTable(dam_hstmt, hrow_loop);
                if(retCode == ip.DAM_FAILURE)
                {
                    return false;
                }
                num_rows ++;
            }
        }

        piNumResRows.setVal(num_rows);
        return true;
    }



}
