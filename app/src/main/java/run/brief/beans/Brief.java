package run.brief.beans;

import android.content.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import run.brief.contacts.ContactsDb;
import run.brief.util.Cal;
import run.brief.util.Num;
import run.brief.util.Sf;
import run.brief.util.json.JSONArray;

public class Brief extends BriefWith{

	public static final int TYPE_IN=0;
	public static final int TYPE_OUT=1;
	public static final int TYPE_ACTION=2;
    public static final int TYPE_MISSED=3;
	
	public static final int STATE_UNREAD=0;
	public static final int STATE_READ=1;
	public static final int STATE_SENDNG=-1;
	public static final int STATE_ERROR=4;
	public static final int STATE_ARCHIVED=3;
	
	private int WITH_;
	private long accountId;
	private int DBIndex;
	private String DBid;


	private int TYPE_;
	private long timestamp;
	private String message;
	private ArrayList<Brief> messageChain;
	public ArrayList<Brief> getMessageChain() {
		return messageChain;
	}
	public void setMessageChain(ArrayList<Brief> messageChain) {
		this.messageChain = messageChain;
	}
	private String subject;
	private String personId ="";
	private ArrayList<BriefOut> outs=new ArrayList<BriefOut>();
	private ArrayList<BriefObject> objects=new ArrayList<BriefObject>();
	private String ratingsIdentifier;
	private int state;
	private String threadId;
	
