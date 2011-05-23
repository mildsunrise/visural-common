/*
 *  Copyright 2010 Richard Nichols.
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
package com.visural.common.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import sun.net.ftp.FtpClient;
import sun.net.TelnetOutputStream;

/**
 * This class sends a file by FTP to a remote server.
 *
 * In the event of a **failure** such as...
 *
 *  - Connection terminated
 *  - Remote filesystem error
 *  - Apocalypse
 *
 * ...an `IOException` will be thrown.
 *
 * @version $Id: FTPSend.java 98 2010-11-21 06:22:39Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class FTPSend {

    /**
     * Sends a file by FTP to a remote server.
     * 
     * @param sSource The source file to be transferred.
     * @param sHost The remote host (IP or domain name).
     * @param sHostFolder The folder on the host to **PUT** the file into.
     * @param sUser The username for the FTP account.
     * @param sPass The password for the FTP account. *Note that the password will not be encrypted.*
     * @throws java.io.IOException In the event of apocalypse.
     */
    public static void ftpSend(String sSource, String sHost, String sHostFolder, String sUser, String sPass) throws IOException {
        try {
            File fSource = new File(sSource);
            if (fSource.exists() && fSource.isDirectory()) {       
                // full folder send
                FtpClient ftp = new FtpClient();
                ftp.openServer(sHost);
                ftp.login(sUser, sPass);
                ftp.binary();
                ftp.cd(sHostFolder);

                File[] fa = fSource.listFiles();
                for (int nF = 0; nF < fa.length; nF++) {
                    TelnetOutputStream out = ftp.put(fa[nF].getName());                            
                    FileInputStream fis = new FileInputStream(fa[nF]);
                    byte[] ba = new byte[32000];
                    int len = 0;
                    while ((len = fis.read(ba, 0, ba.length)) != -1) {
                        out.write(ba, 0, len);                                
                    }
                    out.flush();
                    out.close();
                    fis.close();
                    fis = null;                                  
                }
                ftp.closeServer();
            } else if (fSource.exists()) {
                // single file send
                FtpClient ftp = new FtpClient();
                ftp.openServer(sHost);
                ftp.login(sUser, sPass);
                ftp.binary();
                ftp.cd(sHostFolder);
                TelnetOutputStream out = ftp.put(fSource.getName());                            
                FileInputStream fis = new FileInputStream(fSource);
                byte[] ba = new byte[32000];
                int len = 0;
                while ((len = fis.read(ba, 0, ba.length)) != -1) {
                    out.write(ba, 0, len);                                
                }
                out.flush();
                out.close();
                fis.close();
                fis = null;                                  
                ftp.closeServer();                
            } else {
                throw new Exception("Error - source "+fSource.getPath()+" does not exist.");
            }
        }
        catch (Exception e) {
            throw new IOException(e.toString());
        }            
    }
    
}
