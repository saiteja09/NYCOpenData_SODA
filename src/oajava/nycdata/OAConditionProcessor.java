package oajava.nycdata;

import oajava.sql.ip;
import oajava.sql.jdam;
import oajava.sql.xo_int;

import java.util.ArrayList;
import java.util.HashMap;

import static oajava.sql.ip.SQL_SET_CONDLIST_INTERSECT;

/**
 * Created by sbobba on 8/30/2017.
 */
public class OAConditionProcessor {

    private ArrayList<OAConditionInfo> listOfConditions = null;
    private HashMap<String, Object> eqlConditionsInUse = null;
    private HashMap<String, Object> grtConditionsInUse = null;
    private HashMap<String, Object> lstConditionsInUse = null;
    private HashMap<String, Object> likeConditionsinUse = null;

    public OAConditionProcessor()
    {
        listOfConditions = new ArrayList<>();
        eqlConditionsInUse = new HashMap<>();
        grtConditionsInUse = new HashMap<>();
        lstConditionsInUse = new HashMap<>();
        likeConditionsinUse = new HashMap<>();
    }

    public ArrayList<OAConditionInfo> getListOfConditions() {
        return listOfConditions;
    }

    public HashMap<String, Object> getEqlConditionsInUse() {
        return eqlConditionsInUse;
    }

    public HashMap<String, Object> getGrtConditionsInUse() {
        return grtConditionsInUse;
    }

    public HashMap<String, Object> getLstConditionsInUse() {
        return lstConditionsInUse;
    }

    public HashMap<String, Object> getLikeConditionsinUse() {
        return likeConditionsinUse;
    }



    void processSQLforConditions(long dam_hstmt)
    {
        xo_int partialList = new xo_int(0);
        ArrayList<Long> setOfConditionLists = new ArrayList<>();
        //dam_getSetOfConditionListsEx:
        // This function is used to retrieve expressions from the WHERE clause on one or more columns in the form of AND or OR expressions.
        // It can be used to retrieve all the expressions in the WHERE clause by passing NULL for the column handle.
        //Syntax: long dam_getSetOfConditionListsEx(DAM_HSTMT hstmt, int iType, DAM_HCOL  hcol, int * pbPartialLists)
        //RETURNS: long :  The search condition list. Navigate it by using the dam_getFirstCondList and dam_getNextCondList methods. A 0 is returned if no search list is available.
        // The IP must call dam_freeSetOfConditionList to this handle when finished with the query.

        long hset_condList = jdam.dam_getSetOfConditionListsEx(dam_hstmt, SQL_SET_CONDLIST_INTERSECT , 0, partialList);

        //dam_getFirstCondList: This method gets the first condition list from the set of condition lists.
        // This method is used to navigate through the set of condition lists that was obtained by calling dam_getSetOfConditionListsEx
        //Syntax:  int64 dam_getFirstCondList(int64 hset_of_condlist)

        long hcur_condlist = jdam.dam_getFirstCondList(hset_condList);

        //Iterate through each condition list and add them to ArrayList
        do {
            setOfConditionLists.add(hcur_condlist);

            if (hcur_condlist == 0)
                break;

            //This method is used to navigate through the set of condition lists that was obtained by calling dam_getSetOfConditionListsEx
            //Syntax: int64 dam_getNextCondList(int64 hset_of_condlist)
            hcur_condlist = jdam.dam_getNextCondList(hset_condList);
        } while (hcur_condlist != 0);


        //Iterate the Set of Condition lists to extract the individual conditions
        for(Long hCondList: setOfConditionLists)
        {
            //his method is used to navigate through the conditions in the condition list. It gets the first condition in the search or restriction list.
            // Use the method dam_getNextCond to go through the list.
            //Syntax: long dam_getFirstCond(long hstmt, long hlist)
            long hcond = jdam.dam_getFirstCond(dam_hstmt, hCondList);
            while (hcond != 0) {
                //Extract the Condition contents
                OAConditionInfo condInfo = new OAConditionInfo(dam_hstmt, hcond);
                listOfConditions.add(condInfo);
                //long dam_getNextCond(long hstmt, long hlist)
                hcond = jdam.dam_getNextCond(dam_hstmt,
                        hCondList);
            }
        }

    }

    void populateEqualConditions(){
        for (OAConditionInfo conditionInfo : listOfConditions) {
            if (conditionInfo.getOperator() == oajava.sql.ip.SQL_OP_EQUAL) {
                eqlConditionsInUse.put(conditionInfo.getColumnName(), conditionInfo.getValue());
            }
        }
    }

    void populateGreaterThanConditions(){
        for (OAConditionInfo conditionInfo : listOfConditions) {
            if (conditionInfo.getOperator() == ip.SQL_OP_GREATER) {
                grtConditionsInUse.put(conditionInfo.getColumnName(), conditionInfo.getValue());
            }
        }
    }

    void populateLessThanConditions(){
        for (OAConditionInfo conditionInfo : listOfConditions) {
            if (conditionInfo.getOperator() == ip.SQL_OP_SMALLER) {
                lstConditionsInUse.put(conditionInfo.getColumnName(), conditionInfo.getValue());
            }
        }
    }


    void populateLikeConditions(){
        for (OAConditionInfo conditionInfo : listOfConditions) {
            if (conditionInfo.getOperator() == ip.SQL_OP_LIKE) {
                likeConditionsinUse.put(conditionInfo.getColumnName(), conditionInfo.getValue());
            }
        }
    }
}
