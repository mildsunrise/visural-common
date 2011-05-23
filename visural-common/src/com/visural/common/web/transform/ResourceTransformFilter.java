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

import com.visural.common.DateUtil;
import com.visural.common.web.client.WebClient;
import com.visural.common.web.transform.DataUri.Mode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import com.visural.common.web.lesscss.LessCSS;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    public void init(FilterConfig fc) throws ServletException {
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

    public void doFilter(ServletRequest sr, ServletResponse sr1, FilterChain fc) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) sr;
        HttpServletResponse res = (HttpServletResponse) sr1;
        WebClient client = WebClient.detect(req);

        List<Transform> transforms = getTransforms(req, client);
        if (transforms == null || transforms.isEmpty()) {
            fc.doFilter(sr, sr1);
        } else {
            Request currentRequest = new Request(req.getRequestURI(), transforms);
            String requestURL = req.getRequestURL().toString();
            Response response = transformCache.get(currentRequest);
            if (response == null) {
                OrigResponseWrapper wrap = new OrigResponseWrapper(res);
                fc.doFilter(sr, wrap);
                if (wrap.writer != null ) wrap.writer.flush();
                wrap.sos.flush();
                String returnData = new String(wrap.stream.toByteArray());
                for (Transform transform : transforms) {
                    switch (transform) {
                        case HTML_DATAURI:
                            if (wrap.contentType == null) wrap.contentType = "text/html";
                            returnData = newDataUri(Mode.HTML,
                                    returnData,
                                    new URLBasedResolver(getURLFolder(requestURL)),
                                    getDataUriMaxResourceSize(req, client))
                                    .convert();
                            break;

                        case CSS_DATAURI:
                            if (wrap.contentType == null) wrap.contentType = "text/css";
                            returnData = newDataUri(Mode.CSS,
                                    returnData,
                                    new URLBasedResolver(getURLFolder(requestURL)),
                                    getDataUriMaxResourceSize(req, client))
                                    .convert();
                            break;

                        case CSS_MHTML_SEPARATE:
                        case CSS_MHTML_SINGLE:                            
                            CSSMHTML.ConversionResult result = new CSSMHTML(returnData, new URLBasedResolver(requestURL.substring(0, requestURL.lastIndexOf('/') + 1)), requestURL).convert();
                            if (transform.equals(Transform.CSS_MHTML_SEPARATE)) {
                                if (wrap.contentType == null) wrap.contentType = "text/css";
                                // TODO this doesn't take into account the transforms array in URL
                                returnData = result.mhtmlRefCSS;
                                Response sep = new Response(CSSMHTML.getContentType(), wrap.headers, result.mhtml);
                                transformCache.put(new Request(req.getRequestURI() + CSSMHTML.SEP_MHTML_POSTFIX, Arrays.asList(Transform.CSS_MHTML_SEPARATE)), sep);
                            } else {
                                wrap.contentType = CSSMHTML.getContentType();
                                returnData = result.compositeCSS;
                            }
                            break;

                        case CSS_YUI_COMPRESS:                            
                            if (wrap.contentType == null) wrap.contentType = "text/css";
                            returnData = yuiCompressCSS(returnData);
                            break;

                        case JS_YUI_COMPRESS:
                            if (wrap.contentType == null) wrap.contentType = "text/javascript";
                            returnData = yuiCompressJS(requestURL, returnData);
                            break;

                        case LESSCSS:
                            if (wrap.contentType == null) wrap.contentType = "text/css";
                            returnData = new LessCSS().less(new ByteArrayInputStream(returnData.getBytes()));
                            break;

                        default:
                            throw new UnsupportedOperationException("Not implemented - " + transform.name());
                    }
                }
                if (!transforms.contains(Transform.HTML_DATAURI)) {
                    response = new Response(wrap.contentType, wrap.headers, returnData);
                    transformCache.put(currentRequest, response);
                }
                res.setContentType(wrap.contentType);
            } else {
                res.setContentType(response.getContentType());
                for (String key : response.getHeaders().keySet()) {
                    res.setHeader(key, response.getHeaders().get(key));
                }
            }
            res.setContentLength(response.getReturnData().getBytes().length);
            res.getWriter().print(response.getReturnData());
        }
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

    private class OrigResponseWrapper extends HttpServletResponseWrapper {

        protected final HttpServletResponse origResponse;
        protected String contentType = null;
        protected Map<String,String> headers = new HashMap<String,String>();
        protected ServletOutputStream sos = null;
        protected ByteArrayOutputStream stream = new ByteArrayOutputStream();
        protected PrintWriter writer = null;

        public OrigResponseWrapper(HttpServletResponse response) {
            super(response);
            origResponse = response;
        }

        @Override
        public void setContentType(String type) {
            super.setContentType(type);
            contentType = type;
        }

        @Override
        public void addDateHeader(String name, long date) {
            super.addDateHeader(name, date);
            headers.put(name, DateUtil.formatHttpDate(date));
        }

        @Override
        public void addIntHeader(String name, int value) {
            super.addIntHeader(name, value);
            headers.put(name, Integer.toString(value));
        }

        @Override
        public void addHeader(String name, String value) {
            super.addHeader(name, value);
            headers.put(name, value);
        }

        @Override
        public void setHeader(String name, String value) {
            super.setHeader(name, value);
            headers.put(name, value);
        }
        
        @Override
        public void setIntHeader(String name, int value) {
            super.setIntHeader(name, value);
            headers.put(name, Integer.toString(value));
        }

        @Override
        public void setDateHeader(String name, long date) {
            super.setDateHeader(name, date);
            headers.put(name, DateUtil.formatHttpDate(date));
        }

        public ServletOutputStream createOutputStream() throws IOException {
            return sos == null ? new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    stream.write(b);
                }
            } : sos;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (sos == null) {
                sos = createOutputStream();
            }
            return sos;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            sos = getOutputStream();
            if (writer == null) {
                writer = new PrintWriter(sos);
            }
            return writer;
        }
    }

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
        private final String returnData;

        public Response(String contentType, Map<String,String> headers, String returnData) {
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

        public String getReturnData() {
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
                public void warning(String string, String string1, int i, String string2, int i1) {
                    errors.append(string).append(", ");
                }
                public void error(String string, String string1, int i, String string2, int i1) {
                    errors.append(string).append(", ");
                }
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
