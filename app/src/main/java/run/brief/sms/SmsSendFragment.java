package run.brief.sms;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import run.brief.BriefManager;
import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Device;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.b.bEditText;
import run.brief.b.bImageButton;
import run.brief.b.bToPersonsView;
import run.brief.beans.Brief;
import run.brief.beans.BriefSettings;
import run.brief.beans.Person;
import run.brief.beans.SmsMsg;
import run.brief.beans.SmsSend;
import run.brief.contacts.ContactsDb;
import run.brief.contacts.ContactsSelectedClipboard;
import run.brief.service.BriefService;
import run.brief.util.Functions;
import run.brief.util.ViewManagerText;
import run.brief.util.eicon.EmoticonsGridAdapter.KeyClickListener;
import run.brief.util.json.JSONArray;
import run.brief.util.json.JSONObject;


public final class SmsSendFragment extends BFragment implements BRefreshable, KeyClickListener {
	
	private View view;
	private Activity activity;
	
	private bEditText message;
	private ViewManagerText manager;
	private ListView history;
	private bImageButton smsSendBtn;
	private TextView infoBox;
	private static bImageButton smsSmile;
	private int fdbindex;
	private SmsHistoryListAdapter hAdapt;

	private static SmsMsg fromSms;
	private static bToPersonsView toView;
	private LinearLayout sendPod;
	//private SendSms sendSmsTask;
	
	private boolean isSendTaskRunning = false;
	//private AlertDialog.Builder builder;
	private Handler handler;
	
	private static final int MI_BOTTOM_UP=4252;
	private static final int MI_TOP_DOWN=2625;
	private boolean sendOpen=true;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		
		this.activity=getActivity();
		//this.container=container;
		//this.inflater=inflater;
		
