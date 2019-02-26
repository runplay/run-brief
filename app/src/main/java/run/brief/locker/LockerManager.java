package run.brief.locker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import run.brief.HomeFarm;
import run.brief.R;
import run.brief.b.State;
import run.brief.beans.BriefSettings;
import run.brief.beans.LockerItem;
import run.brief.util.log.BLog;
import run.brief.secure.Encrypt;
import run.brief.secure.Validator;
import run.brief.secure.delete.SecureDeleteFile;
import run.brief.util.Cal;
import run.brief.util.FileReadTask;
import run.brief.util.FileWriteTask;
import run.brief.util.Files;
import run.brief.util.TextFile;
import run.brief.util.json.JSONArray;
import run.brief.util.json.JSONException;
import run.brief.util.json.JSONObject;

public final class LockerManager {
	
	private static final LockerManager L = new LockerManager();
	private LockerDb DB = new LockerDb();
	
	public static final long LOCKER_OPEN_0MIN = 0;
	public static final long LOCKER_OPEN_1MIN = 60000;
	public static final long LOCKER_OPEN_5MIN = 300000;
	public static final long LOCKER_OPEN_30MIN = 1800000;
	
	private boolean isOpen=false;
	private String lastMessage="";
	private boolean isLockerInterfaceOpen=false;
	
	
	
	private static final String PASSCODE_NO_PASS="-1";
    //private static final String PASSCODE_NO_PASS_VALUE="0000";
	//private static FileWriteTask fwt;
	//private static FileReadTask frt;
	
	//private List<LockerItem> items=new ArrayList<LockerItem>();
	
	public static void setLockerInterface(Boolean open) {
		if(Validator.isValidCaller())
			L.isLockerInterfaceOpen=open;
	}

	protected static boolean isLockerInterfaceOpen() {
		if(Validator.isValidCaller())
			return L.isLockerInterfaceOpen;
		return false;
	}
	
	public static LockerItem getLockerItem(int index) {
		if(Validator.isValidCaller() && L.isOpen)
			return L.DB.locker.get(index);
		else
			return null;
	}
	public static final List<LockerItem> getLockerItems() {
		if(Validator.isValidCaller()) 
			return L.DB.locker;
		else 
			return new ArrayList<LockerItem>();
	}
	public static boolean requiresPassCode() {
		BriefSettings bs = State.getSettings();
		String pc=L.DB.getPasscode();//bs.getString(BriefSettings.STRING_LOCKER_PASSWORD);
        //BLog.e("P",pc);
		if(pc!=null && pc.equals(PASSCODE_NO_PASS))
			return false;
		return true;
	}
	public static int addFilesToLocker(Context context,List<File> files) {
        if(Validator.isValidCaller()) {
            int count = 0;
            int itc = files.size();
            for (File f : files) {
                if(!f.getPath().startsWith(Files.HOME_PATH_LOCKER)) {
                    boolean s = addFileToLocker(context,f.getPath());
                    if (s) {
                        count++;
                    }
                }
            }


            L.DB.Save(context);
            return count;

        }
		return 0;
	}

