package sfs2x.extension.geo.src;

import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import com.smartfoxserver.v2.extensions.SFSExtension;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;

import sfs2x.extension.geo.src.ServerReadyHandler;
import sfs2x.extension.geo.src.SessionRequestHandler;

public class GeoExtension extends SFSExtension implements IGeoExtension{
	
	private static String MAP_INFO_DATA_VAR = "mapInfo";
	private static String SCAN_DATA_VAR = "scanData";
	private static String SCAN_REQUEST_DATA_VAR = "scanRequests";
	private static String LAYERS_DATA_VAR = "layers";
	private static String SCAN_DATA_TABLE = "geo.scan_data";
	private static String SCAN_REQUEST_DATA_TABLE = "geo.scan_requests";
	private static String LAYERS_DATA_TABLE = "geo.layers";

	private IDBManager db;
	
	/** {@inheritDoc} */
	public void init()
	{
		trace("*** Init geo extension ***");
		trace("Room: "+getParentRoom().getName());
		
		addEventHandler(SFSEventType.SERVER_READY, new ServerReadyHandler());
		addRequestHandler("game", SessionRequestHandler.class);
	}
	
	
	public void initRoomVars() 
	{
		trace("*** Init game session ***");
		
		try 
		{
			db = getParentZone().getDBManager();
			initSession();
		} 
		catch (SQLException e)
		{
			trace(ExtensionLogLevel.WARN, "SQL Failed: " + e.toString());
		}
		
		trace("*** Session ready ***");
	}
	
	
	public void nextSession() throws SQLException
	{
		try
		{
			int mapId;
			ISFSObject map;
			
	    	String sql = "SELECT id FROM geo.maps ORDER BY RAND() LIMIT 1";
	    	ISFSArray res= db.executeQuery(sql, null);
	    	
	    	map = res.getSFSObject(0);
	    	mapId = map.getInt("id");
	    	sql = "INSERT INTO `geo`.`sessions` (`map_id`) VALUES (?)";
	    	Object[] params = new Object[]{mapId};
	    	db.executeInsert(sql, params);
	    	
	    	loadSession(map);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	

	public void addScanRequest(int x, int y, int layerId) throws SQLException
	{
		try
		{
			String sql = "INSERT INTO geo.scan_requests (cell_x, cell_y, layer_id) VALUES (?,?,?)";
			Object[] params = new Object[]{x,y,layerId};
	    	db.executeInsert(sql, params);
	    	
	    	RoomVariable reqStack = getParentRoom().getVariable(SCAN_REQUEST_DATA_VAR);
	    	
	    	if(reqStack != null)
	    	{
	    		ISFSObject reqData = new SFSObject();
	    		reqData.putInt("cell_x", x);
	    		reqData.putInt("cell_y", y);
	    		reqData.putInt("layer_id", layerId);
	    		reqStack.getSFSArrayValue().addSFSObject(reqData);
	    		
	    		List<RoomVariable> varsArr = new ArrayList<RoomVariable>();
	    		varsArr.add(reqStack);
	    		getApi().setRoomVariables(reqStack.getOwner(), getParentRoom(), varsArr);
	    		
	    	}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	private void initSession() throws SQLException
	{
		int mapId;
		String sql = "SELECT map_id FROM geo.sessions LIMIT 1";
        ISFSArray activeSessions = db.executeQuery(sql, null);
        
		if(activeSessions != null  && activeSessions.size()>0)
        {
			mapId = activeSessions.getSFSObject(0).getInt("map_id");
			restoreSession(mapId);
        }
		else //if no active session add random to active_sessoins table
		{
			nextSession();
		}
	}
	
	
	//
	// Load new Session
	//
	private void loadSession(ISFSObject mapObj) throws SQLException
	{
		trace("Session id: " + mapObj.getInt("id"));
		List<RoomVariable> varsArr = new ArrayList<RoomVariable>();
	
		try
		{
			//reset cache tables
			varsArr.add(dropTable(SCAN_DATA_TABLE,SCAN_DATA_VAR));
			varsArr.add(dropTable(SCAN_REQUEST_DATA_TABLE,SCAN_REQUEST_DATA_VAR));
			
			//restore map
			varsArr.add(setupLayers());
			varsArr.add(new SFSRoomVariable(MAP_INFO_DATA_VAR, mapObj));
			
			//push them all
			getApi().setRoomVariables(varsArr.get(0).getOwner(), getParentRoom(), varsArr);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	
	private void restoreSession(int sessionId) throws SQLException
	{
		trace("restore session id: " + sessionId);
		List<RoomVariable> varsArr = new ArrayList<RoomVariable>();
		
		try
		{
			ISFSObject map;
			String sql = "SELECT * FROM geo.maps WHERE id=?";
			ISFSArray res  = db.executeQuery(sql, new Object[]{sessionId});
			map = res.getSFSObject(0);
			varsArr.add(new SFSRoomVariable(MAP_INFO_DATA_VAR, map));
			
			//restore cache tables
			varsArr.add(restoreTable(SCAN_DATA_TABLE, SCAN_DATA_VAR));
			varsArr.add(restoreTable(SCAN_REQUEST_DATA_TABLE, SCAN_REQUEST_DATA_VAR));
			
			//restore map
			varsArr.add(setupLayers());
			
			//push them all
			getApi().setRoomVariables(varsArr.get(0).getOwner(), getParentRoom(), varsArr);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw e;
		}
		
	}
	
	
	private void dropTable(String tableName) throws SQLException
	{
		try
		{
			String sql = "DELETE FROM "+tableName;
			db.executeUpdate(sql, null);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	
	private SFSRoomVariable dropTable(String tableName, String varName) throws SQLException
	{
		try
		{
			dropTable(tableName);
			return new SFSRoomVariable(varName, new SFSArray());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	
	private SFSRoomVariable restoreTable(String tableName, String varName) throws SQLException
	{
		try
		{
			String sql ="SELECT * FROM "+tableName;
			ISFSArray res = db.executeQuery(sql,null);
			return new SFSRoomVariable(varName, res);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	
	private SFSRoomVariable setupLayers() throws SQLException
	{
		try
		{
			return restoreTable(LAYERS_DATA_TABLE,LAYERS_DATA_VAR);
		}
		catch (SQLException e) 
		{
			trace(ExtensionLogLevel.WARN, "SQL Failed: " + e.toString());
		}
		
		return null;
	}
	/** {@inheritDoc} */
	@Override
	public void destroy()
	{
		super.destroy();
	}
}
