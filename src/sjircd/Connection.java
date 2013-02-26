package sjircd;

import java.io.*;
import java.net.*;
import irc.*;
import irc.command.*;

public class Connection extends Thread
{
	//public ServerSocket listenSocket;
	private Socket connectionSocket;
	private BufferedReader connectionReader;
	private BufferedWriter connectionWriter;
	private ServerCommands commandSender;
	private boolean isAuth;
	private UserInfo userInfo;
	
	public Connection(Socket aConnectionSocket)
	{
		connectionSocket = aConnectionSocket;
	}
	
	public void run()
	{		
		boolean commanddebug = true;
		
		String cmdStr;
		IrcCommand command;
		String nick = null;
		String quitMsg = "Quit:";
		UserCommand uc = null;
		
		try
		{
			InputStreamReader conin = new InputStreamReader(connectionSocket.getInputStream(), "UTF-8");
			connectionReader = new BufferedReader(conin);

			OutputStreamWriter conout = new OutputStreamWriter(connectionSocket.getOutputStream(), "UTF-8");
			connectionWriter = new BufferedWriter(conout);
			
			commandSender = new ServerCommands(connectionWriter);
			
			while (true)
			{
				/*hvis der er de relevante informationer til at brugeren er registreret
				  (bliver sat længere nede i løkken) */
				if (!isAuth && (nick != null) && (uc != null))
				{
					String host = connectionSocket.getInetAddress().getCanonicalHostName();
					commandSender.defaultReceiver = nick;
					isAuth = true;
					userInfo = new UserInfo(nick, uc.ident, host, host, uc.realName, this);
					Sjircd.addUser(userInfo);
					//send velkomst
					commandSender.numericNotice(IrcNumerics.RPL_WELCOME, null, "Welcome to the Internet Relay Network " + userInfo.getFullPrefix());
				}
				
				if ((cmdStr = connectionReader.readLine()) == null)
					break;
				
				command = IrcCommand.parse(cmdStr);
				//ignorer blanke linier
				if (command == null)
					continue;
				
				//udskriv den modtagede streng hvis der debugges
				Sjircd.debug(nick + " -> "+cmdStr);
				
				if (command.hasError())
				{
					commandSender.sendSyntaxErrNotice(command);
					continue;					
				}
				
				if (command instanceof NickCommand)
				{
					NickCommand nc = (NickCommand)command;
					//TODO: check tegn i nick
					if (!isAuth)
					{
						if (Sjircd.getUser(nc.nick) != null)
						{
							//nick'et er i brug
							commandSender.sendNicknameInUseErr(nc.nick);
						}
						else
							nick = nc.nick;
					}
					else
					{
						//TODO: ændre nick
					}
					continue;
				}
				
				if (command instanceof UserCommand)
				{
					if (!isAuth)
					{
						uc = (UserCommand)command; 
					}
					else
					{
						commandSender.numericNotice(IrcNumerics.ERR_ALREADYREGISTRED, userInfo.getNick(), null, "Unauthorized command (already registered)");
					}
					continue;
				}
				
				//Hvis vi kommer hertil uden at vaere autentiseret er kommanden hverken USER eller NICK
				if (!isAuth)
				{
					commandSender.sendErrNotReg(command.commandName);
					continue;
				}
				
				//---------------------------------------------------------------------
				//                         Almindelige kommandoer
				//---------------------------------------------------------------------
				
				if (command instanceof QuitCommand)
				{
					quitMsg = "Quit: "+((QuitCommand)command).message;
					connectionSocket.close();
					break;
				}
				
				if (command instanceof PingCommand)
				{
					PingCommand pc = ((PingCommand)command);
					//Ikke support for flere servere, så eneste tilladte destination er serveren selv
					if ((pc.destination != null) && !pc.destination.equals(Sjircd.serverName))
						commandSender.sendNoSuchServerErr(pc.destination);
					else
						commandSender.pong(pc.text);
					continue;
				}
				
				if (command instanceof UserhostCommand)
				{
					commandSender.sendUserhostReply((UserhostCommand)command);
					continue;
				}
				
				if (command instanceof MsgCommand)
				{
					MsgCommand mc = (MsgCommand)command;
					if (mc.msgtarget.charAt(0) != '#')
					{
						UserInfo receiver = Sjircd.getUser(mc.msgtarget);
						if (receiver == null)
						{
							commandSender.sendNoSuchNickErr(mc.msgtarget);
							continue;
						}
					
						try
						{
							if (mc.commandName.equals("PRIVMSG"))
								receiver.getConnection().commandSender.privmsg(userInfo, mc.message);
							else if (mc.commandName.equals("NOTICE"))
								receiver.getConnection().commandSender.notice(userInfo, mc.message);
						}
						catch (IOException e)
						{
							//Modtageren mistede/lukkede forbindelsen til serveren
							commandSender.sendNoSuchNickErr(mc.msgtarget);
						}
					}
					
					continue;
				}
				
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		
		if (isAuth)
			Sjircd.userDisconnected(userInfo, quitMsg);
		else
			Sjircd.log("Unauthorized client "+connectionSocket.getInetAddress().getHostAddress()+" ("+connectionSocket.getInetAddress().getCanonicalHostName()+") disconnected");
	}

	public ServerCommands getCommandSender() {
		return commandSender;
	}
}
