package com.webcerebrium.kucoin.api;

import com.webcerebrium.kucoin.datatype.KucoinAccount;
import com.webcerebrium.kucoin.datatype.KucoinOrderBook;
import com.webcerebrium.kucoin.datatype.KucoinSymbol;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Slf4j
@Data
public class KucoinMarket {
    KucoinSymbol symbol;
    KucoinOrderBook orderBook;
    BigDecimal feeMaker = BigDecimal.valueOf(0.001);
    BigDecimal feeTaker = BigDecimal.valueOf(0.001);
    BigDecimal lastBuyPrice = null;
    BigDecimal lastSellPrice = null;

    public KucoinMarket(KucoinSymbol symbol) {
        this.symbol = symbol;
        this.orderBook = new KucoinOrderBook(symbol);
        // Temp fix
        if (this.symbol.get().contains("RPX")) noFees();

    }
    public KucoinMarket(KucoinSymbol symbol, KucoinOrderBook orderBook) throws KucoinApiException {
        this.symbol = symbol;
        this.orderBook = orderBook;
        // Temp fix
        if (this.symbol.get().contains("RPX")) noFees();

        if (orderBook == null) {
            throw new KucoinApiException("Market " + symbol + " has to order book");
        }
        if (orderBook.getBuys().size() == 0) {
            throw new KucoinApiException("Market " + symbol + " has to BUY orders");
        }
        if (orderBook.getSells().size() == 0) {
            throw new KucoinApiException("Market " + symbol + " has to SELL orders");
        }
    }

    public KucoinMarket noFees() {
        this.feeMaker = BigDecimal.ZERO;
        this.feeTaker = BigDecimal.ZERO;
        return this;
    }

    public KucoinMarket log() {
        log.info("{} market, can sell: QTY={} {} for PRICE={} {}",
                symbol,
                this.getOrderBook().getBestBuyQty(), symbol.getCoin(),
                this.getOrderBook().getBestBuyPrice(), symbol.getBaseCoin());
        log.info("{} market, can buy: QTY={} {} for PRICE={} {}",
                symbol,
                this.getOrderBook().getBestSellQty(), symbol.getCoin(),
                this.getOrderBook().getBestSellPrice(), symbol.getBaseCoin());
        return this;
    }

    // Sell for market price
    public void sell(KucoinAccount account, BigDecimal qty) {

        BigDecimal bestBuyPrice = this.getOrderBook().getBestBuyPrice();
        BigDecimal charge = qty.multiply(bestBuyPrice.multiply(BigDecimal.ONE.subtract(feeMaker))).round(MathContext.DECIMAL32);

        log.info("MARKET SELL {} qty={} price={} receiving={} {}", symbol.getCoin(), qty, bestBuyPrice, charge, symbol.getBaseCoin());
        // log.info("SYMBOL={} COIN={} BASE={}", symbol.get(), symbol.getCoin(), symbol.getBaseCoin());
        // decrease number of coins
        account.add(symbol.getCoin(), BigDecimal.valueOf(-1).multiply(qty));
        // add funds to the main market wallet
        account.add(symbol.getBaseCoin(), charge);
        // log.info("ACCOUNT AFTER SELL {}", account.walletsAsString());
        this.lastSellPrice = bestBuyPrice;
    }

    public void sellAll(KucoinAccount account) {
        sell(account, account.getBalanceOf(this.getSymbol().getCoin()));
    }

    // Buy for market price
    public void buy(KucoinAccount account, BigDecimal qty) {
        BigDecimal bestSellPrice = this.getOrderBook().getBestSellPrice();
        BigDecimal cost = qty.multiply(bestSellPrice.multiply(BigDecimal.ONE.add(feeTaker))).round(MathContext.DECIMAL32);
        log.info("MARKET BUY {} qty={} price={} giving={} {}", symbol.getCoin(), qty, bestSellPrice, cost, symbol.getBaseCoin());

        // deduct funds to the main market wallet
        account.add(symbol.getBaseCoin(), cost.multiply(BigDecimal.valueOf(-1)));
        // increase number of coins
        account.add(symbol.getCoin(), qty);

        // log.info("ACCOUNT AFTER BUY {}", account.walletsAsString());
        this.lastBuyPrice = bestSellPrice;
    }

    public void buyAll(KucoinAccount account) {
        BigDecimal bestSellPrice = this.getOrderBook().getBestSellPrice();
        BigDecimal maxPurchaseAmount = account.getBalanceOf(this.getSymbol().getBaseCoin())
                .divide(bestSellPrice, RoundingMode.FLOOR)
                .multiply(BigDecimal.ONE.subtract(feeTaker));
        buy(account, maxPurchaseAmount);
    }

    public BigDecimal getMaxQtyToSell(KucoinAccount account) {
        String coin = symbol.getCoin();
        BigDecimal bestBuyQty = this.getOrderBook().getBestBuyQty();
        if (bestBuyQty.compareTo(account.getBalanceOf(coin)) > 0) {
            return account.getBalanceOf(coin);
        }
        return bestBuyQty;
    }

}
