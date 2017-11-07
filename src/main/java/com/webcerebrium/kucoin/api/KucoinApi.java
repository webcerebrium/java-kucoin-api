package com.webcerebrium.kucoin.api;

/* ============================================================
 * java-kucoin-api
 * https://github.com/webcerebrium/java-binance-api
 * ============================================================
 * Copyright 2017-, Viktor Lopata, Web Cerebrium OÜ
 * Released under the MIT License
 * ============================================================
 *
 * Original documentation:
 * http://docs.kucoinapidocs.apiary.io/
 */

import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.webcerebrium.kucoin.datatype.KucoinCurrencyInfo;
import com.webcerebrium.kucoin.datatype.KucoinOrderBook;
import com.webcerebrium.kucoin.datatype.KucoinOrderSide;
import com.webcerebrium.kucoin.datatype.KucoinSymbol;
import com.webcerebrium.kucoin.datatype.KucoinTradingStats;
import com.webcerebrium.kucoin.datatype.KucoinUserInfo;
import com.webcerebrium.kucoin.datatype.KucoinWallet;
import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

@Data
public class KucoinApi {

    /* Actual API key and Secret Key that will be used */
    public String apiKey;
    public String secretKey;

    public KucoinConfig config = new KucoinConfig();

    /**
     * API Base URL
     */
    public String baseUrl = "https://api.kucoin.com/";

    /**
     * Guava Class Instance for escaping
     */
    private Escaper esc = UrlEscapers.urlFormParameterEscaper();


    /**
     * Constructor of API when you exactly know the keys
     * @param apiKey Public API Key
     * @param secretKey Secret API Key
     * @throws KucoinApiException in case of any error
     */
    public KucoinApi(String apiKey, String secretKey) throws KucoinApiException {

        this.apiKey = apiKey;
        this.secretKey = secretKey;
        validateCredentials();
    }

    /**
     * Constructor of API - keys are loaded from VM options, environment variables, resource files
     * @throws KucoinApiException in case of any error
     */
    public KucoinApi() throws KucoinApiException {
        this.apiKey = config.getVariable("KUCOIN_API_KEY");
        this.secretKey = config.getVariable("KUCOIN_SECRET");
    }

    /**
     * Validation we have API keys set up
     * @throws KucoinApiException in case of any error
     */
    protected void validateCredentials() throws KucoinApiException {
        String humanMessage = "Please check environment variables or VM options";
        if (Strings.isNullOrEmpty(this.getApiKey()))
            throw new KucoinApiException("Missing KUCOIN_API_KEY. " + humanMessage);
        if (Strings.isNullOrEmpty(this.getSecretKey()))
            throw new KucoinApiException("Missing KUCOIN_SECRET. " + humanMessage);
    }

    // ======= ======= ======= ======= ======= =======
    // READING ACCOUNT INFORMATION
    // ======= ======= ======= ======= ======= =======
    public KucoinUserInfo getUserInfo() throws KucoinApiException {
        JsonObject response = (new KucoinRequest(baseUrl + "v1/user/info"))
                .sign(this.getApiKey(), this.getSecretKey())
                .read().asJsonObject();
        return new KucoinUserInfo(response.get("data").getAsJsonObject());
    }

    public List<KucoinWallet> readAccountBalance() throws KucoinApiException {
        JsonObject response = (new KucoinRequest(baseUrl + "v1/account/balance"))
                .sign(this.getApiKey(), this.getSecretKey())
                .read().asJsonObject();

        List<KucoinWallet> wallets = new LinkedList<>();
        JsonArray data = response.get("data").getAsJsonArray();
        for (JsonElement elem: data) {
            KucoinWallet wallet = new KucoinWallet(elem.getAsJsonObject());
            if (!wallet.isEmpty()) {
                wallets.add(wallet);
            }
        }
        return wallets;
    }

    public KucoinWallet readAccountBalance(String coin) throws KucoinApiException {
        JsonObject response = (new KucoinRequest(baseUrl + "v1/account/" + coin + "/balance"))
                .sign(this.getApiKey(), this.getSecretKey())
                .read().asJsonObject();
        JsonObject data = response.get("data").getAsJsonObject();
        return new KucoinWallet(data);
    }

    // ======= ======= ======= ======= ======= =======
    // MARKET INFORMATION
    // ======= ======= ======= ======= ======= =======

    /**
     * Getting information about rates of converting main coins (currently it is just BTC) into currencies
     * @return object of KucoinCurrencyInfo
     * @throws KucoinApiException
     */
    public KucoinCurrencyInfo getCurrencyInfo() throws KucoinApiException {
        JsonObject response = (new KucoinRequest(baseUrl + "v1/open/currencies")).read().asJsonObject();
        return new KucoinCurrencyInfo(response);
    }

    public KucoinTradingStats getTradingStats() throws KucoinApiException {
        JsonObject response = (new KucoinRequest(baseUrl + "v1/market/open/symbols")).read().asJsonObject();
        return new KucoinTradingStats(response);
    }

    public KucoinOrderBook getOrderBook(KucoinSymbol symbol, int group, int limit) throws KucoinApiException {
        StringBuffer sb = new StringBuffer()
            .append("?symbol=").append(symbol.get())
            .append("&group=").append(group)
            .append("&limit=").append(limit);
        JsonObject response = (new KucoinRequest(baseUrl + "v1/open/orders" + sb.toString()))
                .read().asJsonObject();
        return new KucoinOrderBook(symbol, response);
    }

    // ======= ======= ======= ======= ======= =======
    // TRADING
    // ======= ======= ======= ======= ======= =======
    public String placeOrder(KucoinSymbol symbol, KucoinOrderSide side, BigDecimal amount, BigDecimal price) throws KucoinApiException {
        JsonObject payload = new JsonObject();
        payload.addProperty("symbol", symbol.get());
        payload.addProperty("type", side.toString());
        payload.addProperty("price", price);
        payload.addProperty("amount", amount);
        JsonObject response = (new KucoinRequest(baseUrl + "v1/order"))
                .payload(payload).post().read().asJsonObject();
        JsonObject data = response.get("data").getAsJsonObject();
        return data.get("orderOid").getAsString();
    }

    public String buy(KucoinSymbol symbol, BigDecimal amount, BigDecimal price) throws KucoinApiException {
        return placeOrder(symbol, KucoinOrderSide.BUY, amount, price);
    }

    public String sell(KucoinSymbol symbol, BigDecimal amount, BigDecimal price) throws KucoinApiException {
        return placeOrder(symbol, KucoinOrderSide.SELL, amount, price);
    }
}
