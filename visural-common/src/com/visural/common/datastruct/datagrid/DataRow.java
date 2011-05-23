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

import java.util.ArrayList;
import java.io.Serializable;
import java.util.List;

/**
 * @version $Id: DataRow.java 28 2010-03-23 07:31:40Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class DataRow implements Serializable, Comparable {

    private boolean inserted;
    private boolean deleted;
    private List fields;
    private TableFormat header;
    private boolean allowEdit;

    public DataRow(TableFormat tfHeader, boolean bAllowEdit, boolean bInsertedInitial, boolean bDeletedInitial) {
        this.allowEdit = bAllowEdit;
        this.header = tfHeader;
        this.setInserted(bInsertedInitial);
        this.setDeleted(bDeletedInitial);
        initRow();
    }

    private void initRow() {
        fields = new ArrayList();
        for (int nLp = 0; nLp < header.getNumFields(); nLp++) {
            DataField dfNew = new DataField(header.getFieldType(nLp), false, null);
            fields.add(dfNew);
        }
    }

    public void setField(String sColumn, Object oData) {
        setField(header.getColumnForName(sColumn), oData);
    }

    public void setField(String sColumn, String sData) {
        setField(header.getColumnForName(sColumn), sData);
    }

    public void setField(int nColumn, Object oData) {
        if (allowEdit) {
            DataField dfSet = (DataField) fields.get(nColumn);
            dfSet.setData(oData);
        }
    }

    public void setField(int nColumn, String sData) {
        if (allowEdit) {
            DataField dfSet = (DataField) fields.get(nColumn);
            dfSet.setData(sData);
        }
    }

    public boolean isValid() {
        boolean bValid = true;

        for (int nLp = 0; nLp < fields.size(); nLp++) {
            DataField dfCurrent = (DataField) fields.get(nLp);
            if (!dfCurrent.isValid()) {
                bValid = false;
                break;
            }
        }

        return bValid;
    }

    public boolean isChanged() {
        boolean bResult = false;

        for (int nF = 0; nF < fields.size(); nF++) {
            if (getField(nF).isChanged()) {
                bResult = true;
                break;
            }
        }

        return bResult;
    }

    public DataField getField(int nColumn) {
        return (DataField) fields.get(nColumn);
    }

    public boolean isInserted() {
        return inserted;
    }

    public void setInserted(boolean bInserted) {
        this.inserted = bInserted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean bDeleted) {
        this.deleted = bDeleted;
    }

    public void acceptChanges() {
        for (int nC = 0; nC < header.getNumFields(); nC++) {
            getField(nC).setChanged(false);
        }
    }

    public int compareTo(Object o) {
        DataRow drOther = (DataRow) o;
        Object oThis = getField(header.getCompareCol()).getData();
        Object oThat = drOther.getField(header.getCompareCol()).getData();
        if (header.isSortAscending()) {
            return oThis.toString().compareTo(oThat.toString());
        } else {
            return -1 * oThis.toString().compareTo(oThat.toString());
        }
    }

    void addColumn(FieldType ft) {
        DataField dfNew = new DataField(ft, false, null);
        fields.add(dfNew);
    }
}
