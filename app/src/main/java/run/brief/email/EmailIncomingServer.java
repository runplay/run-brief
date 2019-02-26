package run.brief.email;


import android.content.Context;

import com.sun.mail.imap.IMAPFolder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

import run.brief.b.B;
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
import run.brief.service.BriefNotify;
import run.brief.service.BriefService;
import run.brief.settings.AccountsDb;
import run.brief.settings.OAuth.OAuth2Provider;
import run.brief.settings.OAuth.OAuth2SaslClient;
import run.brief.settings.OAuth.OAuth2SaslClientFactory;
import run.brief.util.Cal;
import run.brief.util.Files;
import run.brief.util.Sf;
import run.brief.util.TextFile;
import run.brief.util.log.BLog;

public final class EmailIncomingServer extends javax.mail.Authenticator {

    private String user; 
    private String password; 
    private Session session; 
    private Store store = null;
    private Properties props;
    private Account account;
    
    private boolean connected;
    private String connectError;
    private String stored;
    private ArrayList<EmailFolder> folders=new ArrayList<EmailFolder>();

    
    //private static final String FOLDER_INBOX="INBOX";
    private String usefolder=Email.FOLDER_INBOX;
    //Message[] msgs;
    //javax.activation.DataHandler handle;
    private static final int LOAD_ITEMS=10;


    private EmailServiceInstance parentEmailService;
    
    private static final String attachmentPath=Files.HOME_PATH_FILES+File.separator+Files.FOLDER_EMAIL_ATTACHMENTS+File.separator;
    
