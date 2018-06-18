/* OpenAccessIP.java
 *
 * Copyright (c) MIT License 1995-2017 Progress Software Corporation. All Rights Reserved.
 *
 *
 * Description:    OpenAccess IP where you would have to implement various inherited methods from the 
 * interface oajava.sql.ip to build your very own Custom ODBC/JDBC driver
 */

package oajava.nycdata;

import oajava.sql.*;
import org.apache.commons.lang.time.DateUtils;

import java.net.MalformedURLException;
import java.util.*;

/* define the class template to implement the sample IP */
public class OpenAccessIP implements oajava.sql.ip
{
    private long m_tmHandle = 0;

    private RESTHelper restHelper = null;
    private OA_IPHelper oa_ipHelper = null;
    private TableInfo tableInfo = null;
    private OADataHelper oaDataHelper = null;

    private String baseURL = null;
    private String resourceUniqueId = null;

    final static String OA_CATALOG_NAME   = "NYCPARKING";        /* SCHEMA */
    final static String OA_USER_NAME      = "OAUSER";        /* OAUSER */

    /* Support array */
    private final int[]   ip_support_array =
                    {
                        0,
                        1, /* IP_SUPPORT_SELECT */
                        0, /* IP_SUPPORT_INSERT */
                        0, /* IP_SUPPORT_UPDATE */
                        0, /* IP_SUPPORT_DELETE */
                        1, /* IP_SUPPORT_SCHEMA - IP supports Schema Functions */
                        0, /* IP_SUPPORT_PRIVILEGES  */
                        1, /* IP_SUPPORT_OP_EQUAL */
                        0, /* IP_SUPPORT_OP_NOT   */
                        1, /* IP_SUPPORT_OP_GREATER */
                        0, /* IP_SUPPORT_OP_SMALLER */
                        0, /* IP_SUPPORT_OP_BETWEEN */
                        0, /* IP_SUPPORT_OP_LIKE    */
                        0, /* IP_SUPPORT_OP_NULL    */
                        0, /* IP_SUPPORT_SELECT_FOR_UPDATE */
                        0, /* IP_SUPPORT_START_QUERY */
                        0, /* IP_SUPPORT_END_QUERY */
                        0, /* IP_SUPPORT_UNION_CONDLIST */
                        0, /* IP_SUPPORT_CREATE_TABLE */
                        0, /* IP_SUPPORT_DROP_TABLE */
                        0, /* IP_SUPPORT_CREATE_INDEX */
                        0, /* IP_SUPPORT_DROP_INDEX */
                        0, /* IP_SUPPORT_PROCEDURE */
                        0, /* IP_SUPPORT_CREATE_VIEW */
                        0, /* IP_SUPPORT_DROP_VIEW */
                        0, /* IP_SUPPORT_QUERY_VIEW */
                        0, /* IP_SUPPORT_CREATE_USER */
                        0, /* IP_SUPPORT_DROP_USER */
                        0, /* IP_SUPPORT_CREATE_ROLE */
                        0, /* IP_SUPPORT_DROP_ROLE */
                        0, /* IP_SUPPORT_GRANT */
                        0, /* IP_SUPPORT_REVOKE */
                        0,  /* IP_SUPPORT_PASSTHROUGH_QUERY */
                        0,  /* IP_SUPPORT_NATIVE_COMMAND */
                        0,  /* IP_SUPPORT_ALTER_TABLE */
                        0,  /* IP_SUPPORT_BLOCK_JOIN */
                        0,  /* IP_SUPPORT_XA */
                        0,  /* IP_SUPPORT_QUERY_MODE_SELECTION */
                        0,  /* IP_SUPPORT_VALIDATE_SCHEMAOBJECTS_IN_USE */
                        1,  /* IP_SUPPORT_UNICODE_INFO */
                        0,  /* Reserved for future use */
                        0,  /* Reserved for future use */
                        0,  /* Reserved for future use */
                        0,  /* Reserved for future use */
                        0,  /* Reserved for future use */
                        0,  /* Reserved for future use */
                        0,  /* Reserved for future use */
                        0,  /* Reserved for future use */
                        0,  /* Reserved for future use */
                        0   /* Reserved for future use */
                    };


    public OpenAccessIP()
    {
        restHelper = new RESTHelper();
        oa_ipHelper = new OA_IPHelper();
        tableInfo = new TableInfo();

	}

