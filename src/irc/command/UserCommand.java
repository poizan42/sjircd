package irc.command;

import irc.*;

/*
Command: USER
   Parameters: <user> <mode> <unused> <realname>
*/
//mirc syntax: USER (ident) ["](email host)["] ["](server name)["] :(real name)
public class UserCommand extends IrcCommand
{
	public String ident, mailhost, serverName, realName;
		
	public UserCommand(String pFullCommand, String command, String[] prefix, String params)
	{
		super(pFullCommand, command, prefix, params);

		if (parameters.length < 4)
		{
			setError(IrcNumerics.ERR_NEEDMOREPARAMS, "USER", "Not enough parameters");
			return;
		}
		
		ident = parameters[0];
		mailhost = removeQuotes(parameters[1]);
		serverName = removeQuotes(parameters[2]);
		realName = removeQuotes(parameters[3]);
	}
}
