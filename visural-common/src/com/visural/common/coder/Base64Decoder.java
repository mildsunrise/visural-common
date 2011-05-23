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
 * Basic Base64 decoder
 * 
 * @version $Id: Base64Decoder.java 31 2010-05-21 07:15:23Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class Base64Decoder {
    private static final byte[] encodeLookup = {'A','B','C','D','E','F','G','H',
        'I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
        'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r',
        's','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7','8','9',
        '+','/'};
    private static byte[] decodeLookup;
    private byte[] outBuf = new byte[3];        
    
    static {
        // build reverse lookup table
        decodeLookup = new byte[256];
        for (int n = 0; n < decodeLookup.length; n++) {
            decodeLookup[n] = (byte)0xFF;
        }
        for (int n = 0; n < encodeLookup.length; n++) {
            decodeLookup[encodeLookup[n]] = (byte)n;
        }
    }
    
    public Base64Decoder() {
        
    }
           
    private int minorDecode(byte[] inBuf, OutputStream os) throws IOException {
        int nChars = 3;
        if (inBuf[3] == '=' && inBuf[2] == '=') {
            nChars = 1;
        } else if (inBuf[3] == '=') {
            nChars = 2;
        }
        switch (nChars) {
            case 1: if (decodeLookup[inBuf[0]] == (byte)0xFF || decodeLookup[inBuf[1]] == (byte)0xFF) {
                        throw new IOException("Invalid data detected in Base 64 stream.");
                    }
                    outBuf[1] = 0x00; outBuf[2] = 0x00;
                    break;
            case 2: if (decodeLookup[inBuf[0]] == (byte)0xFF || decodeLookup[inBuf[1]] == (byte)0xFF || decodeLookup[inBuf[2]] == (byte)0xFF) {
                        throw new IOException("Invalid data detected in Base 64 stream.");
                    }
                    outBuf[2] = 0x00;
                    break;                             
            case 3: if (decodeLookup[inBuf[0]] == (byte)0xFF || decodeLookup[inBuf[1]] == (byte)0xFF || decodeLookup[inBuf[2]] == (byte)0xFF || decodeLookup[inBuf[3]] == (byte)0xFF) {
                        throw new IOException("Invalid data detected in Base 64 stream.");
                    }
                    break;
        }      
        outBuf[0] = (byte)((decodeLookup[inBuf[0]] << 2)+((decodeLookup[inBuf[1]]&0x30) >> 4));
        outBuf[1] = (byte)(((decodeLookup[inBuf[1]]&0x0F) << 4)+((decodeLookup[inBuf[2]]&0xFC) >> 2));
        outBuf[2] = (byte)(((decodeLookup[inBuf[2]]&0x03) << 6)+((decodeLookup[inBuf[3]]&0x3F)));        
        for (int n = 0; n < nChars; n++) {
            os.write(outBuf[n]);
        }               
        
        return nChars;
    }
    
    public void decode(InputStream is, OutputStream os) throws IOException {
        int nIn = 0;
        byte[] inbuf = new byte[4];
        int n = 0;
        int nDecBlock;
        do {
            nDecBlock = -1;
            n = is.read();
            if (n != -1 && n != '\n'&& n != '\r') {
                inbuf[nIn++] = (byte)n;
                if (nIn == 4) {
                    nDecBlock = minorDecode(inbuf, os);
                    nIn = 0;
                }                
            }
        } while (n != -1 && (nDecBlock == -1 || nDecBlock == 3));
        if (nIn > 0) {
            throw new IOException("base 64 decoder expects data in 4 byte chunks, encountered extra "+nIn+" bytes");
        }
        os.flush();        
    }
}
