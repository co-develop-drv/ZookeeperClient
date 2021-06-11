package com.saaavsaaa.client.util;

import com.saaavsaaa.client.utility.WebRequestClient;
import org.junit.Test;

public class WebRequestClientTest {

    @Test
    public void test() throws Exception {
        String url = "http://10.0.2.17:25000/queries?json";
        System.out.println(WebRequestClient.get(url));
    }
}
