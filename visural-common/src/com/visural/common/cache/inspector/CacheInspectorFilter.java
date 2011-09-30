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
package com.visural.common.cache.inspector;

import com.visural.common.GuiceUtil;
import com.visural.common.IOUtil;
import com.visural.common.StringUtil;
import com.visural.common.cache.CacheModule;
import com.visural.common.cache.impl.CacheStatsAggregated;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.antlr.stringtemplate.StringTemplate;

/**
 * A Cache inspection UI that can be plugged into web applications.
 * Allows you to visualise cache performance.
 * NB. Requires StringTemplate library as a dependency.
 * @author Visural
 */
public class CacheInspectorFilter implements Filter {

    public static final String FILTER_PATH_PARAM = "path";
    public static final String DEFAULT_PATH = "/_cache_inspector/";
    
    private String filterPath = DEFAULT_PATH;
    private static StringTemplate htmlTemplate;
    private static WeakReference<CacheModule> module = null;
    
    static {
        try {
            htmlTemplate = new StringTemplate(IOUtil.urlToString(CacheInspectorFilter.class.getResource("CacheInspector.html")));
        } catch (IOException ex) {
            Logger.getLogger(CacheInspectorFilter.class.getName()).log(Level.SEVERE, null, ex);
            htmlTemplate = new StringTemplate("Error loading template.");
        }
    }
    
    /**
     * You must call this with the {@link CacheModule} on which you want to display
     * statistics. Note that currently only one CacheModule is supported. (static)
     * @param module 
     */
    public static void setCacheModule(CacheModule module) {
        CacheInspectorFilter.module = new WeakReference<CacheModule>(module);
        module.getInterceptor().setTrackReferences(true);
    }
    
    public void init(FilterConfig fc) throws ServletException {
        if (StringUtil.isNotBlankStr(fc.getInitParameter(FILTER_PATH_PARAM))) {
            filterPath = fc.getInitParameter(FILTER_PATH_PARAM);
        }
    }

    public void doFilter(ServletRequest sr, ServletResponse sr1, FilterChain fc) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)sr;
        HttpServletResponse res = (HttpServletResponse)sr1;
        if (req.getServletPath().startsWith(filterPath)) {
            res.setContentType("text/html");
            ServletOutputStream out = res.getOutputStream();
            if (module == null || module.get() == null) {
                out.write("No CacheModule has been registered with CacheInspectorFilter.class".getBytes());
                out.close();
            } else {
                StringTemplate t = htmlTemplate.getInstanceOf();                
                Map<String, Map<String, CacheStatsAggregated>> stats = module.get().getStatistics(true);
                Map<CacheClass, List<StatEntry>> classes = new HashMap();
                for (Entry<String, Map<String, CacheStatsAggregated>> e : stats.entrySet()) {
                    List<StatEntry> methods = new ArrayList();
                    classes.put(new CacheClass(e.getKey()), methods);
                    for (Entry<String, CacheStatsAggregated> s : e.getValue().entrySet()) {
                        String methodName = s.getKey();
                        Pattern p = Pattern.compile(".+"+Pattern.quote(e.getKey())+"\\.(.+)\\(");
                        Matcher m = p.matcher(s.getKey());
                        if (m.find()) {
                            methodName = m.group(1);
                        }
                        methods.add(new StatEntry(s.getKey().replace(e.getKey()+".", ""), methodName, s.getValue(), methods.size()%2 == 0 ? "a" : "b"));
                    }
                }
                t.setAttribute("classes", classes);   
                t.setAttribute("server", req.getServerName()+":"+req.getServerPort()+req.getContextPath());
                out.write(t.toString().getBytes());
                out.close();
            }
        } else {
            fc.doFilter(sr, sr1);
        }
    }

    public void destroy() {
    }    
    
    private class CacheClass {
        private final String name;
        private String scope = "?";

        public CacheClass(String name) {
            this.name = name;
            try {
                Class c = Class.forName(name);
                scope = GuiceUtil.getScopeDesc(module.get().getInjector(), c);
            } catch (ClassNotFoundException ex) {
                // ignore
            }
        }

        public String getName() {
            return name;
        }

        public String getScope() {
            return scope;
        }       
    }
    
    public static class StatEntry {
        private final String method;
        private final String methodName;
        private final CacheStatsAggregated stats;
        private final String rowClass;

        public StatEntry(String method, String methodName, CacheStatsAggregated stats, String rowClass) {
            this.method = method;
            this.methodName = methodName;
            this.stats = stats;
            this.rowClass = rowClass;
        }

        public String getMethod() {
            return method;
        }

        public String getMethodName() {
            return methodName;
        }

        public CacheStatsAggregated getStats() {
            return stats;
        }
        
        public String getTotalLoadTimeSeconds() {
            return StringUtil.formatDecimal((double)stats.getCombinedStats().getTotalLoadTime().get()/1000000000.0, 2);
        }
        
        public String getAverageLoadTimeMillis() {
            return StringUtil.formatDecimal((double)stats.getCombinedStats().getAverageLoadTimeNanos()/1000000.0, 2);
        }

        public String getRowClass() {
            return rowClass;
        }
        
    }
    
}
