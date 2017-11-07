package com.webcerebrium.kucoin.datatype;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;


@Slf4j
public class KucoinAccount {

    @Getter
    @Setter
    List<KucoinWallet> wallets = null;
    boolean locked = true;

    public KucoinAccount() {
    }

    public KucoinAccount(List<KucoinWallet> wallets) {
        this.wallets = wallets;
    }

    public KucoinAccount released() {
        locked = false; // without release it will be false trade
        return this;
    }

    public static KucoinAccount mockOf(String coin) {
        return (new KucoinAccount()).reset().add(coin, BigDecimal.valueOf(100000));
    }
    public static KucoinAccount mockOf(String coin, BigDecimal balance) {
        return (new KucoinAccount()).reset().add(coin, balance);
    }

    public BigDecimal getBalanceOf(String coin) {
        if (wallets == null) return BigDecimal.ZERO;
        for (int i = 0; i < wallets.size(); i ++) {
            KucoinWallet wallet = wallets.get(i);
            if (wallet.getCoinType().equals(coin)) return wallet.getBalance();
        }
        return BigDecimal.ZERO;
    }

    public KucoinAccount reset() {
        if (wallets == null) wallets = new LinkedList<>();
        wallets.clear();
        return this;
    }

    public KucoinAccount add(String coin, BigDecimal amount) {
        if (wallets == null) wallets = new LinkedList<>();

        for (int i = 0; i < wallets.size(); i++) {
            KucoinWallet wallet = wallets.get(i);
            if (wallet != null && wallet.getCoinType().equals(coin)) {
                // log.debug("adding {} to {} balance, {} existing", amount, coin, wallet.getBalance());
                wallet.setBalance(wallet.getBalance().add(amount));
                return this;
            }
        }
        // log.debug("adding {} to {} balance", amount, coin);
        wallets.add(new KucoinWallet(coin, amount));
        return this;
    }

    public KucoinAccount set(String coin, BigDecimal amount) {
        if (wallets == null) wallets = new LinkedList<>();

        for (int i = 0; i < wallets.size(); i++) {
            KucoinWallet wallet = wallets.get(i);
            if (wallet.getCoinType().equals(coin)) {
                wallet.setBalance(amount);
                return this;
            }
        }
        wallets.add(new KucoinWallet(coin, amount));
        return this;
    }

    public String walletsAsString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < wallets.size(); i++) {
            KucoinWallet wallet = wallets.get(i);
            if (!wallet.isEmpty()) {
                sb.append(wallet.getCoinType()).append(":").append(wallet.getBalance()).append(" ");
            }
        }
        return sb.toString();
    }
}