    public String ipGetInfo(int iInfoType)
    {
		String str = null;
        jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipGetInfo called\n");
        switch (iInfoType) {
            case ip.IP_INFO_QUALIFIER_NAMEW:
                str = "NYCPARKING";
                break;


            case ip.IP_INFO_SUPPORT_VALUE_FOR_RESULT_ALIAS:
            case ip.IP_INFO_VALIDATE_TABLE_WITH_OWNER:
                str = "0"; /* false */
                break;

            case ip.IP_INFO_FILTER_VIEWS_WITH_QUALIFIER_NAME:
            case ip.IP_INFO_CONVERT_NUMERIC_VAL:
            case ip.IP_INFO_TABLE_ROWSET_REPORT_MEMSIZE_LIMIT:
                str = "1"; /* true */
                break;

            default:
                break;
        }
        return str;
   }

    public int ipSetInfo(int iInfoType,String InfoVal)
    {
        jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipSetInfo called\n");
        return IP_SUCCESS;
    }


    public oa_types_info[] ipGetTypesInfo()
    {
        /* types information */
        jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipGetTypesInfo called\n");
        return oa_ipHelper.getTypesSupported();
    }


    public int ipGetSupport(int iSupportType)
    {
        jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipGetSupport called\n");
        return(ip_support_array[iSupportType]);
    }

   /*ipConnect is called immediately after an instance of this object is created. You should
    *perform any tasks related to connecting to your data source */
    public int ipConnect(long tmHandle,long dam_hdbc,String sDataSourceName, String sUserName, String sPassword,
						String sCurrentCatalog, String sIPProperties, String sIPCustomProperties)
    {
            /* Save the trace handle */
        m_tmHandle = tmHandle;
        jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipConnect called\n");


        //Read baseURL from Custom Properties set in ODBC/JDBC config, if its not set return failure
        String[] customProperties = sIPCustomProperties.split("=");
        if(customProperties[0].equalsIgnoreCase("baseurl"))
        {
            baseURL = customProperties[1];
        }
        else
        {
            return IP_FAILURE;
        }

        //Extract Resource Unique ID for later steps
        int lastindex =  baseURL.lastIndexOf("/");
        int lastindex2 = baseURL.lastIndexOf(".");
        resourceUniqueId = baseURL.substring(lastindex + 1, lastindex2);

        //check the connection to API endpoint, if unsuccessful, return failure
        try {
            ArrayList<String> response = restHelper.getResponseFromAPI(baseURL, m_tmHandle);

            if(!response.get(0).equalsIgnoreCase("200"))
            {
                return IP_FAILURE;
            }
        }
        catch (MalformedURLException ex)
        {
            return IP_FAILURE;
        }
        catch (java.lang.Exception ex)
        {
            return IP_FAILURE;
        }




        /* Code to connect to your data source source. */
        return IP_SUCCESS;
    }

    public int ipDisconnect(long dam_hdbc)
    {   /* disconnect from the data source */
            jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipDisonnect called\n");
            return IP_SUCCESS;
    }

    public int ipStartTransaction(long dam_hdbc)
    {
            /* start a new transaction */
            jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipStartTransaction called\n");
            return IP_SUCCESS;
    }

    public int ipEndTransaction(long dam_hdbc,int iType)
    {
            /* end the transaction */
            jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipEndTransaction called\n");
            if (iType == DAM_COMMIT)
            {
            }
            else if (iType == DAM_ROLLBACK)
            {
            }
            return IP_SUCCESS;
    }

