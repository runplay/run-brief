package run.brief.settings.OAuth;

/**
 * Created by coops on 07/01/15.
 */
public class XOAuth {

    public static void main(String[] args) throws Exception {
        java.io.BufferedReader input = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in));
        System.out.println("Enter an email:");
        String email = input.readLine();
        System.out.println("Enter a consumer key:");
        String consumerKey = input.readLine();
        System.out.println("Enter a consumer secret:");
        String consumerSecret = input.readLine();
        OAuthEntity requestToken = phase1("https://mail.google.com/", "oob",
                consumerKey, consumerSecret, email);
        System.out.println("To authorize token, "
                + "visit this url and follow the directions "
                + "to generate a verification code:");
        String verifier = input.readLine();
        OAuthEntity accessToken = phase2(consumerKey, consumerSecret, email,
                requestToken, verifier);
        System.out.println("accessToken.key=" + accessToken.key);
        System.out.println("accessToken.secret=" + accessToken.secret);
    }

    static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(XOAuth.class.getName());
    static boolean LOGGABLE = logger.isLoggable(java.util.logging.Level.INFO);
    static final String ENC = "utf-8";
    static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";


    public static OAuthEntity phase1(String scope, String callbackUrl,
                                     String consumerKey, String consumerSecret, String email)
            throws java.security.InvalidKeyException,
            java.security.NoSuchAlgorithmException, java.io.IOException {
        OAuthEntity consumer = new OAuthEntity(consumerKey, consumerSecret);
        GoogleAccountsUrlGenerator generator = new GoogleAccountsUrlGenerator(
                email);
        return generateRequestToken(consumer, scope, callbackUrl, -1, -1,
                generator);
    }


    public static OAuthEntity phase2(String consumerKey, String consumerSecret,
                                     String email, OAuthEntity requestToken, String oauthVerifier)
            throws java.security.InvalidKeyException,
            java.security.NoSuchAlgorithmException, java.io.IOException {
        OAuthEntity consumer = new OAuthEntity(consumerKey, consumerSecret);
        GoogleAccountsUrlGenerator generator = new GoogleAccountsUrlGenerator(
                email);
        return getAccessToken(consumer, requestToken, oauthVerifier, generator);
    }

    static String formatUrlParams(java.util.Map<String, String> params)
            throws java.io.UnsupportedEncodingException {
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (String key : sorted(params)) {
            if (first == false) {
                b.append('&');
            } else {
                first = false;
            }
            b.append(key).append('=').append(
                    java.net.URLEncoder.encode(params.get(key), ENC));
        }
        return b.toString();
    }

    static String escapeAndJoin(Iterable<String> params)
            throws java.io.UnsupportedEncodingException {
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (String s : params) {
            if (first == false) {
                b.append('&');
            } else {
                first = false;
            }
            b.append(java.net.URLEncoder.encode(s, ENC));
        }
        return b.toString();
    }

    static String generateSignatureBaseString(String method,
                                              String requestUrlBase, java.util.Map<String, String> params)
            throws java.io.UnsupportedEncodingException {
        return escapeAndJoin(java.util.Arrays.asList(method, requestUrlBase,
                formatUrlParams(params)));
    }

    static String generateHmacSha1Signature(String data, String key)
            throws java.security.NoSuchAlgorithmException,
            java.security.InvalidKeyException {
        javax.crypto.spec.SecretKeySpec signingKey = new javax.crypto.spec.SecretKeySpec(
                key.getBytes(), HMAC_SHA1_ALGORITHM);
        javax.crypto.Mac mac = javax.crypto.Mac
                .getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        return byteArrayToBase64(mac.doFinal(data.getBytes()));
    }

    static String generateOauthSignature(String baseString,
                                         String consumerSecret, String tokenSecret)
            throws java.security.InvalidKeyException,
            java.security.NoSuchAlgorithmException,
            java.io.UnsupportedEncodingException {
        return generateHmacSha1Signature(baseString,
                escapeAndJoin(java.util.Arrays.asList(consumerSecret,
                        tokenSecret)));
    }

    static java.util.Map<String, String> parseUrlParamString(String paramString)
            throws java.io.UnsupportedEncodingException {
        String[] pairs = paramString.split("&");
        java.util.Map<String, String> map = new java.util.HashMap<String, String>(
                pairs.length);
        for (String pair : pairs) {
            String[] split = pair.split("=");
            map.put(split[0], java.net.URLDecoder.decode(split[1], ENC));
        }
        return map;
    }

    static class OAuthEntity {
        String secret;
        String key;

        OAuthEntity(String key, String secret) {
            this.key = key;
            this.secret = secret;
        }
    }

    static class GoogleAccountsUrlGenerator {
        String appsDomain;

        GoogleAccountsUrlGenerator(String email) {
            int indexOf = email.indexOf('@');
            if (indexOf <= 0) {
                return;
            }
            String domain = email.substring(indexOf + 1).toLowerCase();
            if (domain.equals("gmail.com") == false
                    && domain.equals("googlemail.com") == false) {
                appsDomain = domain;
            }
        }

        String getRequestTokenUrl() {
            return "https://www.google.com/accounts/OAuthGetRequestToken";
        }

        String getAuthorizeTokenUrl() {
            if (appsDomain != null) {
                return String.format(
                        "https://www.google.com/a/%s/OAuthAuthorizeToken",
                        appsDomain);
            } else {
                return "https://www.google.com/accounts/OAuthAuthorizeToken";
            }
        }

        String getAccessTokenUrl() {
            return "https://www.google.com/accounts/OAuthGetAccessToken";
        }
    }

    static void fillInCommonOauthParams(java.util.Map<String, String> params,
                                        OAuthEntity consumer, long nonce, long timestamp) {
        params.put("oauth_consumer_key", consumer.key);
        if (nonce < 0) {
            nonce = (long) (Math.random() * Math.pow(2.0, 64.0));
        }
        params.put("oauth_nonce", String.valueOf(nonce));
        params.put("oauth_signature_method", "HMAC-SHA1");
        params.put("oauth_version", "1.0");
        if (timestamp < 0) {
            timestamp = System.currentTimeMillis() / 1000;
        }
        params.put("oauth_timestamp", String.valueOf(timestamp));
    }

    static OAuthEntity generateRequestToken(OAuthEntity consumer, String scope,
                                            String callbackUrl, long nonce, long timestamp,
                                            GoogleAccountsUrlGenerator urlGenerator)
            throws java.security.InvalidKeyException,
            java.security.NoSuchAlgorithmException, java.io.IOException {
        java.util.Map<String, String> params = new java.util.HashMap<String, String>();
        fillInCommonOauthParams(params, consumer, nonce, timestamp);
        params.put("oauth_callback", callbackUrl); // for installed application.
        params.put("scope", scope);
        String requestUrl = urlGenerator.getRequestTokenUrl();
        OAuthEntity token = new OAuthEntity(null, "");
        String baseString = generateSignatureBaseString("GET", requestUrl,
                params);
        String signature = generateOauthSignature(baseString, consumer.secret,
                token.secret);
        params.put("oauth_signature", signature);
        String url = requestUrl + "?" + formatUrlParams(params);
        String response = readFromUrl(url);
        java.util.Map<String, String> responseParams = parseUrlParamString(response);
        java.util.Iterator<java.util.Map.Entry<String, String>> i = responseParams
                .entrySet().iterator();
        while (i.hasNext()) {
            java.util.Map.Entry<String, String> next = i.next();
            if (LOGGABLE) {
                logger.info(next.getKey() + "=" + next.getValue());
            }
        }
        token = new OAuthEntity(responseParams.get("oauth_token"),
                responseParams.get("oauth_token_secret"));
        if (LOGGABLE) {
            logger.info("To authorize token, "
                    + "visit this url and follow the directions "
                    + "to generate a verification code:");
            logger.info(String.format("  %s?oauth_token=%s", urlGenerator
                    .getAuthorizeTokenUrl(), java.net.URLEncoder.encode(
                    responseParams.get("oauth_token"), ENC)));
        }
        return token;
    }

    static OAuthEntity getAccessToken(OAuthEntity consumer,
                                      OAuthEntity requestToken, String oauthVerifier,
                                      GoogleAccountsUrlGenerator urlGenerator)
            throws java.security.InvalidKeyException,
            java.security.NoSuchAlgorithmException, java.io.IOException {
        java.util.Map<String, String> params = new java.util.HashMap<String, String>();
        fillInCommonOauthParams(params, consumer, -1, -1);
        params.put("oauth_token", requestToken.key);
        params.put("oauth_verifier", oauthVerifier);
        String requestUrl = urlGenerator.getAccessTokenUrl();
        String baseString = generateSignatureBaseString("GET", requestUrl,
                params);
        String signature = generateOauthSignature(baseString, consumer.secret,
                requestToken.secret);
        params.put("oauth_signature", signature);
        String url = requestUrl + "?" + formatUrlParams(params);
        String response = readFromUrl(url);
        java.util.Map<String, String> responseParams = parseUrlParamString(response);
        java.util.Iterator<java.util.Map.Entry<String, String>> i = responseParams
                .entrySet().iterator();
        while (i.hasNext()) {
            java.util.Map.Entry<String, String> next = i.next();
            if (LOGGABLE) {
                logger.info(next.getKey() + "=" + next.getValue());
            }
        }
        OAuthEntity token = new OAuthEntity(responseParams.get("oauth_token"),
                responseParams.get("oauth_token_secret"));
        return token;
    }

    static String generateXOauthString(OAuthEntity consumer,
                                       OAuthEntity accessToken, String user, String proto,
                                       String xoauthRequestorId, long nonce, long timestamp)
            throws java.io.UnsupportedEncodingException,
            java.security.InvalidKeyException,
            java.security.NoSuchAlgorithmException {
        String method = "GET";
        java.util.Map<String, String> urlParams = new java.util.HashMap<String, String>();
        if (xoauthRequestorId != null) {
            urlParams.put("xoauth_requestor_id", xoauthRequestorId);
        }
        java.util.Map<String, String> oauthParams = new java.util.HashMap<String, String>();
        fillInCommonOauthParams(oauthParams, consumer, nonce, timestamp);
        if (accessToken.key != null) {
            oauthParams.put("oauth_token", accessToken.key);
        }
        java.util.Map<String, String> signedParams = new java.util.HashMap<String, String>();
        signedParams.putAll(oauthParams);
        signedParams.putAll(urlParams);
        String requestUrlBase = String.format(
                "https://mail.google.com/mail/b/%s/%s/", user, proto);
        String baseString = generateSignatureBaseString(method, requestUrlBase,
                signedParams);
        if (LOGGABLE) {
            logger.info("signature base string:\n" + baseString + "\n");
        }
        String signature = generateOauthSignature(baseString, consumer.secret,
                accessToken.secret);
        oauthParams.put("oauth_signature", signature);
        java.util.List<String> formattedParams = new java.util.ArrayList<String>();
        for (String key : sorted(oauthParams)) {
            formattedParams.add(key + "="
                    + java.net.URLEncoder.encode(oauthParams.get(key), ENC));
        }
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (String s : formattedParams) {
            if (first == false) {
                b.append(',');
            } else {
                first = false;
            }
            b.append(s);
        }
        String paramList = b.toString();
        String requestUrl;
        if (urlParams != null && urlParams.isEmpty() == false) {
            requestUrl = requestUrlBase + "?" + formatUrlParams(urlParams);
        } else {
            requestUrl = requestUrlBase;
        }
        String preencoded = method + " " + requestUrl + " " + paramList;
        if (LOGGABLE) {
            logger.info("xoauth string (before base64-encoding):\n"
                    + preencoded + "\n");
        }
        return preencoded;
    }

    static java.util.List<String> sorted(java.util.Map<String, String> map) {
        java.util.List<String> list = new java.util.ArrayList<String>(map
                .size());
        list.addAll(map.keySet());
        java.util.Collections.sort(list);
        return list;
    }

    static String readFromUrl(String urlString) throws java.io.IOException {
        java.net.URL url = new java.net.URL(urlString);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url
                .openConnection();
        connection.setRequestMethod("GET");
        if (LOGGABLE) {
            logger.info("read from: " + urlString);
            logger.info("response code: " + connection.getResponseCode());
            logger.info("content length: " + connection.getContentLength());
        }
        java.io.InputStream is = connection.getInputStream();
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] bs = new byte[1024];
        int size = 0;
        while ((size = is.read(bs)) != -1) {
            baos.write(bs, 0, size);
        }
        return new String(baos.toByteArray());
    }


    static String byteArrayToBase64(byte[] bytes) {
        int aLen = bytes.length;
        int numFullGroups = aLen / 3;
        int numBytesInPartialGroup = aLen - 3 * numFullGroups;
        int resultLen = 4 * ((aLen + 2) / 3);
        StringBuilder b = new StringBuilder(resultLen);

        int inCursor = 0;
        for (int i = 0; i < numFullGroups; i++) {
            int byte0 = bytes[inCursor++] & 0xff;
            int byte1 = bytes[inCursor++] & 0xff;
            int byte2 = bytes[inCursor++] & 0xff;
            b.append(intToBase64[byte0 >> 2]);
            b.append(intToBase64[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
            b.append(intToBase64[(byte1 << 2) & 0x3f | (byte2 >> 6)]);
            b.append(intToBase64[byte2 & 0x3f]);
        }

        if (numBytesInPartialGroup != 0) {
            int byte0 = bytes[inCursor++] & 0xff;
            b.append(intToBase64[byte0 >> 2]);
            if (numBytesInPartialGroup == 1) {
                b.append(intToBase64[(byte0 << 4) & 0x3f]);
                b.append("==");
            } else {
                int byte1 = bytes[inCursor++] & 0xff;
                b.append(intToBase64[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
                b.append(intToBase64[(byte1 << 2) & 0x3f]);
                b.append('=');
            }
        }
        return b.toString();
    }

    static final char intToBase64[] = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
            'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
            'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '+', '/' };
}