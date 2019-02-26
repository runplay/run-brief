package run.brief.news;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import run.brief.beans.RssFeed;
import run.brief.beans.RssItem;
import run.brief.beans.RssPage;
import run.brief.beans.RssUserFeed;
import run.brief.util.Cal;
import run.brief.util.Sf;
import run.brief.util.log.BLog;


/**
 *
 * @author root
 */
public class Rss {

    public static RssPage getRssFromFeed(RssFeed feed) {

    	return getRss(feed.getString(RssFeed.STRING_NAME),feed.getString(RssFeed.STRING_URL));
    }
    public static RssPage getRssFromFeed(RssUserFeed feed) {
        if(feed.getInt(RssUserFeed.INT_ERROR_COLLECT_COUNT)<5) {
            RssPage getRss = getRss(feed.getString(RssUserFeed.STRING_PUBLISHER), feed.getString(RssUserFeed.STRING_URL));
            if (getRss.getItems().isEmpty()) {
                BLog.e("Error on feed collect: " + feed.getString(RssUserFeed.STRING_PUBLISHER));
                feed.setInt(RssUserFeed.INT_ERROR_COLLECT_COUNT, feed.getInt(RssUserFeed.INT_ERROR_COLLECT_COUNT) + 1);
                NewsFeedsDb.updateErrorCollectCount(feed);
            } else if(feed.getInt(RssUserFeed.INT_ERROR_COLLECT_COUNT)!=0) {
                NewsFeedsDb.resetErrorCollectCount(feed);
            }
            return getRss;
        }
        RssPage p = new RssPage(feed.getString(RssUserFeed.STRING_URL));
    	return p;
    }
    public static RssPage getRssFromFeedUrl(String urlStr) {

    	return getRss(null,urlStr);
    }

