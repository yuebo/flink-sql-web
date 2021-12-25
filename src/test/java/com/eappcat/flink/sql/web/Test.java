package com.eappcat.flink.sql.web;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import sun.jvm.hotspot.tools.ObjectHistogram;

@RunWith(JUnit4.class)
public class Test {

    @org.junit.Test
    public void test(){
            ObjectHistogram objectHistogram=new ObjectHistogram();
            objectHistogram.run(System.out,System.err);

    }

}
