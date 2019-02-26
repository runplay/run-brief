package run.brief.settings;

import run.brief.beans.Account;

public class AccountsManager {



	public static int countEmailAccounts() {
		int count=0;
		for(int i=0; i<AccountsDb.Size();i++) {
			Account acc=AccountsDb.getAccount(i);
			if(acc.getInt(Account.INT_TYPE_)==Account.TYPE_EMAIL) {
				count++;
			}
		}
		return count;
	}
	public static int countAccountType(int TYPE_) {
		int count=0;
		for(int i=0; i<AccountsDb.Size();i++) {
			Account acc=AccountsDb.getAccount(i);
			if(acc.getInt(Account.INT_TYPE_)==TYPE_) {
				count++;
			}
		}
		return count;
	}


}
