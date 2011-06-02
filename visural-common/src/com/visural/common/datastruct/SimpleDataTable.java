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
package com.visural.common.datastruct;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.List;

/**
 * This SimpleDataTable class will store a table in memory with column headers, all as 
 * strings. It can read a table from a delimited file with error checking.
 * It can write itself to a delimted file. It can perform data manipulation 
 * by adding /removing rows and modifying row values.
 *
 * TODO: rewrite - this is very old code.
 *
 * @author Richard Nichols
 * @version $Id: SimpleDataTable.java 17 2010-02-04 23:52:05Z tibes80@gmail.com $
 */
public class SimpleDataTable implements Serializable {
    private static final boolean WRITE_HEADER_DEFAULT = true;

    private boolean ignoreCase = false;
    private String quote = "\"";
    private String delimiter = "\t"; /* multiple chars not supported at this point */

    private List header;
    private List rows;
    private boolean valid;

    SimpleDataTable() {
        valid = false;
    }

    /**
     * Creates a new instance of a SimpleDataTable using a SimpleDataTable as a base
     * @param dtCopy The data table to copy
     */
    public SimpleDataTable(SimpleDataTable dtCopy) {
        this();
        this.header = new ArrayList();
        this.rows = new ArrayList();
        for (int nLp = 0; nLp < dtCopy.header.size(); nLp++) {
            this.header.add((String) dtCopy.header.get(nLp));
        }
        for (int nR = 0; nR < dtCopy.rows.size(); nR++) {
            List vRow = (List) dtCopy.rows.get(nR);
            List vNewRow = new ArrayList();
            for (int nLp = 0; nLp < vRow.size(); nLp++) {
                vNewRow.add((String) vRow.get(nLp));
            }
            this.rows.add(vNewRow);
        }
        this.valid = dtCopy.valid;
    }

    /**
     * Creates a new instance of a SimpleDataTable and read it from the filename provided
     * @param sFilename The filename to read data from
     */
    public SimpleDataTable(File file) throws FileNotFoundException, IOException {
        this();
        this.readDelimitedFile(file.getCanonicalPath());
    }

    /**
     * Creates a new instance of a SimpleDataTable and read it from the filename provided
     * @param sFilename The filename to read data from
     */
    public SimpleDataTable(BufferedReader brIn) throws IOException {
        this();
        this.readDelimitedFile(brIn);
    }

    /**
     * Creates a new instance of a SimpleDataTable using the header format provided
     * @param vCustomHeader The List containing strings of the header columns required
     */
    public SimpleDataTable(List vCustomHeader) {
        this();
        this.customFormat(vCustomHeader);
    }

    public SimpleDataTable(String... customHeader) {
        this();
        this.customFormat(Arrays.asList(customHeader));
    }

    /**
     * Allows the file delimter string to be defined
     * @param sNewDelimiter The delimiter to use
     */
    public void setDelimiter(String sNewDelimiter) {
        delimiter = sNewDelimiter;
    }

    /**
     * Makes a new blank table with the column headers indicated
     * @param vCustom The List containing strings of the header columns required
     */
    public void customFormat(List vCustom) {
        header = new ArrayList(vCustom);
        rows = new ArrayList();
        valid = true;
    }

    public List getHeader() {
        List vResult = null;

        if (valid && header != null) {
            vResult = new ArrayList();
            vResult.addAll(header);
        }

        return vResult;
    }

    /**
     * Adds a new column to the datatable
     * @param sName The column name
     * @param sDefault the default value for any rows in the table, or null if not required
     */
    public void addColumn(String sName, String sDefault) {
        if (sDefault == null) {
            sDefault = "";
        }

        header.add(sName);
        for (int nRow = 0; nRow < rows.size(); nRow++) {
            List vCurRow = (List) rows.get(nRow);
            vCurRow.add(sDefault);
        }
    }

    /**
     * Returns the number of columns in table
     * @return the number of columns in table
     */
    public int getNumColumns() {
        if (valid) {
            return header.size();
        }

        return 0;
    }

    /**
     * Returns the number of rows in table
     * @return the number of rows in table
     */
    public int getNumRows() {
        if (valid) {
            return rows.size();
        }

        return 0;
    }

