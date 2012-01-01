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
package com.visural.common.web.lesscss;

import com.visural.common.IOUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;

public class LessCSS {

    public final static String LESSJS_LOCATION = "less.js";
    public final static String RUNJS_LOCATION = "run.js";
    public final static String BUNDLED_CHARSET = "UTF-8";


    private final ScriptableObject so;

    /**
     * Initializes a new Less.JS engine from the given scriptfile.
     **/
    public LessCSS(Reader lessjs) throws IOException {
        InputStream runjs = null;
        Context c = null;
        try {
            runjs = getClass().getResourceAsStream(RUNJS_LOCATION);
            c = new ContextFactory().enterContext();
            c.setOptimizationLevel(9);
            so = c.initStandardObjects();
            //FIXME: check if eval went well
            c.evaluateReader(so, lessjs, "less.js", 1, null);
            c.evaluateReader(so, new InputStreamReader(runjs, BUNDLED_CHARSET), "run.js", 1, null);
        } catch (IOException ex) {
            throw new IOException("Failed reading javascript less.js", ex);
        } finally {
            if (runjs != null) IOUtil.closeQuietly(runjs);
            if (c != null) Context.exit();
        }
    }

    /**
     * Initializes and returns a new Less.JS engine (bundled).
     **/
    public static LessCSS newBundled() {
        Reader r = null;
        try {
            r = new InputStreamReader(
                LessCSS.class.getResourceAsStream(LESSJS_LOCATION),
                Charset.forName(BUNDLED_CHARSET));
            return new LessCSS(r);
        } catch (IOException ex) {
            //We're reading from a bundled file,
            //so there shouldn't be any I/O exception.
            throw new RuntimeException(ex);
        } finally {
            if (r != null) IOUtil.closeQuietly(r);
        }
    }

    public String lessFile(File file, boolean compress, boolean yuicompress,
                       int optimization, List<File> includes) throws FileNotFoundException, IOException {
        String data = IOUtil.fileToString(file);
        
        return less(data, file, compress, yuicompress, optimization, includes);
    }
    
    /**
     * The complete method for calling the Less engine.
     * 
     * @param data The LESS code to process.
     * @param file The file in which the Less code resides.
     *             Set it to null if you only want to Less (unlocated) data.
     * @param includes A list of additional include directories, for @import
     *                 rules. Can be null.
     * @param compress Should the generated CSS be compressed with the builtin compressor?
     * @param yuicompress Should the generated CSS be compressed with the YUI compressor?
     * @param optimization The optimization level. On the current version this
     *                     can be either 0, 1 or 2.
     * @return The resulting CSS code.
     **/
    public String less(String data, File file, boolean compress, boolean yuicompress,
                       int optimization, List<File> includes) {
        Context c = null;
        try {
            c = new ContextFactory().enterContext();
            if (includes == null) includes = new ArrayList<File>();

            String path = "-";
            if (file != null) {
                path = file.getAbsolutePath();
                includes.add(file.getParentFile());
            }

            Object[] incl = new Object[includes.size()];
            for (int i = 0; i < incl.length; i++) {
                incl[i] = includes.get(i).getAbsolutePath();
            }

            Object[] args = {data, path,
                             c.newArray(so, incl),
                             compress, yuicompress, optimization};
        
            Callable func = (Callable) so.get("lessIt", so);
            return (String) func.call(c, so, null, args);//TODO: catch
        } finally {
            Context.exit();
        }
    }
    
    public String lessData(String data, boolean compress, boolean yuicompress,
                           int optimization, List<File> includes) {
        return less(data, null, compress, yuicompress, optimization, includes);
    }

    public static void main(String args[]) throws IOException {
        if (args.length < 2) {
            System.err.println("Please specify a [source.less] and a [target.css]");
            System.exit(1);
        }

        File src = new File(args[0]);
        File dst = new File(args[1]);

        LessCSS less = LessCSS.newBundled();
        String result = less.lessFile(src, true,true, 1, null);
        IOUtil.stringToFile(dst, result);
    }
}
