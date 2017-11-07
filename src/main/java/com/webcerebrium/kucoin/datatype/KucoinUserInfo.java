package com.webcerebrium.kucoin.datatype;

import com.google.gson.JsonObject;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class KucoinUserInfo {

    String referrerCode = "";
    boolean photoCredentialValidated;
    boolean videoValidated;
    String language = "";
    String csrf = "";
    String oid = "";
    BigDecimal baseFeeRate = BigDecimal.ONE;
    boolean hasCredential;
    boolean phoneValidated;
    String phone = "";
    boolean credentialValidated;
    boolean googleTwoFaBinding;
    String nickname = "";
    String name = "";
    boolean hasTradePassword;
    String currency = "";
    boolean emailValidated;
    String email ="";

    public KucoinUserInfo() {
    }


    private boolean safeBoolean(JsonObject data, String field) {
        if (data.has(field) && data.get(field).isJsonPrimitive()) {
            return data.get(field).getAsBoolean();
        }
        return false;
    }
    private String safeString(JsonObject data, String field) {
        if (data.has(field) && data.get(field).isJsonPrimitive()) {
            return data.get(field).getAsString();
        }
        return "";
    }

    public KucoinUserInfo(JsonObject data) {
        referrerCode = safeString(data, "referrer_code");
        photoCredentialValidated = safeBoolean(data, "photoCredentialValidated");
        videoValidated = safeBoolean(data, "videoValidated");
        language = safeString(data, "language");
        csrf = safeString(data, "csrf");
        oid = safeString(data, "oid");

        hasCredential = safeBoolean(data, "hasCredential");
        phoneValidated = safeBoolean(data, "phoneValidated");
        phone = safeString(data, "phone");
        credentialValidated = safeBoolean(data, "credentialValidated");
        googleTwoFaBinding = safeBoolean(data, "googleTwoFaBinding");

        nickname = safeString(data, "nickname");
        name = safeString(data, "name");
        hasTradePassword = safeBoolean(data, "hasTradePassword");
        currency = safeString(data, "currency");
        emailValidated = safeBoolean(data, "emailValidated");
        email = safeString(data, "email");

        baseFeeRate = data.get("baseFeeRate").getAsBigDecimal();
    }

}
