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
package com.visural.common.datastruct.datagrid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id: TableFormat.java 28 2010-03-23 07:31:40Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class TableFormat implements Serializable {

    private String tableSource;
    private List fieldTypes;
    private int compareColumn;
    private boolean sortAscending;

    public TableFormat(String sTableSource, ArrayList alFieldType) {
        this.tableSource = sTableSource;
        this.fieldTypes = alFieldType;
        setCompareCol(0);
    }

    public int getNumFields() {
        return fieldTypes.size();
    }

    public FieldType getFieldType(int nColumn) {
        return (FieldType) fieldTypes.get(nColumn);
    }

    public FieldType getFieldType(String sColumn) {
        return getFieldType(getColumnForName(sColumn));
    }

    public String getTableSource() {
        return tableSource;
    }

    public void setTableSource(String sTableSource) {
        this.tableSource = sTableSource;
    }

    public int getColumnForName(String sColName) {
        int nResult = -1;

        for (int nLp = 0; nLp < fieldTypes.size(); nLp++) {
            String sCurrent = ((FieldType) fieldTypes.get(nLp)).getFieldName();
            if (sCurrent.compareToIgnoreCase(sColName) == 0) {
                nResult = nLp;
                break;
            }
        }

        return nResult;
    }

    public int getCompareCol() {
        return compareColumn;
    }

    public void setCompareCol(int nCompareCol) {
        this.compareColumn = nCompareCol;
    }

    public boolean isSortAscending() {
        return sortAscending;
    }

    public void setSortAscending(boolean bSortAscending) {
        this.sortAscending = bSortAscending;
    }

    void addColumn(FieldType ft) {
        fieldTypes.add(ft);
    }
}
