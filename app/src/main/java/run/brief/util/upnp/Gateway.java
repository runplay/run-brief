/* 
 *              weupnp - Trivial upnp java library 
 *
 * Copyright (C) 2008 Alessandro Bahgat Shehata, Daniele Castagna
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, FΩifth Floor, Boston, MA  02110-1301  USA
 * 
 * Alessandro Bahgat Shehata - ale dot bahgat at gmail dot com
 * Daniele Castagna - daniele dot castagna at gmail dot com
 * 
 */

/*
 * refer to miniupnpc-1.0-RC8
 */
package run.brief.util.upnp;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;

import run.brief.R;
import run.brief.b.Device;
import run.brief.util.Cal;
import run.brief.util.log.BLog;

/**
 * This class contains a trivial main method that can be used to test whether
 * weupnp is able to manipulate port mappings on a IGD (Internet Gateway
 * Device) on the same network.
 *
 * @author Alessandro Bahgat Shehata
 */
public class Gateway {

	public static final int STATE_DISCONNECTED=0;
	public static final int STATE_WORKING=2;
	public static final int STATE_CONNECTED=1;
	public static final int STATE_USER_INTERVENTION=3;
	public static final int ALLOW_CONNECTION_ATTEMPTS=3;
	
	private static Gateway G=new Gateway();
	private static Map<InetAddress, GatewayDevice> gateways;
	
	private int connectionAttempts=0;   
	private Runnable task;
	//private Handler taskHandler = new Handler();
	
	private String GATEWAY_NAME_NONE="-----";
	public static final String IP_NONE="0.0.0.0";
	
	private GatewayDevice activeGW;
	private ArrayList<PortMappingEntry> gatewayPorts=new ArrayList<PortMappingEntry>();
	private int STATE=STATE_DISCONNECTED;
    private boolean UpnpDataConnected=false;
    private boolean UpnpVoiceConnected=false;
    
    private String gatewayName=GATEWAY_NAME_NONE;

	private String gatewayInternalIP=IP_NONE;
    private String gatewayExternalIP=IP_NONE;
    
    //private PortMappingEntry voiceMapping;
    private PortMappingEntry dataMapping;
    private GatewayDiscover gatewayDiscover;
    
    private boolean shouldcreate=false;
    
    private ArrayList<PortMappingEntry> allPorts=new ArrayList<PortMappingEntry>();
    
    //private int UpnpPortDataInternal;
    //private int UpnpPortDataExternal;
    //private String UpnpIPDataInternal=IP_NONE;
	
    //private int UpnpPortVoiceInternal;
    //private int UpnpPortVoiceExternal;
    //private String UpnpIPVoiceInternal=IP_NONE;
    public static int getState() {
    	return G.STATE;
    }
    public static String getGatewayName() {
		return G.gatewayName;
	}


	public static String getGatewayInternalIP() {
		return G.gatewayInternalIP;
	}


	public static String getGatewayExternalIP() {
		return G.gatewayExternalIP;
	}
	
