package sfs2x.extension.geo.src;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class ServerReadyHandler extends BaseServerEventHandler 
{
	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException
	{
		((IGeoExtension) getParentExtension()).initRoomVars();
	}	
}
