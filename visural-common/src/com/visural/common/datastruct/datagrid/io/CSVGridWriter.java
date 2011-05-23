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
import com.visural.common.datastruct.datagrid.DataGrid;
import com.visural.common.datastruct.datagrid.DataGridWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 *
 * @author Richard Nichols
 */
public class CSVGridWriter implements DataGridWriter {
    public static final String TEXT_FORMAT_UTF8 = "UTF8";
    public static final String TEXT_FORMAT_ASCII = "US-ASCII";
    private String sOutputFilename = null;
    private boolean bWriteHeader;
    private boolean bUseQuotes;
    private String sDelimiter;
    private String sTextFormat = TEXT_FORMAT_ASCII;
    private OutputStream osOut = null;
    private boolean bCloseOutput = false;
    
    /**
     * Creates a new instance of CSVGridWriter
     * @param sOutputFilename 
     * @param bWriteHeader 
     * @param bUseQuotes 
     * @param sDelimiter 
     */
    public CSVGridWriter(String sOutputFilename, boolean bWriteHeader, boolean bUseQuotes, String sDelimiter) {
        this.sOutputFilename = sOutputFilename;
        this.bWriteHeader = bWriteHeader;
        this.bUseQuotes = bUseQuotes;
        this.sDelimiter = sDelimiter;
        bCloseOutput = true;
    }

    
    /**
     * Creates a new instance of CSVGridWriter
     * @param sOutputFilename 
     * @param bWriteHeader 
     * @param bUseQuotes 
     * @param sDelimiter 
     */
    public CSVGridWriter(OutputStream osOut, boolean bWriteHeader, boolean bUseQuotes, String sDelimiter) {
        this.osOut = osOut;
        this.bWriteHeader = bWriteHeader;
        this.bUseQuotes = bUseQuotes;
        this.sDelimiter = sDelimiter;
    }    
    
    /**
     * 
     * @param dgParent 
     * @return 
     */
    public boolean isCompatibleWith(DataGrid dgParent)
    {
        return true;
    }
    
    /**
     * 
     * @param dgParent 
     * @throws tibes.DataGrid.DataException 
     */
    public void write(DataGrid dgParent) throws DataException
    {
        if (this.isCompatibleWith(dgParent))
        {
            String sQuote = "\"";
            if (!bUseQuotes) {
                sQuote = "";
            }
            StringBuffer sbOutput = new StringBuffer();
            
            if (bWriteHeader)
            {
                for (int nC = 0; nC < dgParent.getFormat().getNumFields(); nC++)
                {
                    String sFieldName = dgParent.getFormat().getFieldType(nC).getFieldName();
                    if (StringUtil.isNotBlankStr(sQuote)) {
                        sFieldName = sFieldName.replace(sQuote, sQuote+sQuote);
                    }                       
                    if (nC > 0) {
                        sbOutput.append(sDelimiter);
                    }
                    sbOutput.append(sQuote+sFieldName+sQuote);
                }                
                sbOutput.append("\n");
            }            
            
            for (int nR = 0; nR < dgParent.getNumRows(); nR++)
            {
                for (int nC = 0; nC < dgParent.getNumColumns(); nC++)
                {
                    String sField = dgParent.getCellAsString(nR, nC);
                    if (StringUtil.isNotBlankStr(sQuote)) {
                        sField = sField.replace(sQuote, sQuote+sQuote);
                    }            
                    if (nC > 0) {
                        sbOutput.append(sDelimiter);
                    }
                    sbOutput.append(sQuote+sField+sQuote);
                }
                sbOutput.append("\n");
            }
            
            try
            {
                BufferedWriter bwOutput = null;
                if (osOut == null) {
                    osOut = new FileOutputStream(sOutputFilename);
                }
                bwOutput = new BufferedWriter(new OutputStreamWriter(osOut, sTextFormat));
                bwOutput.write(sbOutput.toString());
                bwOutput.flush();
                if (this.bCloseOutput) {
                    bwOutput.close();                
                }
            }
            catch (IOException ioe)
            {
                throw new DataException("Error writing output file: "+ioe.getMessage());
            }
        }
        else
        {
            throw new DataException("This data writer is not compatible with the source grid.");
        }        
    }

    /**
     * 
     * @return 
     */
    public String getTextFormat() {
        return sTextFormat;
    }

    /**
     * 
     * @param sTextFormat 
     */
    public void setTextFormat(String sTextFormat) {
        this.sTextFormat = sTextFormat;
    }
}
