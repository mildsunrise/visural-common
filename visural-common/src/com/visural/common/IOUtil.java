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
package com.visural.common;

import java.util.ArrayList;
import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

/**
 * IO related utilities.
 * 
 * @version $Id: IOUtil.java 94 2010-09-29 06:17:07Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class IOUtil extends IOUtils {

    //unnecessary
    public static byte[] readFile(File f) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(f);
            return toByteArray(in);
        } finally {
            if (in != null) closeQuietly(in);
        }
    }

    //FILE I/O stuff
    public static void write(File f, byte[] data) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(f);
            out.write(data);
        } finally {
            if (out != null) closeQuietly(out);
        }
    }

    public static void copy(File s, File t) throws IOException {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(s);
            out = new FileOutputStream(t);
            copyLarge(in, out);
        } finally {
            if (in != null) closeQuietly(in);
            if (out != null) closeQuietly(out);
        }
    }

    //DIGEST stuff
    public static byte[] getSHA1(InputStream is) throws IOException, NoSuchAlgorithmException {
        return getDigest("SHA-1", is);//FIXME: should NoSuchAlgorithm be catched?
    }

    public static byte[] getMD5(InputStream is) throws IOException, NoSuchAlgorithmException {
        return getDigest("MD5", is);
    }

    public static byte[] getSHA1(byte[] data) throws NoSuchAlgorithmException {
        return getDigest("SHA-1", data);
    }

    public static byte[] getMD5(byte[] data) throws NoSuchAlgorithmException {
        return getDigest("MD5", data);
    }

    private static byte[] getDigest(String digest, byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(digest);
        return md.digest(data);
    }

    private static byte[] getDigest(String digest, InputStream in) throws NoSuchAlgorithmException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(8192);//FIXME: why this lenght?
        IOUtils.copy(in, out);
        return getDigest(digest, out.toByteArray());
    }

    //SERIALIZE stuff
    
    public static <T extends Serializable> T readObject(Class<T> clazz, ClassLoader cl, InputStream is) throws IOException {
        ObjectInputStream ois = new ClassLoaderObjectInputStream(cl, is);
        try {
            return (T) ois.readObject();
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        //We don't close any stream, as we're only filtering.
    }

    public static <T extends Serializable> void writeObject(OutputStream os, T obj) throws IOException {
        new ObjectOutputStream(os).writeObject(obj);
        //We don't close any stream, as we're only filtering.
    }
    
    //DB stuff

    public static long getOracleSequenceNextval(Connection con, String sequenceName) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("select " + sequenceName + ".nextval from dual");
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SQLException("No result returned.");
            }
        } finally {
            IOUtil.silentClose(IOUtil.class, rs);
            IOUtil.silentClose(IOUtil.class, ps);
        }
    }

    //CLOSING stuff
    
    public static void silentClose(Class myClass, PreparedStatement ps) {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException se) {
            Logger.getLogger(myClass.getName()).log(Level.SEVERE, "Could not close PreparedStatement in silentClose(...)", se);
        }
    }

    public static void silentClose(Class myClass, Connection con) {
        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException se) {
            Logger.getLogger(myClass.getName()).log(Level.SEVERE, "Could not close Connection in silentClose(...)", se);
        }
    }

    public static void silentClose(Class myClass, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException se) {
            Logger.getLogger(myClass.getName()).log(Level.SEVERE, "Could not close ResultSet in silentClose(...)", se);
        }
    }

    //FILE and URL I/O stuff

    /**
     * Takes the string provided and writes it to the given file. If the
     * file exists it will be overwritten.
     * @param file The file to write the data to
     * @param data The string to write
     * @throws IOException
     */
    public static void stringToFile(File file, String data) throws IOException {
        FileWriter wr = null;
        try {
            wr = new FileWriter(file);
            wr.append(data);
        } finally {
            if (wr != null) closeQuietly(wr);
        }
    }

    /**
     * Read the contents of a given file and return as a String.
     * 
     * @param filename The path to the file to read.
     * @return The (decoded) data.
     * @throws IOException
     */
    public static String fileToString(String filename) throws FileNotFoundException, IOException {
        return fileToString(new File(filename));
    }

    public static String fileToString(File file) throws FileNotFoundException, IOException {
        FileReader r = null;
        try {
            r = new FileReader(file);
            return toString(r);
        } finally {
            if (r != null) closeQuietly(r);
        }
    }

    /**
     * Reads the contents of the given file and returns them as a byte array.
     * 
     * @param filename The path to the file to read.
     * @return The (binary) data.
     * @throws IOException
     */
    public static byte[] readFile(String filename) throws IOException {
        return readFile(new File(filename));
    }

    /**
     * Reads the data of the given URL,
     * decodes it with the platform encoding,
     * and returns it as a String.
     * 
     * @param url The URL to read data from.
     * @return The (decoded) data.
     */
    public static String urlToString(URL url) throws IOException {
        InputStream in = null;
        try {
            in = url.openStream();
            return toString(in);
        } finally {
            if (in != null) closeQuietly(in);
        }
    }
    
    /**
     * Reads the data of the given URL,
     * decodes it with the specified encoding,
     * and returns it as a String.
     * 
     * @param url The URL to read data from.
     * @param encoding The charset to use to decode the data.
     * @return The (decoded) data.
     */
    public static String urlToString(URL url, String encoding) throws IOException {
        InputStream in = null;
        try {
            in = url.openStream();
            return toString(in, encoding);
        } finally {
            if (in != null) closeQuietly(in);
        }
    }

    /**
     * Reads the contents of the given URL and returns them.
     * 
     * @param url The URL to read data from.
     * @return The (binary) data.
     */
    public static byte[] readUrl(URL url) throws IOException {
        InputStream in = null;
        try {
            in = url.openStream();
            return toByteArray(in);
        } finally {
            if (in != null) closeQuietly(in);
        }
    }

    /**
     * Zips the contents of the given folder into the given zip file.
     * 
     * @param destinationZipFilename
     * @param folderToZip
     * @param recurseSubFolders
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void zipFolder(String destinationZipFilename, String folderToZip, boolean recurseSubFolders) throws IOException, FileNotFoundException {
        // check input
        File fOut = new File(destinationZipFilename);
        if (fOut.exists() && !fOut.delete()) {
            throw new IOException("ZIP file " + destinationZipFilename + " already exists and can not be overwritten.");
        }
        File fFolder = new File(folderToZip);
        if (!fFolder.exists() || !fFolder.isDirectory()) {
            throw new FileNotFoundException("Base folder does not exist.");
        }

        // gather file list
        ArrayList<ZipItem> alZI = new ArrayList<ZipItem>();
        zipFolderWorker(alZI, fFolder.getPath(), fFolder.getPath(), recurseSubFolders);

        ZipOutputStream out = null;
        try {
            // create zip
            out = new ZipOutputStream(new FileOutputStream(fOut));
            for (int n = 0; n < alZI.size(); n++) {
                FileInputStream in = null;
                try {
                    ZipEntry ze = new ZipEntry(alZI.get(n).zipName);
                    ze.setTime(System.currentTimeMillis());
                    out.putNextEntry(ze);
                    //Open the source file
                    in = new FileInputStream(alZI.get(n).fileName);
                    //Copy the data
                    copy(in, out);
                    out.closeEntry();
                } finally {
                    if (in != null) closeQuietly(in);
                }
            }
        out.close();
        } finally {
            if (out != null) closeQuietly(out);
        }
    }

    private static void zipFolderWorker(ArrayList<ZipItem> zipItems, String currentFolder, String baseFolder, boolean recurse) {
        File fDir = new File(currentFolder);
        File[] fa = fDir.listFiles();
        for (int n = 0; n < fa.length; n++) {
            if (fa[n].isDirectory()) {
                if (recurse) {
                    zipFolderWorker(zipItems, fa[n].getPath(), baseFolder, true);
                }
            } else {
                ZipItem zi = new ZipItem();
                zi.fileName = fa[n].getPath();
                String sZN = fa[n].getPath();
                if (sZN.startsWith(baseFolder)) {
                    sZN = sZN.substring(baseFolder.length() + 1);
                }
                zi.zipName = sZN;
                zipItems.add(zi);
            }
        }
    }

    private static class ZipItem {
        public String fileName;
        public String zipName;
    }

    /**
     * Recursively deletes this file (and its childs, if it's a directory).
     * 
     * @param folder The file or directory to delete.
     * @throws IOException if an error occurs.
     */
    public static void delete(File f) throws IOException {
        if (f.exists()) {
            if (f.isDirectory())
                for (File child : f.listFiles()) delete(child);
            
            if (!f.delete())
                throw new IOException("Could not delete "+f.getPath());
        }
    }
}