    public int ipExecute(long dam_hstmt, int iStmtType, long hSearchCol,xo_long piNumResRows)
    {
        int fetchSize = 0;
        int topRows = 0;
        xo_int  piValue = new xo_int();
        jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipExecute called\n");
        StringBuffer schema = new StringBuffer();
        StringBuffer table = new StringBuffer();
        HashMap<String, Long> columnHandleMap = new HashMap<>();


        //
        if(iStmtType == DAM_SELECT)
        {
            //Get the Table requested in SQL Query
            jdam.dam_describeTable(dam_hstmt, null, schema, table, null, null);
            OAConditionProcessor oaConditionProcessor = new OAConditionProcessor();
            String tablename = table.toString().toUpperCase();
            oaDataHelper = new OADataHelper(baseURL, tableInfo);

            //Get all the column Handles for all the columns in table
            HashMap<String, Integer> columnsinTable = tableInfo.getColumnsinTable(tablename);
            for(String column : columnsinTable.keySet())
            {
                long columnHandle = jdam.dam_getCol(dam_hstmt, column.toUpperCase());
                columnHandleMap.put(column.toUpperCase(), columnHandle);
            }

            //Set Fetch Block Size
            int retCode = jdam.dam_getInfo(0, dam_hstmt, DAM_INFO_FETCH_BLOCK_SIZE, null, piValue);
            if(retCode != DAM_SUCCESS)
            {
                fetchSize = 1000;
            }
            else
            {
                fetchSize =piValue.getVal();
            }


            //Check for TOP N clause in SELECT statement
            piValue.setVal(0);
            retCode = jdam.dam_getInfo(0, dam_hstmt, oajava.sql.ip.DAM_INFO_QUERY_TOP_ROWS, null, piValue);
            if (retCode == DAM_FAILURE)
                return retCode;
            topRows = piValue.getVal();


            //Process Conditions
            oaConditionProcessor.processSQLforConditions(dam_hstmt);
            //Populate equal conditions
            oaConditionProcessor.populateEqualConditions();
            //Populate greater than conditions
            oaConditionProcessor.populateGreaterThanConditions();
            //Populate less than conditions
            oaConditionProcessor.populateLessThanConditions();
            //Populate like conditions
            oaConditionProcessor.populateLikeConditions();

            //Read Conditions in WHERE Clause
            //If There are no conditions, Fetch all rows or Top N rows
            if(oaConditionProcessor.getListOfConditions().size() == 0)
            {
                jdam.trace(m_tmHandle, UL_TM_F_TRACE, "Fetching TOP N rows or all rows");
                retCode = oaDataHelper.fetchAll_OR_TOP_N(dam_hstmt, iStmtType, piNumResRows, columnHandleMap, topRows, schema.toString(), table.toString().toUpperCase(), m_tmHandle);
                if(retCode == DAM_FAILURE)
                {
                    return IP_FAILURE;
                }
            } else
            {
                jdam.trace(m_tmHandle, UL_TM_F_TRACE, "Fetching filtered records");
                retCode = oaDataHelper.fetchFiltered(dam_hstmt, iStmtType, piNumResRows, columnHandleMap, topRows, schema.toString(), table.toString().toUpperCase(), m_tmHandle, oaConditionProcessor);
                if(retCode == DAM_FAILURE)
                {
                    return IP_SUCCESS;
                }
            }

        }

        return IP_SUCCESS;
    }


