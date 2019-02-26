package run.brief.settings.OAuth;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import run.brief.util.json.JSONException;
import run.brief.util.json.JSONObject;
import run.brief.util.log.BLog;

/**
 * Created by coops on 08/02/15.
 */
public class GetGoogleAccessToken {

    public static String CLIENT_SECRET =null;//"EEOeMFHQpLtaHDU4Rr8k-l3N";
    //Use your own client secret
    public static final String REDIRECT_URI="http://localhost";
    public static final String GRANT_TYPE_AUTH="authorization_code";
    public static final String GRANT_TYPE_REFRESH="refresh_token";

    public static final String TOKEN_URL ="https://accounts.google.com/o/oauth2/token";
    public static final String OAUTH_URL ="https://accounts.google.com/o/oauth2/auth";
    public static final String OAUTH_SCOPE="https://mail.google.com/ https://www.googleapis.com/auth/userinfo.email";

    InputStream is = null;
    JSONObject jObj = null;
    String json = "";
    public GetGoogleAccessToken() {
    }
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    Map<String, String> mapn;
    DefaultHttpClient httpClient;
    HttpPost httpPost;
    public JSONObject gettoken(String address,String token,String client_id,String client_secret,String redirect_uri,String grant_type) {
        // Making HTTP request
        try {
            // DefaultHttpClient
            httpClient = new DefaultHttpClient();
            httpPost = new HttpPost(address);
            if(grant_type.equals(GRANT_TYPE_AUTH)) {
                params.add(new BasicNameValuePair("code", token));
                params.add(new BasicNameValuePair("redirect_uri", redirect_uri));
            } else {
                params.add(new BasicNameValuePair("refresh_token", token));
            }
            params.add(new BasicNameValuePair("client_id", client_id));
            if(client_secret!=null)
                params.add(new BasicNameValuePair("client_secret", client_secret));

            params.add(new BasicNameValuePair("grant_type", grant_type));
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
        } catch (UnsupportedEncodingException e) {
            BLog.e("EXCEPT", "1-" + e.getMessage());e.printStackTrace();
        } catch (ClientProtocolException e) {
            BLog.e("EXCEPT", "2-" + e.getMessage());
        } catch (IOException e) {
            BLog.e("EXCEPT", "3-" + e.getMessage());
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            //BLog.e("JSONStr", json);
        } catch (Exception e) {

            BLog.e("Buffer Error", "Error converting result " + e.getMessage());
        }
        // Parse the String to a JSON Object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            //BLog.e("JSON Parser", "Error parsing data " + e.toString());
        }
        // Return JSON String
        return jObj;
    }
}