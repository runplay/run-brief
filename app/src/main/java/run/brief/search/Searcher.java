package run.brief.search;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import run.brief.b.BCallback;
import run.brief.beans.Account;
import run.brief.beans.Email;
import run.brief.beans.Note;
import run.brief.beans.Person;
import run.brief.beans.RssItem;
import run.brief.beans.SearchResult;
import run.brief.beans.SmsMsg;
import run.brief.contacts.ContactsDb;
import run.brief.contacts.ContactsSelectedClipboard;
import run.brief.email.EmailService;
import run.brief.email.EmailServiceInstance;
import run.brief.news.NewsItemsDb;
import run.brief.notes.NotesDb;
import run.brief.settings.AccountsDb;
import run.brief.sms.SmsDb;

public class Searcher {

	private static ArrayList<SearchResult> results=new ArrayList<SearchResult>(); 
	
	public static ArrayList<SearchResult> getResults() {
		return results;
	}
	public static SearchResult get(int index) {
		if(index>=0 && index<results.size()) {
			return results.get(index);
		}
		return null;
	}
	public static int size() {
		return results.size();
	}
	
	public static void doSearch(Context context, String term, BCallback callback) {
		ArrayList<SearchResult> res = new ArrayList<SearchResult>();
		
		if(term !=null) {
			
			ArrayList<String> st=getWords(term.toLowerCase());
			
			if(ContactsDb.size()>0) {
                for(String s: st) {
                    List<ContactsDb.SearchResult> found =  ContactsDb.find(s, ContactsSelectedClipboard.CONTACTS_TYPE_ALL);
                    for(ContactsDb.SearchResult p: found) {
                        SearchResult sres=new SearchResult();
                        sres.setWITH_(SearchResult.WITH_PERSON);

                        sres.setPersonId(p.person.getString(Person.STRING_PERSON_ID));
                        sres.setDBid(p.person.getString(Person.STRING_PERSON_ID));
                        sres.setSubject(p.person.getString(Person.STRING_NAME));

                        if(p.person.getLong(Person.LONG_ID)>=0) {
                            StringBuilder message= new StringBuilder(p.person.hasEmail()?p.person.getMainEmail():"");
                            if(message.length()!=0)
                                message.append(", ");
                            message.append(p.person.hasPhone()?p.person.getMainPhone():"");
                            sres.setMessage(message.toString());
                        }
                        StringBuilder sb=new StringBuilder();
                        /*
                        for(int i=0; i<p.person.getJSONArray(Person.JSONARRAY_PHONE).length(); i++) {
                            if(i>0)
                                sb.append(", ");
                            sb.append(p.person.getJSONArray(Person.JSONARRAY_PHONE).getString(i));
                        }
                        sb.append("\n");
                        for(int i=0; i<p.person.getJSONArray(Person.JSONARRAY_EMAIL).length(); i++) {
                            if(i>0)
                                sb.append(", ");
                            sb.append(p.person.getJSONArray(Person.JSONARRAY_EMAIL).getString(i));
                        }
                        sb.append("\n");
                        sres.setMessage(sb.toString());
                        */
                        sres.setMessage(p.matches.toString());
                        res.add(sres);
                    }
                }

			}
            if(!res.isEmpty() && callback!=null)
                callback.callback();
			if(SmsDb.size()>0) {
				for(int i=0; i<SmsDb.size(); i++) {
					SmsMsg sms=SmsDb.get(i);

					Person p = ContactsDb.getWithTelephone(context, sms.getMessageNumber());
					boolean addalready=false;
					if(p!=null) {
                        for (String s : st) {

                            if (p.getString(Person.STRING_NAME).toLowerCase().contains(s)) {
                                res.add(new SearchResult(context, sms, i));
                                addalready = true;
                            }
                        }
                    }
					if(!addalready) {
                        String sme=sms.getMessageContent().toLowerCase();
						for(String s: st) {
							int index=sme.indexOf(s);
							if(sme.contains(s)) {
								res.add(new SearchResult(context,sms,i));
							}
							
						}
					}
				}
			}

            for(Account account: AccountsDb.getAllEmailAccounts()) {
                EmailServiceInstance ems = EmailService.getService(context,account);
                for(int i=0; i<ems.getEmails().size(); i++) {
                    Email em = ems.getEmail(i);
                    for(String s: st) {
                        if(em.getString(Email.STRING_TO).contains(s) || em.getString(Email.STRING_FROM).contains(s)
                                || em.getString(Email.STRING_SUBJECT).contains(s) || em.getString(Email.STRING_MESSAGE).contains(s)
                                || em.getString(Email.STRING_ATTACHMENTS).contains(s)
                                )  {
                            res.add(new SearchResult(context,account,em,i));
                        }
                    }
                }
            }

			if(NewsItemsDb.size()>0) {
				for(int i=0; i<NewsItemsDb.size(); i++) {
					RssItem rss=NewsItemsDb.get(i);
					String sme=rss.getString(RssItem.STRING_HEAD).toLowerCase();
					String smt=rss.getString(RssItem.STRING_TEXT).toLowerCase();
					for(String s: st) {
						int index=sme.indexOf(s);
						int indext=smt.indexOf(s);
						if(index!=-1) {
							res.add(new SearchResult(rss,i));
						} else if(indext!=-1) {
							res.add(new SearchResult(rss,i));
						}
						
					}
					
				}
			}
			if(NotesDb.size()>0) {
				for(int i=0; i<NotesDb.size(); i++) {
					Note note=NotesDb.getByIndex(i);
					String sme=note.getString(Note.STRING_TEXT).toLowerCase();
					ArrayList<String> files = note.getFiles();
					boolean cont=true;
					for(String s: st) {
						int index=sme.indexOf(s);
						if(index!=-1) {
							res.add(new SearchResult(note,i));
							cont=false;
						}
						if(cont && !files.isEmpty()){
							for(int j=0; j<files.size(); j++) {
								String fnamepth=files.get(j);
								if(fnamepth.length()>0 && fnamepth.indexOf(s)!=-1) {
									res.add(new SearchResult(note,i));
									break;
								}
									
							}
						}
						
						
					}
					
				}
			}
		
		}
		results=res;
		if(callback!=null)
			callback.callback();
	}
	private static String getResultText(int index, String term, String searchText) {
		StringBuilder sb=new StringBuilder();
		int st=index-10;
		if(st<0)
			st=0;
		int se=index+40;
		if(se>searchText.length()-1)
			se=searchText.length();
		sb.append(searchText.substring(st, index));
		sb.append(term);
		sb.append(searchText.substring(index+term.length(), se));
		return sb.toString();
		
	}
	private static ArrayList<String> getWords(String s) {
		ArrayList<String> fwords=new ArrayList<String>();
		//ArrayList<String> f=new ArrayList<String>();
		String[] tmp = s.split(",");
		if(tmp!=null) {
			for(int i=0; i<tmp.length; i++) {
				String[] etmp = tmp[i].split("\\s");
				if(etmp!=null) {
					for(int j=0; j<etmp.length; j++) {
						if(etmp[j].length()>1)
							fwords.add(etmp[j]);
					}
				}
			}
		}
		return fwords;
	}
}
