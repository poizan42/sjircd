package irc;

import java.io.*;
import sjircd.*;
import irc.command.*;

public class ServerCommands
{
	private BufferedWriter writer;
	public String defaultReceiver = "(null)";
	
	public ServerCommands(BufferedWriter aWriter)
	{
		writer = aWriter;
	}
	
	public synchronized void execute(String command) throws IOException
	{
//		udskriv den sendte kommando hvis der debugges
		Sjircd.debug(defaultReceiver + " <- "+command);
		writer.write(command  + "\r\n");
		writer.flush();
	}
	
	public void executeWithServerPrefix(String command) throws IOException
	{
		execute(":" + Sjircd.serverName + " " + command);
	}
	
	public void executeWithUserPrefix(UserInfo sender, String command) throws IOException
	{
		execute(":" + sender.getNick() + "!" + sender.getIdent() + "@" + sender.getHost() + " " + command);
	}
	
	public void privmsg(UserInfo sender, String message) throws IOException
	{
		executeWithUserPrefix(sender, "PRIVMSG " + defaultReceiver + " :" + message);
	}
	
	public void notice(UserInfo sender, String message) throws IOException
	{
		executeWithUserPrefix(sender, "NOTICE " + defaultReceiver + " :" + message);
	}
	
	public void pong(String text) throws IOException
	{
		executeWithServerPrefix("PONG " + defaultReceiver + " :" + text);
	}
	
	public void numericNotice(int numeric, String receiver, String params, String message) throws IOException
	{
		String s;
		s = Integer.toString(numeric);
		//pad s med nuller til en længde på 3
		if (s.length() == 1)
			s = "00"+s;
		else if (s.length() == 2)
			s = "0"+s;
		
		if (receiver != null)
			s += " " + receiver;
		if (params != null)
			s += " " + params;
		if (message != null)
			s += " :" + message;

		executeWithServerPrefix(s);
	}
	
	public void numericNotice(int numeric, String params, String message) throws IOException
	{
		numericNotice(numeric, defaultReceiver, params, message);
	}
	
	/*public void sendNumeric(int numeric, String receiver, String[] msgParams) throws IOException
	{
		String message = null, params = null;
		
		switch (numeric)
		{
			case IrcNumerics.ERR_NOTREGISTERED: //451 (no receiver)
				params = "";
				message = "You have not registered";
				break;
			case IrcNumerics.ERR_NEEDMOREPARAMS: //461 "<command> :Not enough parameters"
				params = msgParams[0];
				message = "Not enough parameters";
				break;
			case IrcNumerics.ERR_UNKNOWNCOMMAND: //<command> :Unknown command
				params = msgParams[0];
				message = "Unknown command";
				break;
		}
		numericNotice(numeric, receiver, params, message);
	}*/
	
	public void sendErrNotReg(String command) throws IOException
	{
		numericNotice(IrcNumerics.ERR_NOTREGISTERED, null, command, "You have not registered");
	}
	
	public void sendSyntaxErrNotice(IrcCommand commandWithError) throws IOException
	{
		numericNotice(commandWithError.getErrorNum(), commandWithError.getErrorParams(), commandWithError.getErrorMessage());
	}
	
//	"<nickname> :No such nick/channel"
	public void sendNoSuchNickErr(String nick) throws IOException
	{
		numericNotice(IrcNumerics.ERR_NOSUCHNICK, nick, "No such nick/channel");
	}

	//"<nick> :Nickname is already in use"
	public void sendNicknameInUseErr(String nick) throws IOException
	{

		numericNotice(IrcNumerics.ERR_NICKNAMEINUSE, nick, "Nickname is already in use");
	}
	
	//"<server name> :No such server"
	public void sendNoSuchServerErr(String server) throws IOException
	{
		numericNotice(IrcNumerics.ERR_NOSUCHSERVER, server, "No such server");
	}
	
	/*":*1<reply> *( " " <reply> )"

	 - Reply format used by USERHOST to list replies to
	   the query list.  The reply string is composed as
	   follows:

	   reply = nickname [ "*" ] "=" ( "+" / "-" ) hostname

	   The '*' indicates whether the client has registered
	   as an Operator.  The '-' or '+' characters represent
	   whether the client has set an AWAY message or not
	   respectively.
	   
	   Example:

   USERHOST Wiz Michael syrk       ; USERHOST request for information on
                                   nicks "Wiz", "Michael", and "syrk"

   :ircd.stealth.net 302 yournick :syrk=+syrk@millennium.stealth.net
                                   ; Reply for user syrk
	 * */		
	public void sendUserhostReply(UserhostCommand command) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		for (String nick : command.nicks)
		{
			UserInfo user = Sjircd.getUser(nick);
			if (user == null)
				continue;
			sb.append(nick);
			if (user.isOper())
				sb.append('*');
			sb.append("=" + (user.isAway() ? "-" : "+") + user.getIdent() + "@" + user.getHost() + " ");
		}
		numericNotice(IrcNumerics.RPL_USERHOST, null, sb.toString());
	}
}
