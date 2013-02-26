package irc.command;

import java.lang.reflect.*;
import java.util.*;
import irc.*;

public class IrcCommand
{
	private static HashMap<String, Class> commands = new HashMap<String, Class>();
	
	public String fullCommand, commandName, nickOrServer = "", user = "", host = "", paramsStr;
	public String[] parameters;
	private int errorNum;
	private String errorParams, errorMessage;

	protected void clearError()
	{
		errorNum = 0;
		errorParams = null;
		errorMessage = null;
	}
	
	protected void setError(int num, String params, String message)
	{
		if (num == 0)
			clearError();
		else
		{
			errorNum = num;
			errorParams = params;
			errorMessage = message;
		}
	}
	
	public boolean hasError()
	{
		return (errorNum != 0);
	}
	
	public boolean isUnknown()
	{
		return (errorNum == IrcNumerics.ERR_UNKNOWNCOMMAND);
	}
	
	public int getErrorNum()
	{
		return errorNum;
	}
	
	public String getErrorParams() {
		return errorParams;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	//Skal kaldes i en static blok i alle underklasser der tilhoerer en eller flere bestemte kommandoer
	public static void AddCommand(Class cmd, String name)
	{
		commands.put(name, cmd);
	}
	
	static {
		AddCommand(PingCommand.class, "PING");
		AddCommand(NickCommand.class, "NICK");
		AddCommand(UserCommand.class, "USER");
		AddCommand(QuitCommand.class, "QUIT");
		AddCommand(UserhostCommand.class, "USERHOST");
		AddCommand(MsgCommand.class, "NOTICE");
		AddCommand(MsgCommand.class, "PRIVMSG");
	}
	
	public String toString()
	{
		return fullCommand;
	}
	
	protected IrcCommand() //default konstruktoren skal ikke kunne kaldes udefra
	{
	}
	
	//Alle underklasser skal implementere denne
	public IrcCommand(String pFullCommand, String command, String[] prefix, String params)
	{
		fullCommand = pFullCommand;
		commandName = command;
		if ((prefix != null) && (prefix.length == 3))
		{
			host = prefix[0];
			nickOrServer = prefix[1];
			user = prefix[2];
		}
		paramsStr = params;
		parameters = parseParams(params);
	}
	
	public static IrcCommand parse(String cmdstr) throws Throwable
	{
	/*
    message    =  [ ":" prefix SPACE ] command [ params ] crlf
    prefix     =  servername / ( nickname [ [ "!" user ] "@" host ] )
    command    =  1*letter / 3digit
    params     =  *14( SPACE middle ) [ SPACE ":" trailing ]
               =/ 14( SPACE middle ) [ SPACE [ ":" ] trailing ]

    nospcrlfcl =  %x01-09 / %x0B-0C / %x0E-1F / %x21-39 / %x3B-FF
                    ; any octet except NUL, CR, LF, " " and ":"
    middle     =  nospcrlfcl *( ":" / nospcrlfcl )
    trailing   =  *( ":" / " " / nospcrlfcl )

    SPACE      =  %x20        ; space character
    crlf       =  %x0D %x0A   ; "carriage return" "linefeed"
	*/
		int i;
		String[] prefixInf = null;
		String fullCommand, commandName, params;
		
		if ((cmdstr == null) || (cmdstr.length() == 0))
			return null;
		
		fullCommand = cmdstr;
		
		if (cmdstr.charAt(0) == ':') //der er et praefiks
		{
			i = cmdstr.indexOf(' ');
			
			if ((i == -1) || (cmdstr.length() == i+1)) //burde vaere falsk - kommandonavnet skal staa efter praefikset
				return new IrcCommand(cmdstr, "", null, "");
				
			prefixInf = parsePrefix(cmdstr.substring(1, i));
			
			cmdstr = cmdstr.substring(i+1);
		}
		
		//finder kommandonavnet
		i = cmdstr.indexOf(' ');
		if ((i == -1) || (cmdstr.length() == i+1)) //ingen parametre - ingenting der skal parses specifikt
		{
			commandName = cmdstr;
			params = "";
		}
		else
		{
			commandName = cmdstr.substring(0, i);
			params = cmdstr.substring(i+1);
		}
		
		//laver en instans af den rigtige klasse
		Class commandClass = commands.get(commandName.toUpperCase());
		if (commandClass == null) //der er ikke registreret nogen klasse (ukendt kommando)
		{
			IrcCommand instance = new IrcCommand(cmdstr, commandName, prefixInf, params);
			instance.errorNum = IrcNumerics.ERR_UNKNOWNCOMMAND;
			instance.errorParams = commandName;
			instance.errorMessage = "Unknown command";
			return instance;
		}
		
		//finder konstruktoren
		//String pFullCommand, String command, String[] prefix, String params
		try
		{
			Constructor constructor = commandClass.getConstructor(String.class, String.class, String[].class, String.class);
			return (IrcCommand)constructor.newInstance(fullCommand, commandName, prefixInf, params);
		}
		catch (InvocationTargetException e)
		{ 
			throw e.getCause();
		}
	}

	/*Parser prefix. Output: 
	0: host
	1: nickOrServer
	2: user
	*/
	public static String[] parsePrefix(String prefix)
	{
		//prefix     =  servername / ( nickname [ [ "!" user ] "@" host ] )
		int i;
		String nickOrServer, user, host;
		
		i = prefix.indexOf('@');
		if (i == -1) // intet @ saa er det kun nick eller server
			return new String[]{"", prefix, ""};
		
		if (i == prefix.length() -1) //malformed...
			host = "";
		else
			host = prefix.substring(i+1);
		nickOrServer = prefix.substring(0, i); //nickOrServer indeholder enten nickname eller nickname!user
		i = nickOrServer.indexOf('!');
		if (i == -1)
			return new String[]{host, nickOrServer, ""};
		user = nickOrServer.substring(i+1);
		nickOrServer = nickOrServer.substring(0, i);
		
		return new String[]{host, nickOrServer, user};
	}
	
	public static String[] parseParams(String params)
	{
		int i, nextSpace;
		String S;
		
		if ((params == null) || (params.length() == 0) || (params.charAt(0) == ' '))
			return new String[]{}; //ingen parametre
		
		ArrayList<String> list = new ArrayList<String>();
		i = 0;
		
		while (i != -1)
		{
			if (params.charAt(i) == ':')
			{
				if (i == params.length() -1) //tjekker om der kommer noget efter kolonet
					S = "";
				else
					S = params.substring(i+1);
				i = -1;
			}
			else
			{
				nextSpace = params.indexOf(' ', i);
				if ((nextSpace == -1) ||(nextSpace == params.length() -1))
				{
					S = params.substring(i);
					i = -1;
				}
				else
				{
					S = params.substring(i, nextSpace);
					i = nextSpace+1;
				}
			}
			list.add(S);
		}
		
		return list.toArray(new String[]{});
	}
	
	static public String removeQuotes(String S)
	{
		if ((S.charAt(0) == '"') && (S.charAt(S.length() -1) == '"'))
			return S.substring(1, S.length() -2);
		else
			return S;
	}
}