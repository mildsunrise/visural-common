package com.visural.common;

import com.visural.common.web.lesscss.LessCSS;
import junit.framework.TestCase;



public class ObjectSizeTest extends TestCase {
    
    public void testNew() {    
        Object obj = new LessCSS();
        
        ObjectSize.estimate(obj);
        ObjectSize.estimate(obj);
        ObjectSize.estimate(obj);
        ObjectSize.estimate(obj);
        
        long nb = System.nanoTime();
        int n = ObjectSize.estimate(obj);
        long ne = System.nanoTime();
//        
//        ObjectSizeOld.estimate(obj);
//        ObjectSizeOld.estimate(obj);
//        ObjectSizeOld.estimate(obj);
//        ObjectSizeOld.estimate(obj);
//
//        long ob = System.nanoTime();
//        int o = ObjectSizeOld.estimate(obj);
//        long oe = System.nanoTime();
//        
//        System.out.println("Old = "+o+", dur = "+(oe-ob));
        System.out.println("New = "+n+", dur = "+(ne-nb));
    }
    
    
}
