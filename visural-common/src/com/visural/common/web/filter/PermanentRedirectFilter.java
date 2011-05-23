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
package com.visural.common.web.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet filter which assists with doing permanent host and url redirects.
 * @author Richard Nichols
 */
public abstract class PermanentRedirectFilter implements Filter {

    private final List<Route> routes = new ArrayList<Route>();
    private final Map<String,String> rHost = new HashMap<String,String>();
    private final Map<String,String> rURL = new HashMap<String,String>();

    public void init(FilterConfig fc) throws ServletException {
        configureRoutes();
        buildRouteMaps();
        routes.clear();
    }

    /**
     * Implement this method and configure your redirects.
     */
    public abstract void configureRoutes();

    public void doFilter(ServletRequest sr, ServletResponse sr1, FilterChain fc) throws IOException, ServletException {
        HttpServletRequest hsr = (HttpServletRequest) sr;
        HttpServletResponse resp = (HttpServletResponse) sr1;
        String url = hsr.getRequestURL().toString();
        String host = url.replaceAll("https?://([^:/]+).*", "$1");
        if (hsr.getQueryString() != null) {
            url = url + "?" + hsr.getQueryString();
        }
        String redirect = null;
        if (rHost.get(host) != null) {
            redirect = url.replaceFirst(Matcher.quoteReplacement(host), rHost.get(host));
        } else if (rURL.get(url) != null) {
            redirect = rURL.get(url);
        }
        if (redirect == null) {
            redirect = getAlgorithmicRedirect(url);
        }
        if (redirect != null) {
            resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            resp.setHeader("Location", redirect);
        } else {
            fc.doFilter(sr, sr1);
        }
    }

    /**
     * override this method to do an algorithmic redirect, e.g. using regex
     * @param url
     * @return
     */
    protected String getAlgorithmicRedirect(String url) {
        return null;
    }

    public void destroy() {
    }

    protected Route fromURL(String from) {
        return new Route(RouteType.URL, from);
    }

    protected Route fromHost(String from) {
        return new Route(RouteType.HOST, from);
    }

    private void buildRouteMaps() {
        for (Route r : routes) {
            switch (r.type) {
                case HOST:
                    rHost.put(r.from, r.to);
                    break;
                case URL:
                    rURL.put(r.from, r.to);
                    break;
            }
        }
    }

    protected enum RouteType {
        HOST,
        URL;
    }

    public class Route {

        private final RouteType type;
        private String from;
        private String to;

        public Route(RouteType type, String from) {
            this.type = type;
            this.from = from;
        }

        public void to(String to) {
            this.to = to;
            routes.add(this);
        }
    }
}
