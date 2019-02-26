package run.brief.util.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import run.brief.b.ActionBarManager;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.BriefManager;
import run.brief.R;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.Email;
import run.brief.beans.Note;
import run.brief.email.EmailSendFragment;
import run.brief.notes.NotesEditFragment;
import run.brief.settings.AccountsDb;
import run.brief.util.BriefActivityManager;
import run.brief.util.Num;
import run.brief.util.explore.FileExploreFragment;
import run.brief.util.json.JSONArray;
import run.brief.util.log.BLog;


public class CameraFragment extends BFragment implements BRefreshable {

	public static final int ACTION_TAKE_PHOTO_B = 1;
	private static final int ACTION_TAKE_PHOTO_S = 2;
	private static final int ACTION_TAKE_VIDEO = 3;

	private View view;
	private Activity activity;
	
	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
	private ImageView mImageView;
	private Bitmap mImageBitmap;

	private static final String VIDEO_STORAGE_KEY = "viewvideo";
	private static final String VIDEOVIEW_VISIBILITY_STORAGE_KEY = "videoviewvisibility";
	private VideoView mVideoView;
	private Uri mVideoUri;
	
	private TextView textHeading;
	private TextView textData;
	private TextView textFilePath;
	
	private ImageView instagram;
	//private Button picBtn;
	private Button openNote;
	private Button openEmail;
	private ImageView folder;
	
	private boolean hasPhoto=false;
	//private boolean firstLaunch=false;
	

