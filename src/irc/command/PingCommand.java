package irc.command;

import irc.*;

/*
3.7.2 Ping message

      Command: PING
   Parameters: <server1> [ <server2> ]

   The PING command is used to test the presence of an active client or
   server at the other end of the connection.  Servers send a PING
   message at regular intervals if no other activity detected coming
   from a connection.  If a connection fails to respond to a PING
   message within a set amount of time, that connection is closed.  A
   PING message MAY be sent even if the connection is active.

   When a PING message is received, the appropriate PONG message MUST be
   sent as reply to <server1> (server which sent the PING message out)
   as soon as possible.  If the <server2> parameter is specified, it
   represents the target of the ping, and the message gets forwarded
   there.

   Numeric Replies:

           ERR_NOORIGIN                  ERR_NOSUCHSERVER
*/

/*
 Det fremgår ikke tydeligt af RFC2812, men når en klient sender PING text skal serveren
 svare med PONG text
 server1 har altså slet ikke noget at gøre med en server...
 Det er kun server2 der bruges til at sætte beskedens destination
 (vi kalder dem text og destination i stedet for server1 og server2)  
 */
public class PingCommand extends IrcCommand
{
	public String text, destination;
		
	public PingCommand(String pFullCommand, String command, String[] prefix, String params)
	{
		super(pFullCommand, command, prefix, params);

		if (parameters.length < 1)
		{
			setError(IrcNumerics.ERR_NOORIGIN, null, "No origin specified");
			return;
		}
		
		text = parameters[0];
		if (parameters.length > 1)
			destination = parameters[1];
	}
}
