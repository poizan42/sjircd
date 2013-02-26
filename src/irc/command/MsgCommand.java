package irc.command;

import irc.*;

/*
      Command: PRIVMSG
   Parameters: <msgtarget> <text to be sent>

   PRIVMSG is used to send private messages between users, as well as to
   send messages to channels.  <msgtarget> is usually the nickname of
   the recipient of the message, or a channel name.

   The <msgtarget> parameter may also be a host mask (#<mask>) or server
   mask ($<mask>).  In both cases the server will only send the PRIVMSG
   to those who have a server or host matching the mask.  The mask MUST
   have at least 1 (one) "." in it and no wildcards following the last
   ".".  This requirement exists to prevent people sending messages to
   "#*" or "$*", which would broadcast to all users.  Wildcards are the
   '*' and '?'  characters.  This extension to the PRIVMSG command is
   only available to operators.

   Numeric Replies:

           ERR_NORECIPIENT                 ERR_NOTEXTTOSEND
           ERR_CANNOTSENDTOCHAN            ERR_NOTOPLEVEL
           ERR_WILDTOPLEVEL                ERR_TOOMANYTARGETS
           ERR_NOSUCHNICK
           RPL_AWAY

3.3.2 Notice

      Command: NOTICE
   Parameters: <msgtarget> <text>
*/
public class MsgCommand extends IrcCommand
{
	public String msgtarget, message;
		
	public MsgCommand(String pFullCommand, String command, String[] prefix, String params)
	{
		super(pFullCommand, command, prefix, params);

		if (parameters.length < 1)
		{
			setError(IrcNumerics.ERR_NORECIPIENT, null, "No recipient given ("+command+")");
			return;
		}
		
		if (parameters.length < 2)
		{
			setError(IrcNumerics.ERR_NOTEXTTOSEND, null, ":No text to send");
			return;
		}
		
		msgtarget = parameters[0];
		message = parameters[1];
	}
}
