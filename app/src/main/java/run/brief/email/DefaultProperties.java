package run.brief.email;

import run.brief.beans.Account;

public final class DefaultProperties {

    public static boolean isGmail(String email) {
        if(email.contains("@gmail.") || email.contains("@googlemail."))
            return true;
        else
            return false;
    }
	public static Account getDetectAccountFromEmail(String emailAddress) {
		Account account=null;
		if(emailAddress!=null && emailAddress.contains("@")) {
			String em=emailAddress.split("@")[1];
			if(em.startsWith("gmail.") || em.startsWith("googlemail."))
				return makeGmail();
			else if(em.startsWith("yahoo."))
				return makeYahooMail();
			else if(em.startsWith("hotmail.") || em.startsWith("live.") || em.startsWith("outlook."))
				return makeMicrosoftMail();
		}
		return account;
	}
	
	public static Account makeMicrosoftMail() {
		Account account = new Account(Account.TYPE_EMAIL);
		account.setInt(Account.INT_EMAIL_USE_, Account.EMAIL_USE_IMAP);
		account.setInt(Account.INT_EMAIL_OUTGOING_PORT, 587);
		account.setInt(Account.INT_EMAIL_INCOMING_PORT, 993);
        account.setString(Account.STRING_EMAIL_OUTGOING_SERVER, "smtp-mail.outlook.com");
        account.setString(Account.STRING_EMAIL_INCOMING_SERVER, "imap-mail.outlook.com");
		account.setInt(Account.INT_EMAIL_INCOMING_SUBTYPE_, Account.SUBTYPE_SSL);
		account.setInt(Account.INT_EMAIL_OUTGOING_SUBTYPE_, Account.SUBTYPE_TLS);
		return account;
	}

	public static Account makeGmail() {
		Account account = new Account(Account.TYPE_EMAIL);
		account.setInt(Account.INT_EMAIL_USE_, Account.EMAIL_USE_IMAP);
		account.setInt(Account.INT_EMAIL_OUTGOING_PORT, 465);
		account.setInt(Account.INT_EMAIL_INCOMING_PORT, 993);
		account.setString(Account.STRING_EMAIL_OUTGOING_SERVER, "smtp.gmail.com");
		account.setString(Account.STRING_EMAIL_INCOMING_SERVER, "imap.gmail.com");
		account.setInt(Account.INT_EMAIL_INCOMING_SUBTYPE_, Account.SUBTYPE_SSL);
		account.setInt(Account.INT_EMAIL_OUTGOING_SUBTYPE_, Account.SUBTYPE_SSL);

		return account;
	}
    public static Account makeGmailOAuth() {
        Account account = new Account(Account.TYPE_EMAIL);
        account.setInt(Account.INT_EMAIL_USE_, Account.EMAIL_USE_IMAP);
        account.setInt(Account.INT_EMAIL_OUTGOING_PORT, 587);
        account.setInt(Account.INT_EMAIL_INCOMING_PORT, 993);
        account.setString(Account.STRING_EMAIL_OUTGOING_SERVER, "smtp.gmail.com");
        account.setString(Account.STRING_EMAIL_INCOMING_SERVER, "imap.gmail.com");
        account.setInt(Account.INT_EMAIL_INCOMING_SUBTYPE_, Account.SUBTYPE_SSL);
        account.setInt(Account.INT_EMAIL_OUTGOING_SUBTYPE_, Account.SUBTYPE_SSL);

        return account;
    }
	public static Account makeYahooMail() {
		Account account = new Account(Account.TYPE_EMAIL);
		account.setInt(Account.INT_EMAIL_USE_, Account.EMAIL_USE_IMAP);
		account.setInt(Account.INT_EMAIL_OUTGOING_PORT, 465);
		account.setInt(Account.INT_EMAIL_INCOMING_PORT, 993);
		account.setString(Account.STRING_EMAIL_OUTGOING_SERVER, "smtp.mail.yahoo.com");
		account.setString(Account.STRING_EMAIL_INCOMING_SERVER, "imap.mail.yahoo.com");
		account.setInt(Account.INT_EMAIL_INCOMING_SUBTYPE_, Account.SUBTYPE_SSL);
		account.setInt(Account.INT_EMAIL_OUTGOING_SUBTYPE_, Account.SUBTYPE_SSL);
		return account;
	}
	/*
	 * 
	 * 
POP3

Server: pop3.live.com
SSL: true-implicit
Port: 995 (default)
User: pat@hotmail.com
SMTP

Server: smtp.live.com
SSL: true-implicit / true-explicit
Port: 465 (default) / 587 (default)
User: pat@hotmail.com



    Incoming Server: imap.mail.yahoo.com
    Outgoing Server: smtp.mail.yahoo.com
    Incoming Port: 993 (requires SSL)
    Outgoing Port: 465 (requires SSL/TLS)
    Username: full email address (for example, bill@yahoo.com or bill@rocketmail.com)
    Password: the password you login to Yahoo! with.

	 * 
	 */
}
