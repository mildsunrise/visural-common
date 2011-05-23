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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 *
 * @author Richard Nichols
 */
public class RelativeFileResolver implements ResourceResolver {

    private final String canonicalPath;

    public RelativeFileResolver(String canonicalPath) {
        this.canonicalPath = canonicalPath;
    }

    public InputStream resolveReference(String urlRef) {
        try {
            if (urlRef.startsWith("http://") || urlRef.startsWith("https://")) {
                return new URL(urlRef).openStream();
            }
            File f = new File(canonicalPath + File.separator + urlRef);
            if (f.exists()) {
                return new FileInputStream(f);
            } else {
                return null;
            }
        } catch (Throwable t) {
            return null;
        }
    }
}
