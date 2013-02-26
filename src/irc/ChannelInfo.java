package irc;

import java.util.HashMap;

import sjircd.*;

public class ChannelInfo
{
	private String name;
	private HashMap<String, UserInfo> nicksInChannel = new HashMap<String, UserInfo>();

	public String getName() {
		return name;
	}
	
	

	/*public void setName(String newName)
	{
		//TODO: få alle til at forlade den gamle kanal
		synchronized (Sjircd.getChanList())
		{
			Sjircd.getChanList().remove(name.toLowerCase());
			Sjircd.getChanList().put(newName.toLowerCase(), this);
			this.name = name;
		}
		//TODO: få alle til at joine den nye kanal
	}*/
	
	public String getFullName() {
		return "#"+name;
	}
}
