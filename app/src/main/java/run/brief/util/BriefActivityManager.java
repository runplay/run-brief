package run.brief.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;

import run.brief.Main;
import run.brief.R;
import run.brief.beans.Person;
import run.brief.contacts.ContactsDb;
import run.brief.d2d.P2PChatFragment;
import run.brief.util.bluetooth.SendFile;
//import run.brief.util.voip._CLIENT;
//import run.brief.util.voip._MAIN;

public class BriefActivityManager {

    public static void closeAndRestartBrief(Activity activity) {
        //Intent mStartActivity = new Intent(activity, Main.class);
        //int mPendingIntentId = 123456;
        //PendingIntent mPendingIntent = PendingIntent.getActivity(activity, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        //AlarmManager mgr = (AlarmManager)activity.getSystemService(Context.ALARM_SERVICE);
        //mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        activity.recreate();//.finish();
        //activity.startActivity(mStartActivity);
        //System.exit(0);
    }






	public static void openBriefApp(Context context) {
	    Intent intent = new Intent(context, Main.class);
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    context.startActivity(intent);
        //context.removeStickyBroadcast(intent);
	}
    public static void openAndroidContactsCreateNew(Activity activity,Person person) {
        //Uri mSelectedContactUri = Contacts.getLookupUri(Sf.toLong(person.getId()), person.getLookupKey());

        // Creates a new Intent to edit a contact
        //Intent editIntent = new Intent(Intent.ACTION_);
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        if(person.getMainPhone()!=null)
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, person.getMainPhone());
        if(person.getMainEmail()!=null)
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, person.getMainEmail());
        intent.putExtra("finishActivityOnSaveCompleted", true);
        activity.startActivity(intent);
    }
	public static void openAndroidContactsCreateNew(Activity activity) {
	    //Uri mSelectedContactUri = Contacts.getLookupUri(Sf.toLong(person.getId()), person.getLookupKey());

	    // Creates a new Intent to edit a contact
	    //Intent editIntent = new Intent(Intent.ACTION_);
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.putExtra("finishActivityOnSaveCompleted", true);
	    activity.startActivity(intent);
	}
	public static void openAndroidContactsWithPerson(Activity activity,Person person) {
	    Uri mSelectedContactUri =
	            Contacts.getLookupUri(Sf.toLong(person.getString(Person.STRING_PERSON_ID)), ContactsDb.getContactLookupKey(activity,person.getString(Person.STRING_PERSON_ID)));

	    // Creates a new Intent to edit a contact
	    Intent editIntent = new Intent(Intent.ACTION_EDIT);
	    editIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    editIntent.putExtra("finishActivityOnSaveCompleted", true);
	    editIntent.setDataAndType(mSelectedContactUri,Contacts.CONTENT_ITEM_TYPE);
	    activity.startActivity(editIntent);
	}

	public static void openBluetoothSendFile(Activity activity) {

	    Intent intent = new Intent(activity, SendFile.class);
	    activity.startActivity(intent);

	}
	public static void openChat(Activity activity) {

	    //Intent intent = new Intent(activity, Login.class);
	    //activity.startActivity(intent);

	}
	public static void openD2d(Activity activity) {

	    Intent intent = new Intent(activity, P2PChatFragment.class);
	    activity.startActivity(intent);

	}
    /*
	public static void openVoipClient(Activity activity) {

	    Intent intent = new Intent(activity, _CLIENT.class);
	    activity.startActivity(intent);

	}
	public static void openVoipServer(Activity activity) {

	    Intent intent = new Intent(activity, _MAIN.class);
	    activity.startActivity(intent);

	}
	*/
	public static void openPhone(Activity activity) {
		Intent intent = new Intent(Intent.ACTION_DIAL);
		activity.startActivity(intent); 
	}
	public static void openPhone(Activity activity, String telephoneNumber) {
		Intent intent = new Intent(Intent.ACTION_DIAL);
		intent.setData(Uri.parse("tel:"+telephoneNumber));
		activity.startActivity(intent); 
	}
	public static void openAndroidSettingsWifi(Activity activity) {
		try {
			activity.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
		} catch(Exception e) {}
	}
	public static void openAndroidBrowserUrl(Activity activity, String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(intent);
	}
	public static void openSkype(Context myContext, String number) {
		  if (isSkypeClientInstalled(myContext)) {
			  Uri skypeUri = Uri.parse("skype:" + number);
			  Intent myIntent = new Intent(Intent.ACTION_VIEW, skypeUri);
			  //myIntent.setData(Uri.parse());
			  // Restrict the Intent to being handled by the Skype for Android client only.
			  myIntent.setComponent(new ComponentName("com.skype.raider", "com.skype.raider.Main"));
			  myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			  // Initiate the Intent. It should never fail because you've already established the
			  // presence of its handler (although there is an extremely minute window where that
			  // handler can go away).
			  myContext.startActivity(myIntent);
		  }
		  return;
	}

	public static boolean isSkypeClientInstalled(Context myContext) {
		  PackageManager myPackageMgr = myContext.getPackageManager();
		  try {
		    myPackageMgr.getPackageInfo("com.skype.raider", PackageManager.GET_ACTIVITIES);
		  } catch (PackageManager.NameNotFoundException e) {
		    return (false);
		  }
		  return (true);
	}
	
	public static boolean isInstagramInstalled(Context myContext) {
		  PackageManager myPackageMgr = myContext.getPackageManager();
		  try {
		    myPackageMgr.getPackageInfo("com.instagram.android", PackageManager.GET_ACTIVITIES);
		  } catch (PackageManager.NameNotFoundException e) {
		    return (false);
		  }
		  return (true);
	}
	public static void openInstagram(Context myContext, Uri imageUri, String textCaption) {
		  if (isInstagramInstalled(myContext)) {

			  
				Intent instagram = new Intent(android.content.Intent.ACTION_SEND);  
				instagram.setType("image/jpg");
				instagram.putExtra(Intent.EXTRA_STREAM, imageUri);
				instagram.putExtra(Intent.EXTRA_TEXT, textCaption);
				instagram.setPackage("com.instagram.android");   
				//instagram.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); //.FLAG_ACTIVITY_NEW_TASK
				myContext.startActivity(instagram);
		  }
	}

	public static boolean isViberClientInstalled(Context myContext) {
		  PackageManager myPackageMgr = myContext.getPackageManager();
		  try {
		    myPackageMgr.getPackageInfo("com.viber.voip", PackageManager.GET_ACTIVITIES);
		  } catch (PackageManager.NameNotFoundException e) {
		    return (false);
		  }
		  return (true);
	}
	public static void openViber(Context myContext, String number) {
		  if (isViberClientInstalled(myContext)) {
			  
			  //String sphone = "12345678";
			  //Uri uri = Uri.parse("tel:" + Uri.encode(sphone)); 
			  //Intent intent = new Intent("android.intent.action.VIEW");
			  //intent.setClassName("com.viber.voip", "com.viber.voip.WelcomeActivity");
			  //intent.setData(uri); 
			  //context.startActivity(intent);
			  
			  
			  Uri url = Uri.parse("tel:" + number);
			  Intent myIntent = new Intent("android.intent.action.VIEW");
			  myIntent.setData(url);
			  // Restrict the Intent to being handled by the Skype for Android client only.
			  myIntent.setClassName("com.viber.voip", "com.viber.voip.WelcomeActivity");
			  //myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			  myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			  // Initiate the Intent. It should never fail because you've already established the
			  // presence of its handler (although there is an extremely minute window where that
			  // handler can go away).
			  myContext.startActivity(myIntent);
		  }
		  return;
	}
	public static void openGmailClient(Context context, String email) {
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		sendIntent.setType("plain/text");
		sendIntent.setData(Uri.parse(email));
		sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");

		sendIntent.putExtra(Intent.EXTRA_TEXT, "\n\n\n\n\n\n\nSent via Brief.ink");
		sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);



		context.startActivity(sendIntent);
	}
	public static boolean isGmailClientInstalled(Context myContext) {
		  PackageManager myPackageMgr = myContext.getPackageManager();
		  try {
		    myPackageMgr.getPackageInfo("com.google.android.gm", PackageManager.GET_ACTIVITIES);
		  } catch (PackageManager.NameNotFoundException e) {
		    return (false);
		  }
		  return (true);
	}
	public static void openNaverLineClientUser(Context context) {
		String appId = "jp.naver.line.android";

		  Intent intent = new Intent();
		  intent.setAction(Intent.ACTION_VIEW);
		  intent.setData(Uri.parse("line://msg/text/yui"));
		  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		  context.startActivity(intent);
	}
	public static void openNaverLineClient(Context context, String phonenumber) {
		
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.setType("plain/text");
		sendIntent.setData(Uri.parse("tel:"+phonenumber));
		sendIntent.setClassName(PACKAGE_NAME, CLASS_NAME);
		sendIntent.putExtra(Intent.EXTRA_TEXT, "");
		sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(sendIntent);
	}
	public static void openNaverLineClientVideo(Context context, File videoFile) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setClassName(PACKAGE_NAME, CLASS_NAME);
		intent.setType("video/mp4");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(videoFile.getPath()));
		//intent.putExtra(Intent.EXTRA_STREAM, 影片路徑);
		intent.putExtra(Intent.EXTRA_TEXT, "Enjoy the Video");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	public static void openNaverLineClient(Context context, File imageFile) {
		/*
		 *
		 * compress file
		 *
		 InputStream iStream = context.getContentResolver().openInputStream(Uri.parse(imageFile.getPath()));
		 ByteArrayOutputStream os = new ByteArrayOutputStream();
		 Bitmap bm = BitmapFactory.decodeStream(iStream);
		 bm.compress(Bitmap.CompressFormat.JPEG, 100, os);
		 os.flush();
		 byte[] w = os.toByteArray();
		 os.close();
		 iStream.close();
		 FileOutputStream out = new FileOutputStream(imageFile.getPath());
		 out.write(w, 0, w.length);
		 out.flush();
		  */
		 Uri uri = Uri.fromFile(new File(imageFile.getPath()));
		  
		 Intent intent = new Intent(Intent.ACTION_SEND);
		  
		 intent.setClassName(PACKAGE_NAME, CLASS_NAME);
		 intent.setType("image/jpeg");
		 intent.putExtra(Intent.EXTRA_STREAM, uri);
		 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 context.startActivity(intent); 
	}
	public static final String PACKAGE_NAME = "jp.naver.line.android";
	public static final String CLASS_NAME = "jp.naver.line.android.activity.selectchat.SelectChatActivity";

	public static boolean isNaverLineClientInstalled(Context myContext) {
		  PackageManager myPackageMgr = myContext.getPackageManager();
		  try {
		    myPackageMgr.getPackageInfo("jp.naver.line.android", PackageManager.GET_ACTIVITIES);
		    /*
		    FeatureInfo[] feat=myPackageMgr.getSystemAvailableFeatures();
		    if(feat!=null) {
		    	for(FeatureInfo f: feat) {
		    		BLog.e("LINE", ""+f.name);
		    	}
		    }
		    */
		  } catch (PackageManager.NameNotFoundException e) {
		    return (false);
		  }
		  return (true);
	}
	public static void shareExternalFile(Context context, String absolutFilePath) {
		if(absolutFilePath!=null) {
			File f = new File(absolutFilePath);
			if(!f.isDirectory()) {
				String ext=Files.getExtension(Files.removeBriefFileExtension(f.getAbsolutePath()));
				ext = ext.replace(".", "");
				String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());

				if (mime != null) {

					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_SEND);
					intent.setDataAndType(Uri.fromFile(f), mime);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					//startActivityForResult(intent, 10);
					//intent.setType("text/plain");
					//intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file) );
					context.startActivity(intent);
				} else {
					Toast.makeText(context, context.getResources().getString(R.string.no_file_format), Toast.LENGTH_SHORT).show();
				}
			}
		}


	}
	public static void shareExternal(Context context, String text, String absolutFilePath) {
		if(absolutFilePath!=null) {
			File f = new File(absolutFilePath);
			if(!f.isDirectory()) {
				String ext=Files.getExtension(Files.removeBriefFileExtension(f.getAbsolutePath()));
				ext = ext.replace(".", "");
				String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());



				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.putExtra(Intent.EXTRA_TEXT, text);

				intent.setDataAndType(Uri.fromFile(f), mime);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);

			}
		}


	}
	public static void shareExternal(Context context, String text) {

		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, Sf.restrictLength(text,30)+"...");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
		context.startActivity(Intent.createChooser(sharingIntent, context.getResources().getString(R.string.share_using)));
		//if (mime != null) {
/*
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_TEXT, text);

			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
*/


	}
}
