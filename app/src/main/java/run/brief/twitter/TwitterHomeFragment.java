package run.brief.twitter;

public class TwitterHomeFragment  {
	/*
	extends BFragment implements BRefreshable
	private Handler twitterHandler = new Handler();
	
	View view;
	ViewGroup container;
	LayoutInflater inflater;
	WebView webView;
	Activity activity;

	RequestToken mRequestToken;
	String callbackURL;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		State.setCurrentSection(State.SECTION_TWITTER);
		
		this.container=container;
		this.inflater=inflater;
		activity=getActivity();
		callbackURL=getResources().getString(R.string.twitter_callback);
		

		// ensure Twitter accounts Db is initialised
		
		
		if(true) {
			
			twitterHandler.postDelayed(startTwitter, 10);
			view=inflater.inflate(R.layout.twitter,container, false);
			return view;
		} else {
			//goTwitterSetup();
			view=inflater.inflate(R.layout.twitter_empty,container, false);
			return view;
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		State.setCurrentSection(State.SECTION_TWITTER);
		refresh();
	}

	public void refreshData() {
		
	}
	public void refresh() {
		ActionBarManager.setActionBarMenu(getActivity(), R.menu.twitter, R.color.actionbar_twitter);
	}
	private Runnable startTwitter = new Runnable() {
		public void run() {
			BLog.e("NOTE","showTwitter.runnable ");
			showTwitter();
			//new postTwitterMessage().execute("go");
		}
	};

	private void showTwitter() {
		Activity activity = getActivity();
		ListView list=(ListView) activity.findViewById(R.id.twitterList);

	}


	private boolean postMessage(TwitterAccount fromAccount) {
		boolean posted=false;
		
		if(fromAccount!=null) {
			String accessToken = fromAccount.getAccessToken();
			String accessTokenSecret = fromAccount.getAccessTokenSecret();
			Configuration conf = new ConfigurationBuilder()
			    .setOAuthConsumerKey( getKeys().TwitterOauthConsumerKey())
			    .setOAuthConsumerSecret( getKeys().TwitterOauthConsumerSecret() )
			    .setOAuthAccessToken(accessToken)
			    .setOAuthAccessTokenSecret(accessTokenSecret)
			    .build();
			Twitter t = new TwitterFactory(conf).getInstance();
			try {
				t.updateStatus( "@BrieflyApp You guys rock!" );
				posted=true;
			} catch(TwitterException e) {
				BLog.e("POST-TWITTER",e.getMessage());
			}
		}
		
		return posted;
	}
	private void ReadTimeline() {
		try {
			ResponseList<Status> list = new TwitterFactory().getInstance().getHomeTimeline();
		    for (Status each : list) {
		 
		        each.getCreatedAt();
		    }
		} catch(TwitterException e) {
			BLog.e("TWITTER",e.getMessage());
		}
	}


	private class showTwitterTimeline extends AsyncTask<String, Void, String> {
		
		//Twitter mTwitter = new TwitterFactory().getInstance();
		@Override
		protected String doInBackground(String... params) {
		    AccessToken at = null;
		    String oauthVerifier=params[0];

		    return oauthVerifier;
	
		}      
	
		@Override
		protected void onPostExecute(String result) {

			twitterHandler.postDelayed(startTwitter, 10);
			view=inflater.inflate(R.layout.twitter,container, false);
		}
	
		@Override
		protected void onPreExecute() {
		}
	
		@Override
		protected void onProgressUpdate(Void... values) {
			
		}
	 
	}
	private class postTwitterMessage extends AsyncTask<TwitterAccount, Void, TwitterAccount> {
		
		//Twitter mTwitter = new TwitterFactory().getInstance();
		@Override
		protected TwitterAccount doInBackground(TwitterAccount... params) {
		    AccessToken at = null;
		    TwitterAccount account=params[0];
	        postMessage(account);
		        
		    return account;
	
		}      
	
		@Override
		protected void onPostExecute(TwitterAccount result) {
			TextView textview = (TextView) getActivity().findViewById(R.id.twitter_label);
			textview.setText("posted twitter message");
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