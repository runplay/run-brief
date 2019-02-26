package run.brief.email;

import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

import android.util.Log;

public class GMailReader extends javax.mail.Authenticator { 
    private String mailhost = "imaps.gmail.com"; 
    private String user; 
    private String password; 
    private Session session; 
    public GMailReader(String user, String password) { 
        this.user = user; 
        this.password = password; 
        Properties props = new Properties(); 
        props.setProperty("mail.transport.protocol", "imaps"); 
        props.setProperty("mail.imaps.host", mailhost); 
        props.put("mail.imaps.auth", "true"); 
        props.put("mail.imaps.port", "993"); 
        props.put("mail.imaps.socketFactory.port", "993"); 
        props.put("mail.imaps.socketFactory.class", 
                  "javax.net.ssl.SSLSocketFactory"); 
        props.put("mail.imaps.socketFactory.fallback", "false"); 
        props.setProperty("mail.imaps.quitwait", "false"); 
        session = Session.getDefaultInstance(props, this); 
    } 
    public synchronized Message[] readMail() throws Exception { 
        try { 
            Store store = session.getStore("imaps"); 
            store.connect("imaps.gmail.com", user, password); 
            Folder folder = store.getFolder("INBOX"); 
            folder.open(Folder.READ_ONLY); 
            Message[] msgs = folder.getMessages(1, 10); 
            FetchProfile fp = new FetchProfile(); 
            fp.add(FetchProfile.Item.ENVELOPE); 
            folder.fetch(msgs, fp); 
            return msgs; 
        } catch (Exception e) {  
            return null; 
        } 
    } 
}