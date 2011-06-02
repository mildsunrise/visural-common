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
package com.visural.common.web.client;

import com.visural.common.StringUtil;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;

/**
 * A mechanism to detect client software, version and platform from a user-agent string.
 * 
 * @author Richard Nichols
 */
public class WebClient implements Serializable {

    private final UserAgent userAgent;
    private final int majorVersion;
    private final String fullVersion;
    private final Platform platform;

    public WebClient(UserAgent userAgent, int majorVersion, String fullVersion, Platform platform) {
        this.userAgent = userAgent;
        this.majorVersion = majorVersion;
        this.fullVersion = fullVersion;
        this.platform = platform;
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getFullVersion() {
        return fullVersion;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public UserAgent getUserAgent() {
        return userAgent;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WebClient other = (WebClient) obj;
        if (this.userAgent != other.userAgent) {
            return false;
        }
        if (this.majorVersion != other.majorVersion) {
            return false;
        }
        if ((this.fullVersion == null) ? (other.fullVersion != null) : !this.fullVersion.equals(other.fullVersion)) {
            return false;
        }
        if (this.platform != other.platform) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.userAgent != null ? this.userAgent.hashCode() : 0);
        hash = 37 * hash + this.majorVersion;
        hash = 37 * hash + (this.fullVersion != null ? this.fullVersion.hashCode() : 0);
        hash = 37 * hash + (this.platform != null ? this.platform.hashCode() : 0);
        return hash;
    }


    private boolean checkInlineSupport(boolean returnMhtml) {
        boolean mhtml = false;
        boolean dataUri = false;
        switch (getUserAgent()) {
            case CHROME:
            case FIREFOX:
            case SAFARI:
                dataUri = true;
                break;
            case OPERA:
                if (getMajorVersion() >= 7) {
                    dataUri = true;
                }
                break;
            case IE:
                if (getMajorVersion() >= 8) {
                    dataUri = true;
                } else if (getMajorVersion() >= 6 && !platform.equals(Platform.WINVISTA)) { // vista is unreliable for MHTML
                    mhtml = true;
                }
                break;
        }
        return (returnMhtml ? mhtml : dataUri);
    }

    public boolean supportsDataUris() {
        return checkInlineSupport(false);
    }

    public boolean supportsMHTML() {
        return checkInlineSupport(true);
    }

    public static WebClient detect(HttpServletRequest req) {
        return detect(req.getHeader("User-Agent"));
    }

    private static Platform detectedPlatform(String userAgent) {
        if (StringUtil.isBlankStr(userAgent)) {
            return Platform.UNKNOWN;
        } else if(userAgent.contains("Android")) {
            return Platform.ANDROID;
        } else if(userAgent.contains("J2ME")) {
            return Platform.JAVA_ME;
        } else if(userAgent.contains("iPhone") || userAgent.contains("iPod") || userAgent.contains("iPad")) {
            return Platform.IOS;
        } else if(userAgent.contains("Mac OS X")) {
            return Platform.MACOSX;
        } else if (userAgent.contains("Windows NT 5.0")) {
            return Platform.WIN2K;
        } else if (userAgent.contains("Windows NT 5.1") || userAgent.contains("Windows NT 5.2") || userAgent.contains("Windows XP")) {
            return Platform.WINXP;
        } else if (userAgent.contains("Windows NT 6.0")) {
            return Platform.WINVISTA;
        } else if (userAgent.contains("Windows NT 6.1")) {
            return Platform.WIN7;
        } else if (userAgent.contains("Windows NT")) {
            return Platform.WINNT;
        } else if (userAgent.contains("Linux")) {
            return Platform.LINUX;
        }
        return Platform.UNKNOWN;
    }

    public static WebClient detect(String userAgentString) {
        UserAgent ua = UserAgent.UNKNOWN;
        int version = 0;
        String ver = null;

        if (StringUtil.isNotBlankStr(userAgentString)) {
            try {
                if (userAgentString.contains("Yahoo! Slurp")) {
                    ua = UserAgent.YAHOO_SLURP;
                } else if (userAgentString.contains("Googlebot/")) {
                    ua = UserAgent.GOOGLEBOT;
                    ver = userAgentString.substring(userAgentString.indexOf("Googlebot/")+10);
                    ver = ver.substring(0, (ver.indexOf(";") > 0 ? ver.indexOf(";") : ver.length())).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                } else if (userAgentString.contains("msnbot/")) {
                    ua = UserAgent.MSNBOT;
                    ver = userAgentString.substring(userAgentString.indexOf("msnbot/")+7);
                    ver = ver.substring(0, (ver.indexOf(" ") > 0 ? ver.indexOf(" ") : ver.length())).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                } else if(userAgentString.contains("Chrome/")) {
                    ua = UserAgent.CHROME;
                    ver = userAgentString.substring(userAgentString.indexOf("Chrome/")+7);
                    ver = ver.substring(0, ver.indexOf(" ")).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                } else if (userAgentString.contains("Safari/")) {
                    ua = UserAgent.SAFARI;
                    ver = userAgentString.substring(userAgentString.indexOf("Version/")+8);
                    ver = ver.substring(0, (ver.indexOf(" ") > 0 ? ver.indexOf(" ") : ver.length())).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                } else if (userAgentString.contains("Opera Mini/")) {
                    ua = UserAgent.OPERA_MINI;
                    ver = userAgentString.substring(userAgentString.indexOf("Opera Mini/")+11);
                    ver = ver.substring(0, (ver.indexOf("/") > 0 ? ver.indexOf("/") : ver.length())).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                } else if (userAgentString.contains("Opera ")) {
                    ua = UserAgent.OPERA;
                    ver = userAgentString.substring(userAgentString.indexOf("Opera ")+6);
                    ver = ver.substring(0, (ver.indexOf(" ") > 0 ? ver.indexOf(" ") : ver.length())).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                } else if (userAgentString.contains("Firefox/")) {
                    ua = UserAgent.FIREFOX;
                    ver = userAgentString.substring(userAgentString.indexOf("Firefox/")+8);
                    ver = ver.substring(0, (ver.indexOf(" ") > 0 ? ver.indexOf(" ") : ver.length())).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                }  else if (userAgentString.contains("MSIE ")) {
                    ua = UserAgent.IE;
                    ver = userAgentString.substring(userAgentString.indexOf("MSIE ")+5);
                    ver = ver.substring(0, ver.indexOf(";")).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                } else if (userAgentString.contains("Opera/")) {
                    ua = UserAgent.OPERA;
                    ver = userAgentString.substring(userAgentString.indexOf("Opera/")+6);
                    ver = ver.substring(0, ver.indexOf(" ")).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                }
            } catch (NumberFormatException nfe) {
                ver = null;
                version = 0;
            }
        }
        Platform platform = detectedPlatform(userAgentString);
        return new WebClient(ua, version, ver, platform);
    }

    @Override
    public String toString() {
        return userAgent+" "+fullVersion+" / "+platform;
    }

}
