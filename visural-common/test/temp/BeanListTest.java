/*
 *  Copyright 2010 Visural.
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

import com.visural.common.datastruct.BeanList;
import java.util.ArrayList;
import java.util.Arrays;
import junit.framework.TestCase;

/**
 *
 * @author Visural
 */
public class BeanListTest extends TestCase {

    public static class Bean {
        private String prop1;
        private String prop2;
        private String prop3;

        public Bean(String prop1, String prop2, String prop3) {
            this.prop1 = prop1;
            this.prop2 = prop2;
            this.prop3 = prop3;
        }

        public Bean() {
        }

        

        public String getProp1() {
            return prop1;
        }

        public void setProp1(String prop1) {
            this.prop1 = prop1;
        }

        public String getProp2() {
            return prop2;
        }

        public void setProp2(String prop2) {
            this.prop2 = prop2;
        }

        public String getProp3() {
            return prop3;
        }

        public void setProp3(String prop3) {
            this.prop3 = prop3;
        }

        @Override
        public String toString() {
            return prop1+" :: "+prop2+" :: "+prop3+"\n";
        }



    }

    public void testTemp() {
        BeanList<Bean> list = new BeanList<Bean>(new ArrayList<Bean>(Arrays.asList(
                new Bean("one", "two", "three"),
                new Bean("french fry", "apple", "orange"),
                new Bean("a", "n", "c"),
                new Bean("a", null, "c"),
                new Bean("three", "two", "one")
                )), 2);
        list.setNullHandling(BeanList.Nulls.AreLess);
        list.sortByProperties("-prop2", "prop3");
        System.out.println(list);
        list.resortByProperty("prop2");
        System.out.println(list);
        list.resortByProperty("-prop3");
        System.out.println(list);
        list.resortByProperty("-prop2");
        System.out.println(list);
    }

}
