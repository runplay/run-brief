package run.brief.email;


import android.content.Context;

import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64EncoderStream;

import java.io.File;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.BDataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import run.brief.b.B;
import run.brief.b.Device;
import run.brief.R;
import run.brief.beans.Account;
import run.brief.beans.Email;
import run.brief.beans.SyncData;
import run.brief.contacts.ContactsSelectedClipboard;
import run.brief.settings.AccountsDb;
import run.brief.settings.OAuth.OAuth2SaslClient;
import run.brief.settings.OAuth.OAuth2SaslClientFactory;


public final class EmailSendService {

    //protected String message_recip = "";
    List<String> message_recip;
    protected String message_subject = "";
    protected String from = "";
    protected String message_cc = "";
    protected String message_body = "";
    //protected String html_data = "";
    protected Session session;

    private Email email;
    private Account account;
    //protected Message mesg;
    //protected ArrayList<String> bcc=null;
    protected ArrayList<File> attachments = new ArrayList<File>();
    private Properties props = new Properties();
    private ArrayList<String> errors = new ArrayList<String>();

    private String prot;
    private boolean sessionDebug = true;
    //private String proptype;

    public EmailSendService(Account from, Email email) {
        this.account=from;
        this.email=email;
        this.from = from.getString(Account.STRING_EMAIL_ADDRESS);

        this.message_recip = ContactsSelectedClipboard.getEmailSummaryAsList(email.getString(Email.STRING_TO));
        this.message_subject = email.getString(Email.STRING_SUBJECT);;
        this.message_body = email.getString(Email.STRING_MESSAGE);
        //this.html_data = email.getString(Email.STRING_MESSAGE_HTML);
        this.attachments=email.getAttachmentsAsFiles();
        setUpProperties(from);
    }

    private void setUpProperties(Account account) {

        if(account.getInt(Account.INT_EMAIL_OUTGOING_SUBTYPE_)==Account.SUBTYPE_SSL) {
            //BLog.e("DS","smtps secure");
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable","true");
            props.put("mail.smtp.debug", "true");

            props.put("mail.smtp.port", ""+account.getInt(Account.INT_EMAIL_OUTGOING_PORT));
            props.put("mail.smtp.host", account.getString(Account.STRING_EMAIL_OUTGOING_SERVER));
            props.put("mail.smtp.socketFactory.port", account.getInt(Account.INT_EMAIL_OUTGOING_PORT));
            props.put("mail.smtp.socketFactory.class", "run.brief.email.AuthSSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");


            /*
                        props.put("mail.transport.protocol", "smtps");
            props.put("mail.smtps.auth", "true");
            props.put("mail.smtps.port", ""+account.getInt(Account.INT_EMAIL_OUTGOING_PORT));
            props.put("mail.smtps.ssl.trust", account.getString(Account.STRING_EMAIL_OUTGOING_SERVER));

            props.put("mail.smtp.host", account.getString(Account.STRING_EMAIL_OUTGOING_SERVER));
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", ""+account.getInt(Account.INT_EMAIL_OUTGOING_PORT));

            props.put("mail.smtp.socketFactory.port", account.getInt(Account.INT_EMAIL_OUTGOING_PORT));
            props.put("mail.smtp.socketFactory.class", "run.brief.email.AuthSSLSocketFactory");
            props.put("mail.transport.protocol", "smtps");
*/
        } else if(account.getInt(Account.INT_EMAIL_OUTGOING_SUBTYPE_)==Account.SUBTYPE_TLS) {
            //BLog.e("DS","start_tls");
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", account.getString(Account.STRING_EMAIL_OUTGOING_SERVER));
            props.put("mail.smtp.port", ""+account.getInt(Account.INT_EMAIL_OUTGOING_PORT));

        } else {
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", account.getString(Account.STRING_EMAIL_OUTGOING_SERVER));
            props.put("mail.smtp.port", ""+account.getInt(Account.INT_EMAIL_OUTGOING_PORT));
        }
         prot = props.getProperty("mail.transport.protocol",null);
        if( null != prot ){
            //BLog.e("DS","prot: "+prot);
            props.put("mail."+prot+".user",account.getString(Account.STRING_LOGIN_NAME));
            props.put("mail."+prot+".password",account.getString(Account.STRING_LOGIN_PASSWORD));
        }
        props.put("mail.smtp.timeout", "20000");
        props.put("mail.smtp.reportsuccess", "20000");
    }
    public static final class OAuth2Provider extends Provider {
        private static final long serialVersionUID = 1L;

        public OAuth2Provider() {
            super("Google OAuth2 Provider Brief flavour", 1.0, "Provides the XOAUTH2 SASL Mechanism");
            put("SaslClientFactory.XOAUTH2", "run.brief.settings.OAuth.OAuth2SaslClientFactory");
        }
    }
    private run.brief.settings.OAuth.OAuth2Provider o2auth;