    public int ipSchema(long dam_hdbc,long pMemTree,int iType, long pList, Object pSearchObj)
    {
			jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipSchema called\n");
			switch(iType)
			{
			case DAMOBJ_TYPE_CATALOG:
				{
					schemaobj_table TableObj = new schemaobj_table(OA_CATALOG_NAME,null,null,null,null,null,null,null);

					jdam.dam_add_schemaobj(pMemTree,iType,pList,pSearchObj,TableObj);
				}
				break;
			case DAMOBJ_TYPE_SCHEMA:
				{
					schemaobj_table TableObj = new schemaobj_table();

					TableObj.SetObjInfo(null,"SYSTEM",null,null,null,null,null,null);
					jdam.dam_add_schemaobj(pMemTree,iType,pList,pSearchObj,TableObj);

					TableObj.SetObjInfo(null,OA_USER_NAME,null,null,null,null,null,null);
					jdam.dam_add_schemaobj(pMemTree,iType,pList,pSearchObj,TableObj);
				}
				break;
			case DAMOBJ_TYPE_TABLETYPE:
				{
					schemaobj_table TableObj = new schemaobj_table();

					TableObj.SetObjInfo(null,null,null,"SYSTEM TABLE",null,null,null,null);
					jdam.dam_add_schemaobj(pMemTree,iType,pList,pSearchObj,TableObj);

					TableObj.SetObjInfo(null,null,null,"TABLE",null,null,null,null);
					jdam.dam_add_schemaobj(pMemTree,iType,pList,pSearchObj,TableObj);

					TableObj.SetObjInfo(null,null,null,"VIEW",null,null,null,null);
					jdam.dam_add_schemaobj(pMemTree,iType,pList,pSearchObj,TableObj);
				}

				break;

            //Feed OpenAccess the information about the tables available when they connect to this driver.
			case DAMOBJ_TYPE_TABLE:
				{
					schemaobj_table  pTableSearchObj = (schemaobj_table) pSearchObj;

                    //If user is querying on a table, then pTableSearchObj will not be null. You would have to check if the table indeed exists and send the info about table.
					if (pTableSearchObj != null && pTableSearchObj.getTableName() != null)
					{
                        jdam.trace(m_tmHandle, UL_TM_MAJOR_EV, "Dynamic Schema  of table:<"+pTableSearchObj.getTableQualifier()+"."+pTableSearchObj.getTableOwner()+"."+pTableSearchObj.getTableName()+"> is being requested\n");
                        if(pTableSearchObj.getTableName().equalsIgnoreCase("NYCOPENDATA")) {
                            schemaobj_table TableObj = new schemaobj_table();
                            TableObj.SetObjInfo(OA_CATALOG_NAME, OA_USER_NAME, "NYCOPENDATA", "TABLE", null, null, "0x0F", null);
                            tableInfo.addnewTable("NYCOPENDATA");
                            jdam.dam_add_schemaobj(pMemTree, iType, pList, pSearchObj, TableObj);
                        }

					}
                    //Else Add all tables
                    //As we are dealing with only one table here, the code is same for these two conditions in this case.
					else
					{
                        schemaobj_table TableObj = new schemaobj_table();
                        TableObj.SetObjInfo(OA_CATALOG_NAME, OA_USER_NAME, "NYCOPENDATA", "TABLE", null, null, "0x0F", null);
                        jdam.dam_add_schemaobj(pMemTree, iType, pList, pSearchObj, TableObj);
                        tableInfo.addnewTable("NYCOPENDATA");
						jdam.trace(m_tmHandle, UL_TM_MAJOR_EV, "Dynamic Schema for all tables is being requested\n");

					}
				}
				break;
			case DAMOBJ_TYPE_COLUMN:
				{
					schemaobj_column pColSearchObj = (schemaobj_column) pSearchObj;
                    //If user is querying on a table, then pTableSearchObj will not be null. If its not null you would have to send the columns for the table requested else you need to send all columns for all tables
                    //In the example, we have only 1 table, so the code will be the same for both the conditions
					if (pColSearchObj != null && pColSearchObj.getTableName() != null)
					{
                        HashMap<String, Integer> columnMeta = null;
                        if(tableInfo.getColumnsinTable(pColSearchObj.getTableName().toUpperCase()) == null ) {
                            columnMeta = restHelper.getMetadata(resourceUniqueId, m_tmHandle);
                            tableInfo.addcolumnstoTable(pColSearchObj.getTableName().toUpperCase(), columnMeta);
                        }else
                        {
                            columnMeta = tableInfo.getColumnsinTable(pColSearchObj.getTableName().toUpperCase());
                        }
                        for(Map.Entry<String, Integer> currentcolumn: columnMeta.entrySet())
                        {
                            schemaobj_column oa_column = oa_ipHelper.add_column_meta(currentcolumn, OA_CATALOG_NAME, OA_USER_NAME);
                            jdam.dam_add_schemaobj(pMemTree, iType, pList, pSearchObj, oa_column);
                        }

						jdam.trace(m_tmHandle, UL_TM_MAJOR_EV, "Dynamic Schema for column <"+pColSearchObj.getColumnName()+"> of table:<"+pColSearchObj.getTableQualifier()+"."+pColSearchObj.getTableOwner()+"."+pColSearchObj.getTableName()+"> is being requested\n");
					}
					else
					{
                        HashMap<String, Integer> columnMeta = null;
                        if( tableInfo.getColumnsinTable(pColSearchObj.getTableName().toUpperCase()) == null ) {
                            columnMeta = restHelper.getMetadata(resourceUniqueId, m_tmHandle);
                            tableInfo.addcolumnstoTable(pColSearchObj.getTableName().toUpperCase(), columnMeta);
                        }else
                        {
                            columnMeta = tableInfo.getColumnsinTable(pColSearchObj.getTableName().toUpperCase());
                        }
                        for(Map.Entry<String, Integer> currentcolumn: columnMeta.entrySet())
                        {
                            schemaobj_column oa_column =oa_ipHelper.add_column_meta(currentcolumn, OA_CATALOG_NAME, OA_USER_NAME);
                            jdam.dam_add_schemaobj(pMemTree, iType, pList, pSearchObj, oa_column);
                        }
						jdam.trace(m_tmHandle, UL_TM_MAJOR_EV, "Dynamic Schema for all columns of all tables is being requested\n");
					}
				}
				break;

			case DAMOBJ_TYPE_STAT:
				break;

			case DAMOBJ_TYPE_FKEY:
				break;
			case DAMOBJ_TYPE_PKEY:
				break;
			case DAMOBJ_TYPE_PROC:
				break;
			case DAMOBJ_TYPE_PROC_COLUMN:
				break;
			default:
				break;
			}
			return IP_SUCCESS;
    }

