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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Richard Nichols
 */
public class CSSMHTML {
    
    public static final String SEP_MHTML_POSTFIX = ".mhtml";
    public static final String MIME_SEPARATOR = "__SEPARATOR";
    
    private static final Pattern find = Pattern.compile("url\\(\\s*[\"']?(([^\\)]+)\\.((png)|(gif)|(jpg)))[\"']?\\s*\\)", Pattern.CASE_INSENSITIVE);
    private final String css;
    private final ResourceResolver r;
    private Base64Encoder be = new Base64Encoder();
    private final String cssReferencePath;

    public CSSMHTML(String css, ResourceResolver r, String cssReferencePath) {
        this.css = css;
        this.r = r;
        this.cssReferencePath = cssReferencePath;
    }

    private class Repl {
        int start;
        int end;
        String type;
        String mhtmlSection;
        String replacement;
        String base64;
    }

    public static String getContentType() {
        return "multipart/related; type=\"text/css\"; boundary=\""+MIME_SEPARATOR+"\"";
    }

    public ConversionResult convert() {
        StringBuilder inlineResources = new StringBuilder();
        List<Repl> repls = new ArrayList<Repl>();
        Matcher m = find.matcher(css);
        while (m.find()) {
            Repl repl = new Repl();
            repl.start = m.start();
            repl.end = m.end();
            String url = m.group(1);
            repl.type = "image/"+m.group(3);
            InputStream is = r.resolveReference(url);
            if (is != null) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    be.encode(is, baos);
                    repl.mhtmlSection = "inlineimg"+repls.size()+"."+m.group(3);
                    repl.base64 = new String(baos.toByteArray());
                    repl.replacement = "url(mhtml:"+cssReferencePath+"!"+repl.mhtmlSection+")";
//                    repl.replacement = "url("+repl.mhtmlSection+")";
                    repls.add(repl);
                    inlineResources.append("--").append(MIME_SEPARATOR).append('\n')
                                 .append("Content-Location: ").append(repl.mhtmlSection).append('\n')
                                 .append("Content-Type: ").append(repl.type).append('\n')
                                 .append("Content-Transfer-Encoding:base64\n\n\n")
                                 .append(repl.base64)
                                 .append('\n');
                } catch (IOException ex) {
                    Logger.getLogger(CSSMHTML.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        Logger.getLogger(CSSMHTML.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }        
        ConversionResult result = new ConversionResult();
        StringBuilder mhtmlCSS = new StringBuilder(css);
        for (int n = repls.size()-1; n >= 0; n--) {
            Repl repl = repls.get(n);
            mhtmlCSS.delete(repl.start, repl.end);
            mhtmlCSS.insert(repl.start, repl.replacement);
        }
        result.mhtml = "Content-Type: "+getContentType()+"\n\n\n"+inlineResources.toString().replace("\n", "\r\n");// important - if not windows line feeds, it won't work!
        result.mhtmlRefCSS = mhtmlCSS.toString().replace("\n", "\r\n").replace(cssReferencePath, cssReferencePath+SEP_MHTML_POSTFIX);
        // insert header to form composite
        StringBuilder composite = new StringBuilder("Content-Type: "+getContentType()+"\n\n\n")
                 .append("--").append(MIME_SEPARATOR)
                 .append('\n')
                 .append("Content-Location: ").append(cssReferencePath)
                 .append('\n')
                 .append("Content-Transfer-Encoding: quoted-printable\n")
                 .append("Content-Type: text/css\n\n")
                 .append(mhtmlCSS)
                 .append("\n\n")
                 .append(inlineResources.toString())
                 .append("\n\n");
        result.compositeCSS = composite.toString().replace("\n", "\r\n"); // important - if not windows line feeds, it won't work!
        return result;
    }

    public static String convert(String css, ResourceResolver r, String cssReferencePath) {
        return new CSSMHTML(css,r, cssReferencePath).convert().compositeCSS;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Provide input path and output path and CSS base path on command line.");
            return;
        }
        File f = new File(args[0]);
        String css = IOUtil.fileToString(args[0]);
        IOUtil.stringToFile(args[1], convert(css, new RelativeFileResolver(f.getParentFile().getCanonicalPath()), args[2]));
    }

    public static class ConversionResult implements Serializable {
        public String compositeCSS;
        public String mhtml;
        public String mhtmlRefCSS;
    }
}
