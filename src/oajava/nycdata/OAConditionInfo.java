package oajava.nycdata;

/**
 * Created by sbobba on 8/25/2017.
 */
import oajava.sql.ip;
import oajava.sql.jdam;
import oajava.sql.xo_int;

import java.util.ArrayList;
import java.util.HashMap;

import static oajava.sql.ip.SQL_SET_CONDLIST_INTERSECT;


public class OAConditionInfo {
    private String columnName;
    private int leftOperator;
    private int leftValType;
    private int leftValLength;
    private Object leftValue;
    private int rightOperator;
    private int rightValType;
    private int rightValLength;
    private Object rightValue;


    public OAConditionInfo(long dam_hstmt, long hcond) {
        super();

        long colInCond = jdam.dam_getColInCond(hcond);
        StringBuffer sbColName = new StringBuffer();
        xo_int iStatus = new xo_int(0);
        xo_int iOperator = new xo_int(0);
        xo_int iValueType = new xo_int(0);
        xo_int iValueLength = new xo_int(0);

        jdam.dam_describeCol(colInCond, null, sbColName, null, null);
        this.columnName = sbColName.toString().toUpperCase();

        this.leftValue = jdam.dam_describeCondEx(dam_hstmt, hcond,
                oajava.sql.ip.DAM_COND_PART_LEFT, iOperator, iValueType, iValueLength,
                iStatus);
        this.leftOperator = iOperator.getVal();
        this.leftValType = iValueType.getVal();
        this.leftValLength = iValueLength.getVal();

        this.rightValue = jdam.dam_describeCondEx(dam_hstmt, hcond,
                oajava.sql.ip.DAM_COND_PART_RIGHT, iOperator, iValueType, iValueLength,
                iStatus);
        this.rightOperator = iOperator.getVal();
        this.rightValType = iValueType.getVal();
        this.rightValLength = iValueLength.getVal();

    }

    /**
     * @return the columnName
     */
    public String getColumnName() {
        return columnName;
    }
    /**
     * @return the left operator
     */
    public int getOperator() {
        return leftOperator;
    }
    /**
     * @return the left valLength
     */
    public int getValLength() {
        return leftValLength;
    }
    /**
     * @return the left valType
     */
    public int getValType() {
        return leftValType;
    }
    /**
     * @return the left value
     */
    public Object getValue() {
        return leftValue;
    }

    /**
     * @return the right operator
     */
    public int getRightOperator() {
        return rightOperator;
    }
    /**
     * @return the right valLength
     */
    public int getRightValLength() {
        return rightValLength;
    }
    /**
     * @return the right valType
     */
    public int getRightValType() {
        return rightValType;
    }
    /**
     * @return the right value
     */
    public Object getRightValue() {
        return rightValue;
    }




}
