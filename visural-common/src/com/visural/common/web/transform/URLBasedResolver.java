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

import java.io.InputStream;
import java.net.URL;

/**
 *
 * @author Richard Nichols
 */
public class URLBasedResolver implements ResourceResolver {

    private final String baseURL;
    private final String server;

    public URLBasedResolver(String baseURL) {
        this.baseURL = baseURL.endsWith("/") ? baseURL : baseURL+"/";
        String temp = baseURL;
        boolean https = false;
        if (temp.startsWith("http://")) {
            temp = temp.substring(7);
        } else if (temp.startsWith("https://")) {
            temp = temp.substring(8);
            https = true;
        }
        server = (https ? "https://" : "http://")+temp.substring(0, temp.indexOf("/"));
    }

    public InputStream resolveReference(String urlRef) {
        try {
            if (urlRef.startsWith("http://") || urlRef.startsWith("https://")) {
                return new URL(urlRef).openStream();
            } else if (urlRef.startsWith("/")) {
                return new URL(server+urlRef).openStream();
            } else {
                // tricky - take care of relative down urls e.g. ../../folder/file.ext
                String currentBase = baseURL.substring(0, baseURL.length()-1);
                while (urlRef.startsWith("../")) {
                    urlRef = urlRef.substring(3);
                    currentBase = currentBase.substring(0, currentBase.lastIndexOf('/'));
                }
                return new URL(currentBase+"/"+urlRef).openStream();
            }
        } catch (Throwable t) {
            return null;
        }
    }
}
