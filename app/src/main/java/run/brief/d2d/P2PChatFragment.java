/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package run.brief.d2d;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.net.Inet6Address;
import java.net.InetAddress;

import run.brief.b.ActionBarManager;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.R;
import run.brief.b.State;
import run.brief.b.bButton;
import run.brief.util.log.BLog;
import run.brief.service.P2pChatService;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class P2PChatFragment extends BFragment implements BRefreshable {

    P2PHelper mNsdHelper;
    
    Activity activity;

    private View view;
    
    private static P2PChatFragment thisFrag;
    private TextView mStatusView;
    private static Handler mUpdateHandler;

    private static P2PDeviceListAdapter adapter;
    
    private bButton connect;
    
    public static final String TAG = "NsdChat";
    private EditText message;
    P2pChatService mConnection;

    /** Called when the activity is first created. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		thisFrag=this;
		//NotesDb.init(getActivity());
		activity=getActivity();
		view=inflater.inflate(R.layout.d2dchat,container, false);
		
		/*

		*/
		
		return view;

	}
	public void refreshData() {
		
	}
	
    @Override
    public void onPause() {
        if (mNsdHelper != null) {
        	try {
            mNsdHelper.stopDiscovery();
        	} catch(Exception e) {}
        }
        if(mConnection!=null)
        	mConnection.setRefreshableFragment(null);
        super.onPause();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        State.sectionsClearBackstack();
        State.setCurrentSection(State.SECTION_D2D);
        
        
        mStatusView = (TextView) view.findViewById(R.id.status);
        if (mNsdHelper != null) {
            mNsdHelper.discoverServices();
        }
        
        //Button cc = (Button) view.findViewById(R.id.connect_btn);
        //cc.setOnClickListener(clickConnectListner);
        
        Button cs = (Button) view.findViewById(R.id.send_btn);
        cs.setOnClickListener(clickSendListner);
        
        Button cd = (Button) view.findViewById(R.id.discover_btn);
        cd.setOnClickListener(clickDiscoverAdvertiseListner);
        
        bButton refresh = (bButton) view.findViewById(R.id.p2p_refresh_button);
        refresh.setOnClickListener(refreshListner);
        
        connect = (bButton) view.findViewById(R.id.p2p_connect_button);
        connect.setOnClickListener(connectListner);
        
        message = (EditText) view.findViewById(R.id.chatInput);  
        
        refresh();
    }
	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		ActionBarManager.setActionBarBackOnly(activity, activity.getResources().getString(R.string.title_d2d), R.menu.basic, R.color.actionbar_bg);
		ListView p2pdevices = (ListView) view.findViewById(R.id.p2p_device_list);
		adapter=new P2PDeviceListAdapter(getActivity(),P2pChatService.getP2pPeers());
		p2pdevices.setAdapter(adapter);
		
		if(P2pChatService.isP2pEnabled()) {
			connect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.device_comms_connected, 0, 0, 0);
		} else {
			connect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.device_comms_disconnect, 0, 0, 0);
		}
	}

    @Override
    public void onDestroy() {
    	try {
	        mNsdHelper.tearDown();
	        mConnection.tearDown();
    	} catch(Exception e) {}
        super.onDestroy();
    }


	
	protected OnClickListener refreshListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
	        refresh();
		}
	};		
	protected OnClickListener connectListner = new OnClickListener() {    
		@Override
		public void onClick(View view) {
			
			if(!P2pChatService.isP2pEnabled()) {
				P2pChatService.startP2Pservice();
				
		        mUpdateHandler = new Handler() {
		            @Override
			        public void handleMessage(Message msg) {
			            String chatLine = msg.getData().getString("msg");
			            addChatLine(chatLine);
			        }
			    };
			
			    mConnection = P2pChatService.getService(); //new P2pChatService();
			    mConnection.setRefreshableFragment(thisFrag);
			    mConnection.startChatServer(mUpdateHandler);
		
			    mNsdHelper = new P2PHelper(getActivity());
			    mNsdHelper.initializeNsd();
			} else {
				//mNsdHelper.stopDiscovery();
				if(mConnection!=null) {
					mConnection.tearDown();
					P2pChatService.stopP2Pservice();
					
					
					mConnection=null;
					mNsdHelper=null;
				}
			}
			
			
	        refresh();
		}
	};	

