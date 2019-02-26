package run.brief.settings;

public class TwitterAddFragment {
    /*


    import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

    extends
} BOAUTHFragment {
	
	//private Handler twitterHandler = new Handler();
	
	View view;
	ViewGroup container;
	LayoutInflater inflater;
	WebView webView =null;
	Activity activity=null;
	String callbackURL = null;
	RequestToken mRequestToken=null;
	
	private AccessToken at;
	
	//private String OAUTH_CONSUMER_KEY="WKKKjJJMxRYupMZsvrJblA";
	//private String OAUTH_CONSUMER_SECRET="vkpyCvVHRsciMLnzCrw9O65PUj6QSAZSO6zJ3WfE";
	//String callbackURL="http://www.runplay.com";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.container=container;
		this.inflater=inflater;
		activity=getActivity();
		callbackURL=getResources().getString(R.string.twitter_callback);
		
		ActionBarManager.setActionBarBackOnly(activity,activity.getString(R.string.accounts_add_twitter), R.menu.twitter);
		// ensure Twitter accounts Db is initialised
		
		//TwitterAccountsDb.init();
		
		view=inflater.inflate(R.layout.accounts_add_twitter,container, false);
		
		webView = (WebView)view.findViewById(R.id.webviewtwitterauth);
		
		
		return view;
		
	}
	@Override
	public void onResume() {
		super.onResume();

		goTwitterSetup();
	}

	private void goTwitterSetup() {
	    //activity.setContentView(R.layout.twitter_auth);
	    
	    WebSettings settings = webView.getSettings();
	    settings.setPluginState(PluginState.ON);
	    webView.setWebViewClient( new WebViewClient()
	    {
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url)
	        {
	        	//Log.e("TWIT_URL",url);
	            if( url.contains( callbackURL ) )
	            {
	                Uri uri = Uri.parse( url );
	                String oauthVerifier = uri.getQueryParameter( "oauth_verifier" );
	                //mIntent.putExtra( "oauth_verifier", oauthVerifier );
	                setResult( Activity.RESULT_OK,mRequestToken,oauthVerifier);
	                
	                //finish();
	                return true;
	            }
	            return false;
	        }

	    });
	    
	    new authenticateTwitterAccount().execute(true);
		
		
	}

	private void setResult(int resultCode,RequestToken mRequestToken,String oauthVerifier) {
		if (resultCode == Activity.RESULT_OK)	{
			new finaliseTwitterAccount().execute(oauthVerifier);
			view=inflater.inflate(R.layout.twitter,container, false);
		}
	}
	private boolean postMessage(Account fromAccount) {
		boolean posted=false;
		
		if(fromAccount!=null) {
			String accessToken = fromAccount.getString(Account.STRING_ACCESS_TOKEN);
			String accessTokenSecret = fromAccount.getString(Account.STRING_ACCESS_TOKEN_SECRET);
			Configuration conf = new ConfigurationBuilder()
			    .setOAuthConsumerKey( getKeys().TwitterOauthConsumerKey() )
			    .setOAuthConsumerSecret( getKeys().TwitterOauthConsumerSecret() )
			    .setOAuthAccessToken(accessToken)
			    .setOAuthAccessTokenSecret(accessTokenSecret)
			    .build();
			Twitter t = new TwitterFactory(conf).getInstance();
			try {
				t.updateStatus( "@BriefApp You guys rock!" );
				posted=true;
			} catch(TwitterException e) {
				BLog.add("TWITTER",e);
			}
		}
		
		return posted;
	}

	private class authenticateTwitterAccount extends AsyncTask<Boolean, Void, Boolean> {
		
		//Twitter mTwitter = new TwitterFactory().getInstance();
		@Override
		protected Boolean doInBackground(Boolean... params) {
			try
			{
				Configuration conf = new ConfigurationBuilder()
			    .setOAuthConsumerKey( getKeys().TwitterOauthConsumerKey() )
			    .setOAuthConsumerSecret( getKeys().TwitterOauthConsumerSecret() )
			    .build();
				Twitter mTwitter = new TwitterFactory(conf).getInstance();
				BLog.e("TWIT",mTwitter.getAuthorization().toString()+"---");
				//mTwitter.setOAuthConsumer( getKeys().TwitterOauthConsumerKey(), getKeys().TwitterOauthConsumerSecret() );
				
				mRequestToken = mTwitter.getOAuthRequestToken(callbackURL);
			}
			catch (TwitterException e)
			{
			     //BLog.add("TWITTER",e);
			}
			if(mRequestToken!=null)
				return true;
			else
				return false;
	
		}      
	
		@Override
		protected void onPostExecute(Boolean result) {
			if(result)
				webView.loadUrl(mRequestToken.getAuthenticationURL());
		}
	
		@Override
		protected void onPreExecute() {
		}
	
		@Override
		protected void onProgressUpdate(Void... values) {
			
		}
	 
	}
	private class finaliseTwitterAccount extends AsyncTask<String, Void, String> {
		
		//Twitter mTwitter = new TwitterFactory().getInstance();
		@Override
		protected String doInBackground(String... params) {
		    AccessToken at = null;
		    String oauthVerifier=params[0];
		    try
		    {
		        // Pair up our request with the response
		    	ConfigurationBuilder builder = new ConfigurationBuilder();
		    	builder.setOAuthConsumerKey(getKeys().TwitterOauthConsumerKey());
		    	builder.setOAuthConsumerSecret(getKeys().TwitterOauthConsumerSecret());
		    	Configuration configuration = builder.build();
		    	TwitterFactory factory = new TwitterFactory(configuration);
		    	Twitter mTwitter = factory.getInstance();

		        at = mTwitter.getOAuthAccessToken(mRequestToken, oauthVerifier);
		        Account account = new Account();
		        account.setString(Account.STRING_ACCESS_TOKEN, at.getToken());
		        account.setString(Account.STRING_ACCESS_TOKEN_SECRET, at.getTokenSecret());
		        
		        AccountsDb.addAccount(account);
		        //TwitterAccountsDb.addAccount(account);
		        
		    }
		    catch (TwitterException e)
		    {
		    	BLog.add("TWITTER",e);
		    }
		    return oauthVerifier;
	
		}      
	
		@Override
		protected void onPostExecute(String result) {
			Bgo.openFragment(activity, new AccountsHomeFragment());

		}
	
		@Override
		protected void onPreExecute() {
		}
	
		@Override
		protected void onProgressUpdate(Void... values) {
			
		}
	 
	}
*/
}
