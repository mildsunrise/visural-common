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

import com.visural.common.datastruct.datagrid.DataGridGenerator;
import com.visural.common.datastruct.datagrid.FieldType;
import com.visural.common.datastruct.datagrid.TableFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id: HeaderArrayGridGenerator.java 22 2010-03-04 12:32:33Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class HeaderArrayGridGenerator implements DataGridGenerator {

    private TableFormat tfHeader;
    private ArrayList alRows;

    public HeaderArrayGridGenerator(List header) {
        ArrayList alFields = new ArrayList();
        for (int n = 0; n < header.size(); n++) {
            FieldType ft = new FieldType(header.get(n).toString(), true, false, false);
            alFields.add(ft);
        }
        tfHeader = new TableFormat("", alFields);
        alRows = new ArrayList();
    }

    public TableFormat getTableFormat() {
        return tfHeader;
    }

    public ArrayList getRows() {
        return alRows;
    }

    public boolean isReady() {
        return true;
    }
}