/*
    public void clickAdvertise(View v) {
        // Register service
        if(mConnection.getLocalPort() > -1) {
            mNsdHelper.registerService(mConnection.getLocalPort());
        } else {
            BLog.e(TAG, "ServerSocket isn't bound.");
        }
    }
*/
	protected OnClickListener clickDiscoverAdvertiseListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			//mConnection.discoverService();
			mNsdHelper.discoverServices();
		}
	};
	/*
    public void clickDiscover(View v) {
        mNsdHelper.discoverServices();
    }
    */
	/*
	protected OnClickListener clickConnectListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
	        NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
	        if (service != null) {
	            BLog.e(TAG, "Connecting.");
	            mConnection.connectToServer(service.getHost().,
	                    service.getPort());
	        } else {
	            BLog.e(TAG, "No service to connect to!");
	        }
		}
	};	
	
    public void clickConnect(View v) {
        NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            BLog.e(TAG, "Connecting.");
            mConnection.connectToServer(service.getHost(),
                    service.getPort());
        } else {
            BLog.e(TAG, "No service to connect to!");
        }
    }
*/
	protected OnClickListener clickSendListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
	        
	        //if (messageView != null) {
	            String messageString = message.getText().toString();
	            if (!messageString.isEmpty()) {
	            	final P2pDevice dev= (P2pDevice) adapter.getItem(0);
	            	if(dev!=null) {
		            	try {
		            		BLog.e("P2P", "1---");
		            		//IPAddress.
			            	InetAddress add = Inet6Address.getByName(dev.ipAddress);
			            	BLog.e("P2P", "2");
			            	mConnection.connectToServer(add, 0);
			            	BLog.e("P2P", "3");
			                mConnection.sendMessage(messageString);
			               // BLog.e("P2P", "msg sent ok");
		            	} catch(Exception e) {   
		            		//BLog.e("P2P", "e:102 "+e.getMessage());
		            		
		            	}
	            	}
	            } else {
	            	BLog.e("P2P", "msg IS EMPTY !!!");
	            }
	            message.setText("");
	        //}
	        //TextView status = (TextView) view.findViewById(R.id.status);  
	        mStatusView.setText(messageString+"\n"+mStatusView.getText().toString());
		}
	};
	/*
	protected OnClickListener clickSendListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
	        
	        //if (messageView != null) {
	            String messageString = message.getText().toString();
	            if (!messageString.isEmpty()) {
	            	final P2pDevice dev= (P2pDevice) adapter.getItem(0);
	            	if(dev!=null) {
		            	try {
		            		BLog.e("P2P", "1---");
			            	InetAddress add = Inet6Address.ge(dev.ipAddress.getBytes());
			            	BLog.e("P2P", "2");
			            	mConnection.connectToServer(add, 0);
			            	BLog.e("P2P", "3");
			                mConnection.sendMessage(messageString);
			                BLog.e("P2P", "msg sent ok");
		            	} catch(Exception e) {   
		            		BLog.e("P2P", "e:102 "+e.getMessage());
		            		
		            	}
	            	}
	            } else {
	            	BLog.e("P2P", "msg IS EMPTY !!!");
	            }
	            message.setText("");
	        //}
	        //TextView status = (TextView) view.findViewById(R.id.status);  
	        mStatusView.setText(messageString+"\n"+mStatusView.getText().toString());
		}
	};
	
	
	protected OnClickListener clickSendListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
	        
	        //if (messageView != null) {
	            String messageString = message.getText().toString();
	            if (!messageString.isEmpty()) {
	            	final P2pDevice dev= (P2pDevice) adapter.getItem(0);
	            	if(dev!=null) {
	            		//WifiP2pDevice device;
	            		WifiP2pConfig config = new WifiP2pConfig();
	            		config.deviceAddress = dev.ipAddress;
	            		P2pChatService.connectGo(config);

	            	}
	            } else {
	            	BLog.e("P2P", "msg IS EMPTY !!!");
	            }
	            message.setText("");
	        //}
	        //TextView status = (TextView) view.findViewById(R.id.status);  
	        mStatusView.setText(messageString+"\n"+mStatusView.getText().toString());
		}
	};	
	
	
		protected OnClickListener clickSendListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
	        
	        //if (messageView != null) {
	            String messageString = message.getText().toString();
	            if (!messageString.isEmpty()) {
	            	final P2pDevice dev= (P2pDevice) adapter.getItem(0);
	            	if(dev!=null) {
	            		//WifiP2pDevice device;
	            		WifiP2pConfig config = new WifiP2pConfig();
	            		config.deviceAddress = dev.ipAddress;
	            		P2pChatService.connectGo(config);

	            	}
	            } else {
	            	BLog.e("P2P", "msg IS EMPTY !!!");
	            }
	            message.setText("");
	        //}
	        //TextView status = (TextView) view.findViewById(R.id.status);  
	        mStatusView.setText(messageString+"\n"+mStatusView.getText().toString());
		}
	};	
	
    public void clickSend(View v) {
        EditText messageView = (EditText) view.findViewById(R.id.status);  
        if (messageView != null) {
            String messageString = messageView.getText().toString();
            if (!messageString.isEmpty()) {
                mConnection.sendMessage(messageString);
            }
            messageView.setText("");
        }
    }
*/
    public void addChatLine(String line) {
        mStatusView.append("\n" + line);
    }


}
