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
import com.visural.common.web.client.WebClient;
import com.visural.common.web.transform.DataUri.Mode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.visural.common.web.lesscss.LessCSS;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import java.io.File;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

/**
 * A text response transformation filter.
 *
 * Can apply one or more transformations -
 *   * Convert CSS image url(...) references to data URIs, or MHTML
 *   * Apply LessCSS compilation to .less files -> CSS
 *   * Apply YUI compressor to Javascript or CSS output
 *   * Convert HTML image src="..." references to data uris
 *
 * @author Richard Nichols
 */
public class ResourceTransformFilter implements Filter {
    protected FilterConfig config;
    
    private LessCSS engine = null;

    public LessCSS getEngine() {
        if (engine==null) engine = LessCSS.newBundled();
        return engine;
    }

    //TODO FUTURE: implement LessCompressMethod, LessOptimization, InlineURITransform, CSSCompress, JSCompress, LessEngine, ...

    private final Map<Request, Response> transformCache = new HashMap<Request, Response>();

    public enum Transform {
        HTML_DATAURI,
        CSS_DATAURI,
        CSS_MHTML_SINGLE,
        CSS_MHTML_SEPARATE,
        LESSCSS,
        JS_YUI_COMPRESS,
        CSS_YUI_COMPRESS
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {
        this.config = fc;
    }
    
    protected void addJavascriptCompression(String urlLower, List<Transform> result) {
        if (urlLower.endsWith(".js")) {
            result.add(Transform.JS_YUI_COMPRESS);
        }
    }

    protected void addCSSCompression(String urlLower, List<Transform> result) {
        if (urlLower.endsWith(".less") || urlLower.endsWith(".css")) {
            result.add(Transform.CSS_YUI_COMPRESS);
        }
    }

    protected void addLessCSS(String urlLower, List<Transform> result) {
        if (urlLower.endsWith(".less")) {
            result.add(Transform.LESSCSS);
        }
    }

    protected void addResourceInlining(String urlLower, WebClient client, List<Transform> result) {
        if (urlLower.endsWith(".css") || urlLower.endsWith(".css" + CSSMHTML.SEP_MHTML_POSTFIX) ||
                urlLower.endsWith(".less") || urlLower.endsWith(".less" + CSSMHTML.SEP_MHTML_POSTFIX)) {
            if (client.supportsDataUris()) {
                result.add(Transform.CSS_DATAURI);
            } else if (client.supportsMHTML()) {
                result.add(Transform.CSS_MHTML_SINGLE);
            }
        }
    }

    protected int getDataUriMaxResourceSize(HttpServletRequest req, WebClient client) {
        return DataUri.DEFAULT_MAX_SIZE;
    }

    protected List<Transform> getTransforms(HttpServletRequest req, WebClient client) {
        List<Transform> result = new ArrayList<Transform>();
        String urlLower = req.getRequestURI().toLowerCase();
        addLessCSS(urlLower, result);
        addJavascriptCompression(urlLower, result);
        addCSSCompression(urlLower, result);
        addResourceInlining(urlLower, client, result);
        return result;
    }

