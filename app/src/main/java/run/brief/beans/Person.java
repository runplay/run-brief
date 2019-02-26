package run.brief.beans;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import run.brief.R;
import run.brief.contacts.ContactsDb;
import run.brief.util.Cal;
import run.brief.util.json.JSONArray;
import run.brief.util.json.JSONObject;

public class Person extends BJSONBean {
    public static final String LONG_ID ="id";
    public static final String STRING_PERSON_ID ="pid";
	public static final String STRING_NAME ="n";

	public static final String JSONARRAY_PHONE ="p";
	public static final String JSONARRAY_EMAIL ="e";

    public static final String INT_INDEX_USE_PHONE ="iup";
    public static final String INT_INDEX_USE_EMAIL ="iue";


    public static final String LONG_THUMBNAIL_ID="t";

    private Bitmap bitmap;
    private static Bitmap blankIcon;

    private LoadThumbnailTask loadImageTask;

    //public int INDEX_USE_PHONE;
    //public int INDEX_USE_EMAIL;
	
	public static final String NO_ID_START ="n-";
	
	public Person(JSONObject data) {
		super(data);
	}
	public Person() {
		super();
	}

	public static String generateNoId() {
		return NO_ID_START +Cal.getCal().getTimeInMillis();
	}

    public boolean isUnknownPerson() {
        if(getString(Person.STRING_PERSON_ID).startsWith(NO_ID_START))
            return true;
        return false;
    }
    public Person clone() {
        return new Person(this.bean);
    }
    //public long getThumbnailId() {
     //   return getLong(LONG_THUMBNAIL_ID);
    //}
    //public void setThumbnailId(long thumbnailId) {
    //    setLong(LONG_THUMBNAIL_ID,thumbnailId);
    //}
    public boolean hasEmail() {
        if(getJSONArray(JSONARRAY_EMAIL)!=null && getJSONArray(JSONARRAY_EMAIL).length()>0)
            return true;
        return false;
    }
    public String getMainEmail() {
        if(getJSONArray(JSONARRAY_EMAIL)!=null && getJSONArray(JSONARRAY_EMAIL).length()>0)
            return getJSONArray(JSONARRAY_EMAIL).getString(0);
        return null;
    }
    public boolean hasPhone() {
        if(getJSONArray(JSONARRAY_PHONE)!=null && getJSONArray(JSONARRAY_PHONE).length()>0)
            return true;
        return false;
    }
    public String getMainPhone() {
        if(getJSONArray(JSONARRAY_PHONE)!=null && getJSONArray(JSONARRAY_PHONE).length()>0)
            return getJSONArray(JSONARRAY_PHONE).getString(0);
        return null;
    }
    public boolean hasImageThumbnail() {
        if(getLong(LONG_THUMBNAIL_ID)==0)
            return false;
        return true;
    }
    public Bitmap getThumbnail(Context context) {
        if(bitmap==null) {
            if(getLong(LONG_THUMBNAIL_ID)!=0) {
                //BLog.e("IMG", "get thumbnail: " + getLong(LONG_THUMBNAIL_ID));
                ContentResolver cr = context.getContentResolver();
                Uri imguri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, getLong(LONG_THUMBNAIL_ID));
                Cursor imgcursor = cr.query(imguri, PHOTO_BITMAP_PROJECTION, null, null, null);

                try {
                    if (imgcursor.moveToFirst()) {
                        final byte[] thumbnailBytes = imgcursor.getBlob(0);
                        if (thumbnailBytes != null) {
                            Bitmap b = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length);
                            b = Bitmap.createScaledBitmap(b, 120, 120, false);
                            bitmap = b;
                            //BLog.e("IMG", "----GOT thumbnail: " + getLong(LONG_THUMBNAIL_ID));
                        }
                    }
                } catch (Exception e) {
                }
                imgcursor.close();
                cr = null;
                if(bitmap!=null) {
                    return bitmap;
                }
            }
            if(bitmap==null) {
                if (blankIcon == null)
                    blankIcon = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.social_person_contact), 160, 160, false);
                return blankIcon;
            }
        }
        return bitmap;
    }


    private static final String[] PHOTO_BITMAP_PROJECTION = new String[] {
            ContactsContract.CommonDataKinds.Photo.PHOTO
    };
    public static final Bitmap getThumbnailLargeBitmap(Context context, PersonFull personFull) {
        Bitmap b=null;
        if(personFull.getThumbnailId()!=0) {
            ContentResolver cr = context.getContentResolver();
            Uri imguri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, personFull.getThumbnailId());
            Cursor imgcursor = cr.query(imguri, PHOTO_BITMAP_PROJECTION, null, null, null);

            try {
                if (imgcursor.moveToFirst()) {
                    final byte[] thumbnailBytes = imgcursor.getBlob(0);
                    if (thumbnailBytes != null) {
                        b=BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length);

                        //b=Bitmap.createScaledBitmap(b, 400, 400, false);
                        //setThumbnail(b);
                    }
                }
            } catch(Exception e) {}
            imgcursor.close();
            cr=null;
        } else {

        }

        return b;
    }



    public void loadThumbnailAsyncTask(Context context) {
        loadImageTask = new LoadThumbnailTask();
        loadImageTask.setActivity(context);
        loadImageTask.execute(true);
    }
    private class LoadThumbnailTask extends AsyncTask<Boolean, Void, Boolean> {

        private Context context;

        private void setActivity(Context context) {
            this.context=context;
        }
        @Override
        protected Boolean doInBackground(Boolean... params) {
            //BLog.e("POINT", "1");
            if(getLong(LONG_THUMBNAIL_ID)!=0) {
                ContentResolver cr = context.getContentResolver();
                Uri imguri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, getLong(LONG_THUMBNAIL_ID));
                Cursor imgcursor = cr.query(imguri, PHOTO_BITMAP_PROJECTION, null, null, null);

                try {
                    if (imgcursor.moveToFirst()) {
                        final byte[] thumbnailBytes = imgcursor.getBlob(0);
                        if (thumbnailBytes != null) {
                            Bitmap b=BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length);
                            b=Bitmap.createScaledBitmap(b, 120, 120, false);
                            bitmap=b;
                        }
                    }
                } catch(Exception e) {}
                imgcursor.close();
                cr=null;
            }

            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {

        }

    }

    public static Person getNewUnknownPerson(Context context,String number, String email) {
/*
        Person p=null;
        if(number!=null) {
            p=ContactsDb.getWithTelephoneConcatEnd(context,number);
        } else if(email!=null) {
            p=ContactsDb.getWithEmail(email);
        }
        if(p!=null)
            return p;
*/
        Person p = new Person();
        p.setString(Person.STRING_NAME, context.getString(R.string.unknown));

        if(number!=null) {
            p.setJSONArray(Person.JSONARRAY_PHONE,new JSONArray().put(number.trim()));
            p.setString(Person.STRING_NAME, number);
        }
        if(email!=null && email.contains("@")) {
            p.setJSONArray(Person.JSONARRAY_EMAIL,new JSONArray().put(email));
            String ename=null;
            if(email.indexOf("<")!=-1) {
                ename=email.split("<")[0];
            } else {
                ename=email.split("@")[0];
            }
            p.setString(Person.STRING_NAME, ename.replaceAll("\"","").trim());
        }
        //p.setNickname(p.getName());
        p.setString(Person.STRING_PERSON_ID, Person.getNewUknownPersonId(context, number, email));
        ContactsDb.addUnknownPerson(p);
        return p;
    }

    private synchronized static String getNewUknownPersonId(Context context,String number, String email) {

        return Person.NO_ID_START + Cal.getCal().getTimeInMillis();

    }
}
