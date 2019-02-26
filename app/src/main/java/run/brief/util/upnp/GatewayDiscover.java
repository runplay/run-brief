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
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Alessandro Bahgat Shehata - ale dot bahgat at gmail dot com
 * Daniele Castagna - daniele dot castagna at gmail dot com
 *
 */
package run.brief.util.upnp;


import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import run.brief.util.log.BLog;


public class GatewayDiscover {
    private static final String[] DEFAULT_SEARCH_TYPES =
            {
                    "urn:schemas-upnp-org:service:WANIPConnection:1",
                    "urn:schemas-upnp-org:service:WANPPPConnection:1",
                    "urn:schemas-upnp-org:device:InternetGatewayDevice:1"
            };
    /**
     * The SSDP port
     */
    public static final int PORT = 1900;
    /**
     * The broadcast address to use when trying to contact UPnP devices
     */
    public static final String IP = "239.255.255.250";
    /**
     * The timeout to set for the initial broadcast request
     */
    private static final int TIMEOUT = 3000;
    /**
     * A map of the GatewayDevices discovered so far.
     * The assumption is that a machine is connected to up to a Gateway Device
     * per InetAddress
     */
    private Map<InetAddress, GatewayDevice> devices = new HashMap<InetAddress, GatewayDevice>();

    public Map<InetAddress, GatewayDevice> getDevices() {
        return devices;
    }
    /**
     * The default constructor
     */
    public GatewayDiscover() {
    }

    /**
     * Discovers Gateway Devices on the network(s) the executing machine is
     * connected to.
     *
     * The host may be connected to different networks via different network
     * interfaces.
     * Assumes that each network interface has a different InetAddress and
     * returns a map associating every GatewayDevice (responding to a broadcast
     * discovery message) with the InetAddress it is connected to.
     *
     * @return a map containing a GatewayDevice per InetAddress
     * @throws SocketException
     * @throws UnknownHostException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public Map<InetAddress, GatewayDevice> discover() throws SocketException, UnknownHostException, IOException, SAXException, ParserConfigurationException {

        DatagramSocket ssdp = new DatagramSocket();
        int port = ssdp.getLocalPort();

        for(int i=0; i<DEFAULT_SEARCH_TYPES.length; i++) {
            String testtype=DEFAULT_SEARCH_TYPES[i];
            final String searchMessage = "M-SEARCH * HTTP/1.1\r\n" +
                    "HOST: " + IP + ":" + PORT + "\r\n" +
                    "ST: " + testtype + "\r\n" +
                    "MAN: \"ssdp:discover\"\r\n" +
                    "MX: 2\r\n" +
                    "\r\n";
            BLog.e("testing: "+searchMessage);

                byte[] searchMessageBytes = searchMessage.getBytes();
                DatagramPacket ssdpDiscoverPacket = new DatagramPacket(searchMessageBytes, searchMessageBytes.length);
                ssdpDiscoverPacket.setAddress(InetAddress.getByName(IP));
                ssdpDiscoverPacket.setPort(PORT);

                ssdp.send(ssdpDiscoverPacket);
                ssdp.setSoTimeout(TIMEOUT);

                boolean waitingPacket = true;

                while (waitingPacket) {
                    DatagramPacket receivePacket = new DatagramPacket(new byte[1536], 1536);
                    try {
                        ssdp.receive(receivePacket);
                        byte[] receivedData = new byte[receivePacket.getLength()];
                        System.arraycopy(receivePacket.getData(), 0, receivedData, 0, receivePacket.getLength());

                        // TODO: devices should be a map, and receivePacket.address should be the key ;)
                        GatewayDevice d = parseMSearchReplay(receivedData);
                        SocketAddress addr = null;

                        if (d.getLocation() != null) {
                            URL u = new URL(d.getLocation());
                            addr = new InetSocketAddress(u.getHost(),
                                    u.getPort() != -1 ? u.getPort() : u.getDefaultPort());
                        } else {
                            addr = receivePacket.getSocketAddress();
                        }

                    /* Get local address as it appears to the Gateway */
                        InetAddress localAddress = getOutboundAddress(addr);

