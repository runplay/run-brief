package run.brief.news;

import android.content.Context;
import android.os.AsyncTask;

import run.brief.b.Bgo;
import run.brief.beans.Brief;
import run.brief.beans.RssItem;
import run.brief.beans.RssUserFeed;
import run.brief.service.BriefNotify;

//import run.brief.beans.Brief;

public class DoLoadForUserFeed extends AsyncTask<RssUserFeed, Void, Integer> {

    Context context;
	private boolean doNotRefresh=false;

	public void setDoNotRefresh(boolean dnrf) {
		this.doNotRefresh=dnrf;
	}
	//String refreshFragmentClassName;

	public DoLoadForUserFeed() {

	}
	public DoLoadForUserFeed(Context context) {
		this.context=context;
		//this.refreshFragmentClassName=refreshFragmentClassName;
	}

	@Override
	protected Integer doInBackground(RssUserFeed... params) {
		int count = 0;
        NewsFiltersDb.init();
		if(params!=null && params.length>0) {
			for(int i=0; i<params.length; i++) {
				RssUserFeed feed=params[i];
				count=count+NewsItemsDb.refreshNewsFromFeed(feed);

			}
		}
		return Integer.valueOf(count);
	}

	@Override
	protected void onPostExecute(Integer result) {
		NewsItemsDb.addNewCount(result.intValue());
        if(result>0) {
            int getitems=result;
            if(getitems>2)
                getitems=3;
            for(int i=0; i<getitems; i++) {
                RssItem item = NewsItemsDb.get(i);
                if(item!=null) {
                    Brief brief=new Brief(item,i);
                    if(NewsFiltersDb.canShowFeed(item)) {
                        BriefNotify.addNotifyFor(context, brief, true);
                    }
                }
            }


        }
		if(!doNotRefresh) {
			//BLog.e("do not refresh: "+doNotRefresh);
			AdapterListNews.setForceChange(true);
			NewsItemsDb.refreshData();
			Bgo.tryRefreshCurrentFragment();
		}
	}

	@Override
	protected void onPreExecute() {
	}


}
