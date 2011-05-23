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
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;

public class LessCSS {

    private final String lessjs;
    private final String runjs;
    private final ContextFactory cf;
    private final Context c;
    private final ScriptableObject so;

    public LessCSS() {
        try {
            lessjs = IOUtil.urlToString(getClass().getClassLoader().getResource("com/visural/common/web/lesscss/less.js"));
            runjs = IOUtil.urlToString(getClass().getClassLoader().getResource("com/visural/common/web/lesscss/run.js"));
            cf = new ContextFactory();
            c = cf.enterContext();
            so = c.initStandardObjects();
            c.setOptimizationLevel(9);
            c.evaluateString(so, lessjs, "less.js", 1, null);
            c.evaluateString(so, runjs, "run.js", 1, null);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed reading javascript less.js", ex);
        }
    }

    public String less(InputStream input) {
        String data = "Not initialised.";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int r;
            while ((r = input.read()) != -1) {
                baos.write(r);
            }
            input.close();
            data = new String(baos.toByteArray());

            String lessitjs = "lessIt(\""+data.replace("\"", "\\\"").replace("\n", "").replace("\r", "")+"\");";
            String result = c.evaluateString(so, lessitjs, "lessitjs.js", 1, null).toString();
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("LessCSS failed: "+data, e);
        }
    }

    public static void main(String args[]) throws IOException {
        if (args.length < 2) {
            System.err.println("Please specify a [source.less] and a [target.css]");
        } else {
            LessCSS less = new LessCSS();
            FileInputStream fis = new FileInputStream(args[0]);
            String result = less.less(fis);
            fis.close();
            IOUtil.stringToFile(args[1], result);
        }
    }
}
