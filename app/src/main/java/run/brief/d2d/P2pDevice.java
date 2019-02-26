package run.brief.d2d;

public class P2pDevice {
	public String ipAddress;
	public String name;
	public boolean isGroupOwner;
	public int status;
	public P2pDevice(String ipAddress,String name,boolean isGroupOwner, int status) {
		this.ipAddress=ipAddress; this.name=name; this.isGroupOwner=isGroupOwner; this.status=status;
	}
}
