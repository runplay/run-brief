package run.brief.locker;


import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import run.brief.BriefManager;
import run.brief.HomeFarm;
import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.b.Device;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.LockerItem;
import run.brief.secure.Validator;
import run.brief.service.BriefService;
import run.brief.util.Files;
import run.brief.util.Functions;
import run.brief.util.Num;
import run.brief.util.PlusMember;
import run.brief.util.explore.FileExploreAdapter;
import run.brief.util.explore.FileExploreFragment;
import run.brief.util.json.JSONArray;

public class LockerFragment extends BFragment implements BRefreshable,OnScrollListener {
	private View view;

	private Activity activity=null;
	private static FileExploreAdapter adapter;
	private static ListView list;
	private BriefActionBar bar;
	private TextView title;
	//private TextView path;
	private TextView info;
	private TextView files;

	private View layFirstTime;
	private View layPassCode;
	private View layOpen;
	private View layAdd;
    private View layUpgrade;

    private RelativeLayout layImageView;
	
	private static boolean wipeonfinish=true;
	
	private TextView textWarnFirst;
	//private Button btnCreateWithout;
	private Button btnCreateWithPass;
	private EditText textFirstPassCode;
	
	private EditText textEnterPass;
	private Button btnEnterPass;
	private TextView messageLogin;
	private List<File> addLockerItems=new ArrayList<File>();
	
	
	private ListView addlist;
	private Button addCancel;
	private Button addConfirm;
	private View addLockBtns;
	
	private AddFilesTask addFilesTask;
    private RemoveFilesTask removeFilesTask;
	
	private TextView lockerMessage;
    private View lockerMessageView;
    private int openFilePosition;
    private String openeFilePath ="";

    private RelativeLayout addViewLockerImage;



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.activity=getActivity();
		view=inflater.inflate(R.layout.file_locker,container, false);