    private static RssPage getRss(String publisher, String urlStr) {
        ArrayList<RssItem> getRss=new ArrayList<RssItem>();
        System.setProperty("http.agent", "Bread Rss reader v1.69");
        //BLog.e("RSS", "getRssFromUrl() : pub-" + publisher + "- url-" + urlStr + "-");
        RssPage page=new RssPage(urlStr);

        try {
        	DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",Locale.ENGLISH);
        	DateFormat dateFormatterRssPubDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);


            URL url = new URL(urlStr);

            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            //HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            connection.setRequestProperty("User-Agent", "Bread Rss reader v1.69");
            connection.setRequestMethod("GET");
            //connection.setDoInput(true);
            connection.connect();

            //InputStream inputStream = connection.getInputStream();



        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            //Document doc = builder.parse(url.openStream());
            Document doc = builder.parse(connection.getInputStream());

            try {
                String title = doc.getDocumentElement().getElementsByTagName("title").item(0).getTextContent();
                page.setTitle(title);

            } catch(Exception e) {
                BLog.e( "Parse headdings e: " + e.getMessage());
            }


            NodeList items = null;
            ArrayList<String> headtags = new ArrayList<String>();
            headtags.add("item"); headtags.add("entry");
            for(String tag: headtags) {
                items=doc.getDocumentElement().getElementsByTagName(tag);
                if(items!=null && items.getLength()>0)
                    break;
            }

            if(items!=null && items.getLength()!=0) {
                //BLog.e("RSS","size: "+items.getLength());
                for(int i=0; i<items.getLength(); i++) {

                    Node it = items.item(i);
                    if(it !=null) {
                        RssItem add = new RssItem();
                        //String head;
                        //String text;
                        String title=null;
                        //BLog.e("1");
                        for(int j=0; j<it.getChildNodes().getLength(); j++) {
                        	Node node = it.getChildNodes().item(j);
                            if(node.getNodeName().equals("title"))
                            	add.setString(RssItem.STRING_HEAD, Sf.stripHtml(node.getTextContent()).trim());
                            else if(node.getNodeName().equals("description")) {

                                add.setString(RssItem.STRING_TEXT, Sf.stripHtml(node.getTextContent()).trim());

                            } else if(node.getNodeName().equals("pubDate") || node.getNodeName().equals("published")) {
                            	
                            	Date date = null;
                            	try {
                            		date = formatter.parse(node.getTextContent());
                            	} catch(Exception e) {
                                	try {
                                		date = dateFormatterRssPubDate.parse(node.getTextContent());
                                	} catch(Exception e2) {
                                		
                                	}
                            	}
                            	if(date!=null) {
                            		add.setLong(RssItem.LONG_DATE, date.getTime());
                            	}
                            	add.setString(RssItem.STRING_PUB_DATE, node.getTextContent());
                            }
                            else if(node.getNodeName().equals("link")) {
                                if(!node.getTextContent().isEmpty()) {
                                    add.setString(RssItem.STRING_URL, node.getTextContent().trim());
                                } else {
                                    try {
                                        String eurl =node.getAttributes().getNamedItem("href").getNodeValue();
//BLog.e("eurl: "+eurl);
                                        if(eurl!=null) {
                                            add.setString(RssItem.STRING_URL, eurl);
                                        }
                                    } catch(Exception e) {}
                                }
                                //BLog.e("2");
                            } else if(node.getNodeName().equals("enclosure")) {
                            	try {
                            		String type =node.getAttributes().getNamedItem("type").getNodeValue();
                            		String urle =node.getAttributes().getNamedItem("url").getNodeValue();
                            		if(type.equals("image/jpg")) {
                            			add.setString(RssItem.STRING_IMG_URL, urle);
                            		}
                            	} catch(Exception e) {}
                            }else if(node.getNodeName().equals("media:thumbnail")) {
                            	try {
                            		String urle =node.getAttributes().getNamedItem("url").getNodeValue();
                           			add.setString(RssItem.STRING_IMG_URL, urle);
                            	} catch(Exception e) {}
                            }else if(node.getNodeName().equals("media:content") || node.getNodeName().equals("media:group")) {
                                try {
                                    //BLog.e("MEDIA","media:content");
                                    NodeList nl = node.getChildNodes();
                                    if(nl!=null && nl.getLength()>0) {
                                        for(int z=0; z<nl.getLength(); z++) {
                                            Node n = nl.item(z);
                                            //BLog.e("MEDIA","media:content:name: "+n.getNodeName());
                                            if(n.getNodeName().equals("media:thumbnail")) {
                                                String urle =n.getAttributes().getNamedItem("url").getNodeValue();
                                                add.setString(RssItem.STRING_IMG_URL, urle);
                                            } else if(n.getNodeName().equals("media:player")) {
                                                String urle =n.getAttributes().getNamedItem("url").getNodeValue();
                                                add.setString(RssItem.STRING_MEDIA_URL, urle);
                                            } else if(n.getNodeName().equals("media:description")) {
                                                String desc =Sf.stripHtml(n.getTextContent()).trim();
                                                add.setString(RssItem.STRING_TEXT, desc);
                                            } else if(n.getNodeName().equals("media:content")) {
                                                String urle =n.getAttributes().getNamedItem("url").getNodeValue();
                                                add.setString(RssItem.STRING_MEDIA_URL, urle);
                                            }

                                        }
                                    }
                                    if(add.getString(RssItem.STRING_IMG_URL).isEmpty()) {
                                        try {
                                            String urle = node.getAttributes().getNamedItem("url").getNodeValue();
                                            add.setString(RssItem.STRING_IMG_URL, urle);
                                        } catch(Exception ein) {}
                                    }

                                } catch(Exception e) {
                                    //BLog.e("MEDIA",""+e.getMessage());
                                }
                            }else if(node.getNodeName().equals("title")) {
                            		title =node.getTextContent();
                            }

                        }
                        //BLog.e("4");
                        if(add.getLong(RssItem.LONG_DATE)==0)
                        	add.setLong(RssItem.LONG_DATE, Cal.getUnixTime());
                        if(publisher==null || publisher.length()<2) {
                        	if(title!=null)
                        		publisher=title;
                        }
                        add.setString(RssItem.STRING_PUBLISHER, publisher);

                        if(NewsFiltersDb.canShowFeed(add)) {
                            add.setBoolean(RssItem.BOOL_SHOW_BRIEF,true);
                        } else {
                            add.setBoolean(RssItem.BOOL_SHOW_BRIEF,false);
                        }

                        //BLog.e("RSS",add.toString());


                        getRss.add(add);
                    }
                }
                page.setItems(getRss);
            } else {
                BLog.e("RSS","ZERO VLUE size: "+items.getLength());
            }
        } catch(SAXParseException e) {
            BLog.e("Rss getRssFromUrl() ", "" + e.getMessage());
            page.setErrormessage("101");
        }
        catch(Exception e) {
            BLog.e("Rss getRssFromUrl() ", "" + e.getMessage());
            page.setErrormessage("102");
        }
        //BLog.e("page gor count: "+page.getItems().size());
        return page;
    }
    
}