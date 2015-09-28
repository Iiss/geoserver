package sfs2x.extension.geo.src;

import com.smartfoxserver.v2.extensions.SFSExtension;
import com.smartfoxserver.v2.core.SFSEventType;
import sfs2x.extension.geo.src.ServerReadyHandler;

public class GeoExtension extends SFSExtension{
	/** {@inheritDoc} */
	public void init()
	{
		trace("=== Init geo extension ===");
		trace("Room: "+getParentRoom().getName());
		
		addEventHandler(SFSEventType.SERVER_READY, new ServerReadyHandler());	
	}
	
	/** {@inheritDoc} */
	@Override
	public void destroy()
	{
		super.destroy();
	}
}
