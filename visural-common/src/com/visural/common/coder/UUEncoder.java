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
package com.visural.common.coder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Encodes data in uuencode format (same as unix)
 * 
 * @version $Id: UUEncoder.java 31 2010-05-21 07:15:23Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class UUEncoder {
    
    private int nLineCount;
    private int nLineInput;
    private byte[] currentLine = new byte[61];
    private String fileName = "unknown-file-name";
    private String unixFileMode = "600";
    
    public UUEncoder() {
        
    }
           
    public UUEncoder(String fileName, String unixFileMode) {
        this.fileName = fileName;
        this.unixFileMode = unixFileMode;
    }
           
    private void minorEncode(int nChars, byte[] inBuf, OutputStream os) throws IOException {
        if (nChars > 0 && nChars <= 3) {
            switch (nChars) {
                case 1: inBuf[1] = 0x01; inBuf[2] = 0x01;
                        break;
                case 2: inBuf[2] = 0x01;
                        break;                             
            }            
            currentLine[nLineCount+1] = (byte)((inBuf[0] >>> 2) + ' ');
            currentLine[nLineCount+2] = (byte)(((inBuf[0]&0x03) << 4) + (inBuf[1] >>> 4) + ' ');
            currentLine[nLineCount+3] = (byte)(((inBuf[1]&0x0F) << 2) + (inBuf[2] >>> 6) + ' ');
            currentLine[nLineCount+4] = (byte)((inBuf[2]&0x3F) + ' ');            
            nLineInput += nChars;
            nLineCount += 4;           
            if (nLineCount == 60) { 
                completeEncode(os);
            }
        }        
    }
    
    private void completeEncode(OutputStream os) throws IOException {
        currentLine[0] = (byte)(nLineInput+' ');
        if (nLineCount == 60) {
            os.write(currentLine);            
        } else {
            for (int n = 0; n < nLineCount+1; n++) {
                os.write(currentLine[n]);
            }                    
        }
        os.write((byte)'\n');
        nLineCount = 0;
        nLineInput = 0;
    }
    
    public void encode(InputStream is, OutputStream os) throws IOException {
        String sBegin = "begin "+this.unixFileMode+" "+this.fileName;
        char [] begin = sBegin.toCharArray();
        for (int n = 0; n < begin.length; n++) {
            os.write((byte)begin[n]);
        }        
        os.write((byte)'\n');
        nLineCount = 0;
        nLineInput = 0;
        int nIn = 0;
        byte[] inbuf = new byte[3];
        int n = 0;
        do {
            n = is.read();
            if (n != -1) {
                inbuf[nIn++] = (byte)n;
                if (nIn == 3) {
                    minorEncode(3, inbuf, os);
                    nIn = 0;
                }                
            }
        } while (n != -1);
        if (nIn > 0) {
            minorEncode(nIn, inbuf, os);
            completeEncode(os);
        }
        byte[] finish = {' ','\n','e','n','d','\n'};
        os.write(finish);
        os.flush();        
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUnixFileMode() {
        return unixFileMode;
    }

    public void setUnixFileMode(String unixFileMode) {
        this.unixFileMode = unixFileMode;
    }    
}
