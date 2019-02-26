package run.brief.settings;


import run.brief.util.upnp.Gateway;
import run.brief.util.upnp.PortMappingEntry;
import run.brief.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
 
public class GatewayPortMappingEntryAdapter extends BaseAdapter {
 
    private Activity activity;
    //private JSONArray data;
    private static LayoutInflater inflater=null;
 
    public GatewayPortMappingEntryAdapter(Activity a) {
        activity = a;
        //this.data=data;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public int getCount() {
        return Gateway.getAllGatewayPorts().size();
    }
 
    public Object getItem(int position) {
        return Gateway.getAllGatewayPorts().get(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.account_list_item, null);
 
        TextView type = (TextView)vi.findViewById(R.id.account_type); 
        TextView name = (TextView)vi.findViewById(R.id.account_name); 
 

        PortMappingEntry pm = Gateway.getAllGatewayPorts().get(position);
        // Setting all values in listview
        if(pm!=null) {
        	name.setText(pm.getPortMappingDescription());
        	type.setText(pm.getInternalClient()+" : "+pm.getInternalPort()+" : "+pm.getExternalPort());
	        
        }
        return vi;
    }
}
