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

import com.visural.common.IOUtil;
import com.visural.common.datastruct.datagrid.DataException;
import com.visural.common.datastruct.datagrid.DataGridGenerator;
import com.visural.common.datastruct.datagrid.DataRow;
import com.visural.common.datastruct.datagrid.FieldType;
import com.visural.common.datastruct.datagrid.TableFormat;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.StringTokenizer;

/**
 *
 * @author Richard Nichols
 */
public class SQLQueryGridGenerator implements DataGridGenerator {

    private boolean bIsReady;
    private boolean bSingleSource;
    private String sTableSource;
    private TableFormat tfHeader;
    private ArrayList alRows;

    /**
     * Creates a new instance of SQLQueryGridGenerator
     */
    public SQLQueryGridGenerator(PreparedStatement ps) throws DataException {
        PreparedStatement stQuery = null;
        ResultSet rsQuery = null;
        try {
            stQuery = ps;
            rsQuery = stQuery.executeQuery();

            ArrayList alFieldTypes = new ArrayList();

            // get header
            sTableSource = null;
            bSingleSource = true;
            boolean bTryGuessTable = false;
            ResultSetMetaData rsmd = rsQuery.getMetaData();
            int naType[] = new int[rsmd.getColumnCount()];
            for (int nC = 0; nC < rsmd.getColumnCount(); nC++) {
                naType[nC] = rsmd.getColumnType(nC + 1);

                String sFieldName = rsmd.getColumnLabel(nC + 1);
                boolean bRequired = false;
                if (rsmd.isNullable(nC + 1) == rsmd.columnNoNulls) {
                    bRequired = true;
                }

                FieldType ftNew = new FieldType(sFieldName, true, false, bRequired);

                alFieldTypes.add(ftNew);

                if (sTableSource == null) {
                    bTryGuessTable = true;
                    sTableSource = rsmd.getTableName(nC + 1);
                } else if (bSingleSource && !bTryGuessTable) {
                    String sCurrentTable = rsmd.getTableName(nC + 1);
                    if (sCurrentTable.compareTo(sTableSource) != 0) {
                        bSingleSource = false;
                    }
                }
            }

            if (!bSingleSource) {
                sTableSource = null;
            }

            if (bTryGuessTable) {//NOPMD
                //sTableSource = guessTableName(sQuery);
            }

            tfHeader = new TableFormat(sTableSource, alFieldTypes);

            // get rows
            alRows = new ArrayList();
            while (rsQuery.next()) {
                DataRow drNew = new DataRow(tfHeader, true, true, false);
                for (int nC = 0; nC < alFieldTypes.size(); nC++) {
                    Object object = null;
                    if (naType[nC] == Types.CLOB) {
                        object = rsQuery.getString(nC+1);
                    } else if (naType[nC] == Types.BLOB) {
                        object = rsQuery.getBytes(nC+1);
                    } else {
                        object = rsQuery.getObject(nC + 1);
                    }
                    drNew.setField(nC, object);            
                }
                drNew.acceptChanges();
                alRows.add(drNew);
            }

            bIsReady = true;
        } catch (SQLException se) {
            throw new DataException("Error performing query", se);
        } finally {
            IOUtil.silentClose(getClass(), rsQuery);
            IOUtil.silentClose(getClass(), stQuery);
        }
    }

    private String guessTableName(String sQuery) {//NOPMD
        String sResult = null;
        int nFromIdx = 0;
        if ((nFromIdx = sQuery.toUpperCase().indexOf("FROM")) > 0) {
            // get the part of the query from the FROM string onward
            String sFromToLast = sQuery.substring(nFromIdx + 4);
            StringTokenizer stTok = new StringTokenizer(sFromToLast, " ", false);
            String sTable = stTok.nextToken();
            // check that the table name doesn't end with a comma, that means that this is a multi table select
            // in this case we can't set a specific source
            if (!sTable.endsWith(",")) {
                sResult = sTable;
                // now iterate through the remainder of the string
                // exit cases are that we hit a WHERE ORDER or GROUP keyword, 
                // or that there's a comma before that happens. in the case of
                // the comma, we nullify the result.
                while (stTok.hasMoreTokens()) {
                    String sThisToken = stTok.nextToken().toUpperCase();
                    if (sThisToken.compareTo("WHERE") == 0 ||
                            sThisToken.compareTo("ORDER") == 0 ||
                            sThisToken.compareTo("GROUP") == 0) {
                        break;
                    } else if (sThisToken.indexOf(",") >= 0) {
                        sResult = null;
                        break;
                    }
                }
            }
        }

        return sResult;
    }

    /**
     * 
     * @return 
     */
    public TableFormat getTableFormat() {
        return tfHeader;
    }

    /**
     * 
     * @return 
     */
    public ArrayList getRows() {
        return alRows;
    }

    /**
     * 
     * @return 
     */
    public boolean isReady() {
        return bIsReady;
    }
}