    @Override
    public void doFilter(ServletRequest greq, ServletResponse gres, FilterChain fc) throws IOException, ServletException {
        //Check we're dealing with HTTP requests
        if (!((greq instanceof HttpServletRequest) && (gres instanceof HttpServletResponse))) {
            fc.doFilter(greq, gres);
            return;
        }
        HttpServletRequest req = (HttpServletRequest) greq;
        HttpServletResponse res = (HttpServletResponse) gres;
        String reqURL = req.getRequestURL().toString();
        
        //Detect the client
        WebClient client = WebClient.detect(req);

        //Get the list of transforms
        List<Transform> transforms = getTransforms(req, client);
        
        //If there are no transforms to apply
        if (transforms == null || transforms.isEmpty()) {
            //Pass the request directly
            fc.doFilter(req,res);
            return;
        }
        
        //Look at the cache
        Request request = new Request(reqURL, transforms);
        Response response = transformCache.get(request);
        
        //If no entry is found //TODO FUTURE: check modified date
        if (response == null) {

            //Wrap and process the original response
            OrigResponseWrapper wrap = new OrigResponseWrapper(res);
            fc.doFilter(req, wrap);

            //If the orig. response is commited (possibly because of a 404, etc.), return
            if (res.isCommitted()) return;
            
            //Decode the original data using the specified charset
            String data = IOUtils.toString(wrap.createFakePipe(), wrap.getCharacterEncoding());
            String ctype = wrap.getContentType();
            Map<String, String> headers = wrap.getFakeHeaders();

            for (Transform transform : transforms) {
                switch (transform) {
                    case HTML_DATAURI:
                        if (ctype == null) {
                            ctype = "text/html";
                        }
                        data = newDataUri(Mode.HTML,
                                data,
                                new URLBasedResolver(getURLFolder(reqURL)),
                                getDataUriMaxResourceSize(req, client)).convert();
                        break;

                    case CSS_DATAURI:
                        if (ctype == null) ctype = "text/css";
                        data = newDataUri(Mode.CSS,
                                data,
                                new URLBasedResolver(getURLFolder(reqURL)),
                                getDataUriMaxResourceSize(req, client)).convert();
                        break;

                    case CSS_MHTML_SEPARATE:
                    case CSS_MHTML_SINGLE:
                        CSSMHTML.ConversionResult result = new CSSMHTML(data, new URLBasedResolver(reqURL.substring(0, reqURL.lastIndexOf('/') + 1)), reqURL).convert();
                        if (transform.equals(Transform.CSS_MHTML_SEPARATE)) {
                            if (ctype == null) ctype = "text/css";
                            // TODO this doesn't take into account the transforms array in URL
                            data = result.mhtmlRefCSS;
                            Response sep = new Response(CSSMHTML.getContentType(), headers,
                                    IOUtil.toByteArray(new StringReader(result.mhtml), res.getCharacterEncoding()));
                            transformCache.put(new Request(req.getRequestURI() + CSSMHTML.SEP_MHTML_POSTFIX, Arrays.asList(Transform.CSS_MHTML_SEPARATE)), sep);
                        } else {
                            ctype = CSSMHTML.getContentType();
                            data = result.compositeCSS;
                        }
                        break;

                    case CSS_YUI_COMPRESS:
                        if (ctype == null) ctype = "text/css";
                        data = yuiCompressCSS(data);
                        break;

                    case JS_YUI_COMPRESS:
                        if (ctype == null) ctype = "text/javascript";
                        data = yuiCompressJS(reqURL, data);
                        break;

                    case LESSCSS:
                        if (ctype == null || ctype.equals("text/less")) ctype = "text/css";
                        //Try to locate the .less file
                        String path = req.getServletPath();
                        String rPath = config.getServletContext().getRealPath(path);
                        File rFile = null;
                        if (rPath != null) rFile = new File(rPath);
                        //Call the less method
                        data = getEngine().less(data, rFile, true, true, 2, null);
                        break;

                    default:
                        throw new UnsupportedOperationException("Not implemented - " + transform.name());
                }
            }
            //Set the content type
            res.setContentType(ctype);

            if (transforms.contains(Transform.HTML_DATAURI)) return; //FIXME: ???!

            //Encode data
            byte[] bdata = IOUtil.toByteArray(new StringReader(data), wrap.getCharacterEncoding());
            
            //Cache the (binary) data
            response = new Response(ctype, headers, bdata);
            transformCache.put(request, response);
        }
        
        //Set headers
        Map<String, String> headers = response.getHeaders();
        for (String key : headers.keySet())
            res.setHeader(key, headers.get(key));
        //Set content length & type
        res.setContentLength(response.getReturnData().length);
        res.setContentType(response.getContentType());
        //Write content
        res.getOutputStream().write(response.getReturnData());
    }

