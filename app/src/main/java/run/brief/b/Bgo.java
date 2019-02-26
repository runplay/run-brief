package run.brief.b;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import run.brief.BriefHomeFragment;
import run.brief.NewActionFragment;
import run.brief.R;
import run.brief.contacts.ContactViewFragment;
import run.brief.contacts.ContactsHomeFragment;
import run.brief.d2d.P2PChatFragment;
import run.brief.direct.DirectHomeFragment;
import run.brief.email.EmailFoldersFragment;
import run.brief.email.EmailHomeFragment;
import run.brief.email.EmailSendFragment;
import run.brief.email.EmailViewFragment;
import run.brief.locker.LockerFragment;
import run.brief.news.NewsChooseFeedsFragment;
import run.brief.news.NewsHomeFragment;
import run.brief.news.ViewNewsItemFragment;
import run.brief.news.ViewNewsItemWebFragment;
import run.brief.notes.NotesEditFragment;
import run.brief.notes.NotesHomeFragment;
import run.brief.phone.PhoneHomeFragment;
import run.brief.search.SearchFragment;
import run.brief.settings.AboutFragment;
import run.brief.settings.AccountsHomeFragment;
import run.brief.settings.EmailEditFragment;
import run.brief.settings.EmailEditServerFragment;
import run.brief.settings.EmailEditSignaturesFragment;
import run.brief.settings.GmailAddFragment;
import run.brief.settings.HelpFragment;
import run.brief.settings.LegalFragment;
import run.brief.settings.SettingsCommsFragment;
import run.brief.settings.SettingsDataFragment;
import run.brief.settings.SettingsHomeTabbedFragment;
import run.brief.sms.SmsHomeFragment;
import run.brief.sms.SmsSendFragment;
import run.brief.twitter.TwitterHomeFragment;
import run.brief.util.BriefActivityManager;
import run.brief.util.Files;
import run.brief.util.PlusMember;
import run.brief.util.camera.CameraFragment;
import run.brief.util.explore.FileExploreFragment;
import run.brief.util.explore.FileItem;
import run.brief.util.explore.FilesDeleteFragment;
import run.brief.util.explore.FolderChooseFragment;
import run.brief.util.explore.ImagesSliderFragment;
import run.brief.util.explore.TextFileFragment;
import run.brief.util.explore.fm.FileManagerDisk;
import run.brief.util.explore.fm.FileManagerList;
import run.brief.util.json.JSONArray;
import run.brief.util.log.BLog;
import run.brief.util.pay.PlusMemberFragment;

public final class Bgo {

    private static Activity useActivity;

    private static void setUseActivity(Activity activity) {
        useActivity=activity;
    }

	public static boolean action(Activity activity, MenuItem item) {
        actionGeneral(activity, item);
		return false;
	}



