package run.brief.service;

import run.brief.b.Bgo;
import run.brief.news.NewsHomeFragment;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

public class DelayedRefreshNews extends AsyncTask<Boolean, Void, Integer> {
	
	private Context context;
	
	public DelayedRefreshNews() {
		// TODO Auto-generated constructor stub
	}
	public DelayedRefreshNews(Context context) {
		this.context=context;
	}

	@Override
	protected Integer doInBackground(Boolean... params) {
		try {
			this.wait(1500);
		} catch(Exception e){}
		return 0;
	}

	@Override
	protected void onPostExecute(Integer result) {
		Bgo.refreshFragment((Activity) context, NewsHomeFragment.class.getName());
	}

	@Override
	protected void onPreExecute() {
	}


}