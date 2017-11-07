package com.webcerebrium.kucoin.datatype;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.webcerebrium.kucoin.api.KucoinApiException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedList;
import java.util.List;

@Data
@Slf4j
public class KucoinOrderBook {
    KucoinSymbol symbol = null;
    List<KucoinOrderBookLine> sells = new LinkedList<KucoinOrderBookLine>();
    List<KucoinOrderBookLine> buys = new LinkedList<KucoinOrderBookLine>();

    public KucoinOrderBook(KucoinSymbol symbol) {
        this.symbol = symbol;
    }
    public KucoinOrderBook(KucoinSymbol symbol, JsonObject response) throws KucoinApiException {
        if (!response.has("data")) {
            throw new KucoinApiException("Missing data in response object while trying to read order book");
        }
        JsonObject data = response.get("data").getAsJsonObject();
        this.symbol = symbol;

        JsonArray dataSells = data.get("SELL").getAsJsonArray();
        for( JsonElement eachSell: dataSells) {
            //log.debug("{} eachSell: {}", symbol, eachSell);
            sells.add(new KucoinOrderBookLine(KucoinOrderSide.SELL, eachSell.getAsJsonArray()) );
        }
        JsonArray dataBuys = data.get("BUY").getAsJsonArray();
        for( JsonElement eachBuy: dataBuys) {
            //log.debug("{} eachBuy: {}", symbol, eachBuy);
            buys.add(new KucoinOrderBookLine(KucoinOrderSide.BUY, eachBuy.getAsJsonArray()));
        }
    }

    public BigDecimal getBestSellPrice() {
        return sells.get(0).getPrice();
    }
    public BigDecimal getBestSellQty() {
        return sells.get(0).getQuantity();
    }
    public BigDecimal getBestBuyPrice() {
        return buys.get(0).getPrice();
    }
    public BigDecimal getBestBuyQty() {
        return buys.get(0).getQuantity();
    }

    public BigDecimal getPriceSpread() {
        BigDecimal bestSellPrice = getBestSellPrice();
        BigDecimal bestBuyPrice = getBestBuyPrice();
        if (bestBuyPrice == null || bestSellPrice == null) return null;
        return (bestSellPrice.subtract(bestBuyPrice))
                .multiply(BigDecimal.valueOf(100)).divide(bestBuyPrice, MathContext.DECIMAL32);
    }

    public KucoinOrderBookLine getBestSellOrder(BigDecimal minVolume) {
        int index = 0;
        do {
            KucoinOrderBookLine order = sells.get(index);
            if (order.getVolume().compareTo(minVolume) >= 0) return order;
            index ++;
        } while (index < sells.size());
        return null;
    }

    public KucoinOrderBookLine getBestBuyOrder(BigDecimal minVolume) {
        int index = 0;
        do {
            KucoinOrderBookLine order = buys.get(index);
            if (order.getVolume().compareTo(minVolume) >= 0) return order;
            index ++;
        } while (index < buys.size());
        return null;
    }

}