                        d.setLocalAddress(localAddress);
                        devices.put(localAddress, d);
                    } catch (SocketTimeoutException ste) {
                        waitingPacket = false;
                    }
                }



        }
        for (GatewayDevice device : devices.values()) {
            try {
                device.loadDescription();
            } catch (Exception e) {
                BLog.e("LOAD DESC ERROR");
            }
        }
        ssdp.close();

        return devices;
    }

    /**
     * Parses the reply from UPnP devices
     * @param reply the raw bytes received as a reply
     * @return the representation of a GatewayDevice
     */
    private GatewayDevice parseMSearchReplay(byte[] reply) {

        GatewayDevice device = new GatewayDevice();

        // XXX it would be better to pay attention to the encoding
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(reply)));
        String line = null;
        try {
            line = br.readLine().trim();
        } catch (IOException ex) {
        }

        while (line != null && line.trim().length() > 0) {

            if (line.startsWith("HTTP/1.")) {
            } else {
                String key = line.substring(0, line.indexOf(':'));
                String value = line.length() > key.length() + 1 ? line.substring(key.length() + 1) : null;

                key = key.trim();
                if (value != null) {
                    value = value.trim();
                }

                if (key.compareToIgnoreCase("location") == 0) {
                    device.setLocation(value);
                } else if (key.compareToIgnoreCase("st") == 0) {
                    device.setSt(value);
                }
            }
            try {
                line = br.readLine().trim();
            } catch (IOException ex) {
            }
        }

        return device;
    }

    /**
     * Gets the valid gateway
     * @return the first GatewayDevice which is connected to the network, or
     * null if nost present
     */
    public GatewayDevice getMyGateway() {
        Set<InetAddress> it = devices.keySet();
        for(InetAddress in: it) {
                return devices.get(in);

        }
        return null;
    }
    public GatewayDevice getValidGateway() {

        for (GatewayDevice device : devices.values()) {
            BLog.e("device: "+device.getFriendlyName());
            try {
                if (device.isConnected()) {
                    return device;
                } else {
                    //
                }
            } catch (Exception e) {
            }
        }
        return null;
    }
    public GatewayDevice getNotConnectedGateway() {

        for (GatewayDevice device : devices.values()) {
            try {
                if (!device.isConnected()) {
                    return device;
                } else {
                    //
                }
            } catch (Exception e) {
            }
        }
        return null;
    }
    /**
     * Gets the (local) address that can be used to reach this host machine
     * from the remote party identified by <tt>remoteAddress</tt>.
     *
     * @param remoteAddress the address of the remote party.
     * @return the address that should be used to contact the local host from
     *      <tt>remoteAddress</tt>
     * @throws SocketException on network failure
     */
    private InetAddress getOutboundAddress(SocketAddress remoteAddress) throws SocketException {
        DatagramSocket sock = new DatagramSocket();
        // connect is needed to bind the socket and retrieve the local address
        // later (it would return 0.0.0.0 otherwise)
        sock.connect(remoteAddress);

        InetAddress localAddress = sock.getLocalAddress();

        sock.disconnect();
        sock = null;

        // for Windows OSi, bind a UDP socket does not permit to get the local
        // address, so we try to connect via TCP.
        if(localAddress.isAnyLocalAddress())
        {
            try
            {
                Socket tcpSock = new Socket();

                tcpSock.setSoTimeout(1500);
                tcpSock.connect(remoteAddress);
                localAddress = tcpSock.getLocalAddress();
                tcpSock.close();
                tcpSock = null;
            }
            catch(Exception e)
            {
            }
        }

        return localAddress;
    }
}