    /* private helper functions */
    /* searches through the header List for the given column name and returns the index */
    private int columnIndex(String sColumn) {
        int nResult = -1;

        if (valid) {
            for (int nFind = 0; nFind < header.size(); nFind++) {
                if (((String) header.get(nFind)).compareTo(sColumn) == 0) {
                    nResult = nFind;
                    break;
                }
            }
        }

        return nResult;
    }

    /* returns an allocated but empty row */
    private List getBlankRow() {
        if (valid) {
            List vResult = new ArrayList();

            for (int nLp = 0; nLp < header.size(); nLp++) {
                vResult.add("");
            }

            return vResult;
        }

        return null;
    }

    /* takes a string and delimits it into a List of strings */
    private List returnProcessedArray(String sDelimed, boolean bQuoted) {
        if (sDelimed == null) {
            return new ArrayList();
        }

        StringTokenizer stToken = new StringTokenizer(sDelimed, delimiter, true);
        List vResult = new ArrayList();
        boolean bDelim = false;
        boolean bQuote = false;
        String sToken;
        String sQuoteToken = null;

        while (stToken.hasMoreTokens()) {
            sToken = stToken.nextToken();
            if (bQuoted && bQuote && sToken.trim().endsWith(quote)) {
                // in this case we are processing quoted string, and are inside a quote, this token contains the end of the quote
                sQuoteToken = sToken.trim().substring(0, sToken.trim().length() - 1);
                vResult.add(sQuoteToken);
                bQuote = false;
                bDelim = false;
            } else if (bQuoted && bQuote) {
                // in this case we are processing quote string and this section does not contain either the start of end of the quote
                sQuoteToken += sToken;
            } else if (bQuoted && !bQuote && sToken.trim().startsWith(quote) && !sToken.trim().endsWith(quote)) {
                // in this case we are processing quoted strings and we have the first part of a quoted set
                sQuoteToken = sToken.trim().substring(1, sToken.trim().length());
                bQuote = true;
            } else if (bQuoted && !bQuote && sToken.trim().startsWith(quote) && sToken.trim().endsWith(quote)) {
                // this is a complete quoted string, so we must split the start and end quotes off
                sToken = sToken.trim();
                if (sToken.compareTo(quote + quote) == 0) {
                    sToken = "";
                } else {
                    sToken = sToken.substring(1, sToken.length() - 1);
                }
                vResult.add(sToken);
                bDelim = false;
            } else if (sToken.indexOf(delimiter) != -1) {
                if (bDelim) {
                    vResult.add("");
                }
                bDelim = true;
            } else {
                vResult.add(sToken);
                bDelim = false;
            }
        }

        return vResult;
    }

    /* processes the header string line */
    private boolean processHeader(String sHeader) {
        header = returnProcessedArray(sHeader, false);

        if (header != null) {
            return true;
        }

        return false;
    }

    /* processes a generic row */
    private boolean processRow(String sRow, boolean bQuoted) {
        boolean bResult = false;
        List vRow = returnProcessedArray(sRow, bQuoted);
        if (vRow.size() == header.size()) {
            rows.add(vRow);
            bResult = true;
        } else if (vRow.size() == header.size() - 1) {
            vRow.add("");
            rows.add(vRow);
            bResult = true;
        }

        return bResult;
    }

    /**
     * 
     * @param sFilename 
     * @return 
     */
    public int readDelimitedFile(String sFilename) throws FileNotFoundException, IOException {
        return readDelimitedFile(sFilename, true, false, false);
    }

    public int readDelimitedFile(BufferedReader brInput) throws IOException {
        return readDelimitedFile(brInput, true, false, false);
    }

    public int readDelimitedFile(BufferedReader brInput, boolean bReadHeader, boolean bQuoted, boolean bPreprocess) throws IOException {
        int nRead = 0;
        String sLine;
        /* if the file is ready to read.. */
        if (brInput.ready()) {
            if (bReadHeader) {
                /* read the header row */
                processHeader(brInput.readLine());
            }

            /* read data rows */
            rows = new ArrayList();
            while (brInput.ready()) {
                sLine = brInput.readLine();
                /* attempt to process read line, if error then display the bogus row */
                if (processRow(sLine, bQuoted)) {
                    nRead++;
                }
            }
            valid = true;
        }

        return nRead;
    }

