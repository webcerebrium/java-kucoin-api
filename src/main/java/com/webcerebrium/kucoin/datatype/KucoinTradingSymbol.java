package com.webcerebrium.kucoin.datatype;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.webcerebrium.kucoin.api.KucoinApiException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/*
 {
 "coinType": "KCS",
 "trading": true,
 "lastDealPrice": 4500,
 "buy": 4120,
 "sell": 4500,
 "coinTypePair": "BTC",
 "sort": 0,
 "feeRate": 0.001,
 "volValue": 324866889,
 "high": 6890,
 "datetime": 1506051488000,
 "vol": 5363831663913,
 "low": 4500,
 "changeRate": -0.3431
 }

*/

@Slf4j
@Data
public class KucoinTradingSymbol {

    String coinType;
    boolean trading;
    BigDecimal lastDealPrice;
    BigDecimal buy;
    BigDecimal sell;
    String coinTypePair;
    Long sort;
    BigDecimal feeRate;
    BigDecimal volValue;
    BigDecimal high;
    Long datetime;
    BigDecimal vol;
    BigDecimal low;
    BigDecimal changeRate;

    public KucoinTradingSymbol() {
    }

    private void jsonExpect(JsonObject obj, Set<String> fields) throws KucoinApiException {
        Set<String> missing = new HashSet<>();
        for (String f: fields) { if (!obj.has(f) || obj.get(f).isJsonNull()) missing.add(f); }
        if (missing.size() > 0) {
            log.warn("Missing fields {} in {}", missing.toString(), obj.toString());
            throw new KucoinApiException("Missing fields " + missing.toString());
        }
    }

    private Long safeLong(JsonObject obj, String field) {
        if (obj.has(field) && obj.get(field).isJsonPrimitive() && obj.get(field) != null) {
            try {
                return obj.get(field).getAsLong();
            } catch (java.lang.NumberFormatException nfe) {
                log.info("Number format exception in field={} value={} trade={}", field, obj.get(field), obj.toString());
            }
        }
        return 0L;
    }
    private BigDecimal safeDecimal(JsonObject obj, String field) {
        if (obj.has(field) && obj.get(field).isJsonPrimitive() && obj.get(field) != null) {
            try {
                return obj.get(field).getAsBigDecimal();
            } catch (java.lang.NumberFormatException nfe) {
                log.info("Number format exception in field={} value={} trade={}", field, obj.get(field), obj.toString());
            }
        }
        return null;
    }

    public KucoinTradingSymbol(JsonObject obj) throws KucoinApiException {
        jsonExpect(obj, ImmutableSet.of("coinType", "coinTypePair", "trading"));
        coinType = obj.get("coinType").getAsString();
        coinTypePair = obj.get("coinTypePair").getAsString();
        trading = obj.get("trading").getAsBoolean();
        sort = safeLong(obj, "sort");
        datetime = safeLong(obj, "datetime");

        lastDealPrice = safeDecimal(obj, "lastDealPrice");
        buy = safeDecimal(obj, "buy");
        sell = safeDecimal(obj, "sell");
        feeRate = safeDecimal(obj, "feeRate");
        volValue = safeDecimal(obj, "volValue");
        high = safeDecimal(obj, "high");
        vol = safeDecimal(obj, "vol");
        low = safeDecimal(obj, "low");
        changeRate = safeDecimal(obj, "changeRate");
    }

    public KucoinSymbol getSymbol() throws KucoinApiException {
        return KucoinSymbol.valueOf(coinType + "-" + coinTypePair);
    }
}
