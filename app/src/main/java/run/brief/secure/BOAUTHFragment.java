package run.brief.secure;

import run.brief.b.BFragment;

public class BOAUTHFragment extends BFragment  {


	@Override
    public void onResume() {
    	super.onResume();
    	//refresh();
    }

	@Override
    public void onPause() {
    	super.onPause();
    }
	@Override
    public void onStop() {
    	super.onStop();
    }

	public void ref() {}
	private final Keys keys=new Keys();
	
	protected Keys getKeys() {
		return this.keys;
	}
	
	protected class Keys {
		private static final String TWITTER_OAUTH_CONSUMER_KEY="WKKKjJJMxRYupMZsvrJblA";
		private static final String TWITTER_OAUTH_CONSUMER_SECRET="vkpyCvVHRsciMLnzCrw9O65PUj6QSAZSO6zJ3WfE";
		
		private static final String LINKEDIN_OAUTH_CONSUMER_KEY="WKKKjJJMxRYupMZsvrJblA";
		private static final String LINKEDIN_OAUTH_CONSUMER_SECRET="vkpyCvVHRsciMLnzCrw9O65PUj6QSAZSO6zJ3WfE";
		
		private static final String FACEBOOK_OAUTH_CONSUMER_KEY="WKKKjJJMxRYupMZsvrJblA";
		private static final String FACEBOOK_OAUTH_CONSUMER_SECRET="vkpyCvVHRsciMLnzCrw9O65PUj6QSAZSO6zJ3WfE";
		
		private boolean isAuth() {
			//BLog.e("PEMCLASS",this.getClass().getName());
			if(this.getClass().getName().startsWith("run.brief"))
				return true;
			else 
				return false;
		}
		
		public String TwitterOauthConsumerKey() {
			if(isAuth())
				return TWITTER_OAUTH_CONSUMER_KEY;
			else
				return "";
		}
		public String TwitterOauthConsumerSecret() {
			if(isAuth())
				return TWITTER_OAUTH_CONSUMER_SECRET;
			else
				return "";
		}
		public String LinkedinOauthConsumerKey() {
			if(isAuth())
				return LINKEDIN_OAUTH_CONSUMER_KEY;
			else
				return "";
		}
		public String LinkedinOauthConsumerSecret() {
			if(isAuth())
				return LINKEDIN_OAUTH_CONSUMER_SECRET;
			else
				return "";
		}
		public String FacebookOauthConsumerKey() {
			if(isAuth())
				return FACEBOOK_OAUTH_CONSUMER_KEY;
			else
				return "";
		}
		public String FacebookOauthConsumerSecret() {
			if(isAuth())
				return FACEBOOK_OAUTH_CONSUMER_SECRET;
			else
				return "";
		}
	}


}
