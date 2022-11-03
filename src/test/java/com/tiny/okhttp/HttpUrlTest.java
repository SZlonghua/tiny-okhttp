package com.tiny.okhttp;

import junit.framework.TestCase;

public class HttpUrlTest extends TestCase {

    public void testPattern(){
        HttpUrl url = new HttpUrl("http://localhost:8080/api/hello");
        System.out.println(url.host()+":"+url.port()+"$$$"+ url.path);
    }

}