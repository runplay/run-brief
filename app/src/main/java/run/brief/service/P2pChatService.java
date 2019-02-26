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

package run.brief.service;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import run.brief.b.BRefreshable;
import run.brief.b.Device;
import run.brief.b.State;
import run.brief.d2d.P2pDevice;
import run.brief.util.log.BLog;
import run.brief.settings.SettingsDb;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class P2pChatService  extends Service {

	private static P2pChatService CHAT;
	
	public static final int SERVER_PORT=6056;
	
	
    public static final String SERVICE_INSTANCE = "_wifidemotest";
    //public static final String SERVICE_REG_TYPE = "_presence._tcp";
    //public static final String SERVICE_REG_TYPE = "_ipp._tcp";
    public static final String SERVICE_REG_TYPE = "_http._tcp.";
	
	private static final int REFRESH_MILLIS=60000;
	
    private static OnP2PReceiver p2preceiver;
    
    private static boolean isP2PEnabled=false;
    
    private static WifiP2pManager p2pManager;
    private static WifiP2pManager.Channel p2pChannel;
    private static final IntentFilter P2PFilter = new IntentFilter();
    
    //private static HashMap<String, String> buddies = new HashMap<String, String>();
    //private static List<P2pDevice>  buddiesList = new ArrayList<P2pDevice>();

    
    private static List<P2pDevice> currentpeers = new ArrayList<P2pDevice>();
	
	
    private static RegularRefresh regularRefresh;
    private static Handler regularRefreshHandler = new Handler();
	
    private Handler mUpdateHandler;
    private ChatServer mChatServer;
    private ChatClient mChatClient;
    //private Activity activity;

    private static final String TAG = "ChatConnection";

    private Socket mSocket;
    private int mPort = -1;
    private static BRefreshable refreshFrag;

	public P2pChatService() {
    	//this.activity=activity;
    }

	public void setRefreshableFragment(BRefreshable refFragment) {
		refreshFrag=refFragment;
	}
	
    public static boolean isP2pRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (P2pChatService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public synchronized void startupRefreshNetwork() {
    	if(regularRefresh==null)
    		regularRefresh=new RegularRefresh();
    	regularRefreshHandler.postDelayed(regularRefresh, REFRESH_MILLIS);
    }
    public synchronized void stopRefreshNetwork() {
    	if(regularRefresh==null)
    		regularRefreshHandler.removeCallbacks(regularRefresh);
    }
    
    private class RegularRefresh  implements Runnable {  
    	@Override
    	public void run() {
    		
    		
    		refreshPeers();
    		

    		regularRefreshHandler.postDelayed(regularRefresh, REFRESH_MILLIS);
    	}

    }

	
    public static P2pChatService getService() {
    	return CHAT;
    }
    private void ensurStartups() {
    	if(State.getSettings()==null) {
			SettingsDb.init();
			State.setSettings(SettingsDb.getSettings());
    	}
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        //BLog.e("SERVICE","P2pChatConnection service started");
        CHAT=this;
        ensurStartups();
    	//runner = new BriefServiceRunner();
    	//runner.i    intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        //ensurStartups();
        /*
        if(State.getSettings().getBoolean(BriefSettings.BOOL_USE_DIRECT)) {
        	startP2Pservice();
        }
        */
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
    	BLog.e("SERVICE","BriefService stopped");
        // Cancel the persistent notification.
        //mNM.cancel(NOTIFICATION);
        
        stopP2Pservice();
    }
	public void startChatServer(Handler handler) {
		
		if(!Device.isWifiEnabled(this.getBaseContext())) {
			Device.enableWifi(this.getBaseContext());
		}
		
    	CHAT.mUpdateHandler = handler;
    	CHAT.mChatServer = CHAT.createChatServer();
        
        Map<String,String> record = new HashMap<String,String>();
        record.put("listenport", String.valueOf(10));
        record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
        record.put("available", "visible");
         
        // Service information. Pass it an instance name, service type 
        // _protocol._transportlayer , and the map containing 
        // information other devices will want once they connect to this one. 
        WifiP2pDnsSdServiceInfo serviceInfo =
        //WifiP2pDnsSdServiceInfo.newInstance("_nebula", "_ftp._tcp", record);
        WifiP2pDnsSdServiceInfo.newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
        		
        p2pManager.addLocalService(p2pChannel, serviceInfo, new WifiP2pManager.ActionListener() {
	        @Override 
	        public void onSuccess() { 
	        	BLog.e("P2P", "Local service success");
	        	startupRefreshNetwork();
	        // Command successful! Code isn't necessarily needed here, 
	        // Unless you want to update the UI or add logging statements. 
	        } 
	         
	        @Override 
	        public void onFailure(int arg0) {
	        	BLog.e("P2P", "Local service fail :"+arg0);
	        	stopRefreshNetwork();
	        // Command failed. Check for P2P_UNSUPPORTED, ERROR, or BUSY 
	        } 
        }); 
         
    }
	


    
    public static void startP2Pservice() {
        P2PFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
    	
        // Indicates a change in the list of available peers.
        P2PFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        P2PFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        P2PFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        p2pManager = (WifiP2pManager) CHAT.getSystemService(Context.WIFI_P2P_SERVICE);
        p2pChannel = p2pManager.initialize(CHAT, CHAT.getMainLooper(), null);


        p2preceiver= CHAT.getNewOnP2PReceiver();
        CHAT.registerReceiver(p2preceiver, P2PFilter);
        
    }
    public static boolean isP2pEnabled() {
    	if(p2pManager!=null) {
    		return true;
    	}
    	return false;
    }
    private OnP2PReceiver getNewOnP2PReceiver() {
    	return new OnP2PReceiver();
    }
    public static void stopP2Pservice() {
    	if(p2preceiver!=null)
        	CHAT.unregisterReceiver(p2preceiver);
    	p2pManager=null;
    }
    
    public static void refreshPeers() {
		if(p2pManager!=null) {
			
			p2pManager.discoverPeers(p2pChannel, new WifiP2pManager.ActionListener() {

		        @Override
		        public void onSuccess() {
		        	//BLog.e("p2pManeger", "sucess peers");

		        	//p2pManager.requestPeers(p2pChannel, peerListListener); 
		        	//Bgo.re
		            // Code for when the discovery initiation is successful goes here.
		            // No services have actually been discovered yet, so this method
		            // can often be left blank.  Code for peer discovery goes in the
		            // onReceive method, detailed below.
		        }

		        @Override
		        public void onFailure(int reasonCode) {
		        	BLog.e("p2pManeger", "fail peers error code: "+reasonCode);
		            // Code for when the discovery initiation fails goes here.
		            // Alert the user that something went wrong.
		        }
			});
			
		}
    }
	
    public class OnP2PReceiver extends BroadcastReceiver {
  	  
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		//BLog.e("BriefService - OnP2PReceiver","P2P - onReceive()");
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            	//BLog.e("p2P receive", "WIFI_P2P_STATE_CHANGED_ACTION");
                // Determine if Wifi P2P mode is enabled or not, alert
                // the Activity.
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                	isP2PEnabled=true;
                } else {
                	isP2PEnabled=false;
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            	//BLog.e("p2P receive", "WIFI_P2P_PEERS_CHANGED_ACTION");
                if (p2pManager != null) {
                    p2pManager.requestPeers(p2pChannel, peerListListener); 
                }


                // The peer list has changed!  We should probably do something about
                // that.

            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            	//BLog.e("p2P receive", "WIFI_P2P_CONNECTION_CHANGED_ACTION");
                if (p2pManager == null) {
                    return;
                }

                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {

                    // We are connected with the other device, request connection
                    // info to find group owner IP

                    p2pManager.requestConnectionInfo(p2pChannel, p2pConnectionInfoListner);
                }

                // Connection state changed!  We should probably do something about
                // that.

            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            	//BLog.e("p2P receive", "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
            	
            	
                //DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
                //        .findFragmentById(R.id.frag_list);
                //fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                //        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

            }

    	
    	}
    }
    private WifiP2pManager.ConnectionInfoListener p2pConnectionInfoListner = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        	BLog.e("p2P", "WifiP2pManager.ConnectionInfoListener.onConnectionInfoAvailable(): "+info.groupOwnerAddress.getHostAddress());
            // InetAddress from WifiP2pInfo struct.
            //InetAddress groupOwnerAddress = InetAddress.getByName(info.groupOwnerAddress.getHostAddress());
            

            // After the group negotiation, we can determine the group owner.
            if (info.groupFormed && info.isGroupOwner) {
                // Do whatever tasks are specific to the group owner.
                // One common case is creating a server thread and accepting
                // incoming connections.
            } else if (info.groupFormed) {
                // The other device acts as the client. In this case,
                // you'll want to create a client thread that connects to the group
                // owner.
            }
        }
    };
    public static void connectGo(WifiP2pConfig config) {
		p2pManager.connect(p2pChannel, config, new ActionListener() {

		    @Override
		    public void onSuccess() {
		        //success logic
		    	BLog.e("CONNECT", "NICE MADE CONNECTION");
		    }

		    @Override
		    public void onFailure(int reason) {
		        //failure logic
		    	BLog.e("CONNECT", "BAD FAILED CONNECTION");
		    }
		});
    }
    
    public static class FileServerAsyncTask extends AsyncTask<Boolean,Void,Boolean> {

        private Context context;
        private TextView statusText;

        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
            try {

                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */
                ServerSocket serverSocket = new ServerSocket(8888);
                Socket client = serverSocket.accept();

                /**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a JPEG file
                 */
                final File f = new File(Environment.getExternalStorageDirectory() + "/"
                        + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
                        + ".jpg");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();
                InputStream inputstream = client.getInputStream();
                //copyFile(inputstream, new FileOutputStream(f));
                serverSocket.close();
                //return f.getAbsolutePath();
            } catch (IOException e) {
                Log.e("AHH", ""+e.getMessage());
                //return null;
            }
            return Boolean.TRUE;
        }

        /**
         * Start activity that can handle the JPEG image
         */
        @Override
        protected void onPostExecute(Boolean result) {
            if (result != null) {
                statusText.setText("File copied - " + result);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                context.startActivity(intent);
            }
        }
    }

    
    
    private PeerListListener peerListListener = new PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
        	List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
            // Out with the old, in with the new.
            //peers.clear();
            peers.addAll(peerList.getDeviceList());
            //discoverService();
            if(refreshFrag!=null)
            	refreshFrag.refresh();
            //Bgo.refreshCurrentIfFragment(this., P2PChatFragment.class);
            // If an AdapterView is backed by this data, notify it
            // of the change.  For instance, if you have a ListView of available
            // peers, trigger an update.
            //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
            //buddies.clear();
            if(!peers.isEmpty()) {
	            currentpeers.clear();
	            for (int i=0 ; i < peers.size() ; i++) {
	            	currentpeers.add(new P2pDevice(peers.get(i).deviceAddress,peers.get(i).deviceName,peers.get(i).isGroupOwner(),peers.get(i).status));
	            	//buddies.put(peers.get(i).deviceAddress, peers.get(i).deviceName);
	                BLog.e("f-Peers", getStringDeviceStatus(peers.get(i)) + " -- "+ peers.get(i).deviceAddress);
	            }
            }
            //Bgo.refreshCurrentIfFragment(this., P2PChatFragment.class);
 
        }
    };
    
    public static String getStringDeviceStatus(WifiP2pDevice device) {
    	switch(device.status) {
    	case WifiP2pDevice.AVAILABLE:
    		return "Available";
    	case WifiP2pDevice.CONNECTED:
    		return "Connected";
    	case WifiP2pDevice.FAILED:
    		return "Failed";
    	case WifiP2pDevice.UNAVAILABLE:
    		return "Unavailable";
    	default:
    		return "none: "+device.status;
    		
    	}
    }
    /*
    public static List<P2pDevice> getBuddies() {
    	final List<P2pDevice> buds= new ArrayList<P2pDevice>();
    	Set<String> it = buddies.keySet();
    	for(String s: it) {
    		BLog.e("KEY", ""+s);
    		buds.add(new P2pDevice(s,buddies.get(s)));
    	}
    	return buds;
    }
    */
    public static List<P2pDevice> getP2pPeers() {
    	return currentpeers;
    }
    public static P2pDevice getP2pPeer(int index) {
    	try {
    		return currentpeers.get(index);
    	} catch(Exception e) {}
    	return null;
    }
    public static void connectP2pDevice() {
        // Picking the first device found on the network.
        P2pDevice device = currentpeers.get(0);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.ipAddress;
        config.wps.setup = WpsInfo.PBC;
        //p2pManager.
        p2pManager.connect(p2pChannel, config, new ActionListener() {

            @Override
            public void onSuccess() {
            	BLog.e("P2P CONNECT", "P2P CONNECTED OK");
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
            	BLog.e("P2P CONNECT", "P2P CONNECTED FAILED");
                //Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                //        Toast.LENGTH_SHORT).show();
            }
            
        });

    }
    
    
    
    
    
    
	
    private ChatServer createChatServer() {
    	return new ChatServer();
    }
    
    
    public void tearDown() {
        mChatServer.tearDown();
        try {
        mChatClient.tearDown();
        } catch(Exception e) {}
    }

    public void connectToServer(InetAddress address, int port) {
        mChatClient = new ChatClient(address, port);
    }

    public void sendMessage(String msg) {
        if (mChatClient != null) {
            mChatClient.sendMessage(msg);
        }
    }
    
    public int getLocalPort() {
        return mPort;
    }
    
    public void setLocalPort(int port) {
        mPort = port;
    }
    

    public synchronized void updateMessages(String msg, boolean local) {
        Log.e(TAG, "Updating message: " + msg);

        if (local) {
            msg = "me: " + msg;
        } else {
            msg = "them: " + msg;
        }

        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        mUpdateHandler.sendMessage(message);

    }

    private synchronized void setSocket(Socket socket) {
        BLog.e(TAG, "setSocket being called.");
        if (socket == null) {
            BLog.e(TAG, "Setting a null socket.");
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    // TODO(alexlucas): Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        mSocket = socket;
    }

    private Socket getSocket() {
        return mSocket;
    }

    private class ChatServer {
        ServerSocket mServerSocket = null;
        Thread mThread = null;

        public ChatServer() {
            mThread = new Thread(new ServerThread());
            mThread.start();
        }

        public void tearDown() {
            mThread.interrupt();
            try {
                mServerSocket.close();
            } catch (IOException ioe) {
                Log.e(TAG, "Error when closing server socket.");
            }
        }

        class ServerThread implements Runnable {  

            @Override
            public void run() {

                try {
                    // Since discovery will happen via Nsd, we don't need to care which port is
                    // used.  Just grab an available one  and advertise it via Nsd.
                    mServerSocket = new ServerSocket(SERVER_PORT);
                    setLocalPort(mServerSocket.getLocalPort());
                    
                    while (!Thread.currentThread().isInterrupted()) {
                        BLog.e(TAG, "ServerSocket Created, awaiting connection");
                        setSocket(mServerSocket.accept());
                        BLog.e(TAG, "Connected.");
                        /*
                        if (mChatClient == null) {
                            int port = mSocket.getPort();
                            InetAddress address = mSocket.getInetAddress();
                            connectToServer(address, port);
                        }
                        */
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error creating ServerSocket: ", e);
                    e.printStackTrace();
                }
            }
        }
    }

    private class ChatClient {

        private InetAddress mAddress;
        private int PORT;

        private final String CLIENT_TAG = "ChatClient";

        private Thread mSendThread;
        private Thread mRecThread;

        public ChatClient(InetAddress address, int port) {

            //BLog.e(CLIENT_TAG, "Creating chatClient");
            this.mAddress = address;
            this.PORT = port;

            mSendThread = new Thread(new SendingThread());
            mSendThread.start();
        }

        class SendingThread implements Runnable {

            BlockingQueue<String> mMessageQueue;
            private int QUEUE_CAPACITY = 10;

            public SendingThread() {
                mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
            }

            @Override
            public void run() {
                try {
                    if (getSocket() == null) {
                        setSocket(new Socket(mAddress, PORT));
                        BLog.e(CLIENT_TAG, "Client-side socket initialized.");

                    } else {
                        BLog.e(CLIENT_TAG, "Socket already initialized. skipping!");
                    }

                    mRecThread = new Thread(new ReceivingThread());
                    mRecThread.start();

                } catch (UnknownHostException e) {
                    //BLog.e(CLIENT_TAG, "Initializing socket failed, UHE"+ e.getMessage());
                } catch (IOException e) {
                    //BLog.e(CLIENT_TAG, "Initializing socket failed, IOE."+ e.getMessage());
                }

                while (true) {
                    try {
                        String msg = mMessageQueue.take();
                        sendMessage(msg);
                    } catch (InterruptedException ie) {
                        //BLog.e(CLIENT_TAG, "Message sending loop interrupted, exiting");
                    }
                }
            }
        }

        class ReceivingThread implements Runnable {

            @Override
            public void run() {

                BufferedReader input;
                try {
                    input = new BufferedReader(new InputStreamReader(
                            mSocket.getInputStream()));
                    while (!Thread.currentThread().isInterrupted()) {

                        String messageStr = null;
                        messageStr = input.readLine();
                        if (messageStr != null) {
                            BLog.e(CLIENT_TAG, "Read from the stream: " + messageStr);
                            updateMessages(messageStr, false);
                        } else {
                            BLog.e(CLIENT_TAG, "The nulls! The nulls!");
                            break;
                        }
                    }
                    input.close();

                } catch (IOException e) {
                    //Log.e(CLIENT_TAG, "Server loop error: ", e);
                }
            }
        }

        public void tearDown() {
            try {
                getSocket().close();
            } catch (IOException ioe) {
                Log.e(CLIENT_TAG, "Error when closing server socket.");
            }
        }

        public void sendMessage(String msg) {
            try {
                Socket socket = getSocket();
                if (socket == null) {
                    BLog.e(CLIENT_TAG, "Socket is null, wtf?");
                } else if (socket.getOutputStream() == null) {
                    BLog.e(CLIENT_TAG, "Socket output stream is null, wtf?");
                }

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(getSocket().getOutputStream())), true);
                out.println(msg);
                out.flush();
                updateMessages(msg, true);
            } catch (UnknownHostException e) {
                //BLog.e(CLIENT_TAG, "Unknown Host"+ e.getMessage());
            } catch (IOException e) {
                //BLog.e(CLIENT_TAG, "I/O Exception"+ e.getMessage());
            } catch (Exception e) {
                //BLog.e(CLIENT_TAG, "Error3"+ e.getMessage());
            }
            //BLog.e(CLIENT_TAG, "Client sent message: " + msg);
        }
    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	public void discoverService() {
		BLog.e(TAG, "RUNNING DISCOVER SERVICE");
	    DnsSdTxtRecordListener txtListener = new DnsSdTxtRecordListener() {
	        @Override


	        public void onDnsSdTxtRecordAvailable(String fullDomain, Map record, WifiP2pDevice device) {
                BLog.e(TAG, "DnsSdTxtRecord available -" + record.toString());
                buddies.put(device.deviceAddress, record.get("buddyname").toString());
                if(refreshFrag!=null)
                	refreshFrag.refresh();

	        }
	    };
	    DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
	        @Override
	        public void onDnsSdServiceAvailable(String instanceName, String registrationType,
	                WifiP2pDevice resourceType) {

	                // Update the device name with the human-friendly version from
	                // the DnsTxtRecord, assuming one arrived.
	                resourceType.deviceName = buddies
	                        .containsKey(resourceType.deviceAddress) ? buddies
	                        .get(resourceType.deviceAddress) : resourceType.deviceName;
	                if(refreshFrag!=null)
	                	refreshFrag.refresh();


	                BLog.e(TAG, "onBonjourServiceAvailable " + instanceName);
	        }
	    };

	    p2pManager.setDnsSdResponseListeners(p2pChannel, servListener, txtListener);

	    WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        p2pManager.addServiceRequest(p2pChannel,
                serviceRequest,
                new ActionListener() {
                    @Override
                    public void onSuccess() {
                    	 BLog.e(TAG, "addServiceRequest - onSuccess");
                        // Success!
                    }

                    @Override
                    public void onFailure(int code) {
                    	 BLog.e(TAG, "addServiceRequest - onFailure");
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    }
         });
        
        p2pManager.discoverServices(p2pChannel, new ActionListener() {

            @Override
            public void onSuccess() {
                // Success!
            	BLog.e(TAG, "discoverServices - onSuccess");
            }

            @Override
            public void onFailure(int code) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                if (code == WifiP2pManager.P2P_UNSUPPORTED) 
                    BLog.e(TAG, "P2P isn't supported on this device.");  
            }
        });
	}
	*/

}
