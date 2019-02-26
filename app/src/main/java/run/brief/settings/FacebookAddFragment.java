package run.brief.settings;

import android.app.Fragment;

public class FacebookAddFragment extends Fragment {
	/*
	private Handler facebookHandler = new Handler();
	
	View view;
	ViewGroup container;
	LayoutInflater inflater;
	WebView webView =null;
	Activity activity=null;
	String callbackURL = null;
	RequestToken mRequestToken=null;
	
	private AccessToken at;
	
	private String OAUTH_CONSUMER_KEY="WKKKjJJMxRYupMZsvrJblA";
	private String OAUTH_CONSUMER_SECRET="vkpyCvVHRsciMLnzCrw9O65PUj6QSAZSO6zJ3WfE";
	//String callbackURL="http://www.runplay.com";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.container=container;
		this.inflater=inflater;
		activity=getActivity();
		callbackURL=getResources().getString(R.string.facebook_callback);
		ActionBarManager.setActionBarMenu(getActivity(), R.menu.facebook);
		// ensure Facebook accounts Db is initialised
		FacebookAccountsDb.init();
		
		
		view=inflater.inflate(R.layout.facebook_auth,container, false);
		webView = (WebView)activity.findViewById(R.id.webviewfacebookauth);
		goFacebookSetup();
		
		return view;

	}

	private Runnable startFacebook = new Runnable() {
		public void run() {
			showFacebook();
			//new postFacebookMessage().execute("go");
		}
	};
	private Runnable startFacebookSetup = new Runnable() {
		public void run() {
			new authenticateFacebookAccount().execute(true);
			
		}
	};
	private void goFacebookSetup() {
	    //activity.setContentView(R.layout.facebook_auth);
	    
	    WebSettings settings = webView.getSettings();
	    settings.setPluginState(PluginState.ON);
	    webView.setWebViewClient( new WebViewClient()
	    {
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url)
	        {
	        	Log.e("TWIT_URL",url);
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
	    
	    facebookHandler.postDelayed(startFacebookSetup, 10);
		
		
	}
	private void showFacebook() {
		Activity activity = getActivity();
		ListView list=(ListView) activity.findViewById(R.id.facebookList);
	}

	private void setResult(int resultCode,RequestToken mRequestToken,String oauthVerifier) {
		if (resultCode == Activity.RESULT_OK)	{
			new finaliseFacebookAccount().execute(oauthVerifier);
			view=inflater.inflate(R.layout.facebook,container, false);
		}
	}
	private boolean postMessage(FacebookAccount fromAccount) {
		boolean posted=false;
		
		if(fromAccount!=null) {
			String accessToken = fromAccount.getAccessToken();
			String accessTokenSecret = fromAccount.getAccessTokenSecret();
			Configuration conf = new ConfigurationBuilder()
			    .setOAuthConsumerKey( OAUTH_CONSUMER_KEY )
			    .setOAuthConsumerSecret( OAUTH_CONSUMER_SECRET )
			    .setOAuthAccessToken(accessToken)
			    .setOAuthAccessTokenSecret(accessTokenSecret)
			    .build();
			Facebook t = new FacebookFactory(conf).getInstance();
			try {
				t.updateStatus( "@BriefApp You guys rock!" );
				posted=true;
			} catch(FacebookException e) {
				Log.e("POST-TWITTER",e.getMessage());
			}
		}
		
		return posted;
	}

	private class authenticateFacebookAccount extends AsyncTask<Boolean, Void, Boolean> {
		
		//Facebook mFacebook = new FacebookFactory().getInstance();
		@Override
		protected Boolean doInBackground(Boolean... params) {
			try
			{
				Facebook mFacebook = new FacebookFactory().getInstance();
				mFacebook.setOAuthConsumer( OAUTH_CONSUMER_KEY, OAUTH_CONSUMER_SECRET );
			    mRequestToken = mFacebook.getOAuthRequestToken(callbackURL);
			}
			catch (FacebookException e)
			{
			     Log.e("TWITTER",e.getMessage());
			}
			if(mRequestToken!=null)
				return true;
			else
				return false;
	
		}      
	
		@Override
		protected void onPostExecute(Boolean result) {

			Log.e("TWITTER","onPostExecute");
		    webView.loadUrl(mRequestToken.getAuthenticationURL());
		}
	
		@Override
		protected void onPreExecute() {
		}
	
		@Override
		protected void onProgressUpdate(Void... values) {
			
		}
	 
	}
	private class finaliseFacebookAccount extends AsyncTask<String, Void, String> {
		
		//Facebook mFacebook = new FacebookFactory().getInstance();
		@Override
		protected String doInBackground(String... params) {
		    AccessToken at = null;
		    String oauthVerifier=params[0];
		    try
		    {
		        // Pair up our request with the response
		    	ConfigurationBuilder builder = new ConfigurationBuilder();
		    	builder.setOAuthConsumerKey(OAUTH_CONSUMER_KEY);
		    	builder.setOAuthConsumerSecret(OAUTH_CONSUMER_SECRET);
		    	Configuration configuration = builder.build();
		    	FacebookFactory factory = new FacebookFactory(configuration);
		    	Facebook mFacebook = new FacebookFactory().getInstance();
		    	//Facebook mFacebook = new FacebookFactory().getInstance();
				//mFacebook.setOAuthConsumer( OAUTH_CONSUMER_KEY, OAUTH_CONSUMER_SECRET );
		        at = mFacebook.getOAuthAccessToken(mRequestToken, oauthVerifier);
		        FacebookAccount account = new FacebookAccount();
		        account.setAccessToken(at.getToken());
		        account.setAccessTokenSecret(at.getTokenSecret());
		        FacebookAccountsDb.addAccount(account);
		        
		    }
		    catch (FacebookException e)
		    {
		    	Log.e("TWITTER",e.getMessage());
		    }
		    return oauthVerifier;
	
		}      
	
		@Override
		protected void onPostExecute(String result) {

			Log.e("TWITTER","onPostExecute");
			facebookHandler.postDelayed(startFacebook, 10);
			view=inflater.inflate(R.layout.facebook,container, false);
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