    public String doSend(EmailServiceInstance parent,Context context) {
        String retXBiref=null;
        if(account!=null) {


            //BLog.e("DS","1");
            try {

                if(account.getInt(Account.INT_EMAIL_OUTGOING_SUBTYPE_)==Account.SUBTYPE_GOOGLEMAIL) {

                    if(o2auth==null) {
                        o2auth=new run.brief.settings.OAuth.OAuth2Provider();
                    } else {
                        Security.removeProvider(o2auth.getName());
                    }
                    //boolean tryConnect=true;
                    //BLog.e("DS","2");
                    if(Device.getCONNECTION_TYPE()==Device.CONNECTION_TYPE_NONE) {
                        B.forceTryConnection(context);
                    }
                    //else {
                    //    tryConnect=true;
                    //}
                    //BLog.e("DS","3");
                    String token = account.getTokenNoAsync();//account.getString(Account.STRING_OAUTH_CODE);//null;
                    //if(tryConnect) {
                    //    token = OAuthHelper.fetchToken(context,parent,new OAuthHelper.Backoff());
                    //}

                    if(token==null) {
                        account.incrementInt(Account.INT_OAUTH_TOKEN_FAILS);
                        SyncData.updateSyncOutJustCompleted(account, SyncData.SYNC_LAST_RESULT_FAIL, context.getString(R.string.email_oauth_fail));
                        //return false;

                    } else {
                        OAuth2SaslClientFactory.setToken(token);
                        OAuth2SaslClient.setToken(token);
                        Security.addProvider(o2auth);


                        //BLog.e("EMS", "google mail smtp");
                        Properties props = new Properties();
                        props.put("mail.smtp.auth.login.disable", "true");
                        props.put("mail.smtp.auth.plain.disable", "true");

                        props.put("mail.smtp.starttls.enable", "true");
                        props.put("mail.smtp.starttls.required", "true");
                        props.put("mail.smtp.sasl.enable", "true");
                        props.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, token);


                        props.setProperty("mail.imap.auth.login.disable", "true");
                        props.setProperty("mail.imap.auth.plain.disable", "true");
                        props.setProperty("mail.imaps.auth.plain.disable", "true");
                        props.setProperty("mail.imap.auth.ntlm.disable", "true");

                        //BLog.e("DS","4");

                        session = Session.getInstance(props);
                        session.setDebug(true);
                        //BLog.e("EMS", "google mail smtp 1");

                        //BLog.e("DS","5");
                        final URLName unusedUrlName = null;
                        SMTPTransport stransport = new SMTPTransport(session, unusedUrlName);
                        //BLog.e("EMS", "google mail smtp 2: " + account.getString(Account.STRING_EMAIL_OUTGOING_SERVER) + " - " + account.getString(Account.STRING_LOGIN_NAME));
                        // If the password is non-null, SMTP tries to do AUTH LOGIN.
                        final String emptyPassword = null;
                        stransport.connect(account.getString(Account.STRING_EMAIL_OUTGOING_SERVER), account.getInt(Account.INT_EMAIL_OUTGOING_PORT), account.getString(Account.STRING_LOGIN_NAME), emptyPassword);

                        //BLog.e("EMS", "google mail smtp 3");

                        byte[] response = String.format("user=%s\1auth=Bearer %s\1\1", account.getString(Account.STRING_EMAIL_ADDRESS), token).getBytes();
                        //BLog.e("EMS", "google mail smtp 4");
                        response = BASE64EncoderStream.encode(response);

                        //BLog.e("EMS", "google mail smtp 5");
                        stransport.issueCommand("AUTH XOAUTH2 " + new String(response), 235);


                        Message message = buildMessage();

                        //BLog.e("DS","6");
                        //BLog.e("EMS", "google mail smtp 10");

                        try {
                            stransport.sendMessage(message, message.getAllRecipients());

                            retXBiref= EmailService.getXBriefId(message);

                            SyncData.updateSyncOutJustCompleted(account,  SyncData.SYNC_LAST_RESULT_SUCCESS, context.getString(R.string.sync_msg_last_out_sync));
                            //BLog.e("DS","6");
/*
                            try {

                                Enumeration en = message.getAllHeaders();
                                while (en.hasMoreElements()) {
                                    String[] header = (String[]) en.nextElement();
                                    BLog.e("HD", header.toString());

                                }
                                BLog.e("SMTP sent","msg: "+message.getSentDate().toString()+ "____"+message.getSize());
                                BLog.e("SMTP sent","____"+message.getFileName()+"___"+message.isExpunged());
                            } catch(Exception e) {
                                BLog.e("EXC",""+e.getMessage());
                            }
                            */
                        } catch(SendFailedException ex) {
                            SyncData.updateSyncOutJustCompleted(account, SyncData.SYNC_LAST_RESULT_FAIL, "STMP-c: "+ex.getMessage());

                        }
                        //stransport.close();
                    }
                } else {

                    Transport transport =null;
                    Session session = Session.getInstance(props,
                            new javax.mail.Authenticator() {
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(account.getString(Account.STRING_LOGIN_NAME), account.getString(Account.STRING_LOGIN_PASSWORD));
                                }
                            });
                    session.setDebug(sessionDebug);


                    //BLog.e("DS","2");


                    transport = session.getTransport(prot);
                    transport.connect(account.getString(Account.STRING_EMAIL_OUTGOING_SERVER), account.getInt(Account.INT_EMAIL_OUTGOING_PORT), account.getString(Account.STRING_LOGIN_NAME), account.getString(Account.STRING_LOGIN_PASSWORD));


                    Message message = buildMessage();
                    //BLog.e("DS","3");

                    try {
                        transport.sendMessage(message, message.getAllRecipients());
                        //stransport.sendMessage(message, message.getAllRecipients());
                        retXBiref= EmailService.getXBriefId(message);
                        SyncData.updateSyncOutJustCompleted(account,  SyncData.SYNC_LAST_RESULT_SUCCESS, context.getString(R.string.sync_msg_last_out_sync));

                    } catch(SendFailedException ex) {
                        SyncData.updateSyncOutJustCompleted(account, SyncData.SYNC_LAST_RESULT_FAIL, "STMP-c: "+ex.getMessage());

                    }
                    //transport.close();
                }

                //BLog.e("DS","5: "+account.getString(Account.STRING_LOGIN_NAME));
                //BLog.e("DS","6");

                //if(context!=null && account!=null)
                //SyncData.updateSyncOutJustCompleted(account, SyncData.SYNC_LAST_RESULT_SUCCESS, context.getString(R.string.sync_msg_last_out_sync));
                //Transport.send(message);
                //BLog.e("DS","7");
                //System.out.println("Done");

            } catch (MessagingException e) {
                //BLog.e("EMS", "a: "+e.getMessage());
                SyncData.updateSyncOutJustCompleted(account, SyncData.SYNC_LAST_RESULT_FAIL, "STMP-g: "+e.getMessage());
                account.setInt(Account.INT_SIGNATURE_REKEY,1);
                AccountsDb.updateAccount(account);

            } catch (Exception e) {
                SyncData.updateSyncOutJustCompleted(account, SyncData.SYNC_LAST_RESULT_FAIL, "STMP-a: "+e.getMessage());
                //BLog.e("EMS", "g: "+e.getMessage());

            }


        }
        return retXBiref;
    }
    private Message buildMessage() {
        Message message = new MimeMessage(session);
        //BLog.e("DS","2");
        try {
            message.setFrom(new InternetAddress(from));
        } catch(AddressException e) {
            //BLog.e("SEND_EMAIL","Bad f-address: "+e.getMessage());
        } catch(MessagingException e) {
            //BLog.e("SEND_EMAIL","MessageF-Ex: "+e.getMessage());
        }
        //message.setRecipients(Message.RecipientType.TO, (""));
        //BLog.e("DS","3");
        for(String recip: message_recip) {
            //InternetAddress add= ;
            //BLog.e("SEND","email to: "+recip);
            try {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recip));
            } catch(AddressException e) {
                ///BLog.e("SEND_EMAIL","Bad t-address: "+e.getMessage());
            } catch(MessagingException e) {
                //BLog.e("SEND_EMAIL","MessageEx: "+e.getMessage());
            }
        }
        try {
            message.setSubject(message_subject);
        } catch(MessagingException e) {
            //BLog.e("SEND_EMAIL","MsgSub: "+e.getMessage());
        }
        try {
            message.setText(message_body);
        } catch(MessagingException e) {
            //BLog.e("SEND_EMAIL","MsgBody: "+e.getMessage());
        }
        addAttachmentsToMsg(message);
        try {
            message.addHeader(EmailService.XBriefHeaderField,EmailService.generateNewXBriefId());
        } catch(MessagingException e) {
            //BLog.e("SEND_EMAIL","MsgBody: "+e.getMessage());
        }

        //message.
        return message;
    }


  private void addAttachmentsToMsg(Message message) {
	  if(!attachments.isEmpty()) {
		  Multipart multipart = new MimeMultipart();
		  for(File f: attachments) {
		      MimeBodyPart messageBodyPart = new MimeBodyPart();
		      messageBodyPart = new MimeBodyPart();
		      String file = f.getAbsolutePath();
		      String fileName = f.getName();
		      DataSource source = new FileDataSource(file);
		      
		      try{
			      messageBodyPart.setDataHandler(new BDataHandler(source));
			      messageBodyPart.setFileName(fileName);
                  messageBodyPart.setDisposition(Part.ATTACHMENT);
			      multipart.addBodyPart(messageBodyPart);
			      
			      
		      } catch(MessagingException e) {
		    	  //BLog.e("EmailSend", "add part ex: "+e.getMessage());
		      }
		  }
		  try {
			  message.setContent(multipart);
		  } catch(MessagingException e) {
			  //BLog.e("EmailSend", "set content ex: "+e.getMessage());
		  }
	  }

  }
  

}



