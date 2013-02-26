package irc;

import sjircd.*;

public class UserInfo
{
	private String nick,ident,host,realHost, realName;
	private boolean isOper, isAway;
	Connection connection;
	
	public UserInfo(String nick, String ident, String host, String realHost, String realName,
			Connection connection)
	{
		this.nick = nick;
		this.ident = ident;
		this.host = host;
		this.realHost = realHost;
		this.realName = realName;
		this.connection = connection;
	}

	public String getIdent() {
		return ident;
	}

	public boolean isOper() {
		return isOper;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getRealHost() {
		return realHost;
	}

	public void setRealHost(String realHost) {
		this.realHost = realHost;
	}

	public Connection getConnection() {
		return connection;
	}

	public String getFullPrefix()
	{
		return nick+"!"+ident+"@"+host;
	}
	
	@Override
	public String toString()
	{
		return getFullPrefix()+" ["+realHost+"] ("+realName+")";
	}

	public boolean isAway() {
		return isAway;
	}
}
