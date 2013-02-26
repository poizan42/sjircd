package sjircd;

import java.io.*;
import java.net.*;
import java.util.*;
import irc.*;

public class Sjircd {

	private static HashMap<String, ChannelInfo> chanList = new HashMap<String, ChannelInfo>();
	private static HashMap<String, UserInfo> nickList = new HashMap<String, UserInfo>();
	
	public static final String serverName = "SjIRCd.local";
	public static final String networkName = "SjIRCd test network";
	public static final int listenPort = 6642;
	public static final boolean debugMode = true;
	
	public static void addUser(UserInfo user)
	{
		synchronized (nickList)
		{
			nickList.put(user.getNick().toLowerCase(), user);
		}
		log("User connected: "+user.toString());
	}
	
	public static void userDisconnected(UserInfo user, String message)
	{
		synchronized (nickList)
		{
			nickList.remove(user.getNick());
		}
		log(user.getNick()+"!"+user.getIdent()+"@"+user.getHost()+" ("+user.getRealHost()+") quit ("+message+")");
	}
	
	public static UserInfo getUser(String nick)
	{
		synchronized (nickList)
		{
			return nickList.get(nick.toLowerCase());
		}
	}
	
	public static void log(String msg)
	{
		System.out.println(msg);
	}
	
	public static void debug(String msg)
	{
		if (debugMode)
			System.out.println(msg);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Socket conSock; 
		
		try
		{
			//InputStreamReader stdin = new InputStreamReader(System.in);
			//BufferedReader console = new BufferedReader(stdin);
			
			ServerSocket sock = new ServerSocket(listenPort);
			System.out.println("Accepting connections");
			
			while (true)
			{
				conSock = sock.accept();
				Connection con = new Connection(conSock);
				log("Client connected: "+conSock.getInetAddress().getHostAddress()+" ("+conSock.getInetAddress().getCanonicalHostName()+")");
				con.start();
				
				/*s = console.readLine();
				if ((Connection.curCon != null))
				{
					Connection.curCon.connectionWriter.write(s+"\r\n");
					Connection.curCon.connectionWriter.flush();
				}
				else
					System.out.println("Not connected!");*/
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(255);
		}
	}

	public static HashMap<String, ChannelInfo> getChanList() {
		return chanList;
	}


	public static HashMap<String, UserInfo> getNickList() {
		return nickList;
	}

}
