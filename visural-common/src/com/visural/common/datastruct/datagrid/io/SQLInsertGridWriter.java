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
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @version $Id: SQLInsertGridWriter.java 2 2009-11-17 12:26:31Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class SQLInsertGridWriter implements DataGridWriter {

    private OutputStream os = null;
    private String OUT_CHARSET = "UTF-8";

    public SQLInsertGridWriter(OutputStream os) {
        this(os, "UTF-8");
    }

    public SQLInsertGridWriter(OutputStream os, String sCharSet) {
        this.os = os;
        this.OUT_CHARSET = sCharSet;
    }

    public boolean isCompatibleWith(DataGrid dgParent) {
        boolean bResult = true;

        TableFormat tfCurrent = dgParent.getFormat();
        if (StringUtil.isBlankStr(tfCurrent.getTableSource())) {
            bResult = false;
        }

        return bResult;
    }

    private String getDMLStringForField(FieldType ft, Object o) { //NOPMD
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
            if (!dgParent.isValid()) {
                throw new DataException("The table being written fails required field validation.");
            }

            try {
                OutputStreamWriter osw = new OutputStreamWriter(os, this.OUT_CHARSET);

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
                    if (!drCurrent.isDeleted()) {
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
                        osw.write(sInsert + "\n");
                    }
                }
                osw.close();
            } catch (Exception e) {
                throw new DataException("Unexpected error", e);
            }
        } else {
            throw new DataException("This data writer is not compatible with the source grid. The grid must have a source.");
        }
    }
}
