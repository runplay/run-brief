package run.brief.news;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import run.brief.R;
import run.brief.b.B;
import run.brief.b.BCallbackInt;
import run.brief.beans.RssItem;
import run.brief.util.BitmapFunctions;
import run.brief.util.Cal;
import run.brief.util.Num;
import run.brief.util.Sf;
import run.brief.util.UrlImage;

public class AdapterListNews extends BaseAdapter {
 
    private static Activity activity;
    //private JSONArray data;
    //private static LayoutInflater inflater=null;
    //public static int currentSelectedPosition;

    //private static boolean isDarkTheme=true;
    private static int height;
    public void setHeight(int hdp) {
        height=hdp;
    }

    private static List<RssItem> showItems=new ArrayList<RssItem>();
    private static String showing;
    private static String showPublisher=null;
    private static boolean forceChange=false;

    private static BCallbackInt callBackView;

    public static void setForceChange(boolean fc) {
        forceChange=fc;
    }
    public static String getShowPublisher() {
        return showPublisher;
    }
    public static void setShowPublisher(String showPub) {
        showPublisher=showPub;
    }
    public static List<RssItem> getShowItems() {
        return showItems;
    }
    //private static int heightSmall=16;
    //private static int heightBig=20;
    public AdapterListNews(Activity a, BCallbackInt callback) {
        callBackView=callback;
        activity = a;
        //showPublisher=null;

        refresh();
        //heightSmall= Functions.dpToPx(16,activity);
        //heightBig= Functions.dpToPx(16,activity);
        //this.data=data;
        //inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    private void refresh() {
        boolean changed=false;
        HashMap<Long,RssItem> nshowItems=new HashMap<Long,RssItem>();
        //BLog.e("adli", "pub: " + showPublisher );
        if(showPublisher==null) {
            if(forceChange || showing==null || !showing.equals("all")) {
                for (RssItem item : NewsItemsDb.getItemsFiltered()) {
                    nshowItems.put(Long.valueOf(item.getLong(RssItem.LONG_DATE)), item);
                }
                showing="all";
                changed=true;
            }
        } else if(showPublisher.equals("star")) {
            if(forceChange || showing==null || !showing.equals("star")) {
                for (RssItem item : NewsItemsDb.getItemsDatabase().getItemsStarred(0,50)) {
                    nshowItems.put(Long.valueOf(item.getLong(RssItem.LONG_DATE)), item);
                }
                showing="star";
                changed=true;
            }
        } else {
            if(showing==null || !showing.equals(showPublisher)) {
                //BLog.e("load pub: "+showPublisher);
                for (RssItem item : NewsItemsDb.getItemsForPublisher(showPublisher)) {
                    String createLong = item.getLong(RssItem.LONG_DATE) + "" + Num.getRandom(11, 99);
                    nshowItems.put(Long.valueOf(createLong), item);
                }
                showing=showPublisher;
                changed=true;
            }
        }
        if(changed) {
            Object[] briefsSorted = new TreeSet<Long>(nshowItems.keySet()).descendingSet().toArray();

            showItems.clear();
            for (Object ind : briefsSorted) {
                RssItem it = nshowItems.get((Long) ind);
                //BLog.e(ind + " -- " +it.getString(RssItem.STRING_HEAD));
                showItems.add(it);

            }
        }
        forceChange=false;

    }
 
    public int getCount() {
        return showItems.size();
    }
 
