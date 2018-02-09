package com.webcerebrium.kucoin.api;

import com.webcerebrium.kucoin.datatype.KucoinUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class UserInfoTest {

    KucoinApi api = null;

    @Before
    public void setUp() throws Exception, KucoinApiException {
        api = new KucoinApi();
    }

    @Test
    public void testUserInfo() throws Exception, KucoinApiException {
        KucoinUserInfo info = api.getUserInfo();
        log.info("User {}", info.toString());
    }

}
