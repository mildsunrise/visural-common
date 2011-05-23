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

import com.visural.common.StringUtil;
import java.io.Serializable;

/**
 * @version $Id: DataField.java 2 2009-11-17 12:26:31Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class DataField implements Serializable {

    private FieldType fieldType;
    private boolean changed;
    private Object data;

    public DataField(FieldType ftCurrent, boolean bChangedInitial, Object oData) {
        this.fieldType = ftCurrent;
        this.setChanged(bChangedInitial);
        this.setData(oData);
    }

    public Object getData() {
        return data;
    }

    public String getDataAsString() {
        String sResult = "";
        if (data != null) {
            sResult = data.toString();
        }
        return sResult;
    }

    public void setDataAbsolute(Object oData) {
        if ((this.data == null && oData != null) ||
                (this.data != null && oData == null) ||
                (this.data != null && oData != null && !this.data.equals(oData))) {
            setChanged(true);
        }

        this.data = oData;
    }

    public void setData(Object oData) {
        if (fieldType.allowsEdit()) {
            setDataAbsolute(oData);
        }
    }

    public void setData(String sData) {
        Object oData = sData;

        if (StringUtil.isBlankStr(sData)) {
            oData = null;
        }

        this.setData(oData);
    }

    public boolean isValid() {
        boolean bValid = true;

        if ((fieldType.isPK() || fieldType.isRequired()) && data == null) {
            bValid = false;
        }

        return bValid;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean bChanged) {
        this.changed = bChanged;
    }
}
