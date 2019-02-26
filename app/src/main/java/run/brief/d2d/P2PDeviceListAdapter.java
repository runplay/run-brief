package run.brief.d2d;


import java.util.List;

import run.brief.util.log.BLog;
import run.brief.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
 
public class P2PDeviceListAdapter extends BaseAdapter {
 
    private Activity activity;
    //private JSONArray data;
    private static LayoutInflater inflater=null;
 
    private List<P2pDevice> devices;
    public P2PDeviceListAdapter(Activity a, List<P2pDevice> d) {
        activity = a;
        devices = d;
        //this.data=data;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public int getCount() {
        return devices.size();
    }
 
    public Object getItem(int position) {
        return devices.get(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.d2d_chat_device_list, null);
 
        TextView type = (TextView)vi.findViewById(R.id.p2p_device_info); 
        TextView name = (TextView)vi.findViewById(R.id.p2p_device_name); 
 

        //PortMappingEntry pm = Gateway.getAllGatewayPorts().get(position);
        P2pDevice device = devices.get(position);
        // Setting all values in listview
        if(device!=null) {
        	try {
        	name.setText("d: "+device.name);
        	type.setText(device.ipAddress);
        	} catch(Exception e) {
        		BLog.e("P2PAdapter", "e: "+e.getMessage());
        	}
        	//name.setText(pm.getPortMappingDescription());
        	//type.setText(pm.getInternalClient()+" : "+pm.getInternalPort()+" : "+pm.getExternalPort());
	        
        }
        return vi;
    }
}