	private static String mCurrentPhotoPath;

	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	/** Called when the activity is first created. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.camera);
		activity=getActivity();
		//firstLaunch=true;
		
		view = inflater.inflate(R.layout.camera ,container, false);
		return view;

		
		//dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
	}
	@Override
	public void refresh() {
		ActionBarManager.setActionBarBackOnly(activity, activity.getResources().getString(R.string.label_camera),R.menu.camera);

		BriefManager.clearController(activity);
		if(State.hasStateObject(State.SECTION_CAMERA,StateObject.STRING_FILE_PATH)) {
			//BLog.e("REFRESHPHOTO", "state has photo file path");
			JSONArray jarr =null;
			try {
				jarr = new JSONArray(State.getStateObjectString(State.SECTION_CAMERA, StateObject.STRING_FILE_PATH));
			} catch(Exception e) {}
			if(jarr!=null && jarr.length()>0) {
				mCurrentPhotoPath=jarr.getString(0);
				State.addCameraPhoto(mCurrentPhotoPath);
				//mCurrentPhotoPath=State.getStateObjectString(StateObject.STRING_FILE_PATH);
				//BLog.e("REFRESHPHOTO", "state has photo call");
				handleBigCameraPhoto();
				hasPhoto=true;
				instagram.setAlpha(1F);
				openNote.setAlpha(1F);
				openEmail.setAlpha(1F);
			}

		} else if(State.hasCameraHistory()) {
			mCurrentPhotoPath=State.getCameraLastPhoto();
			//mCurrentPhotoPath=State.getStateObjectString(StateObject.STRING_FILE_PATH);
			//BLog.e("REFRESHPHOTO", "state has photo call");
			handleBigCameraPhoto();
			hasPhoto=true;
			instagram.setAlpha(1F);
			openNote.setAlpha(1F);
			openEmail.setAlpha(1F);
		} else {
			//BLog.e("REFRESHPHOTO", "state else else lese ");
			
			hasPhoto=false;
			instagram.setAlpha(0.25F);
			openNote.setAlpha(0.25F);
			openEmail.setAlpha(0.25F);
			//dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
		}
		
		/*
		if(firstLaunch) {
			firstLaunch=false;
			dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
		}
		*/
		State.clearStateObjects(State.SECTION_CAMERA);
	}

	@Override
	public void onResume() {
		super.onResume();
		//BLog.e("RESUME", "camera fragment resume");
		State.sectionsClearBackstack();
		State.setCurrentSection(State.SECTION_CAMERA);
		
		mImageView = (ImageView) view.findViewById(R.id.imageView1);
		mVideoView = (VideoView) view.findViewById(R.id.videoView1);
		

		mImageBitmap = null;
		mVideoUri = null;

		folder=(ImageView) view.findViewById(R.id.btn_camera_folder);
		folder.setClickable(true);
		folder.setOnClickListener(folderClick);
		
		
		instagram = (ImageView) view.findViewById(R.id.btn_camera_open_instagram);
		if(BriefActivityManager.isInstagramInstalled(activity)) {
			instagram.setVisibility(View.VISIBLE);
			instagram.setClickable(true);
			instagram.setOnClickListener(mInstagramClickListener);
		} else {
			instagram.setVisibility(View.GONE);
		}

		openEmail = (Button) view.findViewById(R.id.btn_camera_open_email);
		if(AccountsDb.getAllEmailAccounts().size()>0) {
			setBtnListenerOrDisable( 
					openEmail, 
					mEmailClickListener,
					MediaStore.ACTION_IMAGE_CAPTURE
			);
			openEmail.setVisibility(View.VISIBLE);
		} else {
			openEmail.setVisibility(View.GONE);
		}
		

		openNote = (Button) view.findViewById(R.id.btn_camera_open_note);
		setBtnListenerOrDisable( 
				openNote, 
				mNoteClickListener,
				MediaStore.ACTION_IMAGE_CAPTURE
		);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
		
		textHeading = (TextView) view.findViewById(R.id.camera_info_file_name);
		textHeading.setText(getString(R.string.camera_choose));
		textHeading.setCompoundDrawablesWithIntrinsicBounds(activity.getResources().getDrawable(R.drawable.s_file),null,null,null);
		//B.fixDrawableLevels(textHeading);
		textData = (TextView) view.findViewById(R.id.camera_info_data);
		textData.setText("");
		//textData.setCompoundDrawablesWithIntrinsicBounds(activity.getResources().getDrawable(R.drawable.action_about), null,null,null);
		//B.fixDrawableLevels(textData);
		textFilePath= (TextView) view.findViewById(R.id.camera_info_file_path);
		textFilePath.setText("");
		textFilePath.setCompoundDrawablesWithIntrinsicBounds(activity.getResources().getDrawable(R.drawable.s_folder), null,null,null);
		//B.fixDrawableLevels(textFilePath);
		refresh();
	}
	public void refreshData() {
		
	}
	@Override
	public void onPause() {
		super.onPause();
        System.gc();
		if(mCurrentPhotoPath!=null) {
			File f = new File(mCurrentPhotoPath);
			JSONArray jarr = new JSONArray();
			jarr.put(f.getAbsolutePath());
			
			State.addToState(State.SECTION_CAMERA,new StateObject(StateObject.STRING_FILE_PATH,jarr.toString()));
			//State.addToState(new StateObject(StateObject.STRING_FILE_PATH,mCurrentPhotoPath));
		}
	}
	
	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		//BLog.e("OPTIONS", "onCreateOptionsMenu at new emai view");
	}
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_new_photo:
			dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
		    break;
		}	
		return true;
	}

	/* Photo album for this application */
	private String getAlbumName() {
		return getString(R.string.album_name);
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						//Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}
			
		} else {
			//BLog.e(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}

	private File setUpPhotoFile() throws IOException {
		
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		
		return f;
	}

	private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */
		//BLog.e("PPATH", ""+mCurrentPhotoPath);
		/* Get the size of the ImageView */
		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();
        System.gc();
		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		
		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		//BLog.e(tag, message);
		if(photoW>0 && targetW>0 && photoH>0 && targetH>0) {
			scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
		} else {
            scaleFactor = Math.min(photoW/500, photoH/500);
        }


		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */

		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		
		int spp = mCurrentPhotoPath.lastIndexOf("/");
		
		
		textHeading.setText(mCurrentPhotoPath.substring(spp+1, mCurrentPhotoPath.length()));
		textFilePath.setText(mCurrentPhotoPath.substring(0,spp));
		StringBuilder sb = new StringBuilder(getString(R.string.camera_txt_image_size));
		sb.append(": (");
		sb.append(photoW);
		sb.append("w x ");
		sb.append(photoH);
		sb.append("h) - ");
		DecimalFormat df = new DecimalFormat( "###,###,###,##0.00" );
		File f = new File(mCurrentPhotoPath);
		if(f!=null && f.exists()) {
			sb.append(getString(R.string.camera_txt_image_size)+": ");
			sb.append(Num.btyesToFileSizeString(f.length()));
		
		}
		
		textData.setText(sb.toString());
		
		/* Associate the Bitmap to the ImageView */
		mImageView.setImageBitmap(bitmap);
		//mImageView.set
		mVideoUri = null;
		mImageView.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.INVISIBLE);
	}

	private void galleryAddPic() {
		    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
			File f = new File(mCurrentPhotoPath);
		    Uri contentUri = Uri.fromFile(f);
		    mediaScanIntent.setData(contentUri);
		    activity.sendBroadcast(mediaScanIntent);
	}

	private void dispatchTakePictureIntent(int actionCode) {
		mCurrentPhotoPath = null;
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		//Intent takePictureIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
		
		Camera camera = getCameraInstance();
		if(camera!=null) {
			Parameters params = camera.getParameters();
			if(params!=null) {
				params.setFlashMode(Parameters.FLASH_MODE_ON);
			}
		}
		
		switch(actionCode) {
		case ACTION_TAKE_PHOTO_B:
			File f = null;
			
			try {
				f = setUpPhotoFile();
				mCurrentPhotoPath = f.getAbsolutePath();
				State.addCameraPhoto(mCurrentPhotoPath);
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			} catch (IOException e) {
				e.printStackTrace();
				f = null;
				//mCurrentPhotoPath = null;
			}
			break;
		default:
			break;			
		} // switch

		activity.startActivityForResult(takePictureIntent, actionCode);
	}

	private void dispatchTakeVideoIntent() {
		Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		activity.startActivityForResult(takeVideoIntent, ACTION_TAKE_VIDEO);
	}

	private void handleSmallCameraPhoto(Intent intent) {
		Bundle extras = intent.getExtras();
		mImageBitmap = (Bitmap) extras.get("data");
		mImageView.setImageBitmap(mImageBitmap);
		mVideoUri = null;
		mImageView.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.INVISIBLE);
	}

	private void handleBigCameraPhoto() {

		if (mCurrentPhotoPath != null) {
			setPic();
			galleryAddPic();
			
		}

	}

	private void handleCameraVideo(Intent intent) {
		mVideoUri = intent.getData();
		mVideoView.setVideoURI(mVideoUri);
		mImageBitmap = null;
		mVideoView.setVisibility(View.VISIBLE);
		mImageView.setVisibility(View.INVISIBLE);
	}


	Button.OnClickListener mNoteClickListener = 
			new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(hasPhoto) {
					State.clearStateObjects(State.SECTION_NOTES_ITEM);
					Note n = new Note();
					n.addFile(mCurrentPhotoPath);
					State.addToState(State.SECTION_NOTES_ITEM,new StateObject(StateObject.STRING_BJSON_OBJECT,n.toString()));
					Bgo.openFragmentBackStackAnimate(activity, NotesEditFragment.class);
				}
				
			}
		};
	Button.OnClickListener mInstagramClickListener = 
			new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				///
				if(hasPhoto) {
					File f = new File(mCurrentPhotoPath);
				    Uri contentUri = Uri.fromFile(f);
					BriefActivityManager.openInstagram(activity, contentUri, "photo");
				}
				//dispatchTakePictureIntent(ACTION_TAKE_PHOTO_S);
			}
		};
		Button.OnClickListener mEmailClickListener = 
				new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					///
					if(hasPhoto) {
						State.clearStateObjects(State.SECTION_EMAIL_NEW);
                        Email em = new Email();
                        em.setString(Email.STRING_MESSAGE, activity.getString(R.string.image_attached));
                        File f = new File(mCurrentPhotoPath);
                        em.addAttachment(f.getPath());
                        State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.STRING_BJSON_OBJECT,em.toString()));
                        Bgo.openFragmentBackStack(activity, EmailSendFragment.class);
						/*
						Account acc = null;
						ArrayList<Account> accs=AccountsDb.getAllEmailAccounts();
						if(accs!=null && !accs.isEmpty()) {
							StateObject soba = new StateObject(StateObject.LONG_USE_ACCOUNT_ID,Long.valueOf(accs.get(0).getLong(Account.LONG_ID)));
							State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.INT_FORCE_NEW,1));
							State.addToState(State.SECTION_EMAIL_NEW,soba);

							
							Bgo.openFragmentBackStackAnimate(activity, new EmailSendFragment());
						}
					
					*/
					}
					//dispatchTakePictureIntent(ACTION_TAKE_PHOTO_S);
				}
			};
	Button.OnClickListener mTakePicSOnClickListener = 
		new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			dispatchTakePictureIntent(ACTION_TAKE_PHOTO_S);
		}
	};

	Button.OnClickListener mTakeVidOnClickListener = 
		new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			dispatchTakeVideoIntent();
		}
	};

	private OnClickListener folderClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			State.clearStateObjects(State.SECTION_FILE_EXPLORE);
			JSONObject json = new JSONObject();
			File fp=mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
			try {
				json.put("filepath", fp.getAbsolutePath());
			} catch(Exception e) {}
			State.addToState(State.SECTION_FILE_EXPLORE,new StateObject(StateObject.STRING_BJSON_OBJECT,json.toString()));
			Bgo.openFragmentBackStackAnimate(activity, FileExploreFragment.class);
		}
	};	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTION_TAKE_PHOTO_B: {
			if (resultCode == Activity.RESULT_OK) {
				BLog.e("ACTRESULT", "handleBigCameraPhoto");
				handleBigCameraPhoto();
			}
			break;
		} // ACTION_TAKE_PHOTO_B

		case ACTION_TAKE_PHOTO_S: {
			if (resultCode == Activity.RESULT_OK) {
				handleSmallCameraPhoto(data);
			}
			break;
		} // ACTION_TAKE_PHOTO_S

		case ACTION_TAKE_VIDEO: {
			if (resultCode == Activity.RESULT_OK) {
				handleCameraVideo(data);
			}
			break;
		} // ACTION_TAKE_VIDEO
		} // switch
	}
/*
	// Some lifecycle callbacks so that the image can survive orientation change
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
		outState.putParcelable(VIDEO_STORAGE_KEY, mVideoUri);
		outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
		outState.putBoolean(VIDEOVIEW_VISIBILITY_STORAGE_KEY, (mVideoUri != null) );
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
		mVideoUri = savedInstanceState.getParcelable(VIDEO_STORAGE_KEY);
		mImageView.setImageBitmap(mImageBitmap);
		mImageView.setVisibility(
				savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? 
						ImageView.VISIBLE : ImageView.INVISIBLE
		);
		mVideoView.setVideoURI(mVideoUri);
		mVideoView.setVisibility(
				savedInstanceState.getBoolean(VIDEOVIEW_VISIBILITY_STORAGE_KEY) ? 
						ImageView.VISIBLE : ImageView.INVISIBLE
		);
	}
*/
	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
			packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	private void setBtnListenerOrDisable( 
			Button btn, 
			Button.OnClickListener onClickListener,
			String intentName
	) {
		if (isIntentAvailable(activity, intentName)) {
			btn.setOnClickListener(onClickListener);        	
		} else {
			btn.setText( 
				getText(R.string.cannot).toString() + " " + btn.getText());
			btn.setClickable(false);
		}
	}

}