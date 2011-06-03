/*
 *  Copyright 2009 Richard Nichols.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.visural.common.datastruct.datagrid.io;

import com.visural.common.Function;
import com.visural.common.StringUtil;
import com.visural.common.datastruct.datagrid.DataException;
import com.visural.common.datastruct.datagrid.DataGrid;
import com.visural.common.datastruct.datagrid.DataGridWriter;
import com.visural.common.datastruct.datagrid.DataRow;
import com.visural.common.datastruct.datagrid.FieldType;
import com.visural.common.datastruct.datagrid.TableFormat;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * @version $Id: SQLTableGridWriter.java 2 2009-11-17 12:26:31Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class SQLTableGridWriter implements DataGridWriter {

    private Connection con;

    /**
     * Creates a new instance of SQLTableWriter
     * @param con 
     */
    public SQLTableGridWriter(Connection con) {
        this.con = con;
    }

    /**
     * 
     * @param dgParent 
     * @return 
     */
    public boolean isCompatibleWith(DataGrid dgParent) {
        boolean bResult = true;

        TableFormat tfCurrent = dgParent.getFormat();
        if (StringUtil.isNotBlankStr(tfCurrent.getTableSource())) {
            bResult = false;
        }

        boolean bPK = false;
        for (int nC = 0; nC < tfCurrent.getNumFields(); nC++) {
            if (tfCurrent.getFieldType(nC).isPK()) {
                bPK = true;
                break;
            }
        }

        if (!bPK) {
            bResult = false;
        }

        return bResult;
    }

    private String getDMLStringForField(FieldType ft, Object o) {//NOPMD
        String sObjConverted = "";
        /*                                String sFieldClass = oF.getClass().getName();
        if (sFieldClass.compareTo("any") == 0)
        {
        // TBD
        }
        else
        {
        // generic case, just use a quoted string
        sDataList += "'" + oF.toString() + "'";
        }
         */
        if (o != null) {
            sObjConverted = o.toString();
        }

        return "'" + escapeSQLString(sObjConverted) + "'";
    }

    private static String escapeSQLString(String sCheckStr) {
        String sResult = Function.strnvl(sCheckStr, "");

        sResult = sResult.replace("\'", "\'\'");
        sResult = sResult.replace("\n", "");
        sResult = sResult.replace("\r", "");

        return sResult;
    }

    /**
     * 
     * @param dgParent 
     * @throws tibes.DataGrid.DataException 
     */
    public void write(DataGrid dgParent) throws DataException {
        if (this.isCompatibleWith(dgParent)) {
            try {
                if (!dgParent.isValid()) {
                    throw new DataException("The table being written fails required field validation.");
                }
                int nFields = dgParent.getFormat().getNumFields();
                String sTable = dgParent.getFormat().getTableSource();
                String sInsertList = "";
                for (int nF = 0; nF < nFields; nF++) {
                    if (nF > 0) {
                        sInsertList += ",";
                    }
                    sInsertList += dgParent.getFormat().getFieldType(nF).getFieldName();
                }
                for (int nR = 0; nR < dgParent.getNumRows(); nR++) {
                    DataRow drCurrent = dgParent.getRow(nR);
                    if (!drCurrent.isInserted() || drCurrent.isDeleted() || drCurrent.isChanged()) {
                        if (!drCurrent.isInserted() && !drCurrent.isDeleted()) {
                            // insert the row
                            String sDataList = "";
                            for (int nF = 0; nF < nFields; nF++) {
                                if (nF > 0) {
                                    sDataList += ",";
                                }
                                Object oF = drCurrent.getField(nF).getData();
                                sDataList += getDMLStringForField(dgParent.getFormat().getFieldType(nF), oF);
                            }
                            String sInsert = "INSERT INTO " + sTable + " (" + sInsertList + ") VALUES (" + sDataList + ")";
                            Statement stInsert = con.createStatement();
                            stInsert.executeUpdate(sInsert);
                            stInsert.close();
                        } else if (drCurrent.isDeleted() && drCurrent.isInserted()) {
                            // delete the row
                            String sPKDelete = "";
                            for (int nF = 0; nF < nFields; nF++) {
                                if (dgParent.getFormat().getFieldType(nF).isPK()) {
                                    if (StringUtil.isNotBlankStr(sPKDelete)) {
                                        sPKDelete += " AND ";
                                    }
                                    sPKDelete += dgParent.getFormat().getFieldType(nF).getFieldName() + " = " + getDMLStringForField(dgParent.getFormat().getFieldType(nF), drCurrent.getField(nF).getData());
                                }
                            }
                            String sDelete = "DELETE FROM " + sTable + " WHERE " + sPKDelete;
                            Statement stDelete = con.createStatement();
                            stDelete.executeUpdate(sDelete);
                            stDelete.close();
                        } else if (drCurrent.isChanged() && !drCurrent.isDeleted()) {
                            // update the row
                            String sDataList = "";
                            for (int nF = 0; nF < nFields; nF++) {
                                if (nF > 0) {
                                    sDataList += ", ";
                                }
                                sDataList += dgParent.getFormat().getFieldType(nF).getFieldName() + " = " + getDMLStringForField(dgParent.getFormat().getFieldType(nF), drCurrent.getField(nF).getData());
                            }
                            String sPKUpdate = "";
                            for (int nF = 0; nF < nFields; nF++) {
                                if (dgParent.getFormat().getFieldType(nF).isPK()) {
                                    if (StringUtil.isNotBlankStr(sPKUpdate)) {
                                        sPKUpdate += " AND ";
                                    }
                                    sPKUpdate += dgParent.getFormat().getFieldType(nF).getFieldName() + " = " + getDMLStringForField(dgParent.getFormat().getFieldType(nF), drCurrent.getField(nF).getData());
                                }
                            }
                            String sUpdate = "UPDATE " + sTable + " SET " + sDataList + " WHERE " + sPKUpdate;
                            Statement stUpdate = con.createStatement();
                            stUpdate.executeUpdate(sUpdate);
                            stUpdate.close();
                        }
                    }
                }
                con.commit();
                dgParent.acceptChanges();
                dgParent.acceptInserts();
                dgParent.acceptDeletes();
            } catch (SQLException se) {
                throw new DataException("Database error occured during write operation.", se);
            }
        } else {
            throw new DataException("This data writer is not compatible with the source grid. The grid must have a single source and marked primary key fields.");
        }
    }
}
