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

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;

public class LessCSS {

    public final static String LESSJS_LOCATION =
            "com/visural/common/web/lesscss/less.js";
    public final static String BUNDLED_CHARSET = "UTF-8";


    private final Context c;
    private final ScriptableObject so;

    /**
     * Initializes a new Less.JS engine from the given scriptfile.
     **/
    public LessCSS(Reader lessjs) {
        try {
            InputStream runjs = getClass().getClassLoader().getResourceAsStream("com/visural/common/web/lesscss/run.js");
            c = new ContextFactory().enterContext();
            so = c.initStandardObjects();
            c.setOptimizationLevel(9);
            //FIXME: check if eval went well
            c.evaluateReader(so, lessjs, "less.js", 1, null);
            c.evaluateReader(so, new InputStreamReader(runjs), "run.js", 1, null);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed reading javascript less.js", ex);
        }
    }

    /**
     * Initializes a new Less.JS engine (bundled).
     **/
    public LessCSS() {
        this(new InputStreamReader(
                LessCSS.class.getResourceAsStream(LESSJS_LOCATION),
                Charset.forName(BUNDLED_CHARSET)));
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
        if (includes == null) includes = new ArrayList<File>();
        
        String path = "-";
        if (file != null) {
            path = file.getPath();
            includes.add(file.getParentFile());
        }

        Object[] args = {data, path,
                         c.newArray(so, includes.toArray()),
                         compress, yuicompress, optimization};
        
        Callable func = (Callable) so.get("lessIt", so);
        return (String) func.call(c, so, null, args);//TODO: catch
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

        LessCSS less = new LessCSS();
        String result = less.lessFile(src, true,true, 1, null);
        IOUtil.stringToFile(dst, result);
    }
}
