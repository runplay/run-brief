/* Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package run.brief.settings.OAuth;

import java.util.Map;
import java.util.logging.Logger;

import myjavax.security.auth.callback.CallbackHandler;
import myjavax.security.sasl.SaslClient;
import myjavax.security.sasl.SaslClientFactory;

/**
 * A SaslClientFactory that returns instances of OAuth2SaslClient.
 *
 * <p>Only the "XOAUTH2" mechanism is supported. The {@code callbackHandler} is
 * passed to the OAuth2SaslClient. Other parameters are ignored.
 */
public class OAuth2SaslClientFactory implements SaslClientFactory {
  private static final Logger logger =
      Logger.getLogger(OAuth2SaslClientFactory.class.getName());

  public static final String OAUTH_TOKEN_PROP =
      "mail.imaps.sasl.mechanisms.oauth2.oauthToken";
    public static final String CONSUMER_KEY_PROP =
            "mail.imaps.sasl.mechanisms.xoauth.consumerKey";

    private static String token;
    public static void setToken(String tokena) {
        //BLog.e("FIRST_in","tok: "+tokena);
        token=tokena;
    }
  public SaslClient createSaslClient(String[] mechanisms,
                                     String authorizationId,
                                     String protocol,
                                     String serverName,
                                     Map<String, ?> props,
                                     CallbackHandler callbackHandler) {

    boolean matchedMechanism = false;

      //BLog.e("IN","OAuth2SaslClientFactory.createSaslClient");

    for (int i = 0; i < mechanisms.length; ++i) {
      if ("XOAUTH2".equalsIgnoreCase(mechanisms[i])) {
        matchedMechanism = true;
        break;
      }
    }
    if (!matchedMechanism) {
      logger.info("Failed to match any mechanisms");
      return null;
    }
      if(token==null)
          return new OAuth2SaslClient((String) props.get(OAUTH_TOKEN_PROP),  callbackHandler);
      else
          return new OAuth2SaslClient(token,  callbackHandler);
  }

  public String[] getMechanismNames(Map<String, ?> props) {
      //BLog.e("IN","OAuth2SaslClientFactory.getMechNames");
    return new String[] {"XOAUTH2"};
  }
}