	public String getThreadId() {
		return threadId;
	}
	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}
	public int getState() {
		return state;
	}
	public void setState(int STATE_) {
		this.state=STATE_;
	}
	public String getDBid() {
		return DBid;
	}
	public void setDBid(String dBid) {
		DBid = dBid;
	}
	
	public String getRatingsIdentifier() {
		if(ratingsIdentifier==null)
			ratingsIdentifier=BriefRating.makeRatingsIdentifier(this.getWITH_(), this.getDBid(),this.getAccountId());
		return ratingsIdentifier;
	}
	
	public String getPersonId() {
		return personId;
	}
	public void setPersonId(String personId) {
		this.personId = personId;
	}

	
	
	public long getAccountId() {
		return accountId;
	}
	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}
	public int getDBIndex() {
		return DBIndex;
	}
	public void setDBIndex(int dBIndex) {
		DBIndex = dBIndex;
	}
	
	public Brief() {
		TYPE_=TYPE_OUT;
	}

	public Brief(Context context, SmsMsg sms, int itemDbIndex) {
		if(sms!=null) {
			this.message=sms.getMessageContent();
			this.DBid=sms.getId();
			this.DBIndex =itemDbIndex;
			this.WITH_ = Brief.WITH_SMS;
			this.setThreadId(sms.getThreadId());

			if(sms.isMine()) {
				//BLog.e("SMS", "is mine.....");
				this.TYPE_=TYPE_OUT;
				BriefOut b = new BriefOut(BriefOut.WITH_SMS,"me to",sms.getMessageNumber());
				this.outs.add(b);
				Person p=ContactsDb.getWithTelephoneConcatEnd(context,sms.getMessageNumber());
				if(p!=null) {
					this.personId=p.getString(Person.STRING_PERSON_ID);
				} else {
					Person tmpp = Person.getNewUnknownPerson(context, sms.getMessageNumber(), null);
					this.personId=tmpp.getString(Person.STRING_PERSON_ID);
				}
				
			} else {
				this.TYPE_=TYPE_IN;
				Person p=ContactsDb.getWithTelephoneConcatEnd(context,sms.getMessageNumber());
				
				BriefOut b=null;
				if(p!=null) {
					this.personId=p.getString(Person.STRING_PERSON_ID);
					b = new BriefOut(BriefOut.WITH_SMS,p.getString(Person.STRING_NAME),sms.getMessageNumber());
					
				} else {
					Person tmpp = Person.getNewUnknownPerson(context, sms.getMessageNumber(), null);
					this.personId=tmpp.getString(Person.STRING_PERSON_ID);
					b = new BriefOut(BriefOut.WITH_SMS,sms.getMessageNumber(),sms.getMessageNumber());
				}
				if(b!=null)
					this.outs.add(b);
			}
			this.timestamp=sms.getMessageDate().getTimeInMillis();
		}
	}
	public Brief(RssItem news, int itemDbIndex) {
		if(news!=null) {
			TYPE_=TYPE_IN;
			this.WITH_ = Brief.WITH_NEWS;
			this.DBid=news.getLong(RssItem.LONG_ID)+"";
			this.DBIndex =  itemDbIndex;
			this.message=news.getString(RssItem.STRING_TEXT);
			this.subject=news.getString(RssItem.STRING_HEAD);
			this.timestamp=news.getLong(RssItem.LONG_DATE);
			String source=news.getString(RssItem.STRING_PUBLISHER);
			BriefOut b = new BriefOut(BriefOut.WITH_NEWS,source,source);
			this.outs.add(b);
		}
	}
	public Brief(Note note, int itemDbIndex) {
		if(note!=null) {
			this.TYPE_=TYPE_OUT;
			this.WITH_=Brief.WITH_NOTES;
			this.DBid=note.getInt(Note.INT_ID)+"";
			BriefOut b = new BriefOut(BriefOut.WITH_NOTES,"","");
			outs.add(b);
			this.DBIndex =itemDbIndex;
			this.message=ShortenParagraph(note.getString(Note.STRING_TEXT));
			
			this.subject="Noted: "+Cal.getCal(new Date(note.getLong(Note.LONG_DATE_CREATED)));
			this.timestamp=note.getLong(Note.LONG_DATE_CREATED);
			JSONArray files = note.getJSONArray(Note.JSONARRAY_FILES);
			if(files!=null) {
				for(int i=0; i<files.length(); i++) {
					BriefObject bob=new BriefObject();
					bob.setType(BriefObject.TYPE_FILE_SD);
					bob.setUri(files.get(i).toString());
					this.objects.add(bob);
				}
			}
		}
	}
	public Brief(P2PChat chat, int itemDbIndex) {
		if(chat!=null) {
			this.TYPE_=TYPE_OUT;
			this.WITH_=Brief.WITH_P2P;
			this.DBid=chat.getInt(P2PChat.INT_ID)+"";
			BriefOut b = new BriefOut(BriefOut.WITH_NOTES,"","");
			outs.add(b);
			this.DBIndex =itemDbIndex;
			this.message=ShortenParagraph(chat.getString(P2PChat.STRING_TEXT));
			
			//this.subject="Noted: "+Cal.getCal(new Date(chat.getLong(Note.LONG_DATE_CREATED)));
			this.timestamp=chat.getLong(P2PChat.LONG_DATE_CREATED);
			JSONArray files = chat.getJSONArray(P2PChat.JSONARRAY_FILES);
			if(files!=null) {
				for(int i=0; i<files.length(); i++) {
					BriefObject bob=new BriefObject();
					bob.setType(BriefObject.TYPE_FILE_SD);
					bob.setUri(files.get(i).toString());
					this.objects.add(bob);
				}
			}
		}
	}
	private String ShortenParagraph(String s) {
		if(s==null)
			return "";
		String[] lines = s.split("\n");
		
		if(s.length()<50 && lines.length<2){
			return s;
		}	
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<lines.length; i++) {
			if(i>3)
				break;
			sb.append(lines[i]);
			sb.append(Sf.NEW_LINE);
		}
		return sb.toString();
	}
	public Brief(Context context, Account account, Email email, int itemDbIndex) {
		if(email!=null) {
			//if(email.getBoolean(Email.))
            //BLog.e("WHERE",email.getLong(Email.LONG_ID)+"---------------");
			this.WITH_=Brief.WITH_EMAIL;
			this.DBIndex =itemDbIndex;
			this.DBid=email.getLong(Email.LONG_ID)+"";
			this.accountId=account.getLong(Account.LONG_ID);

            Person p=null;//ContactsDb.getWithEmail(email.getString(Email.STRING_FROM));
            //BLog.e("Email",account.getSentFolder()+" - "+email.getString(Email.STRING_FOLDER));
            if(account.getSentFolder().equals(email.getString(Email.STRING_FOLDER))) {
                TYPE_=TYPE_OUT;
                p=ContactsDb.getWithEmail(email.getString(Email.STRING_TO));
            } else {
                TYPE_=TYPE_IN;
                p=ContactsDb.getWithEmail(email.getString(Email.STRING_FROM));
            }


			
			BriefOut b=null;
			if(p!=null) {
				this.personId=p.getString(Person.STRING_PERSON_ID);
				b = new BriefOut(BriefOut.WITH_EMAIL,p.getString(Person.STRING_NAME),email.getString(Email.STRING_TO));
			} else {
				Person un = Person.getNewUnknownPerson(context, null, email.getString(Email.STRING_FROM));
				this.personId=un.getString(Person.STRING_PERSON_ID);
				b = new BriefOut(BriefOut.WITH_EMAIL,un.getString(Person.STRING_NAME),email.getString(Email.STRING_TO));
			}
			this.outs.add(b);
			String msgStr=email.getString(Email.STRING_MESSAGE);



			if(!Sf.isHtml(msgStr))
				this.message=Sf.restrictLength("📝 "+email.getString(Email.STRING_SUBJECT), 30)+(email.getString(Email.STRING_SUBJECT).length()>60?"...":"")+"\n📄 "+Sf.restrictLength(msgStr,140).replaceAll("\n", " ").trim();
			else
				this.message=Sf.restrictLength("📝 "+email.getString(Email.STRING_SUBJECT), 60)+(email.getString(Email.STRING_SUBJECT).length()>60?"...":"");

            List<String> files = email.getAttachments();
            if(files!=null) {
                for(int i=0; i<files.size(); i++) {
                    BriefObject bob=new BriefObject();
                    bob.setType(BriefObject.TYPE_FILE_SD);
                    bob.setUri(files.get(i));
                    this.objects.add(bob);
                }
            }
			//this.subject=

			this.timestamp=email.getLong(Email.LONG_DATE)+Num.getRandom(1,999);
		}
	}
	public Brief(Tweet tweet, int itemDbIndex) {
		if(tweet!=null) {
			TYPE_=TYPE_IN;
			this.WITH_=Brief.WITH_TWITTER;
			this.message=Sf.restrictLength(tweet.getString(Tweet.STRING_MSG),140);
			this.subject=Sf.restrictLength(tweet.getString(Tweet.STRING_NAME), 80);
			this.timestamp=tweet.getLong(Tweet.LONG_DATE);
		}
	}
	public Brief(Context context, Phonecall call, int itemDbIndex) {
		if(call!=null) {
			if(call.getInt(Phonecall.INT_TYPE)==Phonecall.TYPE_OUT)
				TYPE_=TYPE_OUT;
			else if(call.getInt(Phonecall.INT_TYPE)==Phonecall.TYPE_IN)
                TYPE_=TYPE_IN;
            else
				TYPE_=TYPE_MISSED;
			this.DBid=call.getString(Phonecall.STRING_ID);
			this.WITH_=Brief.WITH_PHONE;
			//this.message=Sf.restrictLength(call.getString(Phonecall.STRING_NUMBER),140);
			//this.subject=Sf.restrictLength(call.getString(Phonecall.STRING_NAME), 80);
			this.timestamp=call.getLong(Phonecall.LONG_DATE);
			//if(call.getInt(Phonecall.INT_TYPE)==Phonecall.TYPE_IN) {
			Person p=ContactsDb.getWithTelephoneConcatEnd(context, call.getString(Phonecall.STRING_NUMBER));
			if(p==null) {
				p= Person.getNewUnknownPerson(context, call.getString(Phonecall.STRING_NUMBER), null);
			}
            this.subject=Sf.restrictLength(p.getString(Person.STRING_NAME), 80);
            this.message= Num.friendlyTimeDuration(call.getInt(Phonecall.INT_DURATION));
			this.personId=p.getString(Person.STRING_PERSON_ID);
			//}
		}
	}
	public ArrayList<BriefObject> getBriefObjects() {
		if(objects==null)
			objects=new ArrayList<BriefObject>();
		return objects;
	}
	public ArrayList<BriefOut> getBriefOuts() {
		if(outs==null)
			outs=new ArrayList<BriefOut>();
		return outs;
	}
	public void setBriefOuts(ArrayList<BriefOut> outs) {
		this.outs = outs;
	}
	public void addBriefOut(BriefOut bout) {
		this.outs.add(bout);
	}
	public int getTYPE_() {
		return TYPE_;
	}
	public void setTYPE_(int tYPE_) {
		TYPE_ = tYPE_;
	}

	public int getWITH_() {
		return WITH_;
	}
	public void setWITH_(int wITH_) {
		WITH_ = wITH_;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
}
