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

package com.visural.common.web.transform;

import com.visural.common.IOUtil;
import com.visural.common.coder.Base64Encoder;
import com.visural.common.web.api.googlecharts.Chart;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts HTML/CSS content to use data URIs instead of image references
 * @author Richard Nichols
 */
public class DataUri {

    public static final int DEFAULT_MAX_SIZE = 1024*50;
    public static final String[] BLOCKED_DOMAINS = {"http://in.getclicky.com/", Chart.apiURL};
    
    private static final Pattern findCSS = Pattern.compile("url\\(\\s*[\"']?(([^\\)]+)\\.((png)|(gif)|(jpg)))[\"']?\\s*\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern findHTML = Pattern.compile("src=\\s*[\"'](([^'\"]+)\\.((png)|(gif)|(jpg)))[\"']", Pattern.CASE_INSENSITIVE);
    private final Mode mode;
    private final String content;
    private final ResourceResolver r;
    private Base64Encoder be = new Base64Encoder();
    private final int maxResourceSize;

    public DataUri(Mode mode, String content, ResourceResolver r, int maxResourceSize) {
        this.mode = mode;
        this.content = content;
        this.r = r;
        this.maxResourceSize = maxResourceSize;
    }

    /**
     * Override with your own filtering if you need it.
     * @param url
     * @return
     */
    protected boolean shouldConvert(String url) {
        for (String domain : BLOCKED_DOMAINS) {
            if (url.startsWith(domain)) {
                return false;
            }
        }
        return true;
    }

    private class Repl {
        int start;
        int end;
        String replacement;
    }

    public String convert() {
        List<Repl> repls = new ArrayList<Repl>();
        Matcher m = mode.equals(Mode.CSS) ? findCSS.matcher(content) : findHTML.matcher(content);
        while (m.find()) {
            Repl repl = new Repl();
            repl.start = m.start();
            repl.end = m.end();
            String url = m.group(1);
            if (!shouldConvert(url)) {
                continue;
            }
            InputStream is = r.resolveReference(url);
            if (is != null) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    be.encode(is, baos);
                    if (baos.size() <= maxResourceSize) {
                        if (mode.equals(Mode.CSS)) {
                            repl.replacement = "url(\"data:image/"+m.group(3)+";base64,"+new String(baos.toByteArray())+"\")";
                        } else {
                            repl.replacement = "src=\"data:image/"+m.group(3)+";base64,"+new String(baos.toByteArray())+"\"";
                        }
                        repls.add(repl);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(DataUri.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        Logger.getLogger(DataUri.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        StringBuilder sb = new StringBuilder(content);
        for (int n = repls.size()-1; n >= 0; n--) {
            Repl repl = repls.get(n);
            sb.delete(repl.start, repl.end);
            sb.insert(repl.start, repl.replacement);
        }
        return sb.toString();
    }

    public static String convert(Mode mode, String css, ResourceResolver r) {
        return convert(mode, css, r, DEFAULT_MAX_SIZE);
    }

    public static String convert(Mode mode, String css, ResourceResolver r, int maxResourceSize) {
        return new DataUri(mode, css, r, maxResourceSize).convert();
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Provide input path and output path on command line.");
            return;
        }
        File f = new File(args[0]);
        String css = IOUtil.fileToString(args[0]);
        IOUtil.stringToFile(args[1], convert(Mode.CSS, css, new RelativeFileResolver(f.getParentFile().getCanonicalPath())));
    }

    public enum Mode {
        CSS,
        HTML
    }
}