    public int        ipDDL(long dam_hstmt, int iStmtType, xo_long piNumResRows)
    {
			jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipDDL called\n");
            return IP_FAILURE;
    }

    public int        ipProcedure(long dam_hstmt, int iType, xo_long piNumResRows)
    {
			jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipProcedure called\n");
            return IP_FAILURE;
    }

    public int        ipDCL(long dam_hstmt, int iStmtType, xo_long piNumResRows)
    {
			jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipProcedure called\n");
            return IP_FAILURE;
    }

    public int        ipPrivilege(int iStmtType,String pcUserName,String pcCatalog,String pcSchema,String pcObjName)
    {
			jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipPrivilege called\n");
			return IP_FAILURE;
    }

    public int        ipNative(long dam_hstmt, int iCommandOption, String sCommand, xo_long piNumResRows)
    {
			jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipNative called\n");
            return IP_FAILURE;
    }

    public int        ipSchemaEx(long dam_hstmt, long pMemTree, int iType, long pList,Object pSearchObj)
	{
			jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipSchemaEx called\n");
            return IP_FAILURE;
    }

    public int        ipProcedureDynamic(long dam_hstmt, int iType, xo_long piNumResRows)
    {
			jdam.trace(m_tmHandle, UL_TM_F_TRACE,"ipProcedureDynamic called\n");
            return IP_FAILURE;
    }

    //scalar functions decalaration
    public scalar_function[] ipGetScalarFunctions()
    {
        scalar_function[] MyFuncs = new scalar_function[1];
        MyFuncs[0]=new scalar_function("DATETRUNC", 1, "ip_func_datetrunc", XO_TYPE_TIMESTAMP , 2);
        return MyFuncs;
    }

