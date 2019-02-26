package run.brief.email;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import run.brief.b.B;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.BriefHomeFragment;
import run.brief.BriefManager;
import run.brief.b.Device;
import run.brief.R;
import run.brief.beans.Account;
import run.brief.beans.Brief;
import run.brief.beans.Email;
import run.brief.beans.EmailFolder;
import run.brief.beans.SyncData;
import run.brief.secure.Validator;
import run.brief.service.BriefNotify;
import run.brief.service.BriefService;
import run.brief.settings.AccountsDb;
import run.brief.util.Cal;
import run.brief.util.Db;
import run.brief.util.DbField;
import run.brief.util.Sf;
import run.brief.util.log.BLog;

public final class EmailServiceInstance extends Db {
	
	//public static String FOLDER_INBOX="INBOX";
    private static final int FETCH_SIZE=30;

/*
    private long lastOAuthFail;

    public void setLastOauthFail(long date) {
        lastOAuthFail=date;
    }

    public boolean canSendOAuth() {
        if(lastOAuthFail==0)
            return true;
        if(lastOAuthFail+(Cal.MINUTES_1_IN_MILLIS*5)<Cal.getCal().getTimeInMillis())
            return true;
        return false;
    }
*/
	private ArrayList<Email> data=new ArrayList<Email>();
	//private BRefreshable refreshFragment;
	private Context context;
	private Account account;
	//private String currentFolder=Email.FOLDER_INBOX;
	//private long noolderthan=1000*60*60*24*5;
	protected Context getContext() {
        return context;
    }
	private EmailIncomingServer server;

    private CheckEmailSentLatestService checkSentFolder;
    private CheckEmailLatestService checkFolders;
    private CheckEmailHistoryService checkHistory;

	
	private static final DbField[] TABLE_FIELDS={
		new DbField(Email.LONG_ID,DbField.FIELD_TYPE_INT,true,false),
		new DbField(Email.STRING_UUID,DbField.FIELD_TYPE_TEXT,false,true),
        new DbField(Email.STRING_XBRIEFID,DbField.FIELD_TYPE_TEXT),
		new DbField(Email.STRING_FOLDER,DbField.FIELD_TYPE_TEXT),
		new DbField(Email.STRING_FROM,DbField.FIELD_TYPE_TEXT),
		new DbField(Email.STRING_TO,DbField.FIELD_TYPE_TEXT),
		new DbField(Email.STRING_SUBJECT,DbField.FIELD_TYPE_TEXT),
		new DbField(Email.STRING_MESSAGE,DbField.FIELD_TYPE_TEXT),
		new DbField(Email.STRING_MESSAGE_HTML,DbField.FIELD_TYPE_TEXT),
		new DbField(Email.STRING_ATTACHMENTS,DbField.FIELD_TYPE_TEXT),
		new DbField(Email.LONG_DATE,DbField.FIELD_TYPE_INT),
		new DbField(Email.INT_COLLECTED,DbField.FIELD_TYPE_INT),
		new DbField(Email.INT_STATE,DbField.FIELD_TYPE_INT,false,true),
		new DbField(Email.INT_PRIORITY,DbField.FIELD_TYPE_INT),
		new DbField(Email.LONG_MSG_SIZE,DbField.FIELD_TYPE_INT),
        new DbField(Email.INT_DELETED,DbField.FIELD_TYPE_INT)
	};

    public EmailIncomingServer getServer() {
        return server;
    }
    public boolean doConnect(Context context) {
        if(server==null || !server.isConnected()) {
            //BLog.e("STORE","TRY CONNECTING SERVER");
            server = new EmailIncomingServer(this, account);
            //BLog.e("CONNECT","test if forcetry");
            boolean tryConnect=true;
            if(Device.getCONNECTION_TYPE()!=Device.CONNECTION_TYPE_WIFI) {
                BLog.e("CONNECT","3g force try");
                B.forceTryConnection(context);
                //tryConnect=true;
            }
            if(tryConnect) {
                BLog.e("CONNECT","Attempting connection....");
                return server.connect(context);
            }
                //return true;

        } else {
            BLog.e("STORE","Already connected ........");
            return true;
        }
        return false;

    }

    public Account getAccount() {
        return account;
    }

