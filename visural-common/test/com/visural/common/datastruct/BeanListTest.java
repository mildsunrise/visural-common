/*
 *  Copyright 2011 Visural.
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

package com.visural.common.datastruct;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Visural
 */
public class BeanListTest extends TestCase {

    public void testNullsSort() {
        BeanList<Bean> list = new BeanList<Bean>(new ArrayList(), 3);
        list.setNullHandling(BeanList.Nulls.AreMore);
        list.add(new Bean(null, "2", "unique "+list.size()));
        list.add(new Bean(null, "2", "unique "+list.size()));
        list.add(new Bean(null, "2", "unique "+list.size()));
        list.add(new Bean(null, "2", "unique "+list.size()));
        list.add(new Bean(null, "2", "unique "+list.size()));
        list.add(new Bean("one", "2", "unique "+list.size()));
        list.add(new Bean("one", "2", "unique "+list.size()));
        list.add(new Bean("one", "2", "unique "+list.size()));
        list.add(new Bean("one", "3", "unique "+list.size()));
        list.add(new Bean("one", "3", "unique "+list.size()));
        unsort(list);
        list.sortByProperties("-s1", "-s2", "s3");
        System.out.println(list.toString());
        assertTrue(list.toString().contains("null 2 unique 0\n"+
                                            ", null 2 unique 1\n"+
                                            ", null 2 unique 2\n"+
                                            ", null 2 unique 3\n"+
                                            ", null 2 unique 4\n"+
                                            ", one 3 unique 8\n"+
                                            ", one 3 unique 9\n"+
                                            ", one 2 unique 5\n"+
                                            ", one 2 unique 6\n"+
                                            ", one 2 unique 7"));
    }

    private void unsort(List l) {
        for (int n = 0; n < l.size(); n++) {
            Object o1 = l.get((int)(Math.random()*1000d)%l.size());
            Object o2 = l.get((int)(Math.random()*1000d)%l.size());
            while (o1 == o2) {
                o2 = l.get((int)(Math.random()*1000d)%l.size());
            }
            l.remove(o2);
            l.remove(o1);
            l.add(o1);
            l.add(o2);
        }
    }

    public static class Bean {
        private String s1;
        private String s2;
        private String s3;

        public Bean() {
        }

        public Bean(String s1, String s2, String s3) {
            this.s1 = s1;
            this.s2 = s2;
            this.s3 = s3;
        }

        public String getS1() {
            return s1;
        }

        public void setS1(String s1) {
            this.s1 = s1;
        }

        public String getS2() {
            return s2;
        }

        public void setS2(String s2) {
            this.s2 = s2;
        }

        public String getS3() {
            return s3;
        }

        public void setS3(String s3) {
            this.s3 = s3;
        }

        @Override
        public String toString() {
            return String.format("%s %s %s\n", s1, s2, s3);
        }
    }
    
    ////////////////////////////////////////////////
    
    public abstract static class Parent {
        private String parent = "p";

        public String getParent() {
            return parent;
        }

        public void setParent(String parent) {
            this.parent = parent;
        }
        
        public abstract String getAbstract();
    }
    
    public class Child1 extends Parent {

        private String abstractStr = Double.valueOf(Math.random()).toString();
        
        @Override
        public String getAbstract() {
            return abstractStr;
        }        
    }    
    
    public class Child2 extends Parent {

        private String abstractStr = Double.valueOf(Math.random()).toString();
        
        @Override
        public String getAbstract() {
            return abstractStr+20;
        }        
    }
    
    /**
     * A test which ensures that beans which descend from a common parent can 
     * be sorted on an abstract parent property even 
     * when different implementations are provided.
     * @throws Exception 
     */
    public void testAbstractBean() throws Exception {       
        BeanList<Parent> bl = new BeanList<BeanListTest.Parent>(new ArrayList<Parent>(), 4);
        for (int n = 0; n < 100; n++) {
            if (Math.random() < 0.5d) {
                bl.add(new Child1());
            } else {
                bl.add(new Child2());
            }
        }
        bl.sortByProperties("abstract");
    }
}
