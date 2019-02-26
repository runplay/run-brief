package run.brief.settings;

import android.app.Activity;

import java.util.ArrayList;

import run.brief.beans.Account;
import run.brief.util.log.BLog;
import run.brief.secure.Validator;
import run.brief.util.FileReadTask;
import run.brief.util.FileWriteTask;
import run.brief.util.Files;
import run.brief.util.json.JSONArray;
import run.brief.util.json.JSONObject;


public final class AccountsDb {
	
	private static final AccountsDb DB = new AccountsDb();
	
	private static final String dbArrayName="accounts";
	
	private ArrayList<Account>data;
	private boolean isLoaded=false;
	
	private static FileWriteTask fwt;
	private static FileReadTask frt;

    public static AccountsDb getDb() {
        if(Validator.isValidCaller())
            return DB;
        return
                null;
    }

	/*
	public static void deleteAllAccounts() {
		DB.data=new ArrayList<Account>();
		Save();
	}
	*/
	public static FileWriteTask getFwt() {
		return fwt;
	}

	public static FileReadTask getFrt() {
		return frt;
	}

	private AccountsDb() {
		//Load();
	}


	public static boolean hasEmailAccounts() {
		return !getAllEmailAccounts().isEmpty();
	}


	public static boolean hasAccounts() {
		return DB.data.isEmpty();
	}
	public static ArrayList<Account> getAllAccounts() {
		return DB.data;
	}
	public static ArrayList<Account> getAllEmailAccounts() {
		ArrayList<Account> emacc=new ArrayList<Account>();
		if(DB.data!=null) {
			for(Account acc: DB.data) {
				int type=acc.getInt(Account.INT_TYPE_);
				if(type==Account.TYPE_EMAIL)
					emacc.add(acc);
			}
		}
		return emacc;
	}
	public static Account getBriefAccount() {
		if(DB.data!=null) {
			for(Account acc: DB.data) {
				if(acc.getInt(Account.INT_TYPE_)==Account.TYPE_BRIEF) {
					return acc;
				}
			}
		}
		return null;
	}
	public static Account getEmailAccount(String emailAddress) {
		//if(!DB.isLoaded)
		//	AccountsDb.init();
		Account emacc=null;
		if(DB.data!=null) {
			for(Account acc: DB.data) {
				int type=acc.getInt(Account.INT_TYPE_);
				String add=acc.getString(Account.STRING_EMAIL_ADDRESS);
				if(type==Account.TYPE_EMAIL && emailAddress.equals(add)) {
					emacc= acc;
									
				}
			}
		}
		return emacc;
	}
	public static int Size() {
		if(DB.isLoaded)
			return DB.data.size();
		else 
			return 0;
	}
	public static Account getAccountById(long id) {
		if(DB.isLoaded) {
			for(Account acc: DB.data) {
				long aid=acc.getLong(Account.LONG_ID);
				//BLog.e("ACCDB", "getbyid: "+aid);
				if(aid==id)
					return acc;
			}
		}
		return null;
	}
	public static Account getAccount(int index) {
		if(DB.isLoaded) {
			Account account = null;
			try {
				account = (Account) DB.data.get(index);
				return account;
			} catch(Exception e) {
				BLog.add("AccountsDB.getAccount() out of index: "+index);
			}
		}
		return null;
	}
	
	public synchronized static boolean updateAccount(Account account) {
		if(DB.isLoaded) {
			long aid=account.getLong(Account.LONG_ID);
			
			for(int i=0; i<DB.data.size(); i++) {
				Account tacc=DB.data.get(i);


				long id=tacc.getLong(Account.LONG_ID);
                //BLog.e("UPDATE",id+"-ACCOUNT-"+aid);
				if(id==aid) {
                    //BLog.e("UPDATE","ACCOUNT");
					DB.data.set(i,account);
					return Save();
				}
			}
		}
		return false;
	}
	public synchronized static int addAccount(Account account) {
		if(DB.isLoaded) {
		try {
			//account.setDate(Cal.getUnixTime());
			DB.data.add(account);
			Save();
			return DB.data.size()-1;
		} catch(Exception e) {
			BLog.add("AccountsDB.addAccount()",e);
		}
		}
		return -1;
	}
	public synchronized static int deleteAccount(Activity activity, Account account) {
        //if(account.get)

		if(DB.isLoaded) {
            try {
                //account.setDate(Cal.getUnixTime());
                DB.data.remove(account);
                Save();
                return DB.data.size()-1;
            } catch(Exception e) {
                //BLog.add("AccountsDB.addAccount()",e);
            }
		}
		return -1;
	}
	public static boolean Save() {
		if(DB.isLoaded) {
            JSONObject db = new JSONObject();
            try {
                JSONArray array = new JSONArray();
                for(Account account: DB.data) {
                    array.put(account.getBean());
                }
                db.put(dbArrayName, array);
                fwt=new FileWriteTask(Files.HOME_PATH_APP, Files.FILENAME_ACCOUNTS, db.toString());
                return fwt.WriteSecureToSd();

            } catch(Exception e) {
                //BLog.e("SAVE",e.getMessage());

            }
		}
		return false;
	}
	public synchronized static void init() {
		
		if(DB.data==null) {
			frt = new FileReadTask(Files.HOME_PATH_APP, Files.FILENAME_ACCOUNTS);
			//String file = Files.ReadFromSd(Files.HOME_PATH, Files.FILENAME_NOTES_DATABASE);     
			if(frt.ReadSecureFromSd()) {
				if(frt.getFileContent()!=null && !frt.getFileContent().isEmpty()) {
				try {
					JSONObject db = new JSONObject(frt.getFileContent());
					if(db!=null) {
						DB.data = new ArrayList<Account>();
						JSONArray accounts = db.getJSONArray(dbArrayName);
						if(accounts!=null) {
							for(int i=0; i<accounts.length(); i++) {
								DB.data.add(new Account(accounts.getJSONObject(i)));
							}
						}
						DB.isLoaded=true;
					}
				} catch(Exception e) {
					if(e.getMessage()!=null)
						BLog.e("AccountsDb.init()",e.getMessage());
				}
				} else {
					BLog.e("AccountsDb.init().empty",frt.getStatusMessage());
				}
			} else {
				BLog.e("AccountsDb.init().no read",frt.getStatusMessage());
			}
		}
		if(!DB.isLoaded) {
			DB.data = new ArrayList<Account>();
			DB.isLoaded=true;
			Save();
		}
	}
}