    /**
     * reads a text file delimted with the string defined earlier.
     * The file must have a header row.
     * Blank columns are dealt with.
     * Rows are validated by the header row.
     * Displays all invalid rows.
     * Returns the number of valid rows stored.
     * @return the number of rows that were read from the file
     * @param bReadHeader 
     * @param bQuoted 
     * @param bPreprocess 
     * @param sFilename The filename to read
     */
    public int readDelimitedFile(String sFilename, boolean bReadHeader, boolean bQuoted, boolean bPreprocess) throws FileNotFoundException, IOException {
        int nRead = 0;

        File fInput = new File(sFilename);

        if (fInput.exists()) {
            BufferedReader brInput = new BufferedReader(new FileReader(fInput));

            nRead = readDelimitedFile(brInput, bReadHeader, bQuoted, bPreprocess);

            brInput.close();
        }

        /* return count of read rows */
        return nRead;
    }

    /**
     * Overloaded version of the file writer, defaults the header being written.
     * @param sFilename file to write
     * @return the number of rows written to the file
     */
    public int writeDelimitedFile(String filename) throws IOException {
        return writeDelimitedFile(filename, WRITE_HEADER_DEFAULT);
    }

    /**
     * Writes a file delimited by the character defined earlier.
     * Returns the number of succesfully written records (including header as a record)
     * @param sFilename the file to write
     * @param bWriteHeader whether the header should be written out as the first row
     * @return the number of rows written to the file
     */
    public int writeDelimitedFile(String filename, boolean writeHeader) throws IOException {
        int rowsWritten = 0;

        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(new File(filename)));
            List vRow;

            /* A little trickery for writing / not writing the header row */
            int nR;
            if (writeHeader) {
                nR = -1;
            } else {
                nR = 0;
            }

