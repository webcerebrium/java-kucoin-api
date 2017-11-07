package com.webcerebrium.kucoin.datatype;


import com.google.gson.JsonArray;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class KucoinOrderBookLine {
    KucoinOrderSide side;
    BigDecimal price;
    BigDecimal quantity;
    BigDecimal volume;

    public KucoinOrderBookLine(KucoinOrderSide side, JsonArray arr) {
        this.side = side;
        this.price = arr.get(0).getAsBigDecimal();
        this.quantity = arr.get(1).getAsBigDecimal();
        this.volume = arr.get(2).getAsBigDecimal();
    }
}
