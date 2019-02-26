package run.brief.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import run.brief.b.B;
import run.brief.b.BCallbackInt;
import run.brief.b.Bgo;
import run.brief.util.log.BLog;

public class UrlImage {
	private static final int URL_TIMEOUT_MILLIS=15000; // 15 second timeout
	private static HashMap<String,String> cookieStore= new HashMap<String,String>();
	private DoLoadFromHost loader;
	private String imgUrl;
	private String filePath;
	private Activity activity;
	private ImageView forView;
	private BCallbackInt callback;
	private int position;

	private Class refreshFragmentClass;


	public final Bitmap get(String imgUrl) {
		return get(null,imgUrl,null);
	}
	//public final Bitmap get(Activity activity,String imgUrl) {
	//	return get(activity,imgUrl,null);
	//}
	public final Bitmap getListImage(Activity activity,String imgUrl,int position, BCallbackInt callback) {
		this.callback=callback;
		this.position=position;
		return get(activity,imgUrl,null);
	}
	public final Bitmap getRefresh(Activity activity,String imgUrl,ImageView refreshView) {
		forView=refreshView;
		return get(activity,imgUrl,null);
	}

	public final Bitmap get(Activity activity,String imgUrl,Class refreshFragmentClass) {
		this.refreshFragmentClass=refreshFragmentClass;
		this.activity=activity;
		this.imgUrl=imgUrl;
		if(imgUrl!=null && imgUrl.length()>2) {



			StringBuilder dir = new StringBuilder(Files.HOME_PATH_FILES);
			dir.append(File.separator);
			dir.append(Files.FOLDER_IMAGES);
			String path=dir.toString();
			//BLog.e("img: "+imgUrl);
			String filename = Files.createFileNameFromUrl(imgUrl);


			dir.append(File.separator);
			dir.append(filename+".brf");
			this.filePath=dir.toString();

			File imgFile = new  File(filePath);
			if(!imgFile.exists()) {
				if(!UrlImageLoadStore.isLoading(imgUrl)) {
					UrlImageLoadStore.addToStore(imgUrl);
					Files.ensurePath(path);

					loader=new DoLoadFromHost();
					loader.execute(new String[]{imgUrl,dir.toString()});
				} else {
					return null;
				}

			} else {
				Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				return myBitmap;

			}
		}
		return null;
	}
	public final Bitmap getIcon(String imgUrl) {
		this.imgUrl=imgUrl;
		StringBuilder dir = new StringBuilder(Files.HOME_PATH_FILES);
		dir.append(File.separator);
		dir.append(Files.FOLDER_IMAGES);
		String path=dir.toString();
		String filename = Files.createFileNameFromUrl(imgUrl);


		dir.append(File.separator);
		dir.append(filename+".brf");
		this.filePath=dir.toString();

		File imgFile = new  File(filePath);
		if(!imgFile.exists()) {
			Files.ensurePath(path);

			loader=new DoLoadFromHost();
			loader.execute(new String[]{imgUrl,dir.toString()});


		} else {
			InputStream is=null;
			boolean error=false;
			try {
				is= new FileInputStream(filePath);
			} catch(Exception e) {
				error=true;
				//BLog.add("UrlImage:1",e);
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int read=0;
			try {
				while((read = is.read()) != -1){
					bos.write(read);
				}
			} catch(Exception e) {
				error=true;
				//BLog.add("UrlImage:2",e);
			}
			if(!error) {
				byte[] ba = bos.toByteArray();

				return BitmapFactory.decodeByteArray(ba, 0, ba.length);
			}
		}
		return null;
	}

	public class DoLoadFromHost extends AsyncTask<String, Void, Boolean> {

		//String refreshFragmentClassName;
		private String url;
		private String file;

		public DoLoadFromHost() {
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				//BLog.e("UrlImage","not exists: "+filePath);
				url=params[0];
				file=params[1];
				getImageFromUrl(url,file);

			} catch(Exception e) {
				//BLog.add("UrlImage Error",e);
			}
			return true;

		}

		@Override
		protected void onPostExecute(Boolean result) {
			//BLog.e("URLIMG",""+refreshFragmentClass.getName()+" -- "+dir+"/"+file);

			if(refreshFragmentClass!=null && activity!=null) {
				Bgo.refreshDataCurrentIfFragment(activity, refreshFragmentClass);
				refreshFragmentClass=null;
				//Bgo.refreshCurrentIfFragment(activity, refreshFragmentClass);
			} else if(callback!=null) {
				callback.callback(position);
				callback=null;
			}
			if(forView!=null) {
				//BLog.e("invalidateview");
				String urlStr=(String)forView.getTag();
				if(urlStr!=null) {
					forView.setImageBitmap(new UrlImage().get(urlStr));

					forView.setAnimation(B.animateAlphaFlash());
				}
			}

			//Log.e("img frag",""+refreshFragmentClassName);
		}

		@Override
		protected void onPreExecute() {
		}

	}
	public void getImageFromUrl(String url, String filePath) throws IOException {
		//BLog.e("getImageFromUrl: "+url);
		URL serverUrl = new URL(url);
		HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();
		String useCookie = cookieStore.get(url);
		if(useCookie!=null) {
			urlConnection.setRequestProperty("Cookie", useCookie);
		}
		try {
			// mock an android header
			urlConnection.setRequestProperty("User-Agent",UrlStore.USER_AGENT);
			urlConnection.setRequestMethod("GET");
			//urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			urlConnection.setConnectTimeout(URL_TIMEOUT_MILLIS);
			urlConnection.setReadTimeout(URL_TIMEOUT_MILLIS);

		} catch(Exception e) {
			BLog.e("getImageFromUrl().error.msg","2:"+e.getMessage());
		}
		try {
			urlConnection.connect();

			switch (urlConnection.getResponseCode())
			{
				case HttpURLConnection.HTTP_MOVED_PERM:
				case HttpURLConnection.HTTP_MOVED_TEMP:
					String location = urlConnection.getHeaderField("Location");
					URL base     = new URL(url);
					URL next     = new URL(base, location);  // Deal with relative URLs
					url      = next.toExternalForm();
					//BLog.e("redirect to: "+url);
					URL newurl = new URL(url);
					urlConnection = (HttpURLConnection) newurl.openConnection();
					urlConnection.connect();
					break;
			}

		} catch(Exception e) {
			cookieStore.remove(url);
			BLog.e("getImageFromUrl().error.msg", "3 - connect(): " + e.getMessage());
		}

		final int BUFFER_SIZE = 8 * 1024;
		BufferedInputStream is = new BufferedInputStream(urlConnection.getInputStream(), BUFFER_SIZE);//urlConnection.getInputStream();

		byte[] baf = new byte[BUFFER_SIZE];
		int actual = 0;

		try {

			FileOutputStream fos = new FileOutputStream(filePath);

			while (actual != -1) {
				fos.write(baf, 0, actual);
				actual = is.read(baf, 0, BUFFER_SIZE);
			}
              /* Convert the Bytes read to a String. */
			fos.flush();
			fos.close();
		} catch(Exception e) {
			cookieStore.remove(url);
			BLog.e("getImageFromUrl().error.msg", "4 - write(): " + e.getMessage());
		} finally {
			is.close();
		}
		UrlImageLoadStore.removeFromStore(url);

	}
}
