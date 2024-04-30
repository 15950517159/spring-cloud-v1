package com.demo.domain;

public class TestStatic {
    static {
        System.out.printf("11111");
    }
    public static String getStr(String ss){
        System.out.printf("getStr "+System.currentTimeMillis());
        return System.currentTimeMillis()+"";
    }
}
