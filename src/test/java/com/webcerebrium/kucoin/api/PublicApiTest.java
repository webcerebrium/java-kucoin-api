package com.webcerebrium.kucoin.api;

import com.webcerebrium.kucoin.datatype.KucoinCurrencyInfo;
import com.webcerebrium.kucoin.datatype.KucoinTradingStats;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class PublicApiTest {

    KucoinApi api = null;

    @Before
    public void setUp() throws Exception, KucoinApiException {
        api = new KucoinApi();
    }

    @Test
    public void testGetCurrencyInfo() throws Exception, KucoinApiException {
        KucoinCurrencyInfo currencyInfo = api.getCurrencyInfo();
        log.info("CURRENCY INFO = {}", currencyInfo.toString());
        log.info("BTC/USD={}", currencyInfo.getRate("BTC", "USD"));
    }

    @Test
    public void testMarkets() throws Exception, KucoinApiException {
        KucoinTradingStats stats = api.getTradingStats();
        log.info( "MARKETS OF ETH={}", stats.getCoinsOf("ETH"));
        log.info( "MARKETS OF NEO={}", stats.getCoinsOf("NEO"));
        log.info( "MARKETS OF BTC={}", stats.getCoinsOf("BTC"));
    }
}
