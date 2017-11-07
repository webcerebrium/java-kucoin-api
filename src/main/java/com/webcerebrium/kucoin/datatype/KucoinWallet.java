package com.webcerebrium.kucoin.datatype;

import com.google.gson.JsonObject;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class KucoinWallet {
    String coinType;
    BigDecimal balance = BigDecimal.ZERO;
    BigDecimal freezeBalance = BigDecimal.ZERO;

    public KucoinWallet(String coin) {
        this.coinType = coin;
        this.balance = BigDecimal.ZERO;
        this.freezeBalance = BigDecimal.ZERO;
    }

    public KucoinWallet(String coin, BigDecimal amount) {
        this.coinType = coin;
        this.balance = amount;
        this.freezeBalance = BigDecimal.ZERO;
    }

    public KucoinWallet(JsonObject obj) {
        this.coinType = obj.get("coinType").getAsString();
        this.balance = obj.get("balance").getAsBigDecimal();
        this.freezeBalance = obj.get("freezeBalance").getAsBigDecimal();
    }

    public boolean isEmpty() {
        return BigDecimal.valueOf(1e-4).compareTo(this.balance.add(this.freezeBalance)) > 0;
    }
}