    public Object getItem(int position) {
        return showItems.get(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        final RssItem t = showItems.get(position);
    	View v=getView(activity, t, position, convertView, true);

    	return v;
    }
    private static int feedFilterId=-1;

    public static void setFeedFilter(int feedId) {
        feedFilterId=feedId;
    }


    public static View getView(final Activity activity, RssItem rss, int position, View convertView, boolean allowDrag) {
        View vi=convertView;
        RssItem t=rss;
        if(convertView==null) {
            LayoutInflater inflater=(LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.news_item, null);

        }
        vi.setMinimumHeight(height);

        vi.setVisibility(View.VISIBLE);
        TextView head = (TextView)vi.findViewById(R.id.news_item_head);
        TextView text = (TextView)vi.findViewById(R.id.news_item_text);
        TextView date = (TextView)vi.findViewById(R.id.news_item_date);
        TextView publisher = (TextView)vi.findViewById(R.id.news_item_pub);
        ImageView image = (ImageView) vi.findViewById(R.id.news_item_image);

        ImageView star = (ImageView) vi.findViewById(R.id.news_item_favourite);

        ImageView read = (ImageView) vi.findViewById(R.id.news_item_read);
        //ImageView img = (ImageView) vi.findViewById(R.id.news_item_image);

        B.addStyle(text, date,publisher);
        B.addStyleBold(head);


        head.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
        text.setAlpha(0.8F);
        // Setting all values in listview
        if(t!=null) {
            String hd=t.getString(RssItem.STRING_HEAD);
            if(hd.length()>80)
                hd= Sf.restrictLength(hd, 80)+"...";
            head.setText(hd);
            String txt = t.getString(RssItem.STRING_TEXT);
            if(txt.length()>180)
                txt= Sf.restrictLength(txt, 180)+"...";
            text.setText(txt);
            String imgurl=t.getString(RssItem.STRING_IMG_URL);
            publisher.setText(t.getString(RssItem.STRING_PUBLISHER));
            String dte= Cal.getCal(new Date(t.getLong(RssItem.LONG_DATE))).friendlyReadDate();

            star.setImageDrawable(B.getDrawable(activity,R.drawable.rate_unrated_grey));

            star.setTag(Long.valueOf(t.getLong(RssItem.LONG_ID)));
            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Long id = (Long) v.getTag();
                    RssItem it=NewsItemsDb.getById(id);
                    if(it!=null) {
                        ImageView iv = (ImageView) v;
                        int fav=it.getInt(RssItem.INT_FAVOURITE);
                        if(fav==0) {
                            fav = 1;
                            iv.setImageDrawable(B.getDrawable(activity,R.drawable.rate_positive));


                        } else {
                            iv.setImageDrawable(B.getDrawable(activity,R.drawable.rate_unrated_grey));
                            fav = 0;
                        }
                        it.setInt(RssItem.INT_FAVOURITE,fav);
                        NewsItemsDb.getItemsDatabase().updateFavouriteRead(it);

                    }
                }
            });
            int fav=t.getInt(RssItem.INT_FAVOURITE);
            if(fav==1) {
                star.setImageDrawable(B.getDrawable(activity, R.drawable.rate_positive));
            } else {
                star.setImageDrawable(B.getDrawable(activity,R.drawable.rate_unrated_grey));
            }
            if(t.getInt(RssItem.INT_READ)==1) {
                read.setVisibility(View.VISIBLE);
                read.setImageDrawable(B.getDrawable(activity, R.drawable.content_read));
            } else {
                read.setVisibility(View.GONE);
            }
            date.setText(dte);

            if(!imgurl.isEmpty()) {
                image.setVisibility(View.VISIBLE);
                text.setTextSize(13F);

                if(txt.length()>32) {
                    text.setPadding(10,5,5,5);
                } else {
                    text.setPadding(10,35,5,5);
                }
                Bitmap b = new UrlImage().getRefresh(activity, imgurl, image);
                image.setVisibility(View.VISIBLE);
                image.setTag(imgurl);
                if(b!=null) {

                    Bitmap resizedBitmap = BitmapFunctions.resizeToFitMax(b, 200,140);//Bitmap.createScaledBitmap(b, w, h, false);
                    image.setImageBitmap(resizedBitmap);

                } else {
                    //text.setHeight(heightBig);
                    image.setImageDrawable(B.getDrawable(activity,R.drawable.downloading200x140));

                }
            } else {
                text.setTextSize(11F);
                image.setImageDrawable(null);
                image.setVisibility(View.GONE);

            }

        }
        return vi;
    }
}