    /**
     * Override point - provide your own version of DataUri if you wish
     * @param mode
     * @param CSS
     * @param returnData
     * @param maxResourceSize
     * @return
     */
    protected DataUri newDataUri(Mode mode, String CSS, URLBasedResolver returnData, int maxResourceSize) {
        return new DataUri(mode, CSS, returnData, maxResourceSize);
    }

    @Override
    public void destroy() {
        transformCache.clear();
    }

    private static class Request implements Serializable {

        private final String requestURI;
        private final List<Transform> transforms;

        public Request(String requestURI, List<Transform> transforms) {
            this.requestURI = requestURI;
            this.transforms = transforms;
        }

        public String getRequestURI() {
            return requestURI;
        }

        public List<Transform> getTransforms() {
            return transforms;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Request other = (Request) obj;
            if ((this.requestURI == null) ? (other.requestURI != null) : !this.requestURI.equals(other.requestURI)) {
                return false;
            }
            if (this.transforms != other.transforms && (this.transforms == null || !this.transforms.equals(other.transforms))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + (this.requestURI != null ? this.requestURI.hashCode() : 0);
            hash = 83 * hash + (this.transforms != null ? this.transforms.hashCode() : 0);
            return hash;
        }
    }

    private static class Response implements Serializable {
        private final String contentType;
        private final Map<String, String> headers;
        private final byte[] returnData;

        public Response(String contentType, Map<String,String> headers, byte[] returnData) {
            this.contentType = contentType;
            this.headers = headers;
            this.returnData = returnData;
        }

        public String getContentType() {
            return contentType;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public byte[] getReturnData() {//TODO: data should be binary, not text
            return returnData;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Response other = (Response) obj;
            if ((this.contentType == null) ? (other.contentType != null) : !this.contentType.equals(other.contentType)) {
                return false;
            }
            if (this.headers != other.headers && (this.headers == null || !this.headers.equals(other.headers))) {
                return false;
            }
            if ((this.returnData == null) ? (other.returnData != null) : !this.returnData.equals(other.returnData)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + (this.contentType != null ? this.contentType.hashCode() : 0);
            hash = 97 * hash + (this.headers != null ? this.headers.hashCode() : 0);
            hash = 97 * hash + (this.returnData != null ? this.returnData.hashCode() : 0);
            return hash;
        }

    }

    protected String yuiCompressCSS(String data) {
        StringWriter sw = new StringWriter();
        try {
            CssCompressor cssCompressor = new CssCompressor(new StringReader(data));
            cssCompressor.compress(sw, -1);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return sw.toString();
    }

    protected String yuiCompressJS(String url, String data) throws IOException {
        StringWriter sw = new StringWriter();
        final StringBuilder errors = new StringBuilder();
        try {
            JavaScriptCompressor javaScriptCompressor = new JavaScriptCompressor(new StringReader(data), new ErrorReporter() {
                @Override
                public void warning(String string, String string1, int i, String string2, int i1) {
                    errors.append(string).append(", ");
                }
                @Override
                public void error(String string, String string1, int i, String string2, int i1) {
                    errors.append(string).append(", ");
                }
                @Override
                public EvaluatorException runtimeError(String string, String string1, int i, String string2, int i1) {
                    errors.append(string).append(", ");
                    return new EvaluatorException(string);
                }
            });
            javaScriptCompressor.compress(sw, -1, true, false, false, false);
        } catch (Exception e) {
            Logger.getLogger(ResourceTransformFilter.class.getName())
                    .log(Level.WARNING, "There were errors (url="+url+"):\n"+errors.toString(), e);
            return data;
        }
        return sw.toString();
    }

    private String getURLFolder(String requestURL) {
        if (requestURL.endsWith("/")) {
            return requestURL;                
        }  else {
            return requestURL.substring(0, requestURL.lastIndexOf('/') + 1);
        }
    }
}
