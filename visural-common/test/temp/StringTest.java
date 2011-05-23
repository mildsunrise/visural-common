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

package temp;

import com.visural.common.StringUtil;
import com.visural.common.coder.Base64Encoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import junit.framework.TestCase;

/**
 * @version $Id: StringTest.java 99 2010-11-22 07:58:42Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class StringTest extends TestCase {

    public void testPST() {
        System.out.println(StringUtil.getStackTrace(new RuntimeException("Error with something.", new IOException("An IO error occurred"))));
    }

    public void testDelim() {
        System.out.println(StringUtil.delimitObjectsToString(", ", 1, 2, 3 ,4 ,5 ,6));
        System.out.println(StringUtil.delimitObjectsToString(", ", " and ",1, "for", 3 ,4 ,5 ,6));
        System.out.println(StringUtil.delimitObjectsToString(", ", " and ", Arrays.asList(1,2,3,4,5,6)));
        System.out.println(StringUtil.delimitObjectsToString(", ", " and ", Arrays.asList(1,2,3,4,5,6), Arrays.asList(1,2,3,4,5,6)));
    }

    public void testBase64() throws IOException {
        //byte[] data = IOUtil.fileToByteArray("c:/dojapps/temp/fw4.pdf");
        Base64Encoder enc = new Base64Encoder();
        ByteArrayInputStream eis = new ByteArrayInputStream("http://www.irs.gov/pub/irs-pdf/fw4.pdf".getBytes());
        ByteArrayOutputStream eos = new ByteArrayOutputStream();
        enc.encode(eis, eos);
        System.out.println(new String(eos.toByteArray()));
    }

}
