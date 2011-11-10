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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * IO related utilities.
 * 
 * @version $Id: IOUtil.java 94 2010-09-29 06:17:07Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class IOUtil {

    public static byte[] read(File f) throws IOException {
        FileChannel in = null;
        try {
            in = (new FileInputStream(f)).getChannel();
            MappedByteBuffer b = in.map(FileChannel.MapMode.READ_ONLY, 0, f.length());
            byte[] data = new byte[(int)f.length()];
            b.get(data);
            return data;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    public static void write(File f, byte[] data) throws IOException {
        FileChannel out = null;
        try {
            out = (new FileOutputStream(f)).getChannel();
            out.write(ByteBuffer.wrap(data));
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static void copy(File s, File t) throws IOException {
        FileChannel in = (new FileInputStream(s)).getChannel();
        FileChannel out = (new FileOutputStream(t)).getChannel();
        in.transferTo(0, s.length(), out);
        in.close();
        out.close();
    }

    public static byte[] getSHA1(InputStream is) {
        return getDigest("SHA-1", is);
    }

    
    public static byte[] getMD5(InputStream is) {
        return getDigest("MD5", is);
    }
    
    public static byte[] getSHA1(byte[] data) {
        return getDigest("SHA-1", data);
    }

    
    public static byte[] getMD5(byte[] data) {
        return getDigest("MD5", data);
    }
    
    private static byte[] getDigest(String digest, byte[] data) throws IllegalStateException {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance(digest);
            md.update(data);
            return md.digest();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    private static byte[] getDigest(String digest, InputStream is) throws IllegalStateException {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance(digest);
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) != -1) {
                md.update(buf, 0, r);
            }
            is.close();
            return md.digest();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static <T extends Serializable> T readObject(Class<T> clazz, ClassLoader cl, InputStream is) {
        ObjectInputStream ois = null;
        try {
            ois = new ClassLoaderObjectInputStream(cl, is);
            T result = (T) ois.readObject();
            return result;
        } catch (Exception ex) {
            String error = "An error occurred while reading a '"+clazz.getCanonicalName()+"' object.";
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, error, ex);
            throw new IllegalStateException(error, ex);
        } finally {
            IOUtil.silentClose(IOUtil.class, ois);
            IOUtil.silentClose(IOUtil.class, is);
        }
    }

    public static <T extends Serializable> void writeObject(OutputStream os , T obj) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(os);
            oos.writeObject(obj);
            oos.close();
        } catch (IOException ex) {
            String error = "An error occurred while writing a '"+obj.getClass().getCanonicalName()+"' object.";
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, error, ex);
            throw new IllegalStateException(error, ex);
        } finally {
            IOUtil.silentClose(IOUtil.class, oos);
            IOUtil.silentClose(IOUtil.class, os);
        }
    }

    public static long getOracleSequenceNextval(Connection con, String sequenceName) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("select "+sequenceName+".nextval from dual");
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

    public static void silentClose(Class myClass, InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException se) {
            Logger.getLogger(myClass.getName()).log(Level.SEVERE, "Could not close InputStream in silentClose(...)", se);
        }
    }
    public static void silentClose(Class myClass, OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch (IOException se) {
            Logger.getLogger(myClass.getName()).log(Level.SEVERE, "Could not close OutputStream in silentClose(...)", se);
        }
    }
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

    /**
     * Reads an InputStream to a byte array. Does not close the stream.
     * 
     * @param os
     * @return
     * @throws IOException
     */
    public static byte[] readStream(InputStream is) throws IOException {
        return readStream(is, false);
    }

    /**
     * Reads an InputStream to a byte array. Optionally can close the stream
     * once all data has been read.
     *
     * If closing fails an error is logged.
     * 
     * @param os
     * @param closeStream
     * @return
     * @throws IOException
     */
    public static byte[] readStream(InputStream is, boolean closeStream) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int r;
            while ((r = is.read()) != -1) {
                baos.write(r);
            }
            return baos.toByteArray();
        } finally {
            if (closeStream) {
                try {
                    is.close();
                } catch (IOException e) {
                    Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, "Could not close stream in readStream()", e);
                }
            }
        }
    }

    /**
     * Takes the string provided and writes it to the given file. If the
     * file exists it will be overwritten.
     * @param filename
     * @param data
     * @throws IOException
     */
    public static void stringToFile(String filename, String data) throws IOException {
        BufferedWriter writer = null;
        try {
            int pathIndex = Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\"));
            if (pathIndex > 0) {
                String folderName = filename.substring(0, pathIndex);
                new File(folderName).mkdirs();
            }
            writer = new BufferedWriter(new FileWriter(filename));
            writer.write(data);
            writer.flush();
            writer.close();
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, "Could not close stream in stringToFile()", ex);
            }
        }
    }
    
    /**
     * Read the contents of the given file and return as a string
     * @param filename
     * @return
     * @throws IOException
     */
    public static String fileToString(String filename) throws IOException {
        File f = new File(filename);
        if (f.exists()) {
            BufferedReader inputReader = new BufferedReader(new FileReader(f));
            String line = "";
            StringBuffer result = new StringBuffer();
            while ((line = inputReader.readLine()) != null) {
                if (result.length() > 0) {
                    result.append('\n');
                }
                result.append(line);
            }
            inputReader.close();
            return result.toString();
        } else {
            throw new IOException("File " + filename + " does not exist.");
        }
    }

    /**
     * Reads the contents of the given file and returns it as a byte array
     * @param filename
     * @return
     * @throws IOException
     */
    public static byte[] fileToByteArray(String filename) throws IOException {
        byte[] baResult = null;
        File f = new File(filename);
        if (f.exists()) {
            baResult = new byte[(int) f.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
            int n = 0;
            int r = 0;
            do {
                r = bis.read();
                if (r >= 0) {
                    baResult[n++] = (byte) r;
                }
            } while (r >= 0);
            bis.close();
        }
        return baResult;
    }

    /**
     * Reads the contents of the given URL and returns it as a string.
     * @param url
     * @return
     */
    public static String urlToString(URL url) throws IOException {
        StringBuffer sb = new StringBuffer("");
        InputStream is = url.openStream();
        int n = 0;
        do {
            n = is.read();
            if (n >= 0) {
                sb.append((char) n);
            }
        } while (n >= 0);
        is.close();
        return sb.toString();
    }


    /**
     * Reads the contents of the given URL and returns it as a string.
     * @param url
     * @return
     */
    public static byte[] urlToBytes(URL url) throws IOException {
        InputStream is = url.openStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int n = 0;
        do {
            n = is.read();
            if (n >= 0) {
                baos.write(n);
            }
        } while (n >= 0);
        is.close();
        baos.close();
        return baos.toByteArray();
    }

    /**
     * Zips the contents of the given folder into the given zip file.
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

        // create zip
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(fOut));
        for (int n = 0; n < alZI.size(); n++) {
            byte[] ba = fileToByteArray(alZI.get(n).fileName);
            ZipEntry ze = new ZipEntry(alZI.get(n).zipName);
            ze.setTime(System.currentTimeMillis());
            zos.putNextEntry(ze);
            zos.write(ba);
            zos.closeEntry();
        }
        zos.close();
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
     * Deletes all files and folder under the path specified.
     * @param folder
     * @throws IOException
     */
    public static void nukeFolder(String folder) throws IOException {
        File f = new File(folder);
        if (f.exists() && f.isDirectory()) {
            File[] fa = f.listFiles();
            for (int n = 0; n < fa.length; n++) {
                if (fa[n].isDirectory()) {
                    nukeFolder(fa[n].getPath());
                } else {
                    if (!fa[n].delete()) {
                        throw new IOException("Failed nuking " + folder + " - could not delete file '" + f.getPath() + "'");
                    }
                }
            }
        }
        f.delete();
    }
}
