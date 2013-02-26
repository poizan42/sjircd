package irc.command;

import irc.*;

/*
   Command: USERHOST
   Parameters: <nickname> *( SPACE <nickname> )

   The USERHOST command takes a list of up to 5 nicknames, each
   separated by a space character and returns a list of information
   about each nickname that it found.  The returned list has each reply
   separated by a space.

   Numeric Replies:

           RPL_USERHOST                  ERR_NEEDMOREPARAMS
*/
public class UserhostCommand extends IrcCommand
{
	public String[] nicks;
		
	public UserhostCommand(String pFullCommand, String command, String[] prefix, String params)
	{
		super(pFullCommand, command, prefix, params);

		if (parameters.length == 0)
		{
			setError(IrcNumerics.ERR_NEEDMOREPARAMS, "USER", "Not enough parameters");
			return;
		}
		
		nicks = parameters;
	}
}