		return view;
		

	}
	@Override
	public void onPause() {
		super.onPause();
        if(bar!=null && bar.getActionBarTitleView()!=null)
            bar.getActionBarTitleView().setAlpha(1F);
	}
	@Override
	public void onResume() {
		super.onResume();
		//BLog.e("HF",HomeFarm.getLocKey(activity));
        State.setCurrentSection(State.SECTION_LOCKER);

        list=(ListView) view.findViewById(R.id.files_list);
        info = (TextView) view.findViewById(R.id.files_directory_info);

        files = (TextView) view.findViewById(R.id.files_directory_info_files);
        title = (TextView) view.findViewById(R.id.files_directory_title);

        layFirstTime=view.findViewById(R.id.locker_first_time);
        layPassCode=view.findViewById(R.id.locker_enter_pass_code);
        layOpen=view.findViewById(R.id.locker_open);
        layAdd=view.findViewById(R.id.locker_add_files);
        layImageView=(RelativeLayout) view.findViewById(R.id.locker_show_locked_image);

        layUpgrade = PlusMember.getPlusMemberUpgradeView(activity,view,new OnClickListener() {
            @Override
            public void onClick(View view) {
                Bgo.goPreviousFragment(activity);
            }
        });

        addViewLockerImage=(RelativeLayout) view.findViewById(R.id.locker_show_locked_image_add);

        textWarnFirst = (TextView) view.findViewById(R.id.locker_ft_message);

        //btnCreateWithout = (Button) view.findViewById(R.id.locker_btn_low_security);
        //btnCreateWithout.setOnClickListener(firstTimeWithoutPassListner);

        textFirstPassCode=(EditText) view.findViewById(R.id.locker_first_passcode);

        btnCreateWithPass = (Button) view.findViewById(R.id.locker_btn_first_high_security);
        btnCreateWithPass.setOnClickListener(firstTimeWithPassListner);

        textEnterPass=(EditText) view.findViewById(R.id.locker_passcode);
        btnEnterPass=(Button) view.findViewById(R.id.locker_btn_high_security);
        btnEnterPass.setOnClickListener(loginListner);
        messageLogin=(TextView) view.findViewById(R.id.locker_ft_message_login);



        addlist=(ListView) view.findViewById(R.id.locker_add_files_list);
        addConfirm=(Button) view.findViewById(R.id.fl_btn_confirm);
        addConfirm.setOnClickListener(addFilesClick);

        addCancel=(Button) view.findViewById(R.id.fl_btn_cancel);
        addCancel.setOnClickListener(cancelAddFiles);
        addLockBtns=view.findViewById(R.id.fl_lock_buttons);


        lockerMessage=(TextView) view.findViewById(R.id.locker_action_message);
        lockerMessageView= view.findViewById(R.id.locker_message_view);
        //header=(LinearLayout) view.findViewById(R.id.files_header);

        TextView txt0 =(TextView) view.findViewById(R.id.locker_ft0);
        TextView txt1 =(TextView) view.findViewById(R.id.locker_ft1);
        //TextView txt2 =(TextView) view.findViewById(R.id.locker_ft2);
        //TextView txt3 =(TextView) view.findViewById(R.id.locker_ft3);
        //TextView txt4 =(TextView) view.findViewById(R.id.locker_ft4);
        TextView txt5 =(TextView) view.findViewById(R.id.locker_ft5);
        TextView txt6 =(TextView) view.findViewById(R.id.locker_ft6);
        //TextView txt7 =(TextView) view.findViewById(R.id.locker_ft7);
        TextView txt8 =(TextView) view.findViewById(R.id.locker_ft8);
        TextView txt9 = (TextView) view.findViewById(R.id.locker_pass_enter_now_msg);
        TextView lehead =(TextView) view.findViewById(R.id.locker_empty_head);
        TextView txt10 =(TextView) view.findViewById(R.id.locker_empty_text);



        B.addStyle(new TextView[]{txt10,txt9,txt0,txt1,txt5,txt6,txt8,lockerMessage,addCancel,addConfirm,messageLogin,btnEnterPass,btnCreateWithPass,textWarnFirst,files,title,info});
        B.addStyleBold(lehead,B.FONT_LARGE);
        refresh();
		
		
	}


    private void showUpgrade() {
        hideAllLayers();
        layUpgrade.setVisibility(View.VISIBLE);
    }
	
	private void hideAllLayers() {
		layFirstTime.setVisibility(View.GONE);
		layPassCode.setVisibility(View.GONE);
		layOpen.setVisibility(View.GONE);
		layAdd.setVisibility(View.GONE);
        layImageView.setVisibility(View.GONE);
        layUpgrade.setVisibility(View.GONE);
	}
	public void refreshData() {
		
	}
	public void refresh() {
		BriefManager.clearController(activity);
        list.setVisibility(View.VISIBLE);
        hideAllLayers();

		if(Validator.isValidCaller()) {
            lockerMessageView.setVisibility(View.GONE);
		//BLog.e("FILEM", "file manager refresh called");
            /*
            amb = new ActionModeBack(activity, activity.getString(R.string.title_locker)
                    ,R.menu.file_locker
                    , new ActionModeCallback() {
                @Override
                public void onActionMenuItem(ActionMode mode, MenuItem item) {
                    onOptionsItemSelected(item);
                }
            });
            */
            ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getString(R.string.title_locker),R.menu.file_locker,R.color.brand);

			
			
			if(State.hasStateObject(State.SECTION_LOCKER,StateObject.STRING_BJSON_ARRAY)) {
				
				JSONArray jarr = new JSONArray(State.getStateObjectString(State.SECTION_LOCKER,StateObject.STRING_BJSON_ARRAY));
				if(jarr!=null) {
					addLockerItems.clear();
					for(int i=0; i<jarr.length(); i++) {
						//BLog.e("NTE", jarr.getString(i));

						addLockerItems.add(new File(jarr.getString(i)));   
					}
				}
			}
			State.clearStateObjects(State.SECTION_LOCKER);



			if(LockerManager.isLockerEnabled()) {
				if(LockerManager.isOpen() || !LockerManager.requiresPassCode()) {
					if(HomeFarm.isSubscriber()) {
                        if (addLockerItems.isEmpty()) {

                            LockerItemsAdapter lfa = new LockerItemsAdapter(activity);
                            list.setAdapter(lfa);
                            list.invalidate();
                            list.setOnItemClickListener(openListener);
                            list.setEmptyView(null);

                            layOpen.setVisibility(View.VISIBLE);
                            files.setText("" + lfa.getCount());
                            info.setVisibility(View.GONE);
                            title.setText(getString(R.string.locker_display_path));

                            if (BriefService.isRunLockerAddFilesTaskActive()) {
                                lockerMessage.setText(activity.getString(R.string.locker_Adding_files));
                                lockerMessageView.setVisibility(View.VISIBLE);
                                list.setVisibility(View.GONE);
                                //list.setEmptyView
                            } else if (BriefService.isRunLockerRemoveFilesTaskActive()) {
                                lockerMessage.setText(activity.getString(R.string.locker_unlocking) + ": " + Files.getFileNameFromPath(openeFilePath) + "\n@ " + Files.getPathLessFileName(openeFilePath));
                                lockerMessageView.setVisibility(View.VISIBLE);
                                list.setVisibility(View.GONE);
                            } else {
                                list.setEmptyView(view.findViewById(R.id.locker_empty));
                            }
/*
                        //     WARN
                        //      for dev only, this wipes the locker database.
                        for(int i=LockerManager.getLockerItems().size()-1; i>=0; i--) {
                            LockerItem li = LockerManager.getLockerItem(i);
                            LockerManager.dbRemove(li);
                        }
*/
                        } else {
                            //BLog.e("HASLI", ""+LockerManager.getLockerItems().size());

                            LockerAddFilesAdapter lfa = new LockerAddFilesAdapter(activity, addLockerItems);
                            addlist.setAdapter(lfa);
                            list.invalidate();
                            layAdd.setVisibility(View.VISIBLE);

                            //mHandler.post(mSwapImageRunnable);

                        }
                    } else {
                        hideAllLayers();
                        showUpgrade();
                    }
                    //BLog.e("LOCKER","is adding: "+BriefService.isRunLockerAddFilesTaskActive());


				} else  {
					layPassCode.setVisibility(View.VISIBLE);
                    textEnterPass.requestFocus();
                    Device.setKeyboard(activity, textEnterPass, true);
				}
			} else {
				layFirstTime.setVisibility(View.VISIBLE);
			}
			
		} else {
            /*
            amb = new ActionModeBack(activity, activity.getString(R.string.title_locker)
                    ,R.menu.basic
                    , new ActionModeCallback() {
                @Override
                public void onActionMenuItem(ActionMode mode, MenuItem item) {
                    onOptionsItemSelected(item);
                }
            });
            */
            ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getString(R.string.title_locker),R.menu.basic,R.color.brand);

		}

	}


    private void openFileInfo(int position) {
        LockerItem li = LockerManager.getLockerItem(position);
        if(li!=null) {
            openFilePosition=position;
            openeFilePath =li.getString(LockerItem.STRING_ORIGIN_PATH);
            bar = new BriefActionBar(activity);


            bar.openActionHeadWithLockerItem(li, 400);
        }
        //bar.start();
    }

    private OnItemClickListener openListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {

            openFileInfo(position);
			
			
		}
	};
    private class openKeyboardLogin extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {
            Device.setKeyboard(activity, textEnterPass, true);
            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {

        }

    }
	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		//BLog.e("OPTIONS", "onCreateOptionsMenu at new emai home");

	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//BLog.e("OPTIONS", "onCreateOptionsMenu at new emai home");
		switch(item.getItemId()) {
			case R.id.action_go_explore:
				//g.setEmailServiceInstance(emailService);
				Bgo.openFragmentAnimate(activity,FileExploreFragment.class);
				break;
		}	
		return false;
	}

    private OnClickListener removeFilesClick = new OnClickListener() {
        @Override
        public void onClick(View arg1) {

                if(LockerManager.isOpen()) {
                    list.setVisibility(View.GONE);

                    removeFilesTask=new RemoveFilesTask();
                    removeFilesTask.execute(true);

                } else {
                    //BLog.e("LF", "----------------2");
                }

        }
    };
    private class RemoveFilesTask extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {
            synchronized(this) {
                List<LockerItem> removeItems = new ArrayList<LockerItem>();
                LockerItem li = LockerManager.getLockerItem(openFilePosition);
                removeItems.add(li);

                BriefService.runLockerRemoveFilesTask(activity, removeItems);

            }

            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {
            list.invalidate();
            refresh();
        }

    }


    private OnClickListener addFilesClick = new OnClickListener() {
		@Override
		public void onClick(View arg1) {
			if(!addLockerItems.isEmpty()) {
				if(LockerManager.isOpen()) {
					addLockBtns.setVisibility(View.VISIBLE);

					addFilesTask=new AddFilesTask();
					addFilesTask.execute(true);
					
				} else {
					//BLog.e("LF", "----------------2");
				}
			} else {
				//BLog.e("LF", "----------------1");
			}
		}
	};
	private class AddFilesTask extends AsyncTask<Boolean, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(Boolean... params) {
			synchronized(this) {
                BriefService.runLockerAddFilesTask(activity, addLockerItems);

                addLockerItems=null;
                addLockerItems=new ArrayList<File>();

			}

		    return Boolean.TRUE;
	
		}      
		@Override
		protected void onPostExecute(Boolean result) {
			refresh();
		}
	 
	}
    private OnClickListener cancelAddFiles = new OnClickListener() {
		@Override
		public void onClick(View arg1) {
			addLockBtns.setVisibility(View.VISIBLE);
			addLockerItems.clear();
			Bgo.goPreviousFragment(activity);
		}
	};
    private OnClickListener loginListner = new OnClickListener() {
		@Override
		public void onClick(View arg1) {
			String passCode=textEnterPass.getText().toString();
            //BLog.e("PW", "1: "+passCode);
			if (LockerManager.isValidPassCode(passCode)) {
                //BLog.e("PW", "2: "+passCode);
				if(LockerManager.open(activity, passCode)) {
					Device.hideKeyboard(activity);
					messageLogin.setText("");
					refresh();
				} else {
					messageLogin.setText(activity.getString(R.string.locker_invalid_code));

				}
				
			} else {
				messageLogin.setText(activity.getString(R.string.locker_invalid_code));
			}
		}
	};

	private OnClickListener firstTimeWithPassListner = new OnClickListener() {
		@Override
		public void onClick(View arg1) {
			String passCode=textFirstPassCode.getText().toString();
			if (LockerManager.isValidPassCode(passCode)) {
				// code ok
				//BLog.e("LOCKER","create locker pass ok");
				LockerManager.createLockerFirstTimeAndOpen(activity, passCode);
                Device.hideKeyboard(activity);
				refresh();
			}
		}
	};
    private OnClickListener firstTimeWithoutPassListner = new OnClickListener() {
		@Override
		public void onClick(View arg1) {
			//BLog.e("LOCKER","create locker no pass");
			LockerManager.createLockerFirstTimeAndOpen(activity, null);
			refresh();
		}
	};
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		/*
		if(listImages!=null && listImages.getVisibility()==View.VISIBLE) {
			adapterImages.promptCacheChange(view.getScrollY(),listImages.getLastVisiblePosition());
		}
		*/
		//enableTouchListner();
		//BLog.e("SCROLL","onScrollStateChanged: "+YPOS);
		
	}
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		

	}
    /*
    private OnItemLongClickListener openLongListener = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {

			File f = FileManager.getDirectoryItemAsFile(position);
			if(f!=null) {
				
				if(State.getFileExploreState()==State.FILE_EXPLORE_STATE_STANDALONE) {
					//openOptions(f.getAbsolutePath());
				}

			}
			return true;
			
			
		}
	};

*/



    private class BriefActionBar {

        private Activity activity;
        private RelativeLayout inview;
        private RelativeLayout container;

        //private ActionBar actionBar;
        private ColorDrawable cd;
        private RelativeLayout.LayoutParams lp;
        private boolean isOpen;

        private int MAX_HEIGHT;
        //private int MIN_HEIGHT=100;
        //private int ACTION_BAR_HEIGHT;
        private int OPEN_HEIGHT;
        private LockerItem currentItem;


        public boolean isOpen() {
            return isOpen;
        }

        public BriefActionBar(Activity activity) {
            this.activity=activity;
            this.container=(RelativeLayout)view.findViewById(R.id.brief_actionbar);
            MAX_HEIGHT= Functions.dpToPx(150, activity);
            //ACTION_BAR_HEIGHT=BriefManager.getActionBarHeight(activity);
            //MIN_HEIGHT=ACTION_BAR_HEIGHT;
            //this.actionBar=((AppCompatActivity) activity).getSupportActionBar();

            cd = new ColorDrawable(activity.getResources().getColor(R.color.actionbar_general));

        }
        public void close() {
            //getActionBarTitleView().setAlpha(1F);
            if(this.isOpen) {
                CloseActionBarView talpha = new CloseActionBarView(0, 0, OPEN_HEIGHT, 0);
                talpha.setDuration(250);
                container.startAnimation(talpha);
                isOpen=false;
            }
        }
        public void showUnlocking() {
            View v = inview.findViewById(R.id.locker_is_unlocking);
            v.setVisibility(View.VISIBLE);
        }
        public void openActionHeadWithLockerItem(LockerItem item,int toHeightInDp) {
            this.isOpen=true;
            currentItem=item;

            inview = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.file_locker_file_data, null);

            ImageView image=(ImageView) inview.findViewById(R.id.locker_file_image);
            TextView fname=(TextView) inview.findViewById(R.id.locker_file_data_name);
            TextView opath=(TextView) inview.findViewById(R.id.locker_file_data_orig_path);
            ImageView closeItem = (ImageView) inview.findViewById(R.id.locker_option_close);
            TextView data=(TextView) inview.findViewById(R.id.locker_file_data_size);
            TextView clickView = (TextView) inview.findViewById(R.id.locker_option_image);
            TextView unlock = (TextView) inview.findViewById(R.id.locker_option_unlock);

            B.addStyle(new TextView[]{opath,data,clickView,unlock});
            B.addStyleBold(fname);

            closeItem.setOnClickListener(closeItemClick);

            String filename=Files.getFileNameFromPath(item.getString(LockerItem.STRING_ORIGIN_PATH));
            fname.setText(filename);
            opath.setText(Files.getPathLessFileName(item.getString(LockerItem.STRING_ORIGIN_PATH)));


            if(Files.isImage(Files.removeBriefFileExtension(filename))) {
                clickView.setVisibility(View.VISIBLE);
                clickView.setOnClickListener(openImageClick);
            } else {
                clickView.setVisibility(View.GONE);
            }


            unlock.setOnClickListener(removeFilesClick);

            image.setImageBitmap(((BitmapDrawable) B.getDrawable(activity,Files.getFileRIcon(filename))).getBitmap());
            data.setText(Num.btyesToFileSizeString(item.getLockerFileSize()));
            //view.setLayoutParams(lp);
            inview.bringToFront();
            inview.setBackgroundColor(activity.getResources().getColor(R.color.white));
            openActionHead(toHeightInDp);
        }

        public OnClickListener closeItemClick = new OnClickListener() {
            @Override
            public void onClick(View arg1) {
                close();
            }
        };

        public OnClickListener openImageClick = new OnClickListener() {
            @Override
            public void onClick(View arg1) {
                hideAllLayers();
                layImageView.setVisibility(View.VISIBLE);


                runStartHandler.postDelayed(goOpenLockedImage,50);

            }
        };
        private Handler runStartHandler = new Handler();
        private Runnable goOpenLockedImage = new Runnable() {
            @Override
            public void run() {
                OpenLockedImageTask oli = new OpenLockedImageTask();
                oli.setWidth(layImageView.getWidth());
                oli.execute(true);
            }
        };
        private class OpenLockedImageTask extends AsyncTask<Boolean, Void, Boolean> {
            Bitmap bitmap;
            private int w;
            public void setWidth(int width) {
                this.w=width;
            }
            @Override
            protected Boolean doInBackground(Boolean... params) {
                                //int h = layImageView.getHeight();

                bitmap = LockerManager.getLockerImage(activity,currentItem,w);


                return Boolean.TRUE;

            }
            @Override
            protected void onPostExecute(Boolean result) {
                finishOpenLockedImageTask(bitmap);
            }

        }
        public void finishOpenLockedImageTask(Bitmap bitmap) {
            TextView close = (TextView) view.findViewById(R.id.locker_show_locked_image_close);
            close.setOnClickListener(closeImageClick);

            B.addStyle(close);

            ImageView img = new ImageView(activity);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            img.setLayoutParams(lp);
            img.setImageBitmap(bitmap);

            addViewLockerImage.addView(img);
        }
        public OnClickListener closeImageClick = new OnClickListener() {
            @Override
            public void onClick(View arg1) {
                addViewLockerImage.removeAllViews();
                hideAllLayers();
                if(LockerManager.isOpen())
                    layOpen.setVisibility(View.VISIBLE);
                else
                    layPassCode.setVisibility(View.VISIBLE);

            }
        };


        private void openActionHead(int toHeightInDp) {
            if(container !=null && view!=null) {
                //BLog.e("OPEN","action head open command");
                container.removeAllViews();

                RelativeLayout.LayoutParams tlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
                tlp.setMargins(0,0,0,0);
                inview.setLayoutParams(tlp);
                inview.setGravity(Gravity.BOTTOM);
                container.addView(inview);

                int h = toHeightInDp;
                if(h>MAX_HEIGHT)
                    h=MAX_HEIGHT;

                OPEN_HEIGHT=h;

                int startY=0;//ACTION_BAR_HEIGHT;
                //TranslateAnimation topen = new TranslateAnimation(0, 0, startY, h);
                //topen.setDuration(150);
                lp=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,startY);
                //lp.setMargins(0,Float.valueOf(moveBy).intValue(),0,0);

                container.setLayoutParams(lp);
                container.setGravity(Gravity.BOTTOM);
                container.setPadding(0,0,0,20);
                container.setBackground(activity.getResources().getDrawable(R.drawable.border_bottom_only));
                //BLog.e("OPEN", "action head open command: "+h);
                OpenActionBarView talpha = new OpenActionBarView(0, 0, startY, h);
                talpha.setDuration(250);
                container.startAnimation(talpha);
                //activity.getActionBar();
            } else {
                //BLog.e("OPEN","action head open command = FAILED");
            }
        }

        private class CloseActionBarView extends TranslateAnimation {
            float mFromAlpha=0F;
            float mToAlpha=1F;
            private float fromY;
            private float toY;

            public CloseActionBarView(int fromX, int toX, int fromY, int toY) {
                super(fromX,toX,fromY,toY);
                this.fromY=fromY;
                this.toY=toY;
                //cd.setAlpha(0);
                //actionBar.setBackgroundDrawable(cd);
            }

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation trans) {
                //super(interpolatedTime,t);
                //this.toY=view.getHeight();//toY;
                final float alpha = mFromAlpha;
                final float toAlpha = alpha + ((mToAlpha - alpha) * interpolatedTime);
                TextView abtv = getActionBarTitleView();
                if(abtv!=null)
                    abtv.setAlpha(toAlpha);

                cd.setAlpha(Float.valueOf(toAlpha*100).intValue());
                //actionBar.setBackgroundDrawable(cd);

                int moveBy = Float.valueOf(((toY-fromY)*interpolatedTime)+fromY).intValue();
                lp=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,moveBy);
                //lp.setMargins(0,Float.valueOf(moveBy).intValue(),0,0);
                container.setLayoutParams(lp);
                //getActionBarTitleView().setAlpha(alpha + ((mToAlpha - alpha) * interpolatedTime));
            }

        }


        private class OpenActionBarView extends TranslateAnimation {
            float mFromAlpha=1F;
            float mToAlpha=0F;
            private float fromY;
            private float toY;

            public OpenActionBarView(int fromX, int toX, int fromY, int toY) {
                super(fromX,toX,fromY,toY);
                this.fromY=fromY;
                this.toY=toY;
                //cd.setAlpha(0);
                //actionBar.setBackgroundDrawable(cd);
            }

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation trans) {
                //super(interpolatedTime,t);
                //this.toY=view.getHeight();//toY;
                final float alpha = mFromAlpha;
                final float toAlpha = alpha + ((mToAlpha - alpha) * interpolatedTime);
                TextView abtv = getActionBarTitleView();
                if(abtv!=null)
                    abtv.setAlpha(toAlpha);

                cd.setAlpha(Float.valueOf(toAlpha*100).intValue());
                //actionBar.setBackgroundDrawable(cd);

                int moveBy = Float.valueOf(((toY-fromY)*interpolatedTime)+fromY).intValue();
                lp=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,moveBy);
                //lp.setMargins(0,Float.valueOf(moveBy).intValue(),0,0);
                container.setLayoutParams(lp);
                //getActionBarTitleView().setAlpha(alpha + ((mToAlpha - alpha) * interpolatedTime));
            }

        }
        public TextView getActionBarTitleView() {
            int id = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
            return (TextView) activity.findViewById(id);
        }







    }
    
    

    
    
}