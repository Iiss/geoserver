package sfs2x.extension.geo.src;

import java.sql.SQLException;

import com.smartfoxserver.v2.annotations.MultiHandler;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import com.smartfoxserver.v2.extensions.SFSExtension;


@MultiHandler
public class SessionRequestHandler extends BaseClientRequestHandler {

	
	@Override
	public void handleClientRequest(User sender, ISFSObject params)
	{
		IGeoExtension ext = (IGeoExtension) getParentExtension();
		String command = params.getUtfString(SFSExtension.MULTIHANDLER_REQUEST_ID);

		try 
		{
			if(command.equalsIgnoreCase("next"))
			{
				((IGeoExtension)ext).nextSession();
			}
			else if(command.equalsIgnoreCase("scanRequest"))
			{
				((IGeoExtension)ext).addScanRequest(params.getInt("x"), params.getInt("y"), params.getInt("layer_id"));
				_reportDone(command, sender);
			}
			else if(command.equalsIgnoreCase("scan"))
			{
				((IGeoExtension)ext).scan(params.getInt("x"), params.getInt("y"), params.getInt("layer_id"));
				_reportDone(command, sender);
			}
			else if(command.equalsIgnoreCase("deliverProbe"))
			{
				((IGeoExtension)ext).deliverProbe(params.getInt("x"), params.getInt("y"),  params.getInt("layer_id"));
				_reportDone(command, sender);
			}
			else if(command.equalsIgnoreCase("assignProbe"))
			{
				((IGeoExtension)ext).assignProbe(params.getInt("probe_id"),params.getInt("kern_id"));
				_reportDone(command, sender);
			}
			else if(command.equalsIgnoreCase("toggleLayer"))
			{
				((IGeoExtension)ext).toggleLayer(params.getInt("layer_id"));
				_reportDone(command, sender);
			}
			else if(command.equalsIgnoreCase("ping"))
			{
				_reportDone(command, sender);
			}
		}
		catch (SQLException e)
		{
			_reportError(command,sender,e);
		}

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