		//messageHandler.postDelayed(start, 10);
		view = inflater.inflate(R.layout.sms_send,container, false);
        cd = new ColorDrawable(activity.getResources().getColor(R.color.actionbar_sms));
		return view;
	}
	/*
	public static void setTos(HashMap<String,Person> tos) {
		to=tos;
		toView.setPersons(to);
		
	}
	*/

	@Override
    public void onResume() {
    	super.onResume();
    	BriefManager.clearController(activity);
    	State.setCurrentSection(State.SECTION_SMS_SEND);
    	
	
    	smsSendBtn =(bImageButton) view.findViewById(R.id.sms_send_btn);
    	smsSendBtn.setOnClickListener(sendSmsListner);



    	message = (bEditText) view.findViewById(R.id.field_sms_message);
    	
        B.addStyle(message,B.FONT_MEDIUM);

    	message.setOnClickListener(messageBoxListner);

    	
    	infoBox=(TextView) view.findViewById(R.id.sms_send_info_box);
    	
        history=(ListView) view.findViewById(R.id.preview_sms_history);
        

    	
		smsSmile = (bImageButton) view.findViewById(R.id.sms_i_smile);


        toView = (bToPersonsView) view.findViewById(R.id.sms_to_persons);
        toView.setContext(activity);

        toView.setMode(bToPersonsView.MODE_VIEW_ADD);
        toView.setContactsType(ContactsSelectedClipboard.CONTACTS_TYPE_NUMBER);
        //Relative Layout smsparent=(RelativeLayout)
        toView.setSearchParent((RelativeLayout)view);
        //toView.setActivity(activity);
        //toView.setPersons(to);
        toView.setMaxHeight(250);
        toView.invalidate();

    	//Device.checkKeyboardHeight(view, popupWindow, emoticonsCover);
		
		manager=null;
    	
    	
    	//message.requestFocus();
		manager = new ViewManagerText();
		manager.manageEditText(activity, message,this,this);
        manager.addClickListneerOpenEmoji(view,message,smsSmile);
        //smsSmile.setOnClickListener(manager.getSmilPopupListener(activity,view,message,smsSmile));
        message.setOnEditTextImeBackListener(new bEditText.EditTextImeBackListener() {
            public void onImeBack() {
                manager.dismissPopup();
                smsSmile.setImageDrawable(activity.getResources().getDrawable(R.drawable.i_smile));
                refreshSendButton();
            }
        });
		sendPod=(LinearLayout) view.findViewById(R.id.sms_send_pod);
		
		refresh();

    }
	public void refresh() {
		/*
		amb = new ActionModeBack(getActivity(), getActivity().getString(R.string.title_sms_send)
				,R.menu.sms_send
				, new ActionModeCallback() {
			@Override
			public void onActionMenuItem(ActionMode mode, MenuItem item) {
				onOptionsItemSelected(item);
			}
		});
*/
		ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),getActivity().getString(R.string.title_sms_send),R.menu.sms_send,R.color.actionbar_sms);

    	State.setContactsMode(State.CONTACT_MODE_SELECT_SMS);
        //boolean didfocus=false;
    	//BLog.e("REFRSH", "SMS Send");
        if(State.hasStateObject(State.SECTION_SMS_SEND,StateObject.INT_FORCE_NEW)) {
            fromSms=null;
            ContactsSelectedClipboard.clear();
            //Device.setKeyboard(activity, true);
            fdbindex=0;
            //Device.setKeyboard(activity,message,true);
            //didfocus=true;
//        	/history.removeAllViews();
        }
        if(State.hasStateObject(State.SECTION_SMS_SEND,StateObject.STRING_USE_DATABASE_ID)) {

            String id = State.getStateObjectString(State.SECTION_SMS_SEND,StateObject.STRING_USE_DATABASE_ID);
            fromSms=SmsDb.getByById(id);
            if(fromSms!=null) {
                ContactsSelectedClipboard.clear();
                Person p = ContactsDb.getWithTelephone(activity, fromSms.getMessageNumber());
                if(p==null) {
                    p= Person.getNewUnknownPerson(activity, fromSms.getMessageNumber(), null);

                }
                ContactsSelectedClipboard.addPerson(p);
                //message.requestFocus();

            }
        } else if(State.hasStateObject(State.SECTION_SMS_SEND,StateObject.INT_USE_SELECTED_INDEX)) {
	        
        	fdbindex = State.getStateObjectInt(State.SECTION_SMS_SEND,StateObject.INT_USE_SELECTED_INDEX);
        	fromSms=SmsDb.get(fdbindex);
        	if(fromSms!=null) {
        		ContactsSelectedClipboard.clear();
        		Person p = ContactsDb.getWithTelephone(activity, fromSms.getMessageNumber());
        		if(p==null) {
        			p= Person.getNewUnknownPerson(activity, fromSms.getMessageNumber(), null);

        		}
        		ContactsSelectedClipboard.addPerson(p);
                //message.requestFocus();

        	}
        } else if(State.hasStateObject(State.SECTION_SMS_SEND,StateObject.STRING_CONTACTS)) {
        	String json = State.getStateObjectString(State.SECTION_SMS_SEND,StateObject.STRING_CONTACTS);
        	JSONArray jar = new JSONArray(json);
        	ContactsSelectedClipboard.clear();
        	for(int i=0; i<jar.length(); i++) {
        		//BLog.e("GOT", jar.getString(i));
                Person p =new Person(new JSONObject(jar.getString(i)));
        		ContactsSelectedClipboard.addPerson(p);
        		
        	}
            Device.setKeyboard(activity,message,true);
            //didfocus=true;
        }

        if(State.hasStateObject(State.SECTION_SMS_SEND,StateObject.STRING_BJSON_OBJECT)) {
        	
        	JSONObject job = new JSONObject(State.getStateObjectString(State.SECTION_SMS_SEND,StateObject.STRING_BJSON_OBJECT));
        	if(job!=null) {
        		fdbindex = job.optInt("findex");
        		//BLog.e("SETTEXT", job.getString("text"));
        		message.setText(job.getString("text"));
        		if(fdbindex!=0) {
        			fromSms=SmsDb.get(fdbindex);
        		}
        	}
        	
        	
        	//romSms=null;
        	//ContactsSelectedClipboard.clear();
        	Device.setKeyboard(activity,message, true);
        }
    	
        State.clearStateObjects(State.SECTION_SMS_SEND);
    	



    	
    	history.invalidate();

    	if(fromSms==null && ContactsSelectedClipboard.size()==1) {
    		//BLog.e("CSMS", "adding from sms");
    		fromSms=new SmsMsg();
    		Set<String> pid = ContactsSelectedClipboard.get().keySet();
    		for(String key: pid) {
                Person p =ContactsSelectedClipboard.get().get(key);
    			fromSms.setMessageNumber((String) p.getJSONArray(Person.JSONARRAY_PHONE).get(p.getInt(Person.INT_INDEX_USE_PHONE)));
    			//BLog.e("CSMS", "addied ------");
    			break;
    		}
    		
    		
    	} else if(ContactsSelectedClipboard.size()!=1) {
    		fromSms=null;
    	}
        
        if(State.getSettings().getInt(BriefSettings.INT_CHAT_VIEW_LAYOUT)==BriefSettings.CHAT_VIEW_LAYOUT_BOTTOM_UP) {
        	
            hAdapt = new SmsHistoryListAdapter(activity,fromSms,true);
            history.setAdapter(hAdapt);
        	
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            
            sendPod.setLayoutParams(params);
            
            history.setStackFromBottom(true);

            history.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            RelativeLayout.LayoutParams histlay = (RelativeLayout.LayoutParams) history.getLayoutParams();
            histlay.addRule(RelativeLayout.BELOW, R.id.sms_to_persons_pod);
            histlay.addRule(RelativeLayout.ABOVE, R.id.sms_send_pod);
            history.setLayoutParams(histlay);
            
            PopupMenu p  = new PopupMenu(activity, null);
            Menu menu = p.getMenu();
            
            ActionBarManager.showMenu(activity, menu);
            //Menu menu = activity.getMenuInflater().inflate(R.menu.sms_send, null);

            //activity.getM
        	
        } else {
        	
            hAdapt = new SmsHistoryListAdapter(activity,fromSms,false);
            history.setAdapter(hAdapt);
            
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.BELOW, R.id.sms_to_persons_pod);
            sendPod.setLayoutParams(params);
            
            history.setStackFromBottom(false);
            RelativeLayout.LayoutParams histlay = (RelativeLayout.LayoutParams) history.getLayoutParams();
            histlay.addRule(RelativeLayout.BELOW, R.id.sms_send_pod);
            histlay.addRule(RelativeLayout.ABOVE, 0);
            history.setLayoutParams(histlay);
            
            PopupMenu p  = new PopupMenu(activity, null);
            Menu menu = p.getMenu();
            
            ActionBarManager.showMenu(activity, menu);
            
            MenuItem mi = menu.getItem(0);
            mi.setTitle(activity.getString(R.string.layout_bottom_up));
        }

        toView.refreshData();

        refreshData();

        //if(didfocus) {
            //BLog.e("CALLFOCUS","isempty: "+ContactsSelectedClipboard.isEmpty());
            if(ContactsSelectedClipboard.isEmpty())
                Device.setKeyboard(activity, toView.getSearchEditText(), true);
            else
                Device.setKeyboard(activity, message, true);

        //}

        sendBtnHandler=new Handler();
        sendBtnHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshSendButton();
            }
        },100);

	}
    private Handler sendBtnHandler;
    public void refreshData() {
        refreshSendButton();
    }
    private boolean isAnimationActive=false;
    public synchronized void refreshSendButton() {
        if(hdp==0) {
            hdp= Functions.dpToPx(60,activity);
            wdp=Functions.dpToPx(50,activity);
        }
        //BLog.e("SND--BTN","------------refresh called, clip: "+ContactsSelectedClipboard.size()+" , alpha: "+smsSendBtn.getAlpha());
        if(ContactsSelectedClipboard.size()>0 && message.getText().length()>0) {
            if(!sendOpen) {


                openSendButton();
                sendOpen=true;
            } else {
                //BLog.e("SND--BTN","------IGNORE------open");
            }
        } else {
            if(sendOpen) {
                //BLog.e("SND--BTN","------------close");
                closeSendButton();
                sendOpen=false;
            } else {
                //BLog.e("SND--BTN","------IGNORE------close");
            }
        }
    }
    private int hdp;
    private int wdp;
    private void openSendButton() {

        //smsSendBtn.setAlpha(1f);
        lp=new LinearLayout.LayoutParams(hdp,wdp);
        //BLog.e("SND--BTN","------------open: "+hdp+","+wdp);

        OpenForSend talpha = new OpenForSend(0, wdp, 0, 0);
        talpha.setDuration(250);
        //smsSendBtn.setLayoutParams(lp);
        smsSendBtn.startAnimation(talpha);
    }
    private void closeSendButton() {

        CloseNoSend talpha = new CloseNoSend(wdp, 0, 0, 0);
        //BLog.e("SND--BTN","------------close: "+hdp+","+wdp);
        talpha.setDuration(250);
        //smsSendBtn.setAlpha(0.0f);
        smsSendBtn.startAnimation(talpha);
    }
    private ColorDrawable cd;
    private LinearLayout.LayoutParams lp;

    private class OpenForSend extends TranslateAnimation {
        float mFromAlpha=1F;
        float mToAlpha=0F;
        private float fromX;
        private float toX;

        public OpenForSend(int fromX, int toX, int fromY, int toY) {
            super(fromX,toX,fromY,toY);
            this.fromX=fromX;
            this.toX=toX;
            //cd.setAlpha(0);
            smsSendBtn.setBackground(cd);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation trans) {
            //super(interpolatedTime,t);
            //this.toY=view.getHeight();//toY;
            final float alpha = mFromAlpha;
            final float toAlpha = alpha + ((mToAlpha - alpha) * interpolatedTime);
            //TextView abtv = getActionBarTitleView();
            //if(abtv!=null)
            //    abtv.setAlpha(toAlpha);

            cd.setAlpha(Float.valueOf(toAlpha*100).intValue());
            //actionBar.setBackgroundDrawable(cd);

            int moveBy = Float.valueOf(((toX-fromX)*interpolatedTime)+fromX).intValue();
            lp=new LinearLayout.LayoutParams(moveBy,hdp);
            //lp.setMargins(0,Float.valueOf(moveBy).intValue(),0,0);
            smsSendBtn.setLayoutParams(lp);
            //getActionBarTitleView().setAlpha(alpha + ((mToAlpha - alpha) * interpolatedTime));
        }

    }

    private class CloseNoSend extends TranslateAnimation {
        float mFromAlpha=0F;
        float mToAlpha=1F;
        private float fromX;
        private float toX;

        public CloseNoSend(int fromX, int toX, int fromY, int toY) {
            super(fromX,toX,fromY,toY);
            this.fromX=fromX;
            this.toX=toX;
            //cd.setAlpha(0);
            smsSendBtn.setBackground(cd);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation trans) {
            //super(interpolatedTime,t);
            //this.toY=view.getHeight();//toY;
            final float alpha = mFromAlpha;
            final float toAlpha = alpha + ((mToAlpha - alpha) * interpolatedTime);
            //TextView abtv = getActionBarTitleView();
            //if(abtv!=null)
            //    abtv.setAlpha(toAlpha);
            int nalpha=Float.valueOf(toAlpha*100).intValue();
            cd.setAlpha(nalpha);
            //actionBar.setBackgroundDrawable(cd);

            int moveBy = Float.valueOf(((toX-fromX)*interpolatedTime)+fromX).intValue();
            lp=new LinearLayout.LayoutParams(moveBy,hdp);
            //lp.setMargins(0,Float.valueOf(moveBy).intValue(),0,0);
            smsSendBtn.setLayoutParams(lp);
            //getActionBarTitleView().setAlpha(alpha + ((mToAlpha - alpha) * interpolatedTime));
        }

    }


	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		//BLog.e("OPTIONS", "onCreateOptionsMenu at new emai home");

	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//BLog.e("OPTIONS", item.getItemId() + " -- onCreateOptionsMenu at new sms home -- " + R.id.action_sms_flip_layout);
		switch(item.getItemId()) {
			case R.id.action_sms_flip_layout:
				BriefSettings bs = State.getSettings();
				int bvl = bs.getInt(BriefSettings.INT_CHAT_VIEW_LAYOUT);
				if(bvl==BriefSettings.CHAT_VIEW_LAYOUT_BOTTOM_UP)
					bvl=BriefSettings.CHAT_VIEW_LAYOUT_TOP_DOWN;
				else
					bvl=BriefSettings.CHAT_VIEW_LAYOUT_BOTTOM_UP;
				bs.setInt(BriefSettings.INT_CHAT_VIEW_LAYOUT, bvl);
				bs.save();
				State.setSettings(bs);
				refresh();
				//State.getSettings()
				//return true;
				break;
		}	
		return false;
	}
	@Override
	public void onPause() {
		super.onPause();
		//messagetext = message.getText().toString();
		//BLog.e("SAING","PAUSE SMSSEND");
        if(manager!=null)
            manager.dismissPopup();
		if(Device.isKeyboardVisible())
			Device.hideKeyboard(activity);
		
		JSONObject messaged=new JSONObject();
		messaged.put("text", message.getText().toString());
		if(fromSms!=null) {
			messaged.put("fid",fdbindex);
		}
		State.addToState(State.SECTION_SMS_SEND,new StateObject(StateObject.STRING_BJSON_OBJECT,messaged.toString()));
        //ViewManagerText.clearManager();
	}

	public void keyClickedIndex(final String index) {

		int cursorPosition = message.getSelectionStart();		

        message.getText().insert(cursorPosition, index);
        /*
        message.getSelectionStart();
        manager.preTextChange();
        message.getText().insert(cursorPosition, index);
        manager.doAfterTextChange(index);
        manager.apresTextChange();
*/

	}
	protected OnClickListener messageBoxListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if(manager.getPopupWindow()!=null)
                manager.getPopupWindow().dismiss();
			smsSmile.setImageDrawable(activity.getResources().getDrawable(R.drawable.i_smile));
            refreshSendButton();

		}
	};	
	protected OnClickListener sendSmsListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if(!isSendTaskRunning) {
				isSendTaskRunning=true;
				
				smsSendBtn.setImageDrawable(activity.getResources().getDrawable(R.drawable.rate_none));
				smsSendBtn.setAlpha(0.7F);
				message.setAlpha(0.7F);
				
				new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                            sendSms();
                    }
                }, 100);
				//endSmsTask = new SendSms();
				//sendSmsTask.execute(true);
			} else {
				// alert this somehow !!
				
			}
		}
	};	
	private void SendSmsFinished(boolean result) {
		refresh();
		isSendTaskRunning=false;
		smsSendBtn.setAlpha(0.7F);
		message.setAlpha(0.7F);
		if(result) {
			// did send
			message.setText("");

		} else {
			// not send no action
			
		}
		smsSendBtn.setImageDrawable(activity.getResources().getDrawable(R.drawable.social_send_now));
		smsSendBtn.setAlpha(1F);
		message.setAlpha(1F);
	}
	private void showAlertNoSend(String message) {
		infoBox.setText(message);
		infoBox.setAlpha(1F);

		//infoBox.setTextAppearance(activity, R.style.warning_on);
		//infoBox.setCompoundDrawables(activity.getResources().getDrawable(R.drawable.warning), null,null,null);
		final Property<TextView, Float> property = new Property<TextView, Float>(float.class, "alpha") {
			   @Override
			    public Float get(TextView object) {
			        return object.getAlpha();
			    }

			   @Override
			    public void set(TextView object, Float value) {
			        object.setAlpha(value);
			    }
			};

			final ObjectAnimator animator = ObjectAnimator.ofFloat(infoBox, property, 0F,1F); //.ofInt(infoBox, property, Color.RED);
			animator.setDuration(333L);
			//animator.setEvaluator(new ArgbEvaluator());
			//animator.setInterpolator(new DecelerateInterpolator(2));
			animator.start();
			new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                	hideAlertNoSend();
                }
            }, 2500);
	
	}
	private void hideAlertNoSend() {
		
				
		final Property<TextView, Float> property = new Property<TextView, Float>(float.class, "alpha") {
			   @Override
			    public Float get(TextView object) {
			        return object.getAlpha();
			    }

			   @Override
			    public void set(TextView object, Float value) {
			        object.setAlpha(value);
			    }
			};


			final ObjectAnimator animator = ObjectAnimator.ofFloat(infoBox, property, 1F,0F); //.ofInt(infoBox, property, Color.RED);
			animator.setDuration(100L);
			//animator.setEvaluator(new ArgbEvaluator());
			//animator.setInterpolator(new DecelerateInterpolator(2));
			animator.start();
			infoBox.setText("");
			//infoBox.setTextAppearance(activity, R.style.warning_off);
			//infoBox.setBackground(null);
			//infoBox.setCompoundDrawables(null, null,null,null);
	}
	private void sendSms() {
		boolean ret=false;
		String msg = message.getText().toString();
		if(msg!=null && msg.length()>0) {
			HashMap<String,Person> tos = ContactsSelectedClipboard.get();
			if(tos!=null && !tos.isEmpty()) {
				Iterator<String> it = tos.keySet().iterator();
				while(it.hasNext()) {
                    Person p = tos.get(it.next());
					if(p!=null) {
						SmsSend sms = new SmsSend();
						sms.setString(SmsSend.STRING_MESSAGE, msg);
						sms.setString(SmsSend.STRING_TO_NUMBER, (String) p.getJSONArray(Person.JSONARRAY_PHONE).get(p.getInt(Person.INT_INDEX_USE_PHONE)));
					    //sms.setBoolean();

						
				    	BriefManager.setDirty(BriefManager.IS_DIRTY_SEND_ITEMS);
				    	if(State.getSettings().getInt(BriefSettings.INT_CHAT_VIEW_LAYOUT)==BriefSettings.CHAT_VIEW_LAYOUT_BOTTOM_UP) 
				    		hAdapt = new SmsHistoryListAdapter(activity,fromSms,true);
				    	else
				    		hAdapt = new SmsHistoryListAdapter(activity,fromSms,false);
				        history.setAdapter(hAdapt);
				        history.invalidate();
				        ret=true;

                        BriefService.addToSendServiceQue(activity, null, Brief.WITH_SMS, sms.getBean());

					}
				}
				
			} else {
				showAlertNoSend(activity.getString(R.string.sms_alert_no_person));
			}
		} else {
			showAlertNoSend(activity.getString(R.string.sms_alert_no_message));
		}
		SendSmsFinished(ret);
	}

	
}
