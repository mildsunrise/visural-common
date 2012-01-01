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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class OrigResponseWrapper extends HttpServletResponseWrapper {

    protected final HttpServletResponse origResponse;
    private ServletOutputStream sos = null;
    protected ByteArrayOutputStream stream = null;
    
    protected String contentType = null;
    protected PrintWriter writer = null;
    protected Map<String, String> headers = new HashMap<String, String>();
    protected int clength = -1;

    public OrigResponseWrapper(HttpServletResponse original) {
        super(original);
        origResponse = original;
    }

    public ByteArrayOutputStream getFakeStream() {
        if (stream == null)
            stream = new ByteArrayOutputStream();
        return stream;
    }

    @Override
    public void setContentType(String type) {
        contentType = type;
    }

    @Override
    public void addDateHeader(String name, long date) {
        headers.put(name, DateUtil.formatHttpDate(date));
    }

    @Override
    public void addIntHeader(String name, int value) {
        headers.put(name, Integer.toString(value));
    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        headers.put(name, Integer.toString(value));
    }

    @Override
    public void setDateHeader(String name, long date) {
        headers.put(name, DateUtil.formatHttpDate(date));
    }

    protected void createOutputStream() throws IOException {
        getFakeStream();
        sos = new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                stream.write(b);
            }
        };
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (sos == null) {
            createOutputStream();
        }
        return sos;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(getFakeStream(), origResponse.getCharacterEncoding()));
        }
        return writer;
    }

    @Deprecated
    @Override
    public void setStatus(int i, String string) {
        checkCommited();
        origResponse.setStatus(i, string);
    }

    @Override
    public void setStatus(int i) {
        checkCommited();
        origResponse.setStatus(i);
    }

    @Override
    public void sendRedirect(String string) throws IOException {
        checkCommited();
        origResponse.sendRedirect(string);
    }

    @Override
    public void sendError(int i) throws IOException {
        checkCommited();
        origResponse.sendError(i);
    }

    @Override
    public void sendError(int i, String string) throws IOException {
        checkCommited();
        origResponse.sendError(i, string);
    }

    @Override
    public String getCharacterEncoding() {
        return origResponse.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setCharacterEncoding(String string) {
        origResponse.setCharacterEncoding(string);
    }

    @Override
    public void setContentLength(int length) {
        checkCommited();
        clength = length;
    }

    @Override
    public void setBufferSize(int size) {
        checkCommited();
        stream = new ByteArrayOutputStream(size);
        bSize = size;
    }

    protected int bSize = 32;
    
    @Override
    public int getBufferSize() {
        return bSize;
    }

    protected boolean commited = false;
    @Override
    public void flushBuffer() throws IOException {
        commited=true;
    }
    
    public void checkCommited() throws IllegalStateException {
        if (isCommitted())
            throw new IllegalStateException("The response is already commited.");
    }

    @Override
    public void resetBuffer() {
        checkCommited();
        if (stream != null) stream.reset();
        contentType = null;
    }

    @Override
    public boolean isCommitted() {
        return origResponse.isCommitted() || commited || (stream != null && stream.size() > 0);
    }

    @Override
    public void reset() {
        origResponse.reset();
        resetBuffer();
    }

    @Override
    public void setLocale(Locale locale) {
        origResponse.setLocale(locale);
    }

    @Override
    public Locale getLocale() {
        return origResponse.getLocale();
    }

    @Override
    @Deprecated
    public String encodeUrl(String string) {
        return origResponse.encodeUrl(string);
    }

    @Override
    public String encodeURL(String string) {
        return origResponse.encodeURL(string);
    }

    @Override
    @Deprecated
    public String encodeRedirectUrl(String string) {
        return origResponse.encodeRedirectUrl(string);
    }

    @Override
    public String encodeRedirectURL(String string) {
        return origResponse.encodeRedirectURL(string);
    }

    @Override
    public boolean containsHeader(String string) {
        return headers.containsKey(string);
    }

    @Override
    public void addCookie(Cookie cookie) {
        origResponse.addCookie(cookie);
    }
    
    public void flushFakeStreams() throws IOException {
        if (sos != null) sos.flush();
        if (writer != null) writer.flush();
    }
    public ByteArrayInputStream createFakePipe() throws IOException {
        flushFakeStreams();
        if (stream == null) return null;
        return new ByteArrayInputStream(stream.toByteArray());
    }
    public Map<String, String> getFakeHeaders() {
        return headers;
    }
    
}
