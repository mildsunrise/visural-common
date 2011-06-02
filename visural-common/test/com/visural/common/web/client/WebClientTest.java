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

import junit.framework.TestCase;

/**
 *
 * @author Richard Nichols
 */
@SuppressWarnings("PMD")
public class WebClientTest extends TestCase {

    private static class BrowserTest {
        String userAgent;
        WebClient expected;

        public BrowserTest(String userAgent, WebClient expected) {
            this.userAgent = userAgent;
            this.expected = expected;
        }       
    }

    private static final BrowserTest[] tests;

    static {
        tests = new BrowserTest[] {
            new BrowserTest(null,
                    new WebClient(UserAgent.UNKNOWN, 0, null, Platform.UNKNOWN)),
            new BrowserTest("   ",
                    new WebClient(UserAgent.UNKNOWN, 0, null, Platform.UNKNOWN)),
            new BrowserTest("Opera/9.80 (J2ME/MIDP; Opera Mini/4.2.14912/870; U; id) Presto/2.4.15",
                    new WebClient(UserAgent.OPERA_MINI, 4, "4.2.14912", Platform.JAVA_ME)),
            new BrowserTest("Opera/9.80 (J2ME/MIDP; Opera Mini/5.0.0176/1150; U; en) Presto/2.4.15",
                    new WebClient(UserAgent.OPERA_MINI, 5, "5.0.0176", Platform.JAVA_ME)),
            new BrowserTest("Opera/9.80 (iPhone; Opera Mini/5.0.0176/764; U; en) Presto/2.4.15",
                    new WebClient(UserAgent.OPERA_MINI, 5, "5.0.0176", Platform.IOS)),
            new BrowserTest("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; InfoPath.2)",
                    new WebClient(UserAgent.IE, 8, "8.0", Platform.WIN7)),
            new BrowserTest("Mozilla/4.0 (compatible; MSIE 7.0b; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)",
                    new WebClient(UserAgent.IE, 7, "7.0b", Platform.WINXP)),
            new BrowserTest("corrupted MSIE; Windows XP)",
                    new WebClient(UserAgent.UNKNOWN, 0, null, Platform.WINXP)),
            new BrowserTest("Mozilla/4.0 (compatible; MSIE 6.1; Windows XP)",
                    new WebClient(UserAgent.IE, 6, "6.1", Platform.WINXP)),
            new BrowserTest("Mozilla/4.0 (MSIE 6.0; Windows NT 5.0)",
                    new WebClient(UserAgent.IE, 6, "6.0", Platform.WIN2K)),
            new BrowserTest("Mozilla/4.0 (compatible; MSIE 5.5; Windows NT)",
                    new WebClient(UserAgent.IE, 5, "5.5", Platform.WINNT)),
            new BrowserTest("Opera/9.64 (X11; Linux i686; U; tr) Presto/2.1.1",
                    new WebClient(UserAgent.OPERA, 9, "9.64", Platform.LINUX)),
            new BrowserTest("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; fr) Opera 8.54",
                    new WebClient(UserAgent.OPERA, 8, "8.54", Platform.WINXP)),
            new BrowserTest("Opera/8.52 (Windows NT 5.0; U; en)",
                    new WebClient(UserAgent.OPERA, 8, "8.52", Platform.WIN2K)),
            new BrowserTest("Mozilla/5.0 (Windows NT 5.1; U) Opera 7.54 [de]",
                    new WebClient(UserAgent.OPERA, 7, "7.54", Platform.WINXP)),
            new BrowserTest("Mozilla/5.0 (Windows; U; Windows NT 6.1; ru; rv:1.9.2.3) Gecko/20100401 Firefox/4.0 (.NET CLR 3.5.30729)",
                    new WebClient(UserAgent.FIREFOX, 4, "4.0", Platform.WIN7)),
            new BrowserTest("Mozilla/5.0 (X11; U; Linux i686; pl-PL; rv:1.9.0.2) Gecko/2008092313 Ubuntu/9.25 (jaunty) Firefox/3.8",
                    new WebClient(UserAgent.FIREFOX, 3, "3.8", Platform.LINUX)),
            new BrowserTest("Mozilla/5.0 (Windows; U; Windows NT 6.0; zh-CN; rv:1.9.2.6) Gecko/20100625 Firefox/3.6.6 GTB7.1",
                    new WebClient(UserAgent.FIREFOX, 3, "3.6.6", Platform.WINVISTA)),
            new BrowserTest("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2",
                    new WebClient(UserAgent.FIREFOX, 3, "3.5.2", Platform.LINUX)),
            new BrowserTest("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.2) Gecko/2008092313 Ubuntu/8.04 (hardy) Firefox/3.1",
                    new WebClient(UserAgent.FIREFOX, 3, "3.1", Platform.LINUX)),
            new BrowserTest("Mozilla/5.0 (X11; U; Linux x86_64; es-AR; rv:1.9) Gecko/2008061017 Firefox/3.0",
                    new WebClient(UserAgent.FIREFOX, 3, "3.0", Platform.LINUX)),
            new BrowserTest("Mozilla/5.0 (X11; U; Linux x86_64; fr; rv:1.8.1.3) Gecko/20070322 Firefox/2.0.0.3",
                    new WebClient(UserAgent.FIREFOX, 2, "2.0.0.3", Platform.LINUX)),
            new BrowserTest("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.0.6) Gecko/20060803 Firefox/1.5.0.6 (Swiftfox)",
                    new WebClient(UserAgent.FIREFOX, 1, "1.5.0.6", Platform.LINUX)),
            new BrowserTest("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; es-es) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16",
                    new WebClient(UserAgent.SAFARI, 5, "5.0", Platform.MACOSX)),
            new BrowserTest("Mozilla/5.0 (Windows; U; Windows NT 6.1; ko-KR) AppleWebKit/531.21.8 (KHTML, like Gecko) Version/4.0.4 Safari/531.21.10",
                    new WebClient(UserAgent.SAFARI, 4, "4.0.4", Platform.WIN7)),
            new BrowserTest("Mozilla/5.0 (Windows; U; Windows NT 5.1; ru-RU) AppleWebKit/525.28 (KHTML, like Gecko) Version/3.2.2 Safari/525.28.1",
                    new WebClient(UserAgent.SAFARI, 3, "3.2.2", Platform.WINXP)),
            new BrowserTest("Mozilla/5.0 (Windows; U; Windows NT 5.2; en-US) AppleWebKit/534.3 (KHTML, like Gecko) Chrome/6.0.463.0 Safari/534.3",
                    new WebClient(UserAgent.CHROME, 6, "6.0.463.0", Platform.WINXP)),
            new BrowserTest("Mozilla/5.0 (Windows; U; Windows NT 5.2; en-US) AppleWebKit/534.3 (KHTML, like Gecko) Chrome/asdfasdf6.0..0 Safari/534.3",
                    new WebClient(UserAgent.CHROME, 0, null, Platform.WINXP)),
            new BrowserTest("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_3; en-US) AppleWebKit/533.3 (KHTML, like Gecko) Chrome/5.0.363.0 Safari/533.3",
                    new WebClient(UserAgent.CHROME, 5, "5.0.363.0", Platform.MACOSX)),
            new BrowserTest("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-US) AppleWebKit/532.2 (KHTML, like Gecko) Chrome/4.0.222.5 Safari/532.2",
                    new WebClient(UserAgent.CHROME, 4, "4.0.222.5", Platform.MACOSX)),
            new BrowserTest("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/3.0.197 Safari/532.0",
                    new WebClient(UserAgent.CHROME, 3, "3.0.197", Platform.MACOSX)),
            new BrowserTest("Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)",
                    new WebClient(UserAgent.YAHOO_SLURP, 0, null, Platform.UNKNOWN)),
            new BrowserTest("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
                    new WebClient(UserAgent.GOOGLEBOT, 2, "2.1", Platform.UNKNOWN)),
            new BrowserTest("msnbot/1.0 (+http://search.msn.com/msnbot.htm)",
                    new WebClient(UserAgent.MSNBOT, 1, "1.0", Platform.UNKNOWN)),
            new BrowserTest("Mozilla/5.0 (Linux; U; Android 1.0; en-us; dream) AppleWebKit/525.10+ (KHTML, like Gecko) Version/3.0.4 Mobile Safari/523.12.2",
                    new WebClient(UserAgent.SAFARI, 3, "3.0.4", Platform.ANDROID)),
            new BrowserTest("Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Version/3.0 Mobile/1A543a Safari/419.3",
                    new WebClient(UserAgent.SAFARI, 3, "3.0", Platform.IOS)),
            new BrowserTest("Mozilla/5.0 (iPod; U; CPU like Mac OS X; en) AppleWebKit/420.1 (KHTML, like Gecko) Version/3.0 Mobile/3A100a Safari/419.3",
                    new WebClient(UserAgent.SAFARI, 3, "3.0", Platform.IOS)),
            new BrowserTest("Mozilla/5.0(iPad; U; CPU iPhone OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B314 Safari/531.21.10",
                    new WebClient(UserAgent.SAFARI, 4, "4.0.4", Platform.IOS)),
            new BrowserTest("Some random user agent",
                    new WebClient(UserAgent.UNKNOWN, 0, null, Platform.UNKNOWN))
        };
    }

    public void testBrowsers() {
        for (BrowserTest test : tests) {
            System.out.println("Test - "+test.userAgent);
            WebClient res = WebClient.detect(test.userAgent);
            assertTrue(res+" != "+test.userAgent, res.equals(test.expected));
        }
    }

}
