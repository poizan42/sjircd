package irc.command;

import irc.*;

/*
3.1.7 Quit

      Command: QUIT
   Parameters: [ <Quit Message> ]

   A client session is terminated with a quit message.  The server
   acknowledges this by sending an ERROR message to the client.

   Numeric Replies:

           None.
*/

public class QuitCommand extends IrcCommand
{
	public String message = "";
		
	public QuitCommand(String pFullCommand, String command, String[] prefix, String params)
	{
		super(pFullCommand, command, prefix, params);

		if (parameters.length < 1)
		{
			setError(IrcNumerics.ERR_NOORIGIN, null, "No origin specified");
			return;
		}
		
		if (parameters.length > 0)
			message = parameters[0];
	}
}