    public static Bitmap getLockerImage(Activity activity,LockerItem li, int maxWidth) {
        Bitmap bitmap=null;

        if(isOpen() && li!=null && li.exists()) {
            System.gc();
            File lockerFile = new File(Files.HOME_PATH_LOCKER+File.separator+Files.FOLDER_LOCKER+File.separator+li.getString(LockerItem.STRING_LOCKER_FILE_NAME));
            if(lockerFile.exists()) {
                int size = (int) lockerFile.length();
                byte[] bytes = new byte[size];
                boolean read = false;
                //BLog.e("LOCK0", ""+file.getPath());
                try {
                    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(lockerFile));
                    buf.read(bytes, 0, bytes.length);
                    buf.close();
                    read = true;
                } catch (FileNotFoundException e) {

                    //BLog.e("LOCK", "" + e.getMessage());
                } catch (IOException e) {

                    //BLog.e("LOCK", "" + e.getMessage());
                }
                if (read) {
                    try {
                        Encrypt enc = new Encrypt(li.getString(LockerItem.STRING_ENCRYPT_KEY));
                        //BLog.e("LOCK3", "---------7");

                        byte[] decbytes = enc.decrypt(bytes);
                        //BLog.e("LOCK3", "---------8");
                        enc=null;
                        BitmapFactory.Options options=new BitmapFactory.Options();// Create object of bitmapfactory's option method for further option use
                        options.inPurgeable = true; // inPurgeable is used to free up memory while required
                        Bitmap tmpImage = BitmapFactory.decodeByteArray(decbytes,0, decbytes.length,options);//Decode image, "thumbnail" is the object of image file
                        if(tmpImage.getWidth()>maxWidth) {
                            double ratio = tmpImage.getWidth()/maxWidth;

                            int w = Double.valueOf(tmpImage.getWidth()/ratio).intValue();
                            int h = Double.valueOf(tmpImage.getHeight()/ratio).intValue();

                            bitmap = Bitmap.createScaledBitmap(tmpImage, w , h , true);
                        } else {
                            bitmap = tmpImage;
                        }
                        // convert decoded bitmap into well scalled Bitmap format.

                        //imageview.SetImageDrawable(songImage);
                        //bitmap = BitmapFactory.decodeByteArray(decbytes , 0, decbytes .length);
                    } catch (Exception e) {
                        //BLog.e("UNLOCK", "" + e.getMessage());
                    }
                }
            }
        }
        return bitmap;
    }

	private static boolean addFileToLocker(Context context,String filePath) {
		
		File file = new File(filePath);
		if(isOpen() && file.exists() && file.length()>0) {
		    int size = (int) file.length();
		    byte[] bytes = new byte[size];
		    boolean read=false;
		    //BLog.e("LOCK0", ""+file.getPath());
		    try {
		        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
		        buf.read(bytes, 0, bytes.length);
		        buf.close();
		        read=true;
		    } catch (FileNotFoundException e) {
		        // TODO Auto-generated catch block
		    	BLog.e("LOCK", ""+e.getMessage());
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    	BLog.e("LOCK", ""+e.getMessage());
		    }
		    if(read) {
		    	
		    	try {
		    		String key = Encrypt.getRandomKey(Encrypt.KEYLENGTH_256);
		    		String lfilename = Encrypt.getRandomKey(Encrypt.KEYLENGTH_32)+".bl";
		    		//BLog.e("LOCK3", "---------4");
		    		LockerItem li = new LockerItem(); 
		    		li.setString(LockerItem.STRING_ENCRYPT_KEY, key);
		    		//BLog.e("LOCK3", "---------5");
		    		li.setString(LockerItem.STRING_ORIGIN_PATH, filePath);
		    		li.setLong(LockerItem.LONG_DATE_CREATE, Cal.getUnixTime());
		    		li.setLong(LockerItem.LONG_ORIGN_FILE_SIZE, file.length());
		    		//BLog.e("LOCK3", "---------6");
		    		li.setString(LockerItem.STRING_LOCKER_FILE_NAME, lfilename);
		    		
		    		Encrypt enc = new Encrypt(key);
		    		//BLog.e("LOCK3", "---------7");
		    		byte[] encbytes = enc.encrypt(bytes);
		    		//BLog.e("LOCK3", "---------8");
		    		Files.ensurePathAndFile(Files.HOME_PATH_LOCKER+File.separator+Files.FOLDER_LOCKER,lfilename);
		    		//BLog.e("LOCK3", "---------9");
		    		FileWriteTask.writeBytesToDisk(Files.HOME_PATH_LOCKER+File.separator+Files.FOLDER_LOCKER+File.separator+lfilename, encbytes);
		    		
		    		// do some verification writes and deletes are ok (multi 'safe' delete files.
                    File added = new File(Files.HOME_PATH_LOCKER+File.separator+Files.FOLDER_LOCKER+File.separator+lfilename);
                    if(added.exists()) {
                        L.DB.add(context,li);

                        SecureDeleteFile.delete(file);

                        //BLog.e("LOCK3", "---------710000");
                        return true;

                    }
		    	} catch(Exception e) {
		    		//BLog.e("LOCK3", ""+e.getMessage());
		    	}
		    	
		    	
		    }

		}
		return false;
	}
	public static boolean removeFileFromLocker(Context context,LockerItem li) {
		if(li!=null && li.exists()) {
            File lockerFile = new File(Files.HOME_PATH_LOCKER+File.separator+Files.FOLDER_LOCKER+File.separator+li.getString(LockerItem.STRING_LOCKER_FILE_NAME));
            if(lockerFile.exists()) {
                int size = (int) lockerFile.length();
                byte[] bytes = new byte[size];
                boolean read = false;
                //BLog.e("LOCK0", ""+file.getPath());
                try {
                    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(lockerFile));
                    buf.read(bytes, 0, bytes.length);
                    buf.close();
                    read = true;
                } catch (FileNotFoundException e) {

                    //BLog.e("LOCK", "" + e.getMessage());
                } catch (IOException e) {

                    //BLog.e("LOCK", "" + e.getMessage());
                }
                if (read) {
                    try {
                        Encrypt enc = new Encrypt(li.getString(LockerItem.STRING_ENCRYPT_KEY));
                        //BLog.e("LOCK3", "---------7");

                        byte[] decbytes = enc.decrypt(bytes);
                        //BLog.e("LOCK3", "---------8");
                        Files.ensurePathAndFile(Files.getPathLessFileName(li.getString(LockerItem.STRING_ORIGIN_PATH)), Files.getFileNameFromPath(li.getString(LockerItem.STRING_ORIGIN_PATH)));
                        //BLog.e("LOCK3", "---------9");
                        FileWriteTask.writeBytesToDisk(li.getString(LockerItem.STRING_ORIGIN_PATH), decbytes);

                        File unlocked = new File(li.getString(LockerItem.STRING_ORIGIN_PATH));
                        if (unlocked.exists()) {
                            L.DB.remove(context,li);

                            SecureDeleteFile.delete(lockerFile);

                            //BLog.e("LOCK3", "---------710000");
                            return true;

                        }

                    } catch (Exception e) {
                        //BLog.e("UNLOCK", "" + e.getMessage());
                    }
                }
            } else {
                L.DB.remove(context,li);
                return true;
            }
        }
		return false;
	}

	private static byte[] getFileUnlockedData(LockerItem li, String passCode) {
		byte[] encbytes = FileReadTask.readBytesFromDisk(Files.HOME_PATH_LOCKER+File.separator+Files.FOLDER_LOCKER+File.separator+li.getString(LockerItem.STRING_LOCKER_FILE_NAME));
		if(encbytes!=null &&  isValidPass(passCode)) {
			try {
    		Encrypt enc = new Encrypt(li.getString(LockerItem.STRING_ENCRYPT_KEY));
    		byte[] rawbytes = enc.encrypt(encbytes);
    		return rawbytes;
			} catch(Exception e) {}
		}
		return null;
	}
 	private static boolean isValidPass(String passCode) {
 		//String compare = L.DB.passcode;//State.getSettings().getString(BriefSettings.STRING_LOCKER_PASSWORD);
            //BriefSettings set = State.getSettings();
        //set.setString(BriefSettings.STRING_LOCKER_PASSWORD,"2222");
        //set.save();
//BLog.e("PW",L.DB.passcode+"--"+passCode);
 		if(L.DB.passcode!=null && L.DB.passcode.equals(passCode)) {
 			return true;
 		}
 		return false;
 	}
	public static boolean open(Activity activity, String passCode) {
        //BLog.e("TEST",!L.isOpen +"--"+ isLockerEnabled() +"--"+ isValidPass(passCode));
        if(isLockerEnabled()) {
            L.DB.init(activity);
            if (L.DB.isInitialised && isValidPass(passCode)){
                L.isOpen =  true;
                return L.isOpen;

            }
        }
		return false;
	}
    public static boolean isValidPassCode(String passCode) {
        //return true;
        if (passCode!=null && passCode.matches("[0-9]+") && passCode.length() > 2 && passCode.length()<21)
            return true;
        return false;
    }
	public static boolean createLockerFirstTimeAndOpen(Activity activity, String password) {
		
		File f = new File(Files.HOME_PATH_LOCKER+File.separator+Files.FILENAME_FILE_LOCKER);
		if(!f.exists()) {
			//BLog.e("LOCKER","creating first toime");
			// not exists, so is first time
			Files.ensurePath(Files.HOME_PATH_LOCKER+File.separator+Files.FOLDER_LOCKER);

			Files.ensurePathAndFile(Files.HOME_PATH_LOCKER,Files.FILENAME_FILE_LOCKER);
			Files.ensurePathAndFile(Files.HOME_PATH_LOCKER+File.separator+Files.FOLDER_LOCKER,"_"+activity.getString(R.string.readme_file_name)+".txt");
			TextFile.writeToFile(Files.HOME_PATH_LOCKER+File.separator+Files.FOLDER_LOCKER+File.separator+"_"+activity.getString(R.string.readme_file_name)+".txt", activity.getString(R.string.readme_file_text));
			
			// create locker
			//BriefSettings bs = State.getSettings();
			if(password==null)
				password=PASSCODE_NO_PASS;
			L.DB.passcode=password;//bs.setString(BriefSettings.STRING_LOCKER_PASSWORD, password);
            L.DB.Save(activity);
			//bs.save();
			
			open(activity,password);
			
			
			
			return true;
		}
		
		return false;
	}
	public static final boolean isLockerEnabled() {
		File f = new File(Files.HOME_PATH_LOCKER+File.separator+Files.FILENAME_FILE_LOCKER);
        if(f.exists()) {
            if(f.length()>0) {
                return true;
            } else {
                f.delete();
                return false;
            }
        }
        return false;
        //BLog.e("PW", "file: "+f.exists()+"-"+f.getPath());
		//return f.exists() && f.length()>0;
	}
	public static final boolean isOpen() {
		return L.isOpen;
	}
	
	public static void dbRemove(Context context,LockerItem li) {
        L.DB.remove(context,li);
    }
	
	
	
	private class LockerDb {

		
		private static final String dbArrayName="locker";
		
		private List<LockerItem> locker=new ArrayList<LockerItem>();
        private String passcode;
		//private boolean isLoaded=false;
		private void setPasscode(String code) {
            this.passcode=code;
        }
        private String getPasscode() {
            return passcode;
        }
		private FileWriteTask fwt;
		private FileReadTask frt;
		
		private boolean isInitialised=false;
		
		public boolean isInitialised() {
			return isInitialised;
		}
		
		public FileWriteTask getFwt() {
			return fwt;
		}

		public FileReadTask getFrt() {
			return frt;
		}

		private LockerDb() {
			//Load();
		}
		
		public List<LockerItem> getLockerItems() {
			return L.DB.locker;
		}
        public void remove(Context context,LockerItem item) {
            if(item!=null) {
                L.DB.locker.remove(item);
                Save(context);
            }
        }
		public void Update(Context context,List<LockerItem> items) {
			if(items!=null) {
				L.DB.locker=items;
				Save(context);
			}
		}
		public void add(Context context,LockerItem item) {
			//BLog.e("LF", "------L DB ADD");
			if(item!=null) {
				L.DB.locker.add(item);
				Save(context);
				//BLog.e("LF", "------L DB ADDEEEEED");
			}
		}
		public boolean Save(Context context) {
			if(Validator.isValidCaller()) {
				try {

                    JSONObject job = new JSONObject();
                    job.put("pw",passcode);
					JSONArray array = new JSONArray();
					
					//Set<Integer> keys = NEWS.feeds.keySet();
					for(LockerItem li: L.DB.locker) {
					//for(int i=0; i<NEWS.feeds.size(); i++) {
						//ArrayList<RssFeed> feeds = get(key);
						//if(!li.isEmpty()) {
							//for(RssFeed feed: feeds) {
								array.put(li.getBean());
							//}
						
					}
                    job.put("ar",array);
					fwt=new FileWriteTask(HomeFarm.getLocKey(context),Files.HOME_PATH_LOCKER, Files.FILENAME_FILE_LOCKER, job.toString());
					return fwt.WriteSecureToSd();
		
				} catch(Exception e) {
					BLog.e("SAVE",e.getMessage());
					
				}
			}
			return false;
		}
		
		public boolean init(Context context) {
			//BLog.e("DB","data1: "+(!L.DB.isInitialised()) +"--"+ (L.DB.locker==null) +"--"+ Validator.isCallerLockerManager());
			if(!L.DB.isInitialised() && L.DB.locker.isEmpty() && Validator.isValidCaller()) {
				
				frt = new FileReadTask(HomeFarm.getLocKey(context),Files.HOME_PATH_LOCKER, Files.FILENAME_FILE_LOCKER);
				//frt = new FileReadTask(Files.HOME_PATH, Files.FILENAME_GENERAL_SETTINGS);
				
				//String file = Files.ReadFromSd(Files.HOME_PATH, Files.FILENAME_NOTES_DATABASE);     
				if(frt.ReadSecureFromSd()) {
                    //BLog.e("DB","data2: ");
					if(frt.getFilesize()==0) {
						L.DB.locker = new ArrayList<LockerItem>();
						L.DB.isInitialised=true;
						return true;
					}
						
					try {
						//BLog.e("LM", ""+frt.getFileContent());
                        JSONObject job = new JSONObject(frt.getFileContent());
						//JSONObject db = new JSONObject(frt.getFileContent());
						if(job!=null) {
                            passcode=job.getString("pw");
                            JSONArray cats = job.getJSONArray("ar");
                            //BLog.e("DB","data4: ");
							L.DB.locker = new ArrayList<LockerItem>();
							
							//if(cats!=null) {
							for(int i=0; i<cats.length(); i++) {
								L.DB.locker.add(new LockerItem(cats.getJSONObject(i)));
							}
							L.DB.isInitialised=true;
							return true;
							//}
							
							//NEWS.isLoaded=true;
						}
					} catch(JSONException e) {
						
						BLog.e("Locker",e.getMessage());
					}
				} else {
					BLog.e("SettingsDb.init().no read",frt.getStatusMessage());
				}
			}
			if(L.DB.isInitialised())
				return true;
		
			return false;
		}
	}
	
	
}