	public EmailIncomingServer(EmailServiceInstance parent, Account account) {
		super();
        this.account=account;
        this.parentEmailService=parent;
		this.user=account.getString(Account.STRING_LOGIN_NAME);
		this.password=account.getString(Account.STRING_LOGIN_PASSWORD);
		createImapProperties(account);
		session = Session.getDefaultInstance(props, this); 

	}
	public ArrayList<EmailFolder> getFolders() {
		return folders;
	}
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }
    public boolean isConnected() {
        if(store!=null && store.isConnected())
            return true;
        else
            return false;

    }
    public String getConnectError() {
    	return connectError;
    }

    public boolean disConnect() {
        if(store!=null && store.isConnected()) {
            try {
                store.close();
            } catch (Exception e) {}
        }
        store=null;
        session=null;
        return true;
    }

    private OAuth2Provider o2auth;

    //private IMAPSaslAuthenticator2 auth;
    protected boolean connect(Context context) {
        connectError=null;
        BLog.e("EMSC","connect()");
        if(store==null || !store.isConnected()) {
        	//try {
                BLog.e("EMSC","Connect google: "+(account.getInt(Account.INT_EMAIL_INCOMING_SUBTYPE_)==Account.SUBTYPE_GOOGLEMAIL));
                if(account.getInt(Account.INT_EMAIL_INCOMING_SUBTYPE_)==Account.SUBTYPE_GOOGLEMAIL) {
                    if(o2auth==null) {
                        o2auth=new OAuth2Provider();
                    } else {
                        Security.removeProvider(o2auth.getName());
                        //o2auth=new OAuth2Provider();
                    }

                    if(Device.getCONNECTION_TYPE()==Device.CONNECTION_TYPE_NONE) {
                        B.forceTryConnection(context);
                    }

                    //BLog.e("EMAIL_CONNECT", "2-");

                    //
                    //    ?? why using CODE, should be token...
                    //
                    String token = account.getTokenNoAsync(); //account.getString(Account.STRING_OAUTH_CODE);//OAuthHelper.fetchToken(context, parentEmailService, new OAuthHelper.Backoff());
                    //alreadyToken=true;


                    OAuth2SaslClientFactory.setToken(token);
                    OAuth2SaslClient.setToken(token);
                    Security.addProvider(o2auth);
                    //BLog.e("EMAIL_CONNECT", "3-");
                    //BLog.e("EMSC","Connect google: "+account.getString(Account.STRING_EMAIL_ADDRESS)+" -- is token null? :   =" +token);
                    if (token!=null) {


                        Session session = Session.getInstance(props);
                        session.setDebug(true);

/*
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        PrintStream ps = new PrintStream(os);
                        session.setDebugOut(ps);
*/
                        //BLog.e("EMAIL_CONNECT", "4-");
                        try {

                            String prot = props.getProperty("mail.transport.protocol", null);
                            store = session.getStore(prot);
                            //BLog.e("EMSC","Connect google style2: "+prot);
                            store.connect(account.getString(Account.STRING_EMAIL_INCOMING_SERVER), account.getInt(Account.INT_EMAIL_INCOMING_PORT), user, token);
                            //BLog.e("EMSC","Connect google style2222222222222222");

                        } catch (MessagingException e) {
                            connectError = e.getMessage();
                            SyncData.updateSyncInJustCompleted(account, SyncData.SYNC_LAST_RESULT_FAIL, "OAuth-a error: " + e.getMessage());
                            account.setInt(Account.INT_SIGNATURE_REKEY,1);
                            AccountsDb.updateAccount(account);
                            //BLog.e("EMAIL_CONNECT", "1-" + e.getMessage());
                        } catch (Exception e) {
                            connectError = e.getMessage();
                            SyncData.updateSyncInJustCompleted(account, SyncData.SYNC_LAST_RESULT_FAIL, "OAuth-b error: " + e.getMessage());
                            //BLog.e("EMAIL_CONNECT", "2-" + e.getMessage());
                        }
/*
                        try {
                            if (ps != null) {
                                String output = os.toString("UTF8");
                                BLog.e("OUT", "PW: " + output);
                                ps.close();
                            }
                            if (os != null)
                                os.close();

                        } catch (Exception e) {}
*/
                        if(store!=null) {
                            connected = store.isConnected();
                            if(account.getInt(Account.INT_OAUTH_TOKEN_FAILS)>0) {
                                account.setInt(Account.INT_OAUTH_TOKEN_FAILS,0);
                                //account.setString(Account.STRING_ACCESS_TOKEN,token);
                                AccountsDb.updateAccount(account);
                            }
                            if(connected) {
                                //if(account.getInt(Account.INT_EMAIL_INCOMING_SUBTYPE_)==Account.EMAIL_USE_IMAP) {
                                    //newMessageListen.run();
                                //}
                            }
                        }
                    } else {
                        SyncData.updateSyncInJustCompleted(account, SyncData.SYNC_LAST_RESULT_FAIL, context.getString(R.string.email_oauth_fail));
                        account.incrementInt(Account.INT_OAUTH_TOKEN_FAILS);
                        connectError="ERR_G1201: Could not get a permission token";
                        if(Device.getCONNECTION_TYPE()==Device.CONNECTION_TYPE_WIFI) {
                            account.setInt(Account.INT_OAUTH_TOKEN_FAILS,account.getInt(Account.INT_OAUTH_TOKEN_FAILS)+1);
                            AccountsDb.updateAccount(account);
                        }
                        //BLog.e("EMAIL_CONNECT", "865:" + connectError);
                    }

                } else {
                    try {
                        store=session.getStore(stored);
                        store.connect(account.getString(Account.STRING_EMAIL_INCOMING_SERVER), user, password);
                    } catch(Exception e) {
                        connectError=e.getMessage();
                        SyncData.updateSyncInJustCompleted(account, SyncData.SYNC_LAST_RESULT_FAIL, "Error: "+e.getMessage());
                        //BLog.e("EMAIL_CONNECT", "86:" + e.getMessage());
                    }

                }
            if(store!=null)
                connected= store.isConnected();


/*
                if(connected) {
                    SyncData.updateSyncInJustCompleted(account, SyncData.SYNC_LAST_RESULT_SUCCESS, context.getString(R.string.sync_msg_last_in_sync) + " " + (new Cal()).getDatabaseDate());
                }
            */
            BLog.e("STORE","connected: "+connected+"-- "+connectError);

            return connected;

        } else {
            BLog.e("STORE","Already connected return true");
            return true;
        }
        //return false;
        
    }
    private Runnable newMessageListen = new Runnable() {
        @Override
        public void run() {
            try {
                //BLog.e("RECEVIED","go listen");
                //IMAPSSLStore useStore = (IMAPSSLStore) store;
                IMAPFolder inbox = (IMAPFolder) store.getFolder(account.getInboxFolder());
                inbox.open(Folder.READ_ONLY);
                inbox.addMessageCountListener(new MessageCountListener() {
                    @Override
                    public void messagesRemoved(MessageCountEvent arg0) {
                    }

                    @Override
                    public void messagesAdded(MessageCountEvent arg0) {
                        //BLog.e("RECEVIED","NEW MESSAGE ADDED");
                        try {
                            Message[] newMessages = arg0.getMessages();
                            if (parentEmailService != null && parentEmailService.getContext()!=null) {
                                for (int i = 0; i < newMessages.length; i++) {
                                    Email msg = MailMessageToEmail(newMessages[i], true, true);
                                    parentEmailService.addEmail(msg);
                                    parentEmailService.addDataEmail(i, msg);
                                    BriefNotify.addNotifyFor(parentEmailService.getContext(), new Brief(parentEmailService.getContext(), account, msg, 0), true);
                                    Bgo.tryRefreshCurrentFragment();
                                }

                            }
                        } catch (Exception e) {
                            BLog.e("CALLBACK","error: "+e.getMessage());
                        }
                        //callBackTest(newMessages);

                    }
                });
                inbox.addConnectionListener(new ConnectionListener() {
                    public void opened(ConnectionEvent e) {
                        // System.out.println("Opened !!");
                    }

                    public void disconnected(ConnectionEvent e) {
                        // System.out.println("Disconnected !!");
                    }

                    public void closed(ConnectionEvent e) {
                        // System.out.println("Closed !!");
                        // Another place to handle reconnecting
                    }
                });
                while (true) {
                    inbox.idle();
                }
            } catch(Exception e) {
                BLog.e("CALLBACK","error2: "+e.getMessage());
            }
        }
    };