            for (; nR < getNumRows(); nR++) {
                if (nR == -1) {
                    vRow = getHeader();
                } else {
                    vRow = getRow(nR);
                }

                /* Output delimited data */
                for (int nC = 0; nC < vRow.size(); nC++) {
                    output.write((String) vRow.get(nC));
                    if (nC == vRow.size() - 1) {
                        output.write("\n");
                    } else {
                        output.write(delimiter);
                    }
                }

                /* increment count and flush buffer */
                rowsWritten++;
                output.flush();
            }
        } finally {
            if (output != null) {
                output.close();
            }
        }

        return rowsWritten;
    }

    /* data grabbing functions */
    /**
     * Get Cell
     *
     * Returns the cell contents as a String at the position specified
     */
    public String getCell(int nRow, int nCol) {
        if (valid) {
            String sResult = null;
            List vRow = (List) rows.get(nRow);
            sResult = (String) vRow.get(nCol);
            return sResult;
        }

        return null;
    }

    /**
     * Returns the cell contents as a String at the position specified
     * @param nRow the number of the row to read
     * @param sColumn the column to read from
     * @return the value of the cell
     */
    public String getCell(int nRow, String sColumn) {
        String sResult = null;

        if (valid) {
            int nColIdx = columnIndex(sColumn);
            if (nColIdx != -1) {
                List vRow = (List) rows.get(nRow);
                sResult = (String) vRow.get(nColIdx);
            }
        }

        return sResult;
    }

    /**
     * Get Row
     *
     * Returns the row with the index provided as a List of strings.
     */
    List getRow(int nRow) {
        if (valid) {
            return (List) rows.get(nRow);
        }

        return null;
    }

    /* data setting functions */
    /**
     * Removes the row with the index given
     * @param nIndex The row index to delete
     */
    public void deleteRow(int nIndex) {
        if (valid) {
            rows.remove(nIndex);
        }
    }

    /**
     * Sets the given cell to the given value
     * @param nRow the row to set
     * @param sColumn the column to set
     * @param sValue the value to assign
     * @return whether the given row/column was found and set.
     */
    public boolean setCell(int nRow, String sColumn, String sValue) {
        boolean bResult = false;

        if (sValue == null) {
            sValue = "";
        }

        if (valid) {
            int nColIdx = columnIndex(sColumn);
            if (nColIdx != -1) {
                List vRow = (List) rows.get(nRow);
                vRow.set(nColIdx, sValue);
                bResult = true;
            }
        }

        return bResult;
    }

    /**
     * Sets the given cell to the given value
     * @param nRow the row to set
     * @param nColumn the column to set
     * @param sValue the value to assign
     * @return whether the given row/column was found and set.
     */
    public boolean setCell(int nRow, int nColumn, String sValue) {
        boolean bResult = false;

        if (sValue == null) {
            sValue = "";
        }

        if (valid) {
            int nColIdx = nColumn;
            if (nColIdx != -1) {
                List vRow = (List) rows.get(nRow);
                vRow.set(nColIdx, sValue);
                bResult = true;
            }
        }

        return bResult;
    }

    /**
     * Adds a blank row to the table and returns the index of the row
     * @return the index of the new row
     */
    public int addRow() {
        int nResult = -1;

        if (valid) {
            List vNewRow = getBlankRow();
            rows.add(vNewRow);
            nResult = rows.size() - 1;
        }

        return nResult;
    }

    /**
     * Returns a duplicate object with the same column structure but without any row data
     * @return a duplicate object with the same column structure but without any row data
     */
    public SimpleDataTable emptyDuplicate() {
        if (valid) {
            SimpleDataTable dtResult = new SimpleDataTable();
            dtResult.delimiter = this.delimiter;
            dtResult.header = new ArrayList(this.header);
            dtResult.rows = new ArrayList();
            dtResult.valid = true;

            return dtResult;
        }

        return null;
    }

    /**
     * Returns true if valid data has been entered into the table.
     * @return if valid data has been entered into the table.
     */
    public boolean isDataValid() {
        return valid;
    }

    /**
     * Returns the given row concatenated with the delimiter
     * @param nRow the number of the row.
     * @return the string concatenation.
     */
    public String getConcatRow(int nRow) {
        if (valid) {
            StringBuffer sbCatRow = new StringBuffer("");

            for (int nCol = 0; nCol < getNumColumns(); nCol++) {
                sbCatRow.append((String) ((List) rows.get(nRow)).get(nCol));
                if (nCol == getNumColumns() - 1) {
                    sbCatRow.append("\n");
                } else {
                    sbCatRow.append(delimiter);
                }
            }

            return sbCatRow.toString();
        }

        return null;
    }

    /**
     * Removes all duplicate rows and returns the number of duplicates removed.
     * A row is considered duplicate if ALL COLUMNS match.
     * @return the number of rows removed.
     */
    public int removeDuplicates() {
        return removeDuplicates(isIgnoreCase());
    }

    /**
     * Removes all duplicate rows and returns the number of duplicates removed.
     * A row is considered duplicate if ALL COLUMNS match.
     * @param bIgnoreCase Whether to ignore upper/lower case.
     * @return the number of rows removed.
     */
    public int removeDuplicates(boolean bIgnoreCase) {
        int nDuplicates = 0;

        if (valid) {
            for (int nR = 0; nR < getNumRows(); nR++) {
                String sCurrentRow = getConcatRow(nR);

                for (int nCheckR = getNumRows() - 1; nCheckR > nR; nCheckR--) {
                    String sCheckRow = getConcatRow(nCheckR);

                    if (bIgnoreCase) {
                        if (sCheckRow.compareToIgnoreCase(sCurrentRow) == 0) {
                            rows.remove(nCheckR);
                            nDuplicates++;
                        }
                    } else {
                        if (sCheckRow.compareTo(sCurrentRow) == 0) {
                            rows.remove(nCheckR);
                            nDuplicates++;
                        }
                    }
                }
            }
        }

        return nDuplicates;
    }

    /**
     * Gets a the row number for a column value that matches one being passed in
     * @param sColumn the column to match on
     * @param sColVal the value to search for
     * @return the number of the row that contains the given column/value pair or -1 if no row is found
     */
    public int getMatchingRow(String sColumn, String sColVal) {
        int nRow = -1;

        if (valid) {
            for (int nLp = 0; nLp < getNumRows(); nLp++) {
                if (ignoreCase) {
                    if (getCell(nLp, sColumn).compareToIgnoreCase(sColVal) == 0) {
                        nRow = nLp;
                        break;
                    }
                } else {
                    if (getCell(nLp, sColumn).compareTo(sColVal) == 0) {
                        nRow = nLp;
                        break;
                    }
                }
            }
        }

        return nRow;
    }

    /**
     * Gets a particular column value for the first match of a value in a 
     * particular column.
     * @param sColumn the column to match on
     * @param sColVal the value to search for
     * @param sColumnReturn the column to return from the found row
     * @return the value of the result column in the 1st matching search column
     */
    public String getOptionValue(String sColumn, String sColVal, String sColumnReturn) {
        String sResult = "";

        if (valid) {

            for (int nLp = 0; nLp < getNumRows(); nLp++) {
                if (ignoreCase) {
                    if (getCell(nLp, sColumn).compareToIgnoreCase(sColVal) == 0) {
                        sResult = getCell(nLp, sColumnReturn);
                        break;
                    }
                } else {
                    if (getCell(nLp, sColumn).compareTo(sColVal) == 0) {
                        sResult = getCell(nLp, sColumnReturn);
                        break;
                    }
                }
            }
        }

        return sResult;
    }

    /**
     * Gets a particular column value for the first match of a value in a 
     * particular column.
     * @return the value of the result column in the 1st matching search column
     * @param sColumn1 
     * @param sCol1Val 
     * @param sColumn2 
     * @param sCol2Val 
     * @param sColumnReturn the column to return from the found row
     */
    public String getTwoOptionValue(String sColumn1, String sCol1Val, String sColumn2, String sCol2Val, String sColumnReturn) {
        String sResult = null;

        if (valid) {

            for (int nLp = 0; nLp < getNumRows(); nLp++) {
                if (ignoreCase) {
                    if (getCell(nLp, sColumn1).compareToIgnoreCase(sCol1Val) == 0 &&
                            getCell(nLp, sColumn2).compareToIgnoreCase(sCol2Val) == 0) {
                        sResult = getCell(nLp, sColumnReturn);
                        break;
                    }
                } else {
                    if (getCell(nLp, sColumn1).compareTo(sCol1Val) == 0 &&
                            getCell(nLp, sColumn2).compareTo(sCol2Val) == 0) {
                        sResult = getCell(nLp, sColumnReturn);
                        break;
                    }
                }
            }
        }

        return sResult;
    }

    public void quickOutput(PrintStream stream) {
        for (int nR = 0; nR < getNumRows(); nR++) {
            stream.print(nR + ": " + getConcatRow(nR));
        }
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean bIgnoreCase) {
        this.ignoreCase = bIgnoreCase;
    }

    public static SimpleDataTable newFromSQL(PreparedStatement ps) throws SQLException {
        SimpleDataTable dtResult = null;

        ResultSet rs = ps.executeQuery();
        int nColCount = 0;
        while (rs.next()) {
            // first time through we init the datatable based on the result set meta data
            if (dtResult == null) {
                List vHeader = new ArrayList();
                ResultSetMetaData rsmd = rs.getMetaData();
                nColCount = rsmd.getColumnCount();
                for (int nC = 0; nC < nColCount; nC++) {
                    String sCol = rsmd.getColumnLabel(nC + 1).toUpperCase(); // rs indexed from 1
                    vHeader.add(sCol);
                }
                dtResult = new SimpleDataTable(vHeader);
            }

            // now retrieve new row
            int nRow = dtResult.addRow();
            for (int nC = 0; nC < nColCount; nC++) {
                String sCurrent = rs.getString(nC + 1); // rs indexed from 1
                dtResult.setCell(nRow, nC, sCurrent);
            }
        }
        rs.close();
        ps.close();

        return dtResult;
    }

    public void writeToCSV(String sFilename) throws IOException {
        BufferedWriter bwOut = null;
        try {
            bwOut = new BufferedWriter(new FileWriter(sFilename));
            StringBuffer sb = new StringBuffer("");
            for (int nC = 0; nC < header.size(); nC++) {
                if (nC != 0) {
                    sb.append(",");
                }
                sb.append(header.get(nC).toString());
            }
            sb.append("\n");
            for (int nR = 0; nR < this.getNumRows(); nR++) {
                for (int nC = 0; nC < this.getNumColumns(); nC++) {
                    if (nC != 0) {
                        sb.append(",");
                    }
                    sb.append("\"");
                    sb.append(this.getCell(nR, nC));
                    sb.append("\"");
                }
                sb.append("\n");
            }
            bwOut.write(sb.toString());
            bwOut.flush();
        } finally {
            if (bwOut != null) {
                bwOut.close();
            }
        }
    }
}
