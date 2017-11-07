package com.webcerebrium.kucoin.datatype;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.webcerebrium.kucoin.api.KucoinApiException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
public class KucoinCurrencyInfo {

    Map<String, String> currencies = new HashMap<>();
    Map<String, Map<String, BigDecimal>> rates = new HashMap<>();

    public KucoinCurrencyInfo() {
    }

    /**
     * constructor of currency information object
     * from response JSON
     * @param response original JSON of response
     * @throws KucoinApiException if case of any error
     */
    public KucoinCurrencyInfo(JsonObject response) throws KucoinApiException {
        if (!response.has("data")) {
            throw new KucoinApiException("Missing data in response object while trying to read currency information");
        }
        JsonObject data = response.get("data").getAsJsonObject();
        if (!data.has("currencies")) {
            throw new KucoinApiException("Data in response object expected to have currencies");
        }
        if (!data.get("currencies").isJsonArray()) {
            throw new KucoinApiException("data.currencies in response object expected to be an array");
        }
        JsonArray arrCurrencies = data.get("currencies").getAsJsonArray();
        for (JsonElement elem: arrCurrencies) {
            JsonArray keyval = elem.getAsJsonArray();
            if (keyval.size() >= 2) {
                String currencyName = keyval.get(0).getAsString();
                String currencySymbol = keyval.get(1).getAsString();
                currencies.put(currencyName, currencySymbol);
            }
        }
        if (!data.has("rates")) {
            throw new KucoinApiException("Data in response object expected to have rates");
        }
        if (!data.get("rates").isJsonObject()) {
            throw new KucoinApiException("data.rates in response object expected to be an object");
        }
        JsonObject objRates = data.get("rates").getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry: objRates.entrySet()) {
            String coin = entry.getKey();
            JsonObject objMapRate = entry.getValue().getAsJsonObject();
            Map<String, BigDecimal> mapRate = new HashMap<>();
            for (Map.Entry<String, JsonElement> entryRate: objMapRate.entrySet()) {
                String currency = entryRate.getKey();
                BigDecimal value = entryRate.getValue().getAsBigDecimal();
                mapRate.put(currency, value);
            }
            rates.put(coin, mapRate);
        }
    }

    /**
     * Helper to get currency symbol by its name
     * @param currencyName, i.e. CNY
     * @return string symbol, i.e. ¥
     */
    public String getCurrencySymbol(String currencyName) {
        if (!currencies.containsKey(currencyName)) {
            return "";
        }
        return currencies.get(currencyName);
    }

    /**
     * example .getRate("BTC", "USD")
     * @param coin, now only BTC
     * @param currency, i.e. USD
     * @return decimal value
     * @throws KucoinApiException if case of any error
     */
    public BigDecimal getRate(String coin, String currency) throws KucoinApiException {
        if (!rates.containsKey(coin)) {
            throw new KucoinApiException("There is no such coin in the rates table");
        }
        Map<String, BigDecimal> mapCoinRates = rates.get(coin);
        if (!mapCoinRates.containsKey(currency)) {
            throw new KucoinApiException("There is no such currency in the rates table");
        }
        return mapCoinRates.get(currency);
    }

}
