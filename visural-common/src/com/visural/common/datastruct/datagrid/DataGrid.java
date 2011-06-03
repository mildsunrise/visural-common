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

import com.visural.common.Function;
import com.visural.common.datastruct.SimpleDataTable;
import java.util.Vector;
import java.util.Collections;
import java.io.Serializable;
import java.util.List;

/**
 * A generic grid of data.
 *
 * TODO: rewrite this to use generics & more modern coding conventions.
 *
 * @version $Id: DataGrid.java 28 2010-03-23 07:31:40Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class DataGrid implements Serializable {

    private DataGridWriter dataGridWriter;
    private boolean allowEdit;
    private TableFormat header;
    private List rows;

    public DataGrid(DataGridGenerator generator, boolean allowEdit) {
        this.dataGridWriter = null;
        this.allowEdit = allowEdit;
        header = generator.getTableFormat();
        rows = generator.getRows();
    }

    public void addColumn(FieldType ft) {
        header.addColumn(ft);
        for (DataRow row : ((List<DataRow>) rows)) {
            row.addColumn(ft);
        }
    }

    public TableFormat getFormat() {
        return header;
    }

    public int getNumRows() {
        return rows.size();
    }

    public int getNumColumns() {
        return header.getNumFields();
    }

    public int getNewRow() {
        DataRow drNew = new DataRow(header, allowEdit, false, false);
        rows.add(drNew);
        return rows.size() - 1;
    }

    public DataRow getRow(int nRow) {
        return (DataRow) rows.get(nRow);
    }

    public void deleteRow(int nRow) {
        getRow(nRow).setDeleted(true);
    }

    public void setCell(int nRow, int nCol, Object oData) {
        getRow(nRow).setField(nCol, oData);
    }

    public void setCell(int nRow, String sCol, Object oData) {
        setCell(nRow, header.getColumnForName(sCol), oData);
    }

    public void setCell(int nRow, int nCol, String sData) {
        getRow(nRow).setField(nCol, sData);
    }

    public void setCell(int nRow, String sCol, String sData) {
        setCell(nRow, header.getColumnForName(sCol), sData);
    }

    public Object getCell(int nRow, int nCol) {
        return getRow(nRow).getField(nCol).getData();
    }

    public Object getCell(int nRow, String sCol) {
        return getCell(nRow, header.getColumnForName(sCol));
    }

    public String getCellAsString(int nRow, int nCol) {
        return getRow(nRow).getField(nCol).getDataAsString();
    }

    public String getCellAsString(int nRow, String sCol) {
        return getCellAsString(nRow, header.getColumnForName(sCol));
    }

    public float getCellAsFloat(int nRow, int nCol) {
        return Float.parseFloat(getRow(nRow).getField(nCol).getDataAsString());
    }

    public float getCellAsFloat(int nRow, String sCol) {
        return getCellAsFloat(nRow, header.getColumnForName(sCol));
    }

    public int getCellAsInt(int nRow, int nCol) {
        return Integer.parseInt(getRow(nRow).getField(nCol).getDataAsString());
    }

    public int getCellAsInt(int nRow, String sCol) {
        return getCellAsInt(nRow, header.getColumnForName(sCol));
    }

    public void setWriter(DataGridWriter dgw) throws DataException {
        if (dgw.isCompatibleWith(this)) {
            this.dataGridWriter = dgw;
        } else {
            throw new DataException("The data grid writer supplied is not compatible with the format and content of the data table.");
        }
    }

    public void acceptChanges() {
        for (int nR = 0; nR < this.getNumRows(); nR++) {
            getRow(nR).acceptChanges();
        }
    }

    public void acceptInserts() {
        for (int nR = 0; nR < this.getNumRows(); nR++) {
            getRow(nR).setInserted(true);
        }
    }

    public void acceptDeletes() {
        for (int nR = this.getNumRows() - 1; nR >= 0; nR--) {
            if (getRow(nR).isDeleted()) {
                rows.remove(nR);
            }
        }
    }

    public void write() throws DataException {
        if (dataGridWriter != null) {
            dataGridWriter.write(this);
        }
    }

    public boolean isValid() {
        boolean bValid = true;

        for (int nR = 0; nR < this.getNumRows(); nR++) {
            if (!this.getRow(nR).isValid()) {
                bValid = false;
                break;
            }
        }

        return bValid;
    }

    public int getMaxStringLengthForColumn(int nCol) {
        int nMax = 0;

        for (int nR = 0; nR < getNumRows(); nR++) {
            int nThisLen = getCellAsString(nR, nCol).length();
            if (nThisLen > nMax) {
                nMax = nThisLen;
            }
        }

        return nMax;
    }

    public int getMaxStringLengthForColumn(String sCol) {
        return getMaxStringLengthForColumn(header.getColumnForName(sCol));
    }

    public void sortGridByColumn(int nCol, boolean bAscending) {
        header.setCompareCol(nCol);
        header.setSortAscending(bAscending);
        Collections.sort(rows);
    }

    public SimpleDataTable toDataTable() {
        Vector vHeader = new Vector();
        for (int nH = 0; nH < this.header.getNumFields(); nH++) {
            vHeader.add(header.getFieldType(nH).getFieldName());
        }

        SimpleDataTable dt = new SimpleDataTable(vHeader);
        for (int n = 0; n < this.getNumRows(); n++) {
            int nRow = dt.addRow();
            for (int nC = 0; nC < header.getNumFields(); nC++) {
                dt.setCell(nRow, nC, Function.strnvl(this.getCellAsString(n, nC), ""));
            }
        }

        return dt;
    }
}
