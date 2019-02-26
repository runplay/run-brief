package run.brief.settings.OAuth;

import java.security.Provider;

/**
 * Created by coops on 07/01/15.
 */
public final class OAuth2Provider extends Provider {
    private static final long serialVersionUID = 1L;

    public OAuth2Provider() {
        super("Google OAuth2 Provider", 1.0,
                "Provides the XOAUTH2 SASL Mechanism");
        put("SaslClientFactory.XOAUTH2",
                "run.brief.settings.OAuth.OAuth2SaslClientFactory");
        put("SaslClientFactory.XOAUTH",
                "run.brief.settings.OAuth.OAuth2SaslClientFactory");

    }
}