    //scalar function datetrunc implemetation
    public long ip_func_datetrunc(long hstmt, long pMemTree, long hValExpList)
    {
        long hVal;
        long hValExp;
        Date truncatedDate = null;


        xo_int piRetCode = new xo_int(0);
        hValExp = jdam.dam_getFirstValExp(hValExpList);
        long hValExp2 = jdam.dam_getNextValExp(hValExpList);

        String intervalObj = (String)jdam.dam_getValueOfExp(pMemTree, hValExpList, hValExp, XO_TYPE_CHAR, piRetCode);
        if(piRetCode.getVal() != DAM_SUCCESS)
            return 0;
        String dateObj = (String) jdam.dam_getValueOfExp(pMemTree, hValExpList, hValExp2, XO_TYPE_CHAR, piRetCode);
        if(piRetCode.getVal() != DAM_SUCCESS)
            return 0;

        if(dateObj == null || intervalObj == null)
        {
            hVal = jdam.dam_createVal(pMemTree, XO_TYPE_TIMESTAMP, null, XO_NULL_DATA);
            return hVal;
        }


        Date inputDate = null;
        try {
            inputDate = getParsedDate(dateObj);
        }
        catch (Exception ex)
        {
            jdam.dam_addError(0, 0, DAM_IP_ERROR, 0, "Exception in DATETRUNC: Unable to Parse the date \n");
            jdam.trace(m_tmHandle, UL_TM_F_TRACE, "Exception in DATETRUNC: Unable to Parse the date \n");
            return IP_FAILURE;
        }
        switch (intervalObj.toLowerCase())
        {
            case "millisecond":
                truncatedDate = DateUtils.truncate(inputDate, Calendar.MILLISECOND);
                break;
            case "second":
                truncatedDate = DateUtils.truncate(inputDate, Calendar.SECOND);
                break;
            case "minute":
                truncatedDate = DateUtils.truncate(inputDate, Calendar.MINUTE);
                break;
            case "hour":
                truncatedDate = DateUtils.truncate(inputDate, Calendar.HOUR);
                break;

            case "day":
                truncatedDate = DateUtils.truncate(inputDate, Calendar.DATE);
                break;

            case "week":
                truncatedDate = DateUtils.truncate(inputDate, Calendar.WEEK_OF_MONTH);
                break;

            case "month":
                truncatedDate = DateUtils.truncate(inputDate, Calendar.MONTH);
                break;

            case "year":
                truncatedDate = DateUtils.truncate(inputDate, Calendar.YEAR);
                break;
            case "monday":
                truncatedDate = DateUtils.truncate(inputDate, Calendar.MONDAY);
                break;
            case "tuesday":
                truncatedDate = DateUtils.truncate(inputDate, Calendar.TUESDAY);
                break;
            case "wednesday":
                truncatedDate = DateUtils.truncate(inputDate, Calendar.WEDNESDAY);
                break;
            case "thursday":
                truncatedDate = DateUtils.truncate(inputDate, Calendar.THURSDAY);
                break;
            case "friday":
                truncatedDate = DateUtils.truncate(inputDate, Calendar.FRIDAY);
                break;
            case "saturday":
                truncatedDate = DateUtils.truncate(inputDate, Calendar.SATURDAY);
                break;
            case "sunday":
                truncatedDate = DateUtils.truncate(inputDate, Calendar.SUNDAY);
                break;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(truncatedDate);

        xo_tm truncatedTMObj = new xo_tm(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), calendar.get(Calendar.MILLISECOND));

        hVal = jdam.dam_createVal(pMemTree, XO_TYPE_TIMESTAMP, truncatedTMObj, XO_NTS);

        return  hVal;
    }

    public Date getParsedDate(String dateString) throws Exception
    {
        String[] patterns = new String[23];
        patterns[0] = "yyyy-MM-dd";

        patterns[1] = "yyyy-MM-dd hh:mm:ss";
        patterns[2] = "yyyy-MM-dd'T'hh:mm:ss";
        patterns[3] = "yyyy-MM-dd'T'hh:mm:ss'Z'";

        patterns[4] = "yyyy-MM-dd hh:mm:ss.S";
        patterns[5] = "yyyy-MM-dd hh:mm:ss.SS";
        patterns[6] = "yyyy-MM-dd hh:mm:ss.SSS";
        patterns[7] = "yyyy-MM-dd hh:mm:ss.SSSS";
        patterns[8] = "yyyy-MM-dd hh:mm:ss.SSSSS";
        patterns[9] = "yyyy-MM-dd hh:mm:ss.SSSSSS";


        patterns[10] = "yyyy-MM-dd'T'hh:mm:ss.S";
        patterns[11] = "yyyy-MM-dd'T'hh:mm:ss.SS";
        patterns[12] = "yyyy-MM-dd'T'hh:mm:ss.SSS";
        patterns[13] = "yyyy-MM-dd'T'hh:mm:ss.SSSS";
        patterns[14] = "yyyy-MM-dd'T'hh:mm:ss.SSSSS";
        patterns[15] = "yyyy-MM-dd'T'hh:mm:ss.SSSSSS";

        patterns[16] = "yyyy-MM-dd'T'hh:mm:ss.S'Z'";
        patterns[17] = "yyyy-MM-dd'T'hh:mm:ss.SS'Z'";
        patterns[18] = "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'";
        patterns[19] = "yyyy-MM-dd'T'hh:mm:ss.SSSS'Z'";
        patterns[20] = "yyyy-MM-dd'T'hh:mm:ss.SSSSS'Z'";
        patterns[21] = "yyyy-MM-dd'T'hh:mm:ss.SSSSSS'Z'";

        patterns[22] = "yyyy-MM-dd'T'hh:mm:ss.SSSSSSSXXX";

        Date inputDate = null;
        try {
            inputDate = DateUtils.parseDate(dateString, patterns);
        }
        catch (Exception ex)
        {
            throw ex;
        }

        return inputDate;
    }
}
