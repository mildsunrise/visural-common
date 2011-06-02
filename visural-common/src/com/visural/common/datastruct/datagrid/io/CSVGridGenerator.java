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

import com.visural.common.StringUtil;
import com.visural.common.datastruct.datagrid.DataException;
import com.visural.common.datastruct.datagrid.DataGridGenerator;
import com.visural.common.datastruct.datagrid.DataRow;
import com.visural.common.datastruct.datagrid.FieldType;
import com.visural.common.datastruct.datagrid.TableFormat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CSV file parser
 * 
 * @version $Id: CSVGridGenerator.java 28 2010-03-23 07:31:40Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class CSVGridGenerator implements DataGridGenerator {
    
    private static final Logger log = Logger.getLogger(CSVGridGenerator.class.getName());

    public static final String TEXT_FORMAT_UTF8 = "UTF8";
    private boolean bProcessedOk = false;
    private int nErrorRows = 0;
    private boolean bHasHeader;
    private String sDelim;
    private String sColQuote;
    private ArrayList alRows;
    private TableFormat tfHeader;

    /**
     * Creates and parses a new CSV type file.
     * @param sFilename The name of the file to read
     * @throws DataException 
     */
    public CSVGridGenerator(String sFilename) throws DataException {
        this(sFilename, true, ",", "\"", null);
    }

    public CSVGridGenerator(String gridName, InputStream is) throws DataException {
        init(gridName, is, true, ",", "\"", null);
    }

    /**
     * Creates and parses a new CSV type file.
     * @param sFilename The name of the file to read
     * @param textFormat Specify a java input text format for the ready (e.g. UTF-8)
     * @throws DataException 
     */
    public CSVGridGenerator(String sFilename, String textFormat) throws DataException {
        this(sFilename, true, ",", "\"", textFormat);
    }

    public CSVGridGenerator(String gridName, InputStream is, String textFormat) throws DataException {
        init(gridName, is, true, ",", "\"", textFormat);
    }

    /**
     * Creates and parses a new CSV type file.
     * @param sFilename The name of the file to read
     * @param bHasHeader Whether the first row of the file is a header row containing column names
     * @param sDelim The delimiter of the file, e.g. comma (,) or tab (\t)
     * @param sColQuote Some files use quotes surrounding field data so that the delimiter itself may be  
     * used within the fields. Provide the quote here or leave as null string to use 
     * no quote.
     * @param sTextFormat Specify a java input text format for the ready (e.g. UTF-8)
     * @throws DataException If a fatal error occured during parsing, e.g. no data or file missing.
     */
    public CSVGridGenerator(String sFilename, boolean bHasHeader, String sDelim, String sColQuote, String sTextFormat) throws DataException {
        File f = new File(sFilename);
        if (f.exists()) {
            try {
                init(sFilename, new FileInputStream(f), bHasHeader, sDelim, sColQuote, sTextFormat);
            } catch (FileNotFoundException ex) {
                throw new DataException("File not found - " + sFilename);
            }
        } else {
            throw new DataException("File not found - " + sFilename);
        }
    }

    public CSVGridGenerator(String gridName, InputStream is, boolean bHasHeader, String sDelim, String sColQuote, String sTextFormat) throws DataException {
        init(gridName, is, bHasHeader, sDelim, sColQuote, sTextFormat);
    }

    private void init(String sourceName, InputStream is, boolean bHasHeader, String sDelim, String sColQuote, String sTextFormat) throws DataException {
        this.bHasHeader = bHasHeader;
        this.sDelim = sDelim;
        this.sColQuote = sColQuote;

        try {
            ArrayList alFile = readRows(is, sTextFormat);
            conformRows(alFile);

            // now move into required format for DataGridGenerator
            if (this.bHasHeader && alFile.size() > 0) {
                ArrayList alH = (ArrayList) alFile.get(0);
                alFile.remove(0);
                ArrayList alFTs = new ArrayList();
                for (int n = 0; n < alH.size(); n++) {
                    FieldType ftNew = new FieldType(alH.get(n).toString(), true, false, false);
                    alFTs.add(ftNew);
                }
                tfHeader = new TableFormat(sourceName, alFTs);
            } else if (alFile.size() > 0) {
                // still need a header/format so we just generate dummy cols from first row
                ArrayList alH = (ArrayList) alFile.get(0);
                ArrayList alFTs = new ArrayList();
                for (int n = 0; n < alH.size(); n++) {
                    FieldType ftNew = new FieldType("COLUMN " + (n + 1), true, false, false);
                    alFTs.add(ftNew);
                }
                tfHeader = new TableFormat(sourceName, alFTs);
            } else {
                throw new DataException("No data in file.");
            }
            // now copy data
            alRows = new ArrayList();
            for (int nR = 0; nR < alFile.size(); nR++) {
                DataRow drNew = new DataRow(tfHeader, true, true, false);
                ArrayList alRow = (ArrayList) alFile.get(nR);
                for (int n = 0; n < alRow.size() && n < tfHeader.getNumFields(); n++) {
                    drNew.setField(n, alRow.get(n));
                }
                alRows.add(drNew);
            }
        } catch (Exception e) {
            throw new DataException("Unexpected error - " + e.getMessage(), e);
        }

    }

    /**
     * Returns the table format (header) data.
     * @return the table format (header) data.
     */
    public TableFormat getTableFormat() {
        return tfHeader;
    }

    /**
     * Returns an array list of DataRows to the caller.
     * @return an array list of DataRows to the caller.
     */
    public ArrayList getRows() {
        return alRows;
    }

    /**
     * Returns whether the parser finished processing correctly and is ready to provide
     * its data to the caller.
     * @return true/false as to whether data is ready.
     */
    public boolean isReady() {
        return bProcessedOk;
    }

    /**
     * Returns the number of rows which errored during parsing.
     * @return the number of rows which errored during parsing.
     */
    public int getNumErrorRows() {
        return nErrorRows;
    }

    private ArrayList readRows(InputStream is, String textFormat) throws IOException {
        ArrayList alRet = new ArrayList();
        BufferedReader brIn = new BufferedReader(
                StringUtil.isNotBlankStr(textFormat) ? new InputStreamReader(is,textFormat) : new InputStreamReader(is));
        String sLine = null;
        while ((sLine = brIn.readLine()) != null) {
            ArrayList alFields = new ArrayList();
            String[] saLine = sLine.split(this.sDelim);
            for (int nF = 0; nF < saLine.length; nF++) {
                alFields.add(saLine[nF]);
            }
            alRet.add(alFields);
        }
        brIn.close();
        return alRet;
    }

    private boolean startQuoted(String s) {
        boolean bReturn = false;
        if (StringUtil.isNotBlankStr(this.sColQuote)) {
            String s2 = s;
            int nCount = 0;
            while (s2.startsWith(this.sColQuote) && StringUtil.isNotBlankStr(s2)) {
                s2 = s2.substring(1);
                nCount++;
            }
            if ((nCount & 1) == 1) {
                bReturn = true;
            }
        }
        return bReturn;
    }

    private boolean endQuoted(String s) {
        boolean bReturn = false;
        if (StringUtil.isNotBlankStr(this.sColQuote)) {
            String s2 = s;
            int nCount = 0;
            while (s2.endsWith(this.sColQuote) && StringUtil.isNotBlankStr(s2)) {
                s2 = s2.substring(0, s2.length() - 1);
                nCount++;
            }
            if ((nCount & 1) == 1) {
                bReturn = true;
            }
        }
        return bReturn;
    }

    private void conformRows(ArrayList alFile) {
        if (alFile.size() > 0) {
            // first deal with quotes if necessary
            if (StringUtil.isNotBlankStr(sColQuote)) {
                for (int nR = 0; nR < alFile.size(); nR++) {
                    ArrayList alLine = (ArrayList) alFile.get(nR);
                    for (int nF = 0; nF < alLine.size(); nF++) {
                        if (startQuoted(alLine.get(nF).toString().trim()) &&
                                endQuoted(alLine.get(nF).toString().trim())) {
                            String sText = alLine.get(nF).toString().trim();
                            alLine.set(nF, sText.substring(1, sText.length() - 1));
                        } else {
                            while (startQuoted(alLine.get(nF).toString().trim()) &&
                                    !endQuoted(alLine.get(nF).toString().trim())) {
                                if (nF == alLine.size() - 1) {
                                    // need to merge following line (CR)
                                    ArrayList alNext = (ArrayList) alFile.get(nR + 1);
                                    alFile.remove(nR + 1);
                                    alLine.set(nF, alLine.get(nF).toString() + "\n" + alNext.get(0).toString());
                                    for (int nE = 1; nE < alNext.size(); nE++) {
                                        alLine.add(alNext.get(nE));
                                    }
                                } else {
                                    // need to merge following field
                                    alLine.set(nF, alLine.get(nF).toString() + this.sDelim + alLine.get(nF + 1).toString());
                                    alLine.remove(nF + 1);
                                }
                            }
                            // trim off quotes
                            if (alLine.get(nF).toString().trim().startsWith(this.sColQuote) &&
                                    alLine.get(nF).toString().trim().endsWith(this.sColQuote)) {
                                String sText = alLine.get(nF).toString().trim();
                                alLine.set(nF, sText.substring(1, sText.length() - 1));
                            }
                        }
                        // replace any double quotes
                        alLine.set(nF, alLine.get(nF).toString().replace(sColQuote + sColQuote, sColQuote));
                    }
                }
            }

            // now find invalid rows
            int nFields = ((ArrayList) alFile.get(0)).size();
            int nRemoveCount = 0;
            for (int nR = alFile.size() - 1; nR > 0; nR--) {
                ArrayList alLine = (ArrayList) alFile.get(nR);
                if (alLine.size() > nFields) {
                    log.log(Level.WARNING, "Row #{0} contains too many fields. Fields parsed: {1}", new Object[]{nR+(this.bHasHeader ? 0 : 1)+nRemoveCount, alLine.toString()});
                } else if (alLine.size() < nFields) {
                    log.log(Level.WARNING, "Row #{0} contains less than expected number of fields. Fields parsed: {1}", new Object[]{nR+(this.bHasHeader ? 0 : 1)+nRemoveCount, alLine.toString()});
                }
            }
        }
    }
}
