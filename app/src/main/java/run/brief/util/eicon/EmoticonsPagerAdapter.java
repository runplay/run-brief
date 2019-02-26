package run.brief.util.eicon;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.List;

import run.brief.R;
import run.brief.util.eicon.EmoticonsGridAdapter.KeyClickListener;

public class EmoticonsPagerAdapter extends PagerAdapter {

    List<List<String>> emoticons;
	private static final int NO_OF_EMOTICONS_PER_PAGE = 24;
	Activity mActivity;
	KeyClickListener mListener;

	public EmoticonsPagerAdapter(Activity activity, List<List<String>> emoticons, KeyClickListener listener) {
		this.emoticons = emoticons;
		this.mActivity = activity;
		this.mListener = listener;
	}

	@Override
	public int getCount() {
		return emoticons.size();
		//return (int) Math.ceil((double) emoticons.size()/ (double) NO_OF_EMOTICONS_PER_PAGE);
	}

	@Override
	public Object instantiateItem(ViewGroup collection, int position) {

		View layout = mActivity.getLayoutInflater().inflate(
				R.layout.emoticons_grid, null);

        GridView grid = (GridView) layout.findViewById(R.id.emoticons_grid);
        /*
        if(position==emoticons.size()) {
            // emoticon
            List<String> emoticonsInAPage = ViewManagerText.getEmoticonsDefaults();
            EmoticonsGridAdapter adapter = new EmoticonsGridAdapter(
                    mActivity.getApplicationContext(), emoticonsInAPage, position,
                    mListener);
            adapter.setStyle(EmoticonsGridAdapter.STYLE_EMOTICONS);
            grid.setAdapter(adapter);
        } else {
*/
            // emojii
            List<String> emoticonsInAPage = emoticons.get(position);
            EmoticonsGridAdapter adapter = new EmoticonsGridAdapter(
                    mActivity.getApplicationContext(), emoticonsInAPage, position,
                    mListener);
            adapter.setStyle(EmoticonsGridAdapter.STYLE_EMOJII);
            grid.setAdapter(adapter);

        //}

		((ViewPager) collection).addView(layout);

		return layout;
	}

	@Override
	public void destroyItem(ViewGroup collection, int position, Object view) {
		((ViewPager) collection).removeView((View) view);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
}