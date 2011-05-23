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

/**
 * @version $Id: FieldType.java 2 2009-11-17 12:26:31Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class FieldType implements Serializable {

    private String fieldName;
    private boolean allowEdit;
    private boolean pk;
    private boolean required;

    public FieldType(String sFieldName, boolean bAllowEdit, boolean bPK, boolean bRequired) {
        this.setFieldName(sFieldName);
        this.allowEdit = bAllowEdit;
        this.pk = bPK;
        this.required = bRequired;
    }

    public boolean allowsEdit() {
        return allowEdit;
    }

    public void setAllowEdit(boolean bAllowEdit) {
        this.allowEdit = bAllowEdit;
    }

    public boolean isPK() {
        return pk;
    }

    public void setPK(boolean bPK) {
        this.pk = bPK;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean bRequired) {
        this.required = bRequired;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String sFieldName) {
        this.fieldName = sFieldName;
    }
}
