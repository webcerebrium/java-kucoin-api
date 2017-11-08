package com.webcerebrium.kucoin.datatype;

import com.google.common.base.Strings;
import com.webcerebrium.kucoin.api.KucoinApiException;

public class KucoinSymbol {

    String symbol = "";

    public KucoinSymbol(String symbol)  throws KucoinApiException {
        // sanitizing symbol, preventing from common user-input errors
        if (Strings.isNullOrEmpty(symbol)) {
            throw new KucoinApiException("Symbol cannot be empty. Example: RPX-BTC");
        }
        if (symbol.contains(" ")) {
            throw new KucoinApiException("Symbol cannot contain spaces. Example: RPX-BTC");
        }
        if (!symbol.endsWith("-BTC") &&
            !symbol.endsWith("-NEO") &&
            !symbol.endsWith("-ETH") &&
            !symbol.endsWith("-USDT")) {
            throw new KucoinApiException("Market Symbol should be ending with -BTC, -ETH, -NEO or -USDT. Example: RPX-BTC");
        }
        this.symbol = symbol.toUpperCase();
    }

    public String get(){ return this.symbol; }

    public String getSymbol(){ return this.symbol; }

    public String getCoin(){
        // in case of NEO-to-ETH and NEO-to-BTC we are selling NEO
        if (symbol.startsWith("NEO-") && (symbol.endsWith("-BTC") || symbol.endsWith("-ETH"))) return "NEO";

        if (symbol.equals("ETH-NEO")) return "NEO";
        if (symbol.equals("ETH-BTC")) return "ETH";

        return this.symbol.replace("-BTC", "").replace("-ETH", "").replace("-NEO", "").replace("-USDT", "");
    }
    public String getBaseCoin(){
        // in case of NEO-to-ETH and NEO-to-BTC we are selling NEO
        if (symbol.startsWith("NEO-") && (symbol.endsWith("-BTC") || symbol.endsWith("-ETH"))) return symbol.substring(4);
        if (symbol.equals("ETH-NEO")) return "ETH";
        if (symbol.equals("ETH-BTC")) return "BTC";

        if (symbol.endsWith("-BTC")) return "BTC";
        if (symbol.endsWith("-ETH")) return "ETH";
        if (symbol.endsWith("-NEO")) return "NEO";
        if (symbol.endsWith("-USDT")) return "USDT";
        return "";
    }

    public String getOpposite(String coin) {
        if (symbol.startsWith(coin + "-")) {
            return symbol.substring((coin + "-").length());
        }
        if (symbol.endsWith("-" + coin)) {
            int index = symbol.length() - ("-" + coin).length();
            return symbol.substring(0, index);
        }
        return "";
    }

    public String toString() { return this.get(); }

    public static KucoinSymbol valueOf(String s) throws KucoinApiException {
        return new KucoinSymbol(s);
    }

    public static KucoinSymbol BTC(String pair) throws KucoinApiException {
        return KucoinSymbol.valueOf(pair.toUpperCase() + "-BTC");
    }

    public static KucoinSymbol ETH(String pair) throws KucoinApiException {
        return KucoinSymbol.valueOf(pair.toUpperCase() + "-ETH");
    }

    public static KucoinSymbol NEO(String pair) throws KucoinApiException {
        if (pair.toUpperCase().equals("BTC") || pair.toUpperCase().equals("ETH")) {
            return KucoinSymbol.valueOf("NEO-" + pair.toUpperCase());
        }
        return KucoinSymbol.valueOf(pair.toUpperCase() + "-NEO");
    }

    public static KucoinSymbol NEO_BTC() throws KucoinApiException {
        return KucoinSymbol.valueOf("NEO-BTC");
    }
    public static KucoinSymbol NEO_ETH() throws KucoinApiException {
        return KucoinSymbol.valueOf("NEO-ETH");
    }
    public static KucoinSymbol ETH_BTC() throws KucoinApiException {
        return KucoinSymbol.valueOf("ETH-BTC");
    }

    public static KucoinSymbol USDT(String pair) throws KucoinApiException {
        return KucoinSymbol.valueOf(pair.toUpperCase() + "-USDT");
    }

}
