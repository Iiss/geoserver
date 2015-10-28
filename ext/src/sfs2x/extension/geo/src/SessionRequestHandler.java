package sfs2x.extension.geo.src;

import java.sql.SQLException;

import com.smartfoxserver.v2.annotations.MultiHandler;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import com.smartfoxserver.v2.extensions.ISFSExtension;
import com.smartfoxserver.v2.extensions.SFSExtension;


@MultiHandler
public class SessionRequestHandler extends BaseClientRequestHandler {

	@Override
	public void handleClientRequest(User sender, ISFSObject params)
	{
		ISFSExtension ext = getParentExtension();
		String command = params.getUtfString(SFSExtension.MULTIHANDLER_REQUEST_ID);

		if(command.equalsIgnoreCase("next"))
		{
			try 
			{
				((IGeoExtension)ext).nextSession();
			} 
			catch (SQLException e)
			{
				trace(ExtensionLogLevel.WARN, e.toString());
			}
		}
		else if(command.equalsIgnoreCase("scanRequest"))
		{
			try 
			{
				((IGeoExtension)ext).addScanRequest(params.getInt("x"), params.getInt("y"), params.getInt("layer_id"));
				_reportDone(command, sender);
			} 
			catch (SQLException e)
			{
				_reportError(command,sender,e);
			}
		}
		else if(command.equalsIgnoreCase("reportResult")){}
		else if(command.equalsIgnoreCase("addToFavorites")){}
		else if(command.equalsIgnoreCase("mine")){}

	}
	
	private void _reportDone(String cmdName,User sender)
	{
		ISFSObject obj = new SFSObject();
		obj.putInt("result", 1);
		send(cmdName,obj,sender);
	}
	
	private void _reportError(String cmdName,User sender, SQLException e)
	{
		trace(ExtensionLogLevel.WARN, e.toString());
		ISFSObject obj = new SFSObject();
		obj.putInt("result", 0);
		send(cmdName,obj,sender);
	}

}