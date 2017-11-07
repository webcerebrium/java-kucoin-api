package com.webcerebrium.kucoin.api;

/* ============================================================
 * java-kucoin-api
 * https://github.com/webcerebrium/java-binance-api
 * ============================================================
 * Copyright 2017-, Viktor Lopata, Web Cerebrium OÜ
 * Released under the MIT License
 * ============================================================ */

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
public class KucoinRequest {

    public String userAgent = "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0";
    public HttpsURLConnection conn = null;
    public String requestUrl = "";
    public URL url = null;
    public String method = "GET";
    public String lastResponse = "";

    public String apiKey = "";
    public String secretKey = "";

    public Map<String, String> headers = new HashMap<String, String>();

    // Internal JSON parser
    private JsonParser jsonParser = new JsonParser();
    private String requestBody = "";

    // Creating public request
    public KucoinRequest(String requestUrl)  throws KucoinApiException {

        this.requestUrl = requestUrl;
        try {
            url = new URL(requestUrl);
        } catch (MalformedURLException e) {
            throw new KucoinApiException("Mailformed URL " + e.getMessage());
        }

    }

    // HMAC encoding
    public static String hmacEncode(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
    }

    /**
     * Settings method as post, keeping interface fluid
     * @return this request object
     */
    public KucoinRequest post() {
        this.setMethod("POST");
        return this;
    }

    /**
     * Settings method as PUT, keeping interface fluid
     * @return this request object
     */
    public KucoinRequest put() {
        this.setMethod("PUT");
        return this;
    }


    /**
     * Settings method as DELETE, keeping interface fluid
     * @return this request object
     */
    public KucoinRequest delete() {
        this.setMethod("DELETE");
        return this;
    }

    /**
     * Opens HTTPS connection and save connection Handler
     * @return this request object
     * @throws KucoinApiException in case of any error
     */
    public KucoinRequest connect() throws KucoinApiException {

        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };

        log.info("{} {}", this.getMethod(), this.getRequestUrl());
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            throw new KucoinApiException("SSL Error " + e.getMessage() );
        } catch (KeyManagementException e) {
            throw new KucoinApiException("Key Management Error " + e.getMessage() );
        }

        try {
            conn = (HttpsURLConnection)url.openConnection();
        } catch (IOException e) {
            throw new KucoinApiException("HTTPS Connection error " + e.getMessage());
        }

        try {
            conn.setRequestMethod(method);
        } catch (ProtocolException e) {
            throw new KucoinApiException("HTTP method error " + e.getMessage());
        }
        conn.setRequestProperty("User-Agent", getUserAgent());
        for(String header: headers.keySet()) {
            conn.setRequestProperty(header, headers.get(header));
        }
        return this;
    }

    /**
     * Saving response into local string variable
     * @return this request object
     * @throws KucoinApiException in case of any error
     */
    public KucoinRequest read() throws KucoinApiException {
        if (conn == null) {
            connect();
        }
        try {

            // posting payload it we do not have it yet
            if (!Strings.isNullOrEmpty(getRequestBody())) {
                log.debug("Payload: {}", getRequestBody());
                conn.setDoInput(true);
                conn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                writer.write(getRequestBody());
                writer.close();
            }

            InputStream is;
            if (conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                is = conn.getInputStream();
            } else {
                /* error from server */
                is = conn.getErrorStream();
            }

            BufferedReader br = new BufferedReader( new InputStreamReader(is));
            lastResponse = IOUtils.toString(br);
            log.debug("Response: {}", lastResponse);

            if (conn.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                // Try to parse JSON
                JsonObject obj = (JsonObject)jsonParser.parse(lastResponse);
                if (obj.has("code") && obj.has("msg")) {
                    throw new KucoinApiException("ERROR: " +
                            obj.get("code").getAsString() + ", " + obj.get("msg").getAsString() );
                }
            }
        } catch (IOException e) {
            throw new KucoinApiException("Error in reading response " + e.getMessage());
        }
        return this;
    }

    public KucoinRequest payload(JsonObject payload) {
        if (payload == null) return this; // this is a valid case
        // according to documentation we need to have this header if we have preload
        headers.put("Content-Type", "application/json");
        this.requestBody = payload.toString();
        return this;
    }

    /**
     * Getting last response as google JsonObject
     * @return response as Json Object
     */
    public JsonObject asJsonObject() {
        return (JsonObject)jsonParser.parse(getLastResponse());
    }
    /**
     * Getting last response as google GAON JsonArray
     * @return response as Json Array
     */
    public JsonArray asJsonArray() {
        return (JsonArray)jsonParser.parse(getLastResponse());
    }

    public KucoinRequest sign(String apiKey) throws KucoinApiException {
        String humanMessage = "Please check environment variables or VM options";
        if (Strings.isNullOrEmpty(apiKey))
            throw new KucoinApiException("Missing KUCOIN_API_KEY. " + humanMessage);
        headers.put("KC-API-KEY", apiKey);
        return this;
    }

    public KucoinRequest sign(String apiKey, String secretKey) throws KucoinApiException {
        String humanMessage = "Please check environment variables or VM options";
        if (Strings.isNullOrEmpty(apiKey))
            throw new KucoinApiException("Missing KUCOIN_API_KEY. " + humanMessage);
        if (Strings.isNullOrEmpty(secretKey))
            throw new KucoinApiException("Missing KUCOIN_SECRET. " + humanMessage);

        log.info("PATH={}, QUERY={}", this.url.getPath(), this.url.getQuery());

        String endpoint = this.url.getPath();
        String signature = null;
        String queryString = this.url.getQuery() != null ?  this.url.getQuery() : "";
        String nonce = String.valueOf(new Date().getTime());

        try {
            String strForSign = endpoint + "/" + nonce +"/" + queryString;
            String signatureStr = Base64.getEncoder().encodeToString(strForSign.getBytes("UTF-8"));
            signature = hmacEncode(secretKey, signatureStr);
            log.info("Nonce={}, Endpoint={} Signature={}", nonce, endpoint, signature);
            if (Strings.isNullOrEmpty(signature)) {
                throw new KucoinApiException("Signature failed");
            }
        } catch (UnsupportedEncodingException e) {
            throw new KucoinApiException("Cannot encode " + e.getMessage());
        } catch (Exception e) {
            throw new KucoinApiException("Cannot sign request " + e.getMessage());
        }

        headers.put("KC-API-KEY", apiKey);
        headers.put("KC-API-NONCE", nonce);
        headers.put("KC-API-SIGNATURE", signature);
        return this;
    }

}