	private static void actionGeneral(Activity activity, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search:
			State.sectionsClearBackstack();
			openFragmentBackStackAnimate(activity, SearchFragment.class);
			break;
		case R.id.action_contacts_add_new:
			BriefActivityManager.openAndroidContactsCreateNew(activity);
			//openFragment(activity, new NotesEditFragment());
			break;


		default:
			break;

		}

	}




	public static void openCurrentState(Activity activity) {
		Class<? extends BFragment> fragment = null;
		//BLog.e("STATE", "bgo: " + State.getCurrentSection() + ", size: "+ State.getSectionsSize());
		switch (State.getCurrentSection()) {
			case State.SECTION_BRIEF:
				fragment = BriefHomeFragment.class;
				break;
			case State.SECTION_CONTACTS:
				fragment = ContactsHomeFragment.class;
				break;
			case State.SECTION_CONTACTS_ITEM:
				fragment = ContactViewFragment.class;
				break;

			case State.SECTION_SMS:
				fragment = SmsHomeFragment.class;
				break;
			case State.SECTION_SMS_SEND:
				fragment = SmsSendFragment.class;
				break;
			case State.SECTION_CAMERA:
				fragment = CameraFragment.class;
				break;
			case State.SECTION_SETTINGS:
				fragment = SettingsHomeTabbedFragment.class;
				break;
			case State.SECTION_NEWS:
				fragment = NewsHomeFragment.class;
				break;
			case State.SECTION_NEWS_VIEW:
				fragment = ViewNewsItemFragment.class;
				break;
			case State.SECTION_NEWS_CHOOSE:
				fragment=NewsChooseFeedsFragment.class;
				break;
			case State.SECTION_SEARCH:
				fragment = SearchFragment.class;
				break;
			case State.SECTION_ACCOUNTS:
				fragment = AccountsHomeFragment.class;
				break;
			case State.SECTION_PHONE:
				fragment = PhoneHomeFragment.class;
				break;
			case State.SECTION_NOTES:
				fragment = NotesHomeFragment.class;
				break;
			case State.SECTION_NOTES_ITEM:
				fragment = NotesEditFragment.class;
				break;
			case State.SECTION_EMAIL:
				fragment = EmailHomeFragment.class;
				break;
			case State.SECTION_EMAIL_VIEW:
				fragment = EmailViewFragment.class;
				break;
			case State.SECTION_EMAIL_FOLDER:
				fragment = EmailFoldersFragment.class;
				break;
			case State.SECTION_EMAIL_NEW:
				fragment = EmailSendFragment.class;
				break;
			case State.SECTION_EMAIL_SIGNATURES:
				fragment = EmailEditSignaturesFragment.class;
				break;
			//case State.SECTION_TWITTER:
			//	fragment = new TwitterHomeFragment();
			//	break;
			case State.SECTION_FILE_EXPLORE:
				fragment = FileExploreFragment.class;
				break;
			case State.SECTION_FILE_EXPLORE_DELETE:
				fragment = FilesDeleteFragment.class;
				break;
			case State.SECTION_D2D:
				fragment = P2PChatFragment.class;
				break;
			case State.SECTION_LOCKER:
				fragment = LockerFragment.class;
				break;
			case State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL:
				fragment = EmailEditFragment.class;
				break;
			case State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL_SERVERS:
				fragment = EmailEditServerFragment.class;
				break;
			case State.SECTION_HELP:
				fragment = HelpFragment.class;
				break;
			case State.SECTION_SETTINGS_DATA:
				fragment = SettingsDataFragment.class;
				break;
			case State.SECTION_SETTINGS_ABOUT:
				fragment = AboutFragment.class;
				break;
			case State.SECTION_LEGAL:
				fragment = LegalFragment.class;
				break;
			case State.SECTION_OAUTH_GOOGLE:
				fragment = GmailAddFragment.class;
				break;
			case State.SECTION_PLUS_MEMBER:
				fragment = PlusMemberFragment.class;
				break;
			case State.SECTION_DIRECT:
				fragment = DirectHomeFragment.class;
				break;
			case State.SECTION_POP_FOLDER_CHOOSER:
				fragment = FolderChooseFragment.class;
				break;
			case State.SECTION_IMAGES_SLIDER:
				fragment = ImagesSliderFragment.class;
				break;

			case State.SECTION_TEXT_FILE_VIEW:
				fragment= TextFileFragment.class;
				break;
			case State.SECTION_NEWS_WEBVIEW:
				fragment= ViewNewsItemWebFragment.class;
				break;
			case State.SECTION_SETTINGS_NETWORK:
				fragment= SettingsCommsFragment.class;
				break;

			default:
				fragment = BriefHomeFragment.class;
				break;
		}
		if (fragment != null) {
			BLog.e("STATE", "bgo: " +fragment.getName()+" -- "+ State.getCurrentSection() + ", size: "+ State.getSectionsSize());
			Bgo.openFragment(activity, fragment);
		}
	}

    //@SuppressWarnings()
	public static boolean openFragment(Activity activity, Class<? extends Fragment> fragment) {
        //if(!activity.isDestroyed()) {
            setUseActivity(activity);

            Device.hideKeyboard(activity);
            State.sectionsGoBackstack();
            FragmentManager fm = activity.getFragmentManager();
            FragmentTransaction tr = fm.beginTransaction();
            tr.replace(R.id.container, Fragment.instantiate(activity, fragment.getName()),fragment.getName());
            //tr.replace(R.id.container, fragment, fragment.getClass().getName());

            tr.commit();
        //}
		return true;
	}

	public static boolean openFragmentAnimate(Activity activity, Class<? extends Fragment> fragment) {
        setUseActivity(activity);

		Device.hideKeyboard(activity);
		State.sectionsGoBackstack();
		FragmentManager fm = activity.getFragmentManager();
		FragmentTransaction tr = fm.beginTransaction();
        tr.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		tr.replace(R.id.container, Fragment.instantiate(activity, fragment.getName()), fragment.getName());
		tr.commit();
		return true;
	}

	public static boolean openFragmentBackStackAnimate(Activity activity,Class<? extends Fragment> fragment) {
        setUseActivity(activity);
		Device.hideKeyboard(activity);

		FragmentManager fm = activity.getFragmentManager();
		FragmentTransaction tr = fm.beginTransaction();
        tr.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		tr.replace(R.id.container, Fragment.instantiate(activity, fragment.getName()), fragment.getName());

		tr.commit();
		return true;
	}

	public static boolean openFragmentBackStack(Activity activity,Class<? extends Fragment> fragment) {

        setUseActivity(activity);
		Device.hideKeyboard(activity);

		FragmentManager fm = activity.getFragmentManager();
		FragmentTransaction tr = fm.beginTransaction();
        tr.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        
		tr.replace(R.id.container, Fragment.instantiate(activity, fragment.getName()), fragment.getName());
		tr.commit();
		return true;
	}

	public static void clearBackStack(Activity activity) {
		FragmentManager fm = activity.getFragmentManager();
		int backStackCount = fm.getBackStackEntryCount();
		for (int i = 0; i < backStackCount; i++) {
			int backStackId = fm.getBackStackEntryAt(i).getId();

			fm.popBackStack(backStackId,
					FragmentManager.POP_BACK_STACK_INCLUSIVE);

		}
		State.sectionsClearBackstack();
	}

	public static void refreshFragment(Activity activity,String fragmentClassName) {
		FragmentManager fm = activity.getFragmentManager();
		try {
			BRefreshable f = (BRefreshable) fm
					.findFragmentByTag(fragmentClassName);
			if (f != null) {
				f.refresh();
			}
		} catch (Exception e) {

		}
	}

	public static void refreshBriefListview(Activity activity) {
		if (activity != null) {

			BRefreshable f = getCurrentRefeshableFragment(activity);
			if (f != null && State.getCurrentSection() == State.SECTION_BRIEF) {
				f.refresh();
			} else {

			}

		}
	}

	public static void refreshCurrentFragmentIfBrief(Activity activity) {
		if (activity != null) {
			BRefreshable f = getCurrentRefeshableFragment(activity);
			if (f != null && State.getCurrentSection() == State.SECTION_BRIEF) {
				f.refresh();
			}
		}
	}
    public static void tryRefreshCurrentFragment() {
        if (useActivity != null) {
            refreshCurrentFragment(useActivity);
        } else {
           // BLog.e("TRY","********************** activity=null");
        }
    }
    public static void tryRefreshDataCurrentFragment() {
        if (useActivity != null) {
            refreshDataCurrentFragment(useActivity);
        }
    }
    public static void tryRefreshCurrentIfFragment(Class ifRefreshableClass) {
        if (useActivity != null) {
            refreshCurrentIfFragment(useActivity,ifRefreshableClass);
        }
    }
    public static void refreshDataCurrentIfFragment(Class ifRefreshableClass) {
        if (useActivity != null) {

            final BRefreshable f = getCurrentRefeshableFragment(useActivity);
            //BLog.e("REFC","current: "+f.getClass().getName()+" -- need match : "+ifRefreshableClass.getName());
            if (f != null && f.getClass().getName().equals(ifRefreshableClass.getName())) {
                useActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        f.refreshData();
                    }
                });
            }

        }
    }
	public static void refreshCurrentFragment(Activity activity) {
		if (activity != null) {

			BRefreshable f = getCurrentRefeshableFragment(activity);
			if (f != null) {

				f.refresh();
			}

		}
	}
    public static void refreshDataCurrentFragment(Activity activity) {
        if (activity != null) {

            BRefreshable f = getCurrentRefeshableFragment(activity);
            if (f != null) {
                f.refreshData();
            }

        }
    }
    public static void refreshDataCurrentIfFragment(Activity activity,Class ifRefreshableClass) {
        if (activity != null) {

            BRefreshable f = getCurrentRefeshableFragment(activity);
            if (f != null && f.getClass().getName().equals(ifRefreshableClass.getName())) {
                f.refreshData();
            }

        }
    }
	public static void refreshCurrentIfFragment(Activity activity,Class ifRefreshableClass) {
		if (activity != null) {

			final BRefreshable f = getCurrentRefeshableFragment(activity);
            //BLog.e("REFC","current: "+f.getClass().getName()+" -- need match : "+ifRefreshableClass.getName());
			if (f != null && f.getClass().getName().equals(ifRefreshableClass.getName())) {
				activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        f.refresh();
                    }
                });
			}

		}
	}

	public static BRefreshable getCurrentRefeshableFragment(Activity activity) {
		if (activity != null) {
			FragmentManager fm = activity.getFragmentManager();
			try {

				BRefreshable f = (BRefreshable) fm
						.findFragmentByTag(getFragmentNameBystate(State.getCurrentSection()));
                //BLog.e("REF","---"+getFragmentNameBystate(State.getCurrentSection()));
				if (f != null) {
					return f;
				} else {
                    BRefreshable fr = (BRefreshable) fm
                            .findFragmentById(R.id.container);
                    if (fr != null) {
                        return fr;
                    }
                }
			} catch (Exception e) {
               // BLog.e("REF","ex: "+e.toString());
			}
		}
		return null;
	}
    public static void removeFragmentFromFragmentManager(Activity activity, String TAG_FRAGMENT) {
        if (activity != null) {
            Fragment fragment = activity.getFragmentManager().findFragmentByTag(TAG_FRAGMENT);
            if(fragment != null)
                activity.getFragmentManager().beginTransaction().remove(fragment).commit();


        }
    }
	private static String getFragmentNameBystate(int STATE_) {
		String fragname = null;
		switch (STATE_) {
		case State.SECTION_NOTES:
			fragname = NotesHomeFragment.class.getName();
			break;
		case State.SECTION_NOTES_ITEM:
			fragname = NotesEditFragment.class.getName();
			break;
		case State.SECTION_NEW_ACTION:
			fragname = NewActionFragment.class.getName();
			break;
		case State.SECTION_NEWS:
			fragname = NewsHomeFragment.class.getName();
			break;
		case State.SECTION_NEWS_VIEW:
			fragname = ViewNewsItemFragment.class.getName();
			break;
		case State.SECTION_NEWS_CHOOSE:
			fragname = NewsChooseFeedsFragment.class.getName();
			break;
		case State.SECTION_CAMERA:
			fragname = CameraFragment.class.getName();
			break;

		case State.SECTION_CONTACTS:
			fragname = ContactsHomeFragment.class.getName();
			break;
		case State.SECTION_CONTACTS_ITEM:
			fragname = ContactViewFragment.class.getName();
			break;
		case State.SECTION_EMAIL:
			fragname = EmailHomeFragment.class.getName();
			break;
		case State.SECTION_EMAIL_VIEW:
			fragname = EmailViewFragment.class.getName();
			break;
		case State.SECTION_EMAIL_NEW:
			fragname = EmailSendFragment.class.getName();
			break;
		case State.SECTION_EMAIL_FOLDER:
			fragname = EmailFoldersFragment.class.getName();
			break;
        case State.SECTION_EMAIL_SIGNATURES:
            fragname = EmailEditSignaturesFragment.class.getName();
            break;
		case State.SECTION_SMS:
			fragname = SmsHomeFragment.class.getName();
			break;
        case State.SECTION_SEARCH:
            fragname = SearchFragment.class.getName();
            break;
		case State.SECTION_SMS_SEND:
			fragname = SmsSendFragment.class.getName();
			break;
		case State.SECTION_ACCOUNTS:
			fragname = AccountsHomeFragment.class.getName();
			break;
		case State.SECTION_SETTINGS:
			fragname = SettingsHomeTabbedFragment.class.getName();
			break;
        case State.SECTION_PHONE:
            fragname = PhoneHomeFragment.class.getName();
            break;
		case State.SECTION_FILE_EXPLORE:
			fragname = FileExploreFragment.class.getName();
			break;
		case State.SECTION_FILE_EXPLORE_DELETE:
			fragname = FilesDeleteFragment.class.getName();
			break;
		case State.SECTION_TWITTER:
			fragname = TwitterHomeFragment.class.getName();
			break;
		case State.SECTION_D2D:
			fragname = P2PChatFragment.class.getName();
			break;
		case State.SECTION_LOCKER:
			fragname = LockerFragment.class.getName(); 
			break;
        case State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL:
            fragname = EmailEditFragment.class.getName();
            break;
        case State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL_SERVERS:
            fragname = EmailEditServerFragment.class.getName();
            break;
        case State.SECTION_HELP:
            fragname = HelpFragment.class.getName();
            break;
        case State.SECTION_SETTINGS_DATA:
            fragname = SettingsDataFragment.class.getName();
            break;
        case State.SECTION_SETTINGS_ABOUT:
            fragname = AboutFragment.class.getName();
            break;
		case State.SECTION_LEGAL:
			fragname = LegalFragment.class.getName();
			break;
		case State.SECTION_OAUTH_GOOGLE:
			fragname = GmailAddFragment.class.getName();
			break;
		case State.SECTION_PLUS_MEMBER:
			fragname = PlusMember.class.getName();
			break;
		case State.SECTION_DIRECT:
			fragname = DirectHomeFragment.class.getName();
			break;
		case State.SECTION_POP_FOLDER_CHOOSER:
			fragname = FolderChooseFragment.class.getName();
			break;

		case State.SECTION_TEXT_FILE_VIEW:
			fragname = TextFileFragment.class.getName();
			break;
		case State.SECTION_NEWS_WEBVIEW:
			fragname = ViewNewsItemWebFragment.class.getName();
			break;
		case State.SECTION_SETTINGS_NETWORK:
			fragname = SettingsCommsFragment.class.getName();
			break;
		default:
			fragname = BriefHomeFragment.class.getName();
			break;


		}
		return fragname;
	}

	public static void goPreviousFragment(Activity activity) {
		Device.hideKeyboard(activity);
//BLog.e("SS","sections size: "+State.getSectionsSize());
		if (State.getSectionsSize() == 0) {
			Bgo.clearBackStack(activity);
			Bgo.openFragment(activity, BriefHomeFragment.class);
		} else {
			State.sectionsGoBackstack();
			Bgo.openCurrentState(activity);

		}

	}
	public static void openFile(Activity activity, FileManagerDisk fm, File f) {
		if(f!=null) {

			if(Files.isImage(f.getName())) {

				//boolean hitfile=false;
				int usepos=0;
				List<FileItem> useitems = new ArrayList<FileItem>();

				for(int i=0; i<fm.getDirectory(activity).size(); i++) {
					File testFile =fm.getDirectoryItem(i);
					if(!Files.isImage(Files.removeBriefFileExtension(testFile.getName()))) {

					} else {
						useitems.add(fm.getDirectoryItem(i));

					}
					if(f.getName().equals(testFile.getName())) {

						usepos=useitems.size()-1;
					}
				}


				FileManagerList fml = new FileManagerList(useitems);
				fml.setStartAtPosition(usepos);
				State.addCachedFileManager(fml);

				Bgo.openFragmentBackStack(activity,ImagesSliderFragment.class);

			} else if(State.getFileExploreState()==State.FILE_EXPLORE_STATE_STANDALONE) {
				//openOptions(f.getAbsolutePath());
				Device.openAndroidFile(activity, f);
			} else {

				JSONArray jarr = new JSONArray();
				jarr.put(f.getAbsolutePath());
				//State.clearStateObjects(State.getPreviousSection());
				//Log.e("FEF", "back to: " + State.getPreviousSection() + " -- with--" + jarr.toString());
				State.addToState(State.SECTION_FILE_EXPLORE,new StateObject(StateObject.STRING_FILE_PATH,jarr.toString()));
				Bgo.goPreviousFragment(activity);
			}

		}
	}
}