    public String getConnectError() {
        if(server!=null)
            return server.getConnectError();
        return "ERROR-ESCONNECT-234";
    }
    public EmailServiceInstance(Context context,Account account,boolean forceConnect) {
        super(getTableName(account),TABLE_FIELDS,context);


        try {
            this.context=context;
        } catch(Exception e){}

        this.account=account;

        loadEmails();

    }
	public EmailServiceInstance(Context context,Account account) {
		super(getTableName(account),TABLE_FIELDS,context);


		try {
		this.context=context;
		} catch(Exception e){}

		this.account=account;
        loadEmails();

		
	}

	public boolean isConnected() {
		if(server!=null && server.isConnected())
			return true;
		return false;
	}


    public boolean disConnect() {
        if(server!=null) {
            new DisconnectService().execute(true);
            return true;
        }
        return false;
    }
    private class DisconnectService extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {

            server.disConnect();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

        }

    }
	public void loadFolders() {
		new EmailServiceInstance.LoadFoldersService().execute(true);
		
	}
	public ArrayList<EmailFolder> getLoadFolders() {
        if(server==null)
            doConnect(context);
		return server.getFolders();
	}
    public List<String> getFoldersSync() {
        List<String> folders = new ArrayList<>();
        if(account!=null) {
            account.getEmailFolders();
        }
        return folders;
    }
    public List<String> getFoldersOther() {
        List<String> folders = new ArrayList<>();
        if(account!=null) {
            folders=account.getEmailFoldersOther();
        }
        return folders;
    }

	public void clearAllDbData() {
        if(Validator.isValidCaller()) {
            BLog.e("DELETE", "EMAIL DATA");
            db.execSQL("DELETE FROM " + TABLE_NAME);
            db.execSQL("DROP TABLE " + TABLE_NAME);
            db.close();
            data.clear();
            disConnect();

        }
	}
    private long getLastReceivedDate() {
        return getLastReceivedDateFolder(null);
    }
	private long getLastReceivedDateFolder(String foldername) {
		if(!data.isEmpty()) {

            for(int i=0; i<data.size(); i++) {
                if(data.get(i).has(Email.LONG_DATE)) {
                    if(foldername==null || data.get(i).getString(Email.STRING_FOLDER).equals(foldername))
                        return data.get(i).getLong(Email.LONG_DATE);
                }
            }
		}
		//return (new Date()).getTime()-(Cal.HOURS_24_IN_MILLIS);
        return (new Date()).getTime()-(Cal.DAYS_7_IN_MILLIS);
	}
	public long getLastHistoryDate() {
		long lastDate=0;
		Cursor cursor = db.query(TABLE_NAME, getFieldNames(), null, null, null, null, "id ASC LIMIT 1");
        if (cursor != null) {
        	cursor.moveToFirst();
            if (cursor.getCount() > 0) {
            	Email em = getEmailFromCursor(account,cursor);
            	lastDate=em.getLong(Email.LONG_DATE);
            }
            cursor.close();
        }
        if(lastDate==0)
        	lastDate=(new Date()).getTime();
		return lastDate;
        /*
		if(!data.isEmpty()) {
			 if(data.get(0).has(Email.LONG_DATE)) {
				 return data.get(0).getLong(Email.LONG_DATE);
			 }
		}
		return (new Date()).getTime()-Cal.HOURS_24_IN_MILLIS;
        */
	}

	private static String getTableName(Account account) {
		String tablename="noemail";
		if(account!=null && account.has(Account.STRING_EMAIL_ADDRESS)) {
			tablename=account.getString(Account.STRING_EMAIL_ADDRESS);
			tablename = tablename.replaceAll("@", "_");
			//BLog.e("DBTBLE",tablename);
			tablename = tablename.replaceAll("[^_A-Za-z0-9]", "");
			
		}
		//BLog.e("DBTBLE",tablename);
		return tablename;
	}
	
	public ArrayList<Email> getEmails() {
        if(data.isEmpty())
            loadEmails();
		return data;
		
	}
	 
	public Brief getAsBrief(int index) {
		if(data!=null && data.size()>index){
			Brief b=new Brief(context,account,data.get(index),index);
			
			return b;
			
		}
		return null;
	}
	public Email getEmail(int DBindex) {
		if(data!=null && !data.isEmpty() && DBindex<data.size()) {
			return data.get(DBindex);
		}
		return null;
	}
	public Email getEmailById(long emailId) {
		if(data!=null && !data.isEmpty()) {
			for(Email em: data) {
				if(em.getLong(Email.LONG_ID)==emailId)
					return em;
			}
		}
		return null;
		
	}
	public Email getEmail(String uuid) {
		if(data!=null && !data.isEmpty()) {
			for(Email em: data) {
				if(em.getString(Email.STRING_UUID).equals(uuid))
					return em;
			}
		}
		return null;
		
	}
	
	public void fetchLatestEmails(Context cotext) {
		//this.refreshFragment = fromFragment;
        this.context=context;
        checkFolders=new CheckEmailLatestService();
        checkFolders.execute(true);
		//getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent()
		
	}
    public void fetchLatestEmailsSentFolder() {
        //this.refreshFragment = fromFragment;
        checkSentFolder=new CheckEmailSentLatestService();
        checkSentFolder.execute("");
        //getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent()

    }
	public void fetchHistoryEmails(BRefreshable fromFragment) {
		//this.refreshFragment = fromFragment;
        checkHistory=new CheckEmailHistoryService();
        checkHistory.execute("");
		//getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent()
		
	}
    public boolean loadEmailsHistory() {
        //ContentValues values = new ContentValues();
        //values.put(Email.INT_DELETED, 0);
        Cursor cursor = db.query(TABLE_NAME,
                getFieldNames(), Email.INT_DELETED+"=?", new String[]{"0"}, null, null, Email.LONG_DATE+" DESC LIMIT "+(data.size()-1)+","+((data.size()-1)+FETCH_SIZE));
        boolean didload=false;
        if (cursor != null) {
            cursor.moveToFirst();
            //cursor.moveToLast();

            if (cursor.getCount() > 0) {
                do {
                    didload=true;
                    data.add(getEmailFromCursor(account,cursor));

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return didload;
    }
	public void loadEmails() {
        data.clear();
		Cursor cursor = db.query(TABLE_NAME,
				getFieldNames(),  Email.INT_DELETED+"=?", new String[]{"0"}, null, null, Email.LONG_DATE+" DESC LIMIT "+FETCH_SIZE);
		
        if (cursor != null) {
        	cursor.moveToFirst();
            //cursor.moveToLast();
            if (cursor.getCount() > 0) {
                do {
                	
                 	data.add(getEmailFromCursor(account,cursor));
                 	
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
		
	}
    public boolean isEmailAlreadyExist(String uuid) {
        Cursor cursor = db.query(TABLE_NAME,
                getFieldNames(),  Email.STRING_UUID+"=?", new String[]{uuid}, null, null, "id DESC LIMIT 1");
        boolean has=false;
        if (cursor != null) {
            cursor.moveToFirst();
            //cursor.moveToLast();
            if (cursor.getCount() > 0) {
                has=true;
            }
            cursor.close();
        }
        return has;
    }
	
	private static Email getEmailFromCursor(Account account,Cursor cursor) {
       	Email em = new Email();
    	em.setString(Email.STRING_UUID, cursor.getString(cursor.getColumnIndex(Email.STRING_UUID)));
    	em.setString(Email.STRING_TO, cursor.getString(cursor.getColumnIndex(Email.STRING_TO)));
        em.setString(Email.STRING_XBRIEFID, cursor.getString(cursor.getColumnIndex(Email.STRING_XBRIEFID)));
    	em.setString(Email.STRING_FROM, cursor.getString(cursor.getColumnIndex(Email.STRING_FROM)));
    	em.setString(Email.STRING_SUBJECT, cursor.getString(cursor.getColumnIndex(Email.STRING_SUBJECT)));
    	em.setString(Email.STRING_MESSAGE, Sf.cleanEmailText(cursor.getString(cursor.getColumnIndex(Email.STRING_MESSAGE))));
        em.setString(Email.STRING_MESSAGE_HTML, cursor.getString(cursor.getColumnIndex(Email.STRING_MESSAGE_HTML)));
        em.setString(Email.STRING_FOLDER, cursor.getString(cursor.getColumnIndex(Email.STRING_FOLDER)));
    	em.setString(Email.STRING_ATTACHMENTS, cursor.getString(cursor.getColumnIndex(Email.STRING_ATTACHMENTS)));

        //BLog.e("EM","from: "+em.getString(Email.STRING_FROM)+", account: "+account.getString(Account.STRING_EMAIL_ADDRESS));
        if(em.getString(Email.STRING_FROM).equals(account.getString(Account.STRING_EMAIL_ADDRESS))) {
            em.setBoolean(Email.BOOL_IS_MINE_NO_SAVE,true);
        } else {
            em.setBoolean(Email.BOOL_IS_MINE_NO_SAVE,false);
        }
    	
    	em.setLong(Email.LONG_ID, cursor.getLong(cursor.getColumnIndex(Email.LONG_ID)));
    	em.setLong(Email.LONG_DATE, cursor.getLong(cursor.getColumnIndex(Email.LONG_DATE)));
        em.setLong(Email.LONG_MSG_SIZE, cursor.getLong(cursor.getColumnIndex(Email.LONG_MSG_SIZE)));

        em.setInt(Email.INT_PRIORITY, cursor.getInt(cursor.getColumnIndex(Email.INT_PRIORITY)));
    	em.setInt(Email.INT_COLLECTED, cursor.getInt(cursor.getColumnIndex(Email.INT_COLLECTED)));
    	em.setInt(Email.INT_STATE, cursor.getInt(cursor.getColumnIndex(Email.INT_STATE)));
        em.setInt(Email.INT_DELETED, cursor.getInt(cursor.getColumnIndex(Email.INT_DELETED)));

    	return em;
	}

    public Email confirmSentEmail(Email emailData) {
        if(doConnect(context)) {
            long glrd = getLastReceivedDateFolder(account.getSentFolder());

            //BLog.e("DT", "SENT folder no older than: " + (new Date(glrd)).toString());
            List<Email> emails = server.getEmailsFromFolder(context, glrd, account.getSentFolder(),false,false);
            for (int i = emails.size() - 1; i >= 0; i--) {
                Email tmpe=emails.get(i);
                if(sameEmailXBirefCompare(tmpe,emailData)) {
                    return tmpe;
                }
            }
            //SyncData.updateSyncOutJustCompleted(account, SyncData.SYNC_LAST_RESULT_SUCCESS, context.getString(R.string.sync_msg_last_out_sync) + " " + (new Cal()).getDatabaseDate());
        }
        return null;
    }

    private boolean sameEmailXBirefCompare(Email email, Email comare) {

        String to = email.getString(Email.STRING_XBRIEFID);
        String cto = comare.getString(Email.STRING_XBRIEFID);
        if(to!=null && !to.isEmpty()
                && cto!=null && !cto.isEmpty()) {
            if(to.equals(cto))
                return true;
        }
        return false;
    }


    public void deleteEmail(Email email) {
        email.setInt(Email.INT_DELETED,1);

        open();
        ContentValues values = new ContentValues();
        values.put(Email.INT_DELETED, email.getInt(Email.INT_DELETED));


        long id = db.update(TABLE_NAME, values, Email.LONG_ID+"=?", new String[]{""+email.getLong(Email.LONG_ID)});
        for(int i=0; i<data.size(); i++) {
            Email em = data.get(i);
            if(em.getLong(Email.LONG_ID)==email.getLong(Email.LONG_ID)) {
                em=null;
                data.remove(i);
                break;
            }
        }


    }



	public Email addEmail(Email email) {
		open();
		ContentValues values = new ContentValues();
	    values.put(Email.STRING_UUID, email.getString(Email.STRING_UUID));
	    values.put(Email.STRING_FROM, email.getString(Email.STRING_FROM));
	    values.put(Email.STRING_TO, email.getString(Email.STRING_TO));
	    values.put(Email.STRING_SUBJECT, email.getString(Email.STRING_SUBJECT));
	    values.put(Email.STRING_MESSAGE, email.getString(Email.STRING_MESSAGE));
	    values.put(Email.STRING_ATTACHMENTS, email.getString(Email.STRING_ATTACHMENTS));
	    values.put(Email.LONG_DATE, email.getLong(Email.LONG_DATE));
	    values.put(Email.INT_COLLECTED, email.getInt(Email.INT_COLLECTED));
	    values.put(Email.INT_PRIORITY, email.getInt(Email.INT_COLLECTED));
	    values.put(Email.STRING_FOLDER, email.getString(Email.STRING_FOLDER));
        values.put(Email.INT_DELETED, email.getInt(Email.INT_DELETED));
        values.put(Email.STRING_XBRIEFID, email.getString(Email.STRING_XBRIEFID));
	    //Email.
	    long id = db.insert(TABLE_NAME, null, values);

	    email.setLong(Email.LONG_ID, id);
        //BLog.e("EMAIL", "id: "+email.getLong(Email.LONG_ID));
	    return email;
	}



    private class CheckEmailSentLatestService extends AsyncTask<String, Void, String> {

        //Twitter mTwitter = new TwitterFactory().getInstance();
        @Override
        protected String doInBackground(String... params) {

            if(doConnect(context)) {
                long glrd = getLastReceivedDate();

                BLog.e("DT", "SENT folder no older than: " + (new Date(glrd)).toString());
                List<Email> emails = server.getEmailsFromFolder(context, glrd, account.getSentFolder(),true,true);
                for (int i = emails.size() - 1; i >= 0; i--) {
                    Email email = addEmail(emails.get(i));
                    //r(Email email: emails) {
                    if (email.getLong(Email.LONG_ID) != 0) {
                        data.add(0, email);
                    }
                    BriefManager.setDirty(BriefManager.IS_DIRTY_EMAIL);
                }
                SyncData.updateSyncOutJustCompleted(account, SyncData.SYNC_LAST_RESULT_SUCCESS, context.getString(R.string.sync_msg_last_out_sync) + " " + (new Cal()).getDatabaseDate());
            }
            return "";

        }

        @Override
        protected void onPostExecute(String result) {

            Bgo.tryRefreshCurrentFragment();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }

    }
    public void addDataEmail(int pos,Email email) {
        data.add(0, email);
    }
	private class CheckEmailLatestService extends AsyncTask<Boolean, Void, Boolean> {
		
		//Twitter mTwitter = new TwitterFactory().getInstance();
		@Override
		protected Boolean doInBackground(Boolean... params) {
            boolean ok=false;
            BriefService.setAccountActiveSyncing(account.getLong(Account.LONG_ID), true);
            if(doConnect(context)) {

                long glrd = getLastReceivedDate();

                BLog.e("DT", "no older than: " + (new Date(glrd)).toString());
                List<Email> emails = server.readMailAfter(context, glrd,true);
                //boolean added = false;
                for (Email email : emails) {
                    //.get(i);
                    //r(Email email: emails) {
                    //if (email.getLong(Email.LONG_ID) != 0) {

                    //    added = true;
                    if(!email.getString(Email.STRING_FOLDER).equals(account.getSentFolder())) {
                        BriefNotify.addNotifyFor(context, new Brief(context, account, email, 0), true);
                    }
                    ok=true;
                    //}
                    //BriefManager.setDirty(BriefManager.IS_DIRTY_EMAIL);
                }
                SyncData.updateSyncInJustCompleted(account, SyncData.SYNC_LAST_RESULT_SUCCESS, context.getString(R.string.sync_msg_last_in_sync) + " " + (new Cal()).getDatabaseDate());
            } else {
                BLog.e("EMS","DOCONNECT() Failed !!!!!!!!!!!!!");
            }
            BriefService.setAccountActiveSyncing(account.getLong(Account.LONG_ID), false);
		    return ok;
	
		}      
	
		@Override
		protected void onPostExecute(Boolean result) {
            if(result) {
                Bgo.tryRefreshCurrentIfFragment(EmailHomeFragment.class);
                Bgo.tryRefreshCurrentIfFragment(BriefHomeFragment.class);
            }
		}
	
		@Override
		protected void onPreExecute() {
		}
	
		@Override
		protected void onProgressUpdate(Void... values) {
			
		}
	 
	}
    public void checkHistoryNoAsync() {
        if(doConnect(context)) {
            //BLog.e("FROMREM","history remote call2 ");
            long start=getLastHistoryDate();
            List<Email> emails = server.readMailBefore(context,start,start-Cal.DAYS_7_IN_MILLIS ,true);
            if(emails.isEmpty()) {
                BLog.e("READMORE","history try read more");
                emails = server.readMailBefore(context,start-Cal.DAYS_7_IN_MILLIS,start-Cal.DAYS_7_IN_MILLIS*4 ,true);
            }
            for (int i = emails.size() - 1; i >= 0; i--) {
                Email email = addEmail(emails.get(i));
                //r(Email email: emails) {
                //if (email.getLong(Email.LONG_ID) != 0) {
                    data.add(email);
                //}
            }
        }
    }
	private class CheckEmailHistoryService extends AsyncTask<String, Void, String> {
		
		//Twitter mTwitter = new TwitterFactory().getInstance();
		@Override
		protected String doInBackground(String... params) {
            //BLog.e("FROMREM","history remote call1 ");
            if(doConnect(context)) {
                //BLog.e("FROMREM","history remote call2 ");
                long start=getLastHistoryDate();
                List<Email> emails = server.readMailBefore(context,start,start-Cal.DAYS_7_IN_MILLIS ,true);
                if(emails.isEmpty()) {
                    emails = server.readMailBefore(context,start-Cal.DAYS_7_IN_MILLIS,start-Cal.DAYS_7_IN_MILLIS*4 ,true);
                }
                for (int i = emails.size() - 1; i >= 0; i--) {
                    Email email = addEmail(emails.get(i));
                    //r(Email email: emails) {
                    //if (email.getLong(Email.LONG_ID) != 0) {
                        data.add(email);
                    //}
                }
            }
		    return "";
	
		}      
	
		@Override
		protected void onPostExecute(String result) {
			//refreshMail();
            Bgo.tryRefreshCurrentFragment();
			//Bgo.refreshCurrentFragmentIfBrief(activity);
		}
	
		@Override
		protected void onPreExecute() {
		}
	
		@Override
		protected void onProgressUpdate(Void... values) {
			
		}
	 
	}
    public boolean loadFoldersNoAsync() {
        if(doConnect(context)) {

            server.loadFoldersSlim(context);
            updateProcessFolders();
            return true;
        }
        return false;
    }
    private void updateProcessFolders() {
        List<String> syncFolders = account.getEmailFolders();
        List<String> otherFolders = account.getEmailFoldersOther();
        //List<String> addNewOtherFolder = new ArrayList<String>();
        boolean firstTime=true;
        boolean save=false;

        if(!syncFolders.isEmpty())
            firstTime=false;
        if(account.getInt(Account.INT_EMAIL_USE_)==Account.EMAIL_USE_IMAP) {

            List<EmailFolder>  folders = server.getFolders();
            String testInbox = Email.FOLDER_INBOX.toLowerCase();
            String testSent = Email.FOLDER_SENT.toLowerCase();

            if(firstTime) {
                if (account.getInt(Account.INT_EMAIL_INCOMING_SUBTYPE_) == Account.SUBTYPE_GOOGLEMAIL) {
                    syncFolders.add("[Gmail]/Sent Mail");
                    syncFolders.add("INBOX");
                } else {
                    for (EmailFolder folder : folders) {
                        if (testInbox.equals(folder.getString(EmailFolder.STRING_FOLDERNAME).toLowerCase())) {
                            syncFolders.add(folder.getString(EmailFolder.STRING_FOLDERNAME));
                        } else if (testSent.equals(folder.getString(EmailFolder.STRING_FOLDERNAME).toLowerCase())) {
                            syncFolders.add(folder.getString(EmailFolder.STRING_FOLDERNAME));
                        } else {
                            otherFolders.add(folder.getString(EmailFolder.STRING_FOLDERNAME));
                        }
                    }
                }
                save=true;
            } else {

                for(EmailFolder folder: folders) {
                    boolean addfolder=true;
                    for(String f: syncFolders) {
                        if(f.equals(folder.getString(EmailFolder.STRING_FOLDERNAME)))
                            addfolder=false;
                    }
                    for(String f: otherFolders) {
                        if(f.equals(folder.getString(EmailFolder.STRING_FOLDERNAME)))
                            addfolder=false;
                    }
                    if(addfolder) {
                        save=true;
                        otherFolders.add(folder.getString(EmailFolder.STRING_FOLDERNAME));
                    }
                }

            }


        } else if(firstTime) {
             syncFolders.add(Email.FOLDER_INBOX);
            save=true;
        }



        if(save) {
            account.setEmailFolders(syncFolders);
            account.setEmailFoldersOther(otherFolders);
            //BLog.e("SAVE","saving: "+account.toString());
            AccountsDb.updateAccount(account);
        }
    }
	private class LoadFoldersService extends AsyncTask<Boolean, Void, Boolean> {

		//Twitter mTwitter = new TwitterFactory().getInstance();
		public LoadFoldersService() {

		}
        @Override
		protected Boolean doInBackground(Boolean... params) {
            if(doConnect(context)) {

                server.loadFoldersSlim(context);
                updateProcessFolders();
                return true;
            }
            return false;
		}

        @Override
        protected void onPostExecute(Boolean result) {

            //BLog.e("GOT FOLDERs","size: "+server.getLoadFolders().size());
            if(result) {
                Bgo.tryRefreshCurrentFragment();
            }
        }
	 
	}
}
