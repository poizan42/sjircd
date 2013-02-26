package irc.command;

import irc.*;

//syntax: NICK (nick)
public class NickCommand extends IrcCommand
{
	public String nick;
		
	public NickCommand(String pFullCommand, String command, String[] prefix, String params)
	{
		super(pFullCommand, command, prefix, params);

		if (parameters.length < 1)
			setError(IrcNumerics.ERR_NONICKNAMEGIVEN, null, "No nickname given");
		else
			nick = parameters[0];
	}
}