/*



import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;

import run.brief.util.log.BLog;


public class GatewayDiscover {



import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import run.brief.util.log.BLog;

public static final int PORT = 1900;

    //public static final String IP = "239.255.255.250";
    public static final String IP = "255.255.255.255";

    private static final int TIMEOUT = 25000;


    private String[] searchTypes;
    

    private static final String[] DEFAULT_SEARCH_TYPES =
        {
            "urn:schemas-upnp-org:service:WANIPConnection:1",
            "urn:schemas-upnp-org:service:WANPPPConnection:1",
            "urn:schemas-upnp-org:device:InternetGatewayDevice:1"
        };
            
    

    private final Map<InetAddress, GatewayDevice> devices = new HashMap<InetAddress, GatewayDevice>();


    private class SendDiscoveryThread extends Thread {
        //InetAddress ip;
        String searchMessage;
        int PORT;

        SendDiscoveryThread(InetAddress localIP, String searchMessage, int port) {
            //this.ip = localIP;
            this.searchMessage = searchMessage;
            this.PORT=port;
        }

        @Override
        public void run() {

            DatagramSocket ssdp = null;

            try {
            	
                // Create socket bound to specified local address

                //DatagramChannel channel = DatagramChannel.open();
                //ssdp = channel.socket();
                //ssdp.setReuseAddress(true);
                //ssdp.setBroadcast(true);
                //ssdp.bind();
                InetAddress ip = InetAddress.getByName(IP);
                ssdp = new DatagramSocket(new InetSocketAddress(ip, PORT));

                byte[] searchMessageBytes = searchMessage.getBytes();
                DatagramPacket ssdpDiscoverPacket = new DatagramPacket(searchMessageBytes, searchMessageBytes.length);
                ssdpDiscoverPacket.setAddress(ip);
                ssdpDiscoverPacket.setPort(PORT);

                ssdp.send(ssdpDiscoverPacket);
                ssdp.setSoTimeout(TIMEOUT);

                boolean waitingPacket = true;
                while (waitingPacket) {
                	BLog.e("GatewayDiscover", "responce from ");
                    DatagramPacket receivePacket = new DatagramPacket(new byte[516], 516);
                    try {
                        ssdp.receive(receivePacket);
                        BLog.e("GatewayDiscover", "packet received: "+receivePacket.getLength()+" -- "+new String(receivePacket.getData()));
                        byte[] receivedData = new byte[receivePacket.getLength()];
                        System.arraycopy(receivePacket.getData(), 0, receivedData, 0, receivePacket.getLength());

                        // Create GatewayDevice from response
                        GatewayDevice gatewayDevice = parseMSearchReply(receivedData);

                        gatewayDevice.setLocalAddress(ip);
                        try {
                        	gatewayDevice.loadDescription();

                        } catch(Exception e) {
                        	BLog.e("GTD", "discription did not load: "+e.getMessage());
                        }
                        // verify that the search type is among the requested ones
                        if (Arrays.asList(searchTypes).contains(gatewayDevice.getSt())) {
                            synchronized (devices) {
                            	//gatewayDevice.isConnected()
                                devices.put(ip, gatewayDevice);
                                break; // device added for this ip, nothing further to do
                            }
                        }
                    } catch (SocketTimeoutException ste) {
                        waitingPacket = false;
                        BLog.e("GTD", "SocketTimeoutException: "+ste.getMessage());
                    }
                }

            } catch (Exception e) {
                BLog.e("GTD", ""+e.getMessage());
            } finally {
                if (null != ssdp) {
                    ssdp.close();
                }
            }
        }
    }


    public GatewayDiscover() {
        this(DEFAULT_SEARCH_TYPES);
    }


    public GatewayDiscover(String st) {
        this(new String[]{st});
    }

    public GatewayDiscover(String[] types) {
        this.searchTypes = types;
    }

    public Map<InetAddress, GatewayDevice> discover(String gatewayIP) {

    	System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
    	
        Collection<InetAddress> ips = new ArrayList<InetAddress>();//getLocalInetAddresses(true, false, false);
        try {
        	ips.add(InetAddress.getByName(gatewayIP));
        } catch(UnknownHostException e) {
        	//BLog.e("GDUNK", "unknown host: "+e.getMessage());
        }

        //BLog.e("GatewayDiscover", (ips!=null?""+ips.size():"ips is null"));
        for (int i = 0; i < searchTypes.length; i++) {

            String searchMessage = "M-SEARCH * HTTP/1.1\r\n" +
                    "HOST: " + IP + ":" + PORT + "\r\n" +
                    "ST: " + searchTypes[i] + "\r\n" +
                    "MAN: \"ssdp:discover\"\r\n" +
                    "MX: 2\r\n" +    // seconds to delay response
                    "\r\n";
            
            // perform search requests for multiple network adapters concurrently
            Collection<SendDiscoveryThread> threads = new ArrayList<SendDiscoveryThread>();
            for (InetAddress ip : ips) {
            	//BLog.e("GatewayDiscover", ip+"--"+Sf.restrictLength(searchMessage,5));
                SendDiscoveryThread thread = new SendDiscoveryThread(ip, searchMessage, PORT);
                threads.add(thread);
                thread.start();
            }

            // wait for all search threads to finish
            for (SendDiscoveryThread thread : threads)
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    // continue with next thread
                }

            // If a search type found devices, don't try with different search type
            //if (!devices.isEmpty())
            //    break;

        } // loop SEARCHTYPES

        return devices;
    }


    private GatewayDevice parseMSearchReply(byte[] reply) {

        GatewayDevice device = new GatewayDevice();

        String replyString = new String(reply);

        BLog.e("Gateway reply: "+replyString);

        StringTokenizer st = new StringTokenizer(replyString, "\n");

        while (st.hasMoreTokens()) {
            String line = st.nextToken().trim();
            //BLog.e("GD-L", line);
            if (line.isEmpty())
                continue;

            if (line.startsWith("HTTP/1.") || line.startsWith("NOTIFY *"))
                continue;

            String key = line.substring(0, line.indexOf(':'));
            String value = line.length() > key.length() + 1 ? line.substring(key.length() + 1) : null;

            key = key.trim();
            if (value != null) {
                value = value.trim();
            }

            if (key.compareToIgnoreCase("location") == 0) {
                device.setLocation(value);

            } else if (key.compareToIgnoreCase("st") == 0) {    // Search Target
                device.setSt(value);
            }
        }

        return device;
    }


    public GatewayDevice getValidGateway() {

        for (GatewayDevice device : devices.values()) {
            //try {
                if (device.isConnected()) {
                    return device;
                }
            //} catch (Exception e) {
            //BLog.e("GTD", "is valid device: "+device.getFriendlyName());
            //}
        }

        return null;
    }


    public Map<InetAddress, GatewayDevice> getAllGateways() {
        return devices;
    }


    private List<InetAddress> getLocalInetAddresses(boolean getIPv4, boolean getIPv6, boolean sortIPv4BeforeIPv6) {
        List<InetAddress> arrayIPAddress = new ArrayList<InetAddress>();
        int lastIPv4Index = 0;

        // Get all network interfaces
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return arrayIPAddress;
        }

        if (networkInterfaces == null)
            return arrayIPAddress;

        // For every suitable network interface, get all IP addresses
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface card = networkInterfaces.nextElement();

            try {
                // skip devices, not suitable to search gateways for
                if (card.isLoopback() || card.isPointToPoint() ||
                        card.isVirtual() || !card.isUp())
                    continue;
            } catch (SocketException e) {
                continue;
            }

            Enumeration<InetAddress> addresses = card.getInetAddresses();

            if (addresses == null)
                continue;

            while (addresses.hasMoreElements()) {
                InetAddress inetAddress = addresses.nextElement();
                int index = arrayIPAddress.size();

                if (!getIPv4 || !getIPv6) {
                    if (getIPv4 && !Inet4Address.class.isInstance(inetAddress))
                        continue;

                    if (getIPv6 && !Inet6Address.class.isInstance(inetAddress))
                        continue;
                } else if (sortIPv4BeforeIPv6 && Inet4Address.class.isInstance(inetAddress)) {
                    index = lastIPv4Index++;
                }

                arrayIPAddress.add(index, inetAddress);
            }
        }

        return arrayIPAddress;
    }

}
*/