/*


    inbox.addMessageCountListener(new MessageCountListener()
    {
        @Override
        public void messagesRemoved(MessageCountEvent arg0) {
    }

        @Override
        public void messagesAdded(MessageCountEvent arg0) {
        Message[] newMessages = arg0.getMessages();
        callBackTest(newMessages);

    }
    });
}
    private static void callBackTest(Message[] messages) {

        for (Message message : messages) {
            try {
                System.out.println("subject--->> " + message.getSubject());
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

    }




    */
    //start googlemail

    //private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

            //"https://www.googleapis.com/auth/plus.login";



    // end googlemail


    private void setFolder(String folder) {
    	this.usefolder=folder;
    }
    private String geFolder() {
    	return this.usefolder;
    }

    private void addGmailExtraProperties() {
        props.put("mail.imap.sasl.enable", "true");
        props.put("mail.imap.sasl.mechanisms", "XOAUTH2");
        props.put("mail.imap.auth.login.disable", "true");
        props.put("mail.imap.auth.plain.disable", "true");
    }
    private Properties createImapProperties(Account account) {
    	
    	props = new Properties();
    	int subtype=account.getInt(Account.INT_EMAIL_INCOMING_SUBTYPE_);
    	//Log.e("createImap",type+"-loading properties");
    	int usetype=account.getInt(Account.INT_EMAIL_USE_);
    	if(usetype==Account.EMAIL_USE_IMAP) {
            if(subtype==Account.SUBTYPE_GOOGLEMAIL) {

                stored="imap";
                props.put("mail.transport.protocol", "imap");
                props.put("mail.store.protocol", "imap");
                props.put("mail.imap.host", account.getString(Account.STRING_EMAIL_INCOMING_SERVER));
                //props.put("mail.imap.auth", "true");
                //props.put("mail.imap.starttls.enable", true);
                props.put("mail.imap.port", account.getInt(Account.INT_EMAIL_INCOMING_PORT));

                props.put("mail.imap.ssl.enable", "true");
                props.put("mail.imap.socketFactory.port", account.getInt(Account.INT_EMAIL_INCOMING_PORT));
                props.put("mail.imap.socketFactory.class", "run.brief.email.AuthSSLSocketFactory");
                //props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

                //props.put("mail.imap.socketFactory.fallback", true);
                //props.put("mail.imap.timeout", "900000");
                props.put("mail.imap.timeout", "20000");
                //props.put("mail.imaps.quitwait", true);

                props.put("mail.imap.sasl.enable", "true");
                props.put("mail.imap.sasl.mechanisms", "XOAUTH2");


                props.setProperty("mail.imap.auth.login.disable", "true");
                props.setProperty("mail.imap.auth.plain.disable", "true");
                props.setProperty("mail.imaps.auth.plain.disable", "true");
                props.setProperty("mail.imap.auth.ntlm.disable", "true");


            } else if(subtype==Account.SUBTYPE_SSL) {
                stored="imaps";
                //BLog.e("IMAP","loading SSL properties");

                props.put("mail.transport.protocol", "imaps");
                props.put("mail.store.protocol", "imaps");
                props.put("mail.imaps.host", account.getString(Account.STRING_EMAIL_INCOMING_SERVER));
                props.put("mail.imaps.auth", true);
                props.put("mail.imap.starttls.enable", true);
                props.put("mail.imaps.port", account.getInt(Account.INT_EMAIL_INCOMING_PORT));
                props.put("mail.imaps.socketFactory.port", account.getInt(Account.INT_EMAIL_INCOMING_PORT));

                props.put("mail.imaps.socketFactory.class", "run.brief.email.AuthSSLSocketFactory");
                //props.put("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

                props.put("mail.imaps.socketFactory.fallback", "false");
                props.put("mail.imap.timeout", "20000");
                props.setProperty("mail.imaps.quitwait", "false");

            } else if(subtype==Account.SUBTYPE_NONE) {
                stored="imap";
                //BLog.e("IMAP","loading generalproperties");

                props.put("mail.transport.protocol", "imap");
                props.put("mail.store.protocol", "imap");
                props.put("mail.imap.partialfetch", true);

                props.put("mail.imap.host", account.getString(Account.STRING_EMAIL_INCOMING_SERVER));
                props.put("mail.imap.port", Integer.toString(account.getInt(Account.INT_EMAIL_INCOMING_PORT)));

                props.put("mail.imap.usr", account.getString(Account.STRING_LOGIN_NAME));
                props.put("mail.imaps.quitwait", "false");
                props.put("mail.imap.timeout", "20000");

            }  else if(subtype==Account.SUBTYPE_TLS) {
                stored="imaps";
                //BLog.e("IMAP","loading TLS properties");

                props.put("mail.transport.protocol", "imaps");
                props.put("mail.store.protocol", "imaps");
                props.put("mail.imaps.host", account.getString(Account.STRING_EMAIL_INCOMING_SERVER));
                props.put("mail.imaps.auth", true);
                props.put("mail.imaps.port", Integer.toString(account.getInt(Account.INT_EMAIL_INCOMING_PORT)));
                props.put("mail.imaps.socketFactory.port", Integer.toString(account.getInt(Account.INT_EMAIL_INCOMING_PORT)));
                //props.put("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.imaps.socketFactory.class", "run.brief.email.AuthSSLSocketFactory");
                props.put("mail.imaps.starttls.enable", true);
                props.put("mail.imap.starttls.enable", true);
                props.put("mail.imaps.socketFactory.fallback", "false");
                props.put("mail.imaps.timeout", "20000");
            }
    	
    	
    	} else {
            //BLog.e("POP","loading POP properties");
            stored="pop3";
    		// POP server
            props.put("mail.pop3.host", account.getString(Account.STRING_EMAIL_INCOMING_SERVER)); 
            props.put("mail.pop3.port", Integer.toString(account.getInt(Account.INT_EMAIL_INCOMING_PORT))); 

            props.put("mail.pop3.timeout", "20000");
            
            //props.put("mail.imap.partialfetch", true);
            props.put("mail.transport.protocol", "pop3");
            props.put("mail.store.protocol", "pop3");
            //props.put("mail.imap.partialfetch", true);
    		
    		
    		
    	}

        //props.put("mail.pop3.connectiontimeout", "900000");
        //props.put("mail.imap.connectiontimeout", "900000");
        //props.put("mail.imaps.connectiontimeout", "900000");

        this.user=account.getString(Account.STRING_LOGIN_NAME);
        this.password=account.getString(Account.STRING_LOGIN_PASSWORD);
        return props;
    }


    private String getDefaultFolderName(String Email_FOLDER_) {
        if(account!=null) {
            List<String> folders=account.getEmailFolders();
            for(String folder: folders) {
                if(Email_FOLDER_.toLowerCase().equals(folder.toLowerCase()))
                    return folder;
            }
        }
        return "none";
    }


    public synchronized List<Email> readMailAfter(Context context,long lastReceivedDate,boolean collectAttachments) {
    	List<Email> emails=new ArrayList<Email>();
        if(account!=null) {
            List<String> folders = account.getEmailFolders();
            for(String folder: folders) {
                emails.addAll(getEmailsFromFolder(context,lastReceivedDate, folder,collectAttachments,true));
            }

        }
        return emails;
    } 

    public class EmailResult {
        public boolean sucess=false;
        List<Email> emails=new ArrayList<Email>();
    }
    public List<Email> getEmailsFromFolder(Context context,long lastReceivedDate, String foldername, boolean collectAttachments, boolean addToDabase) {
        List<Email> emails=new ArrayList<Email>();
        try {

            connect(context);

            if(isConnected()) {
                SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GT, new Date(lastReceivedDate));

                //BLog.e("GETEMAIL", "GET EMAIL FOLDER: " + foldername + " -- " + newerThan.toString());

                Message[] msgs = null;
                Folder folder = store.getFolder(foldername);
                //BLog.e("GETEMAIL", "GET EMAIL 2");

                folder.open(Folder.READ_ONLY);
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                fp.add(FetchProfile.Item.FLAGS);

                //BLog.e("GETEMAIL", "GET EMAIL 3");

                int size = folder.getMessageCount();
                int startSize = size - LOAD_ITEMS;
                if (startSize < 0)
                    startSize = 0;

                //BLog.e("EMAIL","Read emails since:  "+(new Cal(lastReceivedDate)).friendlyReadDate());

                msgs = folder.search(newerThan); //folder.getMessages(startSize, size);


                EmailServiceInstance ems = EmailService.getService(context,account);
                boolean dirtyEmailIssueConnection=false;
                if (msgs != null && msgs.length > 0) {
                    //BLog.e("EMAIL","total emails to collect:  "+msgs.length);
                    for (int i = 0; i <msgs.length; i++) {
                        //BLog.e("EM_MSG", "with size: "+msgs[i].getSize());

                        String uuid=getMessageId(msgs[i]);
                        boolean exists = ems.isEmailAlreadyExist(uuid);
                        if(!exists && !dirtyEmailIssueConnection) {

                            Email email = MailMessageToEmail(msgs[i], true,collectAttachments);

                            if(email.getString(Email.STRING_FROM).length()>1 || email.getString(Email.STRING_TO).length()>1) {



                                email.setString(Email.STRING_FOLDER, foldername);
                                //if (email.has(Email.LONG_DATE) && email.getLong(Email.LONG_DATE) > lastReceivedDate) {
                                BLog.e("NEW", "NEW received: " + uuid);
                                if (addToDabase) {
                                    Email nemail = parentEmailService.addEmail(email);
                                    parentEmailService.addDataEmail(0, nemail);
                                    emails.add(nemail);
                                } else {
                                    emails.add(email);
                                }

                                if (BriefService.isAppStarted()) {
                                    BriefManager.setDirty(BriefManager.IS_DIRTY_EMAIL);

                                    Bgo.tryRefreshCurrentIfFragment(EmailHomeFragment.class);
                                }

                            } else {
                                // makes sure has to field at least, this irradicates connection cutoffs and saving emails.
                                dirtyEmailIssueConnection=true;
                            }
                              //} else {
                            //    break;
                            //}
                        }
                    }
                    BriefManager.setDirty(BriefManager.IS_DIRTY_EMAIL);
                    Bgo.refreshDataCurrentIfFragment(BriefHomeFragment.class);
                } else {
                    //BLog.e("EMAIL","No messages to collect");
                }


                folder.close(true);

                //BLog.e("EMAIL", "collected message count: " + emails.size());

            }
        } catch (MessagingException e) {
               // BLog.e("EMAIL:E ", "6: "+e.getMessage());
            //return null;
        }
        return emails;
    }

    public void loadFolders(Context context) {

    	connect(context);
        if(isConnected()) {
            folders.clear();
            try {
                Folder[] folder = store.getDefaultFolder().list();
                for (Folder f : folder) {
                    folders.add(new EmailFolder(f.getName(), f.getMessageCount(), f.getUnreadMessageCount(), f.getNewMessageCount(),false));
                }

            } catch (Exception e) {
            }

        }
    }
    public void loadFoldersSlim(Context context) {

        connect(context);
        if(isConnected()) {
            folders.clear();
            try {
                Folder[] folder = store.getDefaultFolder().list();
                for (Folder f : folder) {
                    folders.add(new EmailFolder(f.getName(), 0, 0, 0,false));
                }
                //BLog.e("ERROR","LOAD FOLDER size: "+folder.length);
            } catch (Exception e) {
                //BLog.e("ERROR","LOAD FOLDER: "+e.getMessage());
            }
        }

    }
    private synchronized List<Email> loadEmailsFromFolderHistory(Context context,long lastReceivedDate, long toPastDate, String foldername,boolean collectAttachments) {
        ArrayList<Email> emails=new ArrayList<Email>();
        try {
            connect(context);
            if(isConnected()) {

                //long somePastPlusDate = lastReceivedDate - Cal.DAYS_7_IN_MILLIS;

                SearchTerm olderThan = new ReceivedDateTerm(ComparisonTerm.LT, new Date(lastReceivedDate));
                SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GT, new Date(toPastDate));
                SearchTerm andTerm = new AndTerm(olderThan, newerThan);
                //BLog.e("FROMREM","history termr: "+andTerm.toString());
                Message[] msgs = null;

                Folder folder = store.getFolder(foldername);

                folder.open(Folder.READ_ONLY);
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                fp.add(FetchProfile.Item.FLAGS);


                msgs = folder.search(andTerm);
                EmailServiceInstance ems = EmailService.getService(context,account);
                if (msgs != null && msgs.length > 0) {
                    BLog.e("EMAIL","total emails to collect:  "+msgs.length);
                    //for (int i = msgs.length - 1; i >= 0; i--) {
                    for(int i=0; i<msgs.length; i++) {

                        String uuid=getMessageId(msgs[i]);
                        //BLog.e("EM_MSG",uuid+ " with size: "+msgs[i].getSize());
                        if(uuid!=null && ems.getEmail(uuid)==null) {
                            Email email = MailMessageToEmail(msgs[i], true,collectAttachments);
                            email.setString(Email.STRING_FOLDER, foldername);
                            //if (email.has(Email.LONG_DATE) && email.getLong(Email.LONG_DATE) > lastReceivedDate) {
                            emails.add(email);
                            parentEmailService.addEmail(email);

                        }
                    }

                } else {
                    //BLog.e("EMAIL","No messages to collect");
                }


                folder.close(true);

                //BLog.e("EMAIL", "collected message count: " + emails.size());
            }
        } catch (Exception e) {
            //BLog.e("EMAIL:E", e.getMessage());
        }
        return emails;
    }

    public synchronized List<Email> readMailBefore(Context context,long lastReceivedDate, long toPastDate, boolean collectAttachments) {
        //BLog.e("FROMREM","history rmb: ");
        //BLog.e("FROMREM","history remote account "+account.toString());
        List<Email> emails=new ArrayList<Email>();
        if(account!=null) {
            List<String> folders = account.getEmailFolders();
            for(String folder: folders) {
                //BLog.e("FROMREM","history remote call1: "+folder);
                emails.addAll(loadEmailsFromFolderHistory(context, lastReceivedDate, toPastDate, folder,collectAttachments));
            }
        }
        return emails;


    } 
    
 
    private Email MailMessageToEmail(Message msg, boolean collectBody, boolean collectAttachments) {
    	Email em = new Email();
    	em.setString(Email.STRING_UUID, getMessageId(msg)); 
    	em.setString(Email.STRING_TO, getRecipents(msg));
    	em.setString(Email.STRING_FROM, getSenders(msg));
    	em.setString(Email.STRING_SUBJECT, getSubject(msg));
    	em.setLong(Email.LONG_DATE, getDate(msg));
        em.setString(Email.STRING_XBRIEFID,EmailService.getXBriefId(msg));
    	//msg.
    	Object content = null;  
        //Multipart mp = null;
        try {
        	//BLog.e("EM","trying");
        	content=msg.getContent();
        } catch(MessagingException e) {
        	//BLog.e("MSGE", "Me ex: "+e.getMessage());
        } catch(IOException e) {
        	//BLog.e("MSGE", "IOe : "+e.getMessage());
        }
        if(content!=null) {

            if (content instanceof String) {
                //
                //
                //BLog.e("MailMessageToEmail", "********** String content: "+ em.getString(Email.STRING_UUID)+": string content: "+ content.toString());


                // this is a text file.....
                em.setString(Email.STRING_MESSAGE,content.toString());
            } else if (content instanceof Multipart) {

                Multipart mp=(Multipart) content;
                //BLog.e("MailMessageToEmail", "********** Multipart content: "+em.getString(Email.STRING_UUID));
                processMultiPartForEmail(mp,em,true,collectAttachments);

            } else if (content instanceof Message) {
                //BLog.e("MIMessage", content.toString());
            } else if (content instanceof InputStream) {
                //BLog.e("MIInputStream", content.toString());
            }
        }


        if(em.getString(Email.STRING_MESSAGE)==null)
        	em.setString(Email.STRING_MESSAGE, "");
        if(!em.has(Email.LONG_DATE)) {
            em.setLong(Email.LONG_DATE,Cal.getUnixTime());
        }
    	em.setInt(Email.INT_STATE, Brief.STATE_UNREAD);

    	return em;
    }

    private void processMultiPartForEmail(Multipart mp, Email email, boolean isMainpart, boolean collectAttachments) {

        try {


            for(int i=0; i<mp.getCount(); i++) {
                BodyPart bp = (BodyPart) mp.getBodyPart(i);

                String disposition = bp.getDisposition();
                //BLog.e("EM-FILE", "part ctype: "+bp.getContentType()+ " - disp: "+disposition);

                if(bp.getContentType().toLowerCase().contains("multipart")
                        ) {
                    //BLog.e("EM", "part ctype matched as multi part");
                    Multipart extrapart=(Multipart) bp.getContent();
                    processMultiPartForEmail(extrapart,email,false,collectAttachments);

                } else {

                    if (disposition != null
                            && (disposition.equals(BodyPart.ATTACHMENT))               // ||disposition.equals("ATTACHMENT")
                            ) {

                        //if(collectBody) {
                        BLog.e("ISATTACHMENT",bp.getFileName()+" - "+disposition+ " - "+ bp.getDescription());
                        //String attachment = bp.getFileName();//
                        String fn=bp.getFileName();
                        if(fn==null || !fn.contains("."))
                            continue;
                        String fname=getAttachmentFileName(fn);
                        //String fname=getAttachmentFileName(bp.getFileName());
                        if(collectAttachments) {
                            fname=getAddAttachment(fname,bp);
                        }
                        email.addAttachment(fname);

                        //}
                        email.setInt(Email.INT_COLLECTED, email.getInt(Email.INT_COLLECTED)+1);

                    } else {

                        String contentType = bp.getContentType().toLowerCase();
                        BLog.e("EM_", isMainpart+"******************has -- "+contentType);

                        if (contentType.contains("text/plain")) {

                            if(contentType.toLowerCase().contains("name=")) {
                                //BLog.e("EM_C_TYPE", isMainpart+"AS TEXT FILE -- "+contentType);
                                // texfile attachment
                                String name=Cal.getUnixTime()+".txt";
                                try {
                                    String []sp=contentType.split("name=");
                                    name=Sf.restrictLength(sp[1].trim(),8)+"-"+Cal.getUnixTime()+".txt";
                                }catch(Exception e) {}

                                String fname=getAttachmentFileName(bp.getFileName());
                                if(collectAttachments) {
                                    TextFile.writeToFile(attachmentPath+File.separator+fname,bp.getContent().toString());
                                }
                                email.addAttachment(fname);

                                //BLog.e("IMAGE-JPG", isMainpart+"has -- "+contentType+" - "+bp.getFileName());
                                //}
                                email.setInt(Email.INT_COLLECTED, email.getInt(Email.INT_COLLECTED)+1);
                                //saveFile(textfile,Sf.stre)
                            } else {
                                //BLog.e("EM_C_TYPE", isMainpart+"AS CONTENT TEXT -- "+contentType);
                                email.setString(Email.STRING_MESSAGE, bp.getContent().toString());
                            }
                        }  else if (contentType.contains("text/html")) {
                            //BLog.e("EM_C_TYPE", isMainpart+"has -- "+contentType);
                            email.setString(Email.STRING_MESSAGE, bp.getContent().toString());
                        } else if (bp.isMimeType("image/jpeg")) {
                            //BLog.e("IMG", isMainpart+"image/ -- "+contentType+" - "+bp.getFileName());
                            String fn=bp.getFileName();
                            BLog.e("IMG", fn+"     ---image/ -- "+contentType+" - "+bp.getFileName());
                            if(fn==null || !fn.endsWith(".jpg")) {
                                fn=Cal.getUnixTime()+".jpg";
                            }
                            BLog.e("IMG", fn+"     ---image/ -- "+contentType+" - "+bp.getFileName());
                            String fname = getAttachmentFileName(fn);
                            BLog.e("IMG", fname+"     ---image/ -- "+contentType+" - "+bp.getFileName());
                            if (collectAttachments) {
                                fname = getAddImageAttachment(fname, bp);
                            }
                            email.addAttachment(fname);

                            email.setInt(Email.INT_COLLECTED, email.getInt(Email.INT_COLLECTED) + 1);

                        }  else if (contentType.contains("image/")) {
                            //BLog.e("IMG", isMainpart+"image/ -- "+contentType+" - "+bp.getFileName());
                            String fn=bp.getFileName();
                            if(fn==null || !fn.contains("."))
                                continue;
                            String fname=getAttachmentFileName(fn);
                            if(collectAttachments) {
                                fname=getAddAttachment(fname,bp);
                            }
                            email.addAttachment(fname);
                            email.setInt(Email.INT_COLLECTED, email.getInt(Email.INT_COLLECTED)+1);
                        }  else if (contentType.contains("application/")) {
                            BLog.e("FILE_APPLICATION", isMainpart+"has -- "+contentType+" - "+bp.getFileName());

                            String fname=getAttachmentFileName(bp.getFileName());
                            if(collectAttachments) {
                                fname=getAddAttachment(fname,bp);
                            }
                            email.addAttachment(fname);

                            //}
                            email.setInt(Email.INT_COLLECTED, email.getInt(Email.INT_COLLECTED)+1);
                        }  else if (bp.isMimeType("message/rfc822")) {
                            //BLog.e("rfc822", isMainpart+" rfc822 ------------ "+contentType+" - "+bp.getFileName());
                            // should process as full message (attachment?)
                            //writePart((Part) p.getContent());
                        }   else {
                            //BLog.e("UNKNOWN", "------------------------------ has -- "+contentType);
                        }

                    }

                }


            }

        } catch(MessagingException e) {
            //BLog.e("MSGE", "****************** Me: "+e.getMessage());
        } catch(IOException e) {
            //BLog.e("MSGE", "****************** IOe: "+e.getMessage());
        }

    }

    private static void getFromDecoderStream(BodyPart part) {
        InputStream is = null;
        try {
           is= (InputStream) part.getInputStream();
        } catch (Exception e) {
            //BLog.e("ERROR","ERROR GETTING INPUT STREAM: "+e.getMessage());
        }
        if(is!=null) {
            BufferedReader br = null;
            String totalLine = new String(), line;
            try {
                br = new BufferedReader(new InputStreamReader(is));

                while ((line = br.readLine()) != null)
                    totalLine += line + "\r\n";

                byte[] bytes = totalLine.getBytes();

                totalLine = new String(bytes, "UTF-8");
            } catch (Exception e) {
                //BLog.e("ERROR","ERROR GETTING INPUT STREAM-2: "+e.getMessage());
            }
        }
    }

    private static String getAddImageAttachment(String filename,BodyPart bp) {
        Object o = null;

        try {

            o=bp.getContent();
        } catch(Exception e) {

        }
        if(o!=null && filename!=null) {
            InputStream x = (InputStream) o;
            return  saveFile(filename,x);

        } else {
            //BLog.e("EMAIL","Get: image/jpg - null: "+filename+" - "+(o==null?"null":"not_null"));
        }
        return "";
    }

    private static String getAddAttachment(String useFilename,BodyPart mbp) {
    	try {
    		return saveFile(useFilename, mbp.getInputStream());

    	} catch(Exception e) {
            //BLog.e("EMAIL","file collect erro: "+e.getMessage());
        }
    	return "";
    }
    private static String getAttachmentFileName(String filename) {
        String fname=Sf.restrictLength(filename,5);
        fname=fname.replaceAll(".","");
        fname=fname+Cal.getUnixTime();
        String  ext = Files.getExtension(filename);
        String bext=".brf";
        //String ext=filename.substring(filename.lastIndexOf("."),filename.length());
        String newfname=fname+ext+bext;

        Files.ensurePath(attachmentPath);

        // Do no overwrite existing file
        File file = new File(attachmentPath+newfname);

        for (int i=0; file.exists(); i++) {
            fname=Sf.restrictLength(filename,5);
            fname=fname.replaceAll(".","");
            fname=fname+Cal.getUnixTime();

            //String ext=filename.substring(filename.lastIndexOf("."),filename.length());
            newfname=fname+i+ext+bext;
            file = new File(attachmentPath+newfname);
        }
        //Files.ensurePathAndFile(attachmentPath,newfname);
        return file.getName();
    }
    public static String saveFile(String filename,  InputStream input) {
        //BLog.e("SAVEFILE", "------ Getting file: "+filename);
	    if (filename == null) {
	      return null;
	    }

        //File file=getAttachmentFile(filename);
        File file=new File(attachmentPath+File.separator+filename);
        Files.ensurePathAndFile(attachmentPath,file.getName());
	    try {

		    FileOutputStream fos = new FileOutputStream(file);
		    BufferedOutputStream bos = new BufferedOutputStream(fos);
	
		    BufferedInputStream bis = new BufferedInputStream(input);
		    int aByte;
		    while ((aByte = bis.read()) != -1) {
		      bos.write(aByte);
		    }
		    bos.flush();
		    bos.close();
		    bis.close();
            //BLog.e("addFile","ok: "+file.getAbsolutePath());
		    return file.getName();
	    } catch(Exception e) {
            //BLog.e("addFile","ex: "+e.getMessage());
        }
	    return file.getName();
    }
    	
    
    private static String getSubject(Message msg) {
    	try {
    		return msg.getSubject();
    	} catch(Exception e) {
    		//BLog.e("E", e.getMessage());
    		return "err: em106";
    	}
    }
    private static long getDate(Message msg) {
    	try {
    		return msg.getReceivedDate().getTime();
    	} catch(Exception e) {
    		//BLog.e("E1", e.getMessage());
    		return (new Date()).getTime();
    	}
    }
    private static String getRecipents(Message msg) {
    	StringBuilder sb = new StringBuilder();
    	Address[] rec = null;
    	try {
    		rec=msg.getAllRecipients();
    	} catch(Exception e) {
    		//BLog.e("E2", e.getMessage());
    	}
    	
    	if(rec!=null && rec.length>0) {
    		for(int i=0; i<rec.length; i++) {
    			if(i!=0)
    				sb.append(";");
    			sb.append(rec[i].toString());
    		}
    	}
    	return sb.toString();
    }
    private static String getSenders(Message msg) {
    	StringBuilder sb = new StringBuilder();
    	Address[] rec = null;
    	try {
    		rec=msg.getFrom();
    	} catch(Exception e) {
    		//BLog.e("E3", e.getMessage());
    	}
    	
    	if(rec!=null && rec.length>0) {
    		for(int i=0; i<rec.length; i++) {
    			if(i!=0)
    				sb.append(";");
    			sb.append(rec[i].toString());
    		}
    	}
    	return sb.toString();
    }

    private static String getMessageId(Message msg) {
    	String[] idHeaders=null;
    	try {
    		idHeaders = msg.getHeader("Message-ID");
    	} catch(Exception e) {
    		//BLog.e("E4", e.getMessage());
    	}
    	if(idHeaders!=null && idHeaders.length>0)
    		return idHeaders[0];
    	else {
            long date=getDate(msg);
            String sub=getSubject(msg);
            if(sub!=null) {
                sub=sub.toLowerCase(Locale.getDefault());
                sub=sub.replaceFirst("fwd","");
                sub=sub.replaceFirst("re","");
                sub=sub.replaceAll(":", "");
                sub = Sf.restrictLength(sub.replaceAll(" ", ""), 5);
            } else
                sub="null";
            return date+sub;
        }

    }

}
