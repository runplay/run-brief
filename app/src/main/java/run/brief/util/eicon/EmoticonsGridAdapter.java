package run.brief.util.eicon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.List;

import run.brief.b.B;
import run.brief.R;

public class EmoticonsGridAdapter extends BaseAdapter{
	
	private List<String> paths;
	private int pageNumber;
	Context mContext;
	
	KeyClickListener mListener;
    private int STYLE_=0;
    public static final int STYLE_EMOJII=0;
    public static final int STYLE_EMOTICONS=1;

    public void setStyle(int STYLE_) {
        this.STYLE_=STYLE_;
    }

	public EmoticonsGridAdapter(Context context, List<String> paths, int pageNumber, KeyClickListener listener) {
		this.mContext = context;
		this.paths = paths;
		this.pageNumber = pageNumber;
		this.mListener = listener;
	}
	public View getView(int position, View convertView, ViewGroup parent){

		View v = convertView;
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.emoticons_item, null);			
		}
		
		final String path = paths.get(position);

		EditText emo = (EditText) v.findViewById(R.id.item);
        View bg = v.findViewById(R.id.item_bg);
        ImageView img = (ImageView) v.findViewById(R.id.emo_img);
		//image.setImageBitmap(getImage(path));
        //BLog.e("EMO",""+path);
        emo.setBackground(null);
        /*
        if(STYLE_==STYLE_EMOTICONS) {
            img.setImageDrawable(mContext.getResources().getDrawable(ViewManagerText.getEmoticonsRiconLookup(path)));
            img.setVisibility(View.VISIBLE);
            //bg.setBackground(mContext.getResources().getDrawable(ViewManagerText.getEmoticonsRiconLookup(path)));
            emo.setVisibility(View.GONE);
            img.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mListener.keyClickedIndex(path.toString());
                    //v.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                    //Animation animation1 = AnimationUtils.loadAnimation(mContext, R.anim.alpha_flash);

                    ((View)v.getParent()).startAnimation(B.animateAlphaFlash());
                }
            });
        } else {
            */
            bg.setBackground(null);
            emo.setVisibility(View.VISIBLE);
            img.setVisibility(View.GONE);
            emo.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mListener.keyClickedIndex(path.toString());
                    //v.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                    //Animation animation1 = AnimationUtils.loadAnimation(mContext, R.anim.alpha_flash);

                    ((View)v.getParent()).startAnimation(B.animateAlphaFlash());
                }
            });
        //}
		//Spannable sp = new SpannableString(path);


		emo.setText(path);



        //emo.
		return v;
	}
	
	@Override
	public int getCount() {		
		return paths.size();
	}
	
	@Override
	public String getItem(int position) {		
		return paths.get(position);
	}
	
	@Override
	public long getItemId(int position) {		
		return position;
	}
	
	public int getPageNumber () {
		return pageNumber;
	}
	/*
	private Bitmap getImage (String path) {

        Bitmap temp = ViewManagerText.emojiBitmaps.get(path);
        if(temp!=null)
            return temp;

		AssetManager mngr = mContext.getAssets();
		InputStream in = null;
		
		 try {
				in = mngr.open("emoticons/" + path);
		 } catch (Exception e){
					e.printStackTrace();
		 }
		 
		 //BitmapFactory.Options options = new BitmapFactory.Options();
		 //options.inSampleSize = chunks;
		 
		 return BitmapFactory.decodeStream(in ,null ,null);
	}
	*/
	public interface KeyClickListener {
		
		public void keyClickedIndex(String index);
	}
}