    public static ArrayList<PortMappingEntry> getAllGatewayPorts() {
    	return G.gatewayPorts;
    }
    public synchronized void refresh(Context context) {
    	refresh(context,true);
    }
	public static synchronized void goRefresh(Context context, boolean forceIfWifi) {
		G.refresh(context,true);
	}
    public synchronized void refresh(Context context, boolean forceIfWifi) {
/*
		InternetGatewayDevice dev = null;
		try {
			dev=InternetGatewayDevice.getDevices(1000)[0];
			//String thisWillBeHandy = dev.getExternalIPAddress();
			String connType = "TCP"; // or "UDP"
			String remoteHost = null; // allow any remote host to connect
			int internetSidePort = 1337;
			String localHost = G.gatewayInternalIP;
			int localPort = 7331;
			boolean added=dev.addPortMapping("A mapping for my game", connType, remoteHost, internetSidePort, localHost, localPort, 0);


			boolean deleted=dev.deletePortMapping( null, internetSidePort, connType );

			BLog.e("internetgatewaydev ADDDEL: "+added+ " -- "+deleted);
		} catch(Exception e) {
			BLog.e("internetgatewaydev: "+e.getMessage());
		}
*/

    	G.GATEWAY_NAME_NONE = context.getResources().getString(R.string.comms_no_gateway);
    	Device.CheckInternet(context);
    	if(Device.getCONNECTION_STATE()==Device.CONNECTION_STATE_CONNECTED) {
    		
    		if(Device.getCONNECTION_TYPE()==Device.CONNECTION_TYPE_WIFI) {
    	    	WifiManager wifii= (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    	    	DhcpInfo d=wifii.getDhcpInfo();
    	    	
    	    	//String gotIP=intToIp(d.serverAddress);
    	    	String gotIP=intToIp(d.gateway);
    	    	if(!G.gatewayInternalIP.equals(gotIP) || forceIfWifi) {
    	    		G.gatewayInternalIP=gotIP;
    	    		notifyWifiConnected();
    	    	}
    		}
    		
    	} else {
    		if(Device.getCONNECTION_STATE()==Device.CONNECTION_STATE_NONE) {
    			zeroOutGateway();
    		}
    	}
   		
    		
    }
    private static String intToIp(int i) {

    	   return ( i & 0xFF) + "." +
    			   ((i >> 8 ) & 0xFF) + "." +
    			   ((i >> 16 ) & 0xFF) + "." +
    	           ((i >> 24 ) & 0xFF ) ;
    }
    public static void notifyWifiConnected() {
    	BLog.e("GTEW", "NotifyWifiConnected=tryue");
    	G.shouldcreate=false;
    	
    	switch(G.STATE) {
    		case STATE_USER_INTERVENTION:
    		case STATE_WORKING:
    			break;
    		case STATE_DISCONNECTED:
    			if(G.connectionAttempts<ALLOW_CONNECTION_ATTEMPTS) {
	    			G.connectionAttempts++;
	    			G.shouldcreate=true;  	
	    			Discover();
	    			
    			}
    		default:

    			break;
    	}
    	
    }
    
    public static void finishedDiscoveryCheck() {
		if(G.gatewayDiscover!=null) {
			G.activeGW = G.gatewayDiscover.getMyGateway();//.getValidGateway();
			if (G.activeGW!=null && G.activeGW.isConnected()) {
				G.STATE=STATE_CONNECTED;
				G.gatewayName=G.activeGW.getFriendlyName();
				G.gatewayExternalIP=G.activeGW.getExternalIPAddress();
				G.gatewayInternalIP=G.activeGW.getLocalAddress().getHostAddress();
				BLog.e("UPNP","Using gateway: " + G.activeGW.getFriendlyName());
			} else {
				G.STATE=STATE_DISCONNECTED;
				G.gatewayInternalIP=IP_NONE;
				G.gatewayExternalIP=IP_NONE;
				G.gatewayName=G.GATEWAY_NAME_NONE;
				BLog.e("UPNP","No active gateway device found");
				//BLog.e("UPNP","Stopping weupnp");
			}
	
			BLog.e("GTEW", "active gateway devices: "+G.gatewayDiscover.getDevices().size());
	    	boolean b=true;
	    	if(b) {
				BLog.e(""+(G.activeGW!=null) +"--"+ (G.STATE!=STATE_CONNECTED) +"--"+ G.shouldcreate);
	    	if(G.activeGW!=null && G.STATE!=STATE_CONNECTED && G.shouldcreate) {
				//G.activeGW = G.gatewayDiscover.getNotConnectedGateway();
				//if (G.activeGW != null) {
					BLog.e("GTEW", "should create called: " + G.shouldcreate);

					Cal cal = new Cal();
					int dataPort = getPortData(0);
					int voicePort = getPortVoice(0);
					String thisIP = Device.getInternalIP();
					G.dataMapping = new PortMappingEntry(dataPort, dataPort, G.gatewayInternalIP, thisIP, "TCP", "Brief Md -" + cal.getTimeMMSS());
					//G.voiceMapping=new PortMappingEntry(voicePort,voicePort,G.gatewayInternalIP,thisIP,"TCP","Brief Mv -"+cal.getTimeMMSS());

					boolean hasdata = false;
					boolean hasvoice = false;
					int appenddata = 0;
					int appendvoice = 0;


					if (G.dataMapping != null) {
						PortMappingEntry portdata = G.activeGW.getSpecificPortMappingEntry(G.dataMapping.getExternalPort(), G.dataMapping.getProtocol(), G.dataMapping);
						if (portdata != null) {
							BLog.e("port data not null");
							appenddata++;
							if (G.dataMapping.getInternalClient().equals(portdata.getInternalClient())
									&& G.dataMapping.getInternalPort() == portdata.getInternalPort()
									) {
								// same
								G.dataMapping = portdata;
								hasdata = true;
							} else {
								G.dataMapping.setExternalPort(G.dataMapping.getExternalPort() + 1);
								G.dataMapping.setInternalPort(G.dataMapping.getInternalPort() + 1);
								portdata = G.activeGW.getSpecificPortMappingEntry(G.dataMapping.getExternalPort(), G.dataMapping.getProtocol(), G.dataMapping);
								appenddata++;
								if (portdata != null) {
									if (G.dataMapping.getInternalClient().equals(portdata.getInternalClient())
											&& G.dataMapping.getInternalPort() == portdata.getInternalPort()
											) {
										// same
										G.dataMapping = portdata;
										hasdata = true;
									}
								}
							}
						}
						BLog.e("GTEW", "data mapping: not null");
					} else {
						BLog.e("GTEW", "data mapping: IS null");
					}
				BLog.e("has data: "+hasdata);
					if (hasdata) {
						G.STATE = STATE_CONNECTED;
					}
				if(!hasdata) {
					try {
						hasdata = G.activeGW.addPortMapping(G.dataMapping.getExternalPort() + appenddata, G.dataMapping.getInternalPort(), G.dataMapping.getInternalClient(), "TCP", "Brief Md -" + cal.getTimeMMSS());
						BLog.e("ADDED PORT MAPPING");
					} catch(Exception e) {
						BLog.e("ADD EX: "+e.getMessage());

					}
				}

				/*
	    		if(G.voiceMapping!=null) {
	    			PortMappingEntry portvoice = G.activeGW.getSpecificPortMappingEntry(G.voiceMapping.getExternalPort(), G.voiceMapping.getProtocol(),G.voiceMapping);

	    			//PortMappingEntry portdata = G.activeGW.getSpecificPortMappingEntry(G.dataMapping.getExternalPort(), G.dataMapping.getProtocol());
				    if(portvoice!=null) {
				    	appendvoice++;
				    	if(G.voiceMapping.getInternalClient().equals(portvoice.getInternalClient())
				    		&& G.voiceMapping.getInternalPort()==portvoice.getInternalPort()
				    			) {
				    		BLog.e("GTEW","ALREADY has voiceMapping");
				    		// same   
				    		G.voiceMapping=portvoice;
				    		hasvoice=true;
				    	} else {
				    		BLog.e("GTEW","ALREADY has voiceMapping");
				    		G.voiceMapping.setExternalPort(G.voiceMapping.getExternalPort()+1);
				    		G.voiceMapping.setInternalPort(G.voiceMapping.getInternalPort()+1);
				    		portvoice = G.activeGW.getSpecificPortMappingEntry(G.voiceMapping.getExternalPort(), G.voiceMapping.getProtocol(),G.voiceMapping);
				    		appendvoice++;
						    if(portvoice!=null) {
						    	if(G.voiceMapping.getInternalClient().equals(portvoice.getInternalClient())
						    		&& G.voiceMapping.getInternalPort()==portvoice.getInternalPort()
						    			) {
						    		// same
						    		G.voiceMapping=portvoice;
						    		hasvoice=true;
						    	} 
						    }
				    	}
				    }
	    		}
*/
					// create if needed
				/*

	    		if(!hasdata) {
	    			hasdata = G.activeGW.addPortMapping(G.dataMapping.getExternalPort()+appenddata, G.dataMapping.getInternalPort(), G.dataMapping.getInternalClient(),"TCP","Brief Md -"+cal.getTimeMMSS(),G.dataMapping);
	    		}  
	    		if(!hasvoice) {
	    			hasvoice = G.activeGW.addPortMapping(G.voiceMapping.getExternalPort(), G.voiceMapping.getInternalPort(),G.voiceMapping.getInternalClient(),"TCP","Brief Mv -"+cal.getTimeMMSS(),G.voiceMapping);
	    		}

        		if(!hasdata) {
        			hasdata = G.activeGW.addPortMapping(G.dataMapping.getExternalPort(), G.dataMapping.getInternalPort(), G.dataMapping.getInternalClient(),"TCP","Brief Md -"+cal.getTimeMMSS());
        		}
        		if(!hasvoice) {
        			hasvoice = G.activeGW.addPortMapping(G.voiceMapping.getExternalPort(), G.voiceMapping.getInternalPort(), G.voiceMapping.getInternalClient(),"TCP","Brief Mv -"+cal.getTimeMMSS());
        		}
	    		   */


				//}
			}


	    	}
    	
	    	if(G.activeGW!=null)
	    		G.gatewayPorts = G.activeGW.getAllPortMappings();
		}
    	
    }
    private static int getPortVoice(int add) {
    	return 21014+add;
    }
    private static int getPortData(int add) {
    	return 21034+add;
    }
    public static void notifyWifiDisconnected() {
    	G.STATE=STATE_DISCONNECTED;
        G.UpnpDataConnected=false;
        G.UpnpVoiceConnected=false;
        G.gatewayPorts.clear();
    }
	
    private static void zeroOutGateway() {
    	G.gatewayPorts.clear();
    	G.gatewayName=G.GATEWAY_NAME_NONE;
    	G.gatewayInternalIP=null;
    	
        G.UpnpDataConnected=false;
        G.UpnpVoiceConnected=false;
        
        G.dataMapping=null;
        //G.voiceMapping=null;

    }
    
	public static int SAMPLE_PORT = 6991;
	private static short WAIT_TIME = 10;
	private static boolean LIST_ALL_MAPPINGS = true;

	private static synchronized void Discover() {

		G.gatewayDiscover = new GatewayDiscover();
		BLog.e("UPNP","Looking for Gateway Devices on: "+G.gatewayInternalIP);
		try {
			gateways = G.gatewayDiscover.discover();
		} catch(Exception e) {
			BLog.e("GATEWAY CRASH: "+e.getMessage());
		}
		if (gateways==null || gateways.isEmpty()) {
			BLog.e("UPNP","No gateways found");
			return;
		}
		BLog.e("UPNP",gateways.size()+" gateway(s) found\n");

		int counter=0;
		for (GatewayDevice gw: gateways.values()) {
			//gw.addPortMapping(10027,10026,"TCP","BDirect");
			counter++;
			String mk="Listing gateway details of device #" + counter+
					"\n\tFriendly name: " + gw.getFriendlyName()+
					"\n\tPresentation URL: " + gw.getPresentationURL()+
					//"\n\tModel name: " + gw.getModelName()+
					//"\n\tModel number: " + gw.getModelNumber()+
					"\n\tLocal interface address: " + gw.getLocalAddress().getHostAddress()+"\n";
			/*
			for(PortMappingEntry pme: gw.getAllPortMappings()) {
				mk+="\tpm: "+pme.getRemoteHost()+"\n"
					+"\tpm: "+pme.getExternalPort()+"\n"
					+"\tpm: "+pme.getInternalPort()+"\n";
			}
			BLog.e("UPNP",		mk	);
			*/

		}

		finishedDiscoveryCheck();
		

	}
	
}
