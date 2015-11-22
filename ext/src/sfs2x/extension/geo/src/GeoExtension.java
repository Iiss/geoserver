package sfs2x.extension.geo.src;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class GeoExtension extends SFSExtension implements IGeoExtension{
	
	private static String SESSIONS_DATA_TABLE = "geo.sessions";
	private static String MAP_INFO_DATA_VAR = "mapInfo";
	private static String SCAN_DATA_VAR = "scanData";
	private static String SCAN_REQUEST_DATA_VAR = "scanRequests";
	private static String LAYERS_DATA_VAR = "layers";
	private static String SCAN_DATA_TABLE = "geo.scan_data";
	private static String SCAN_REQUEST_DATA_TABLE = "geo.scan_requests";
	private static String LAYERS_DATA_TABLE = "geo.layers";
	private static String MAP_DATA_TABLE = "geo.map_data";
	private static String MAPS_TABLE = "geo.maps";
	private static String STORAGE_DATA_TABLE = "geo.storage";
	private static String STORAGE_DATA_VAR = "storage";
	private static String LOCK_DATA_VAR = "locked";
	private static String HAS_COMMON_MAPS_VAR = "hasCommonMaps";
	private static String EPIC_MAPS_VAR = "epicMaps";
	

	private IDBManager db;
	private HashMap<String,Integer> mapHash;
	
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
			lockSession(true);
			defineMapListOptions();
			initSession();
			lockSession(false);
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
			lockSession(true);
			ISFSObject map;
	    	String sql = "SELECT * FROM geo.maps ORDER BY RAND() LIMIT 1";
	    	ISFSArray res= db.executeQuery(sql, null);
	    	
	    	map = res.getSFSObject(0);
	    	
	    	loadSession(map);
	    	lockSession(false);
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
			String sql = "INSERT INTO " + SCAN_REQUEST_DATA_TABLE + " (cell_x, cell_y, layer_id) VALUES (?,?,?)";
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
	
	public void scan(int x, int y, int layerId) throws SQLException
	{
		try
		{
			RoomVariable mapVar = getParentRoom().getVariable(MAP_INFO_DATA_VAR);
			double value = mapHash.get(getMapHashKey(x,y,layerId));
			
			String sql ="DELETE FROM " + SCAN_REQUEST_DATA_TABLE + " WHERE (cell_x=? AND cell_y=? AND layer_id=?)";
			Object[] params = new Object[]{x, y, layerId};
			db.executeUpdate(sql, params);
			
			sql = "INSERT INTO " + SCAN_DATA_TABLE + " (cell_x,cell_y,layer_id,value) VALUES(?,?,?,?)";
			params = new Object[]{ x ,y , layerId, value};
			db.executeInsert(sql, params);
			
			List<RoomVariable> varsArr = new ArrayList<RoomVariable>();
			varsArr.add(restoreTable(SCAN_DATA_TABLE, SCAN_DATA_VAR));
			varsArr.add(restoreTable(SCAN_REQUEST_DATA_TABLE, SCAN_REQUEST_DATA_VAR));
			getApi().setRoomVariables(mapVar.getOwner(), getParentRoom(), varsArr);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	public void deliverProbe(int x, int y, int layerId) throws SQLException
	{
		try
		{
			int rockKey = mapHash.get(getMapHashKey(x, y, layerId));
			String sql = "INSERT INTO " + STORAGE_DATA_TABLE + " (cell_x,cell_y,rock_key) VALUES(?,?,?)";
			Object[] params = new Object[]{x, y, rockKey};
			db.executeInsert(sql, params);
			
			List<RoomVariable> varsArr = new ArrayList<RoomVariable>();
			RoomVariable values = restoreTable(STORAGE_DATA_TABLE, STORAGE_DATA_VAR);
			varsArr.add(values);
			getApi().setRoomVariables(values.getOwner(), getParentRoom(), varsArr);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	public void assignProbe(int probeId, int kernId) throws SQLException
	{
		try
		{
			String sql = "UPDATE " + STORAGE_DATA_TABLE + " SET kern_id=? WHERE id=?";
			Object[] params = new Object[]{kernId, probeId};
			db.executeUpdate(sql, params);
			
			List<RoomVariable> varsArr = new ArrayList<RoomVariable>();
			RoomVariable values = restoreTable(STORAGE_DATA_TABLE, STORAGE_DATA_VAR);
			varsArr.add(values);
			getApi().setRoomVariables(values.getOwner(), getParentRoom(), varsArr);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	public void toggleLayer(int layerId)//ADMIN method
	{
		boolean changed = false;
		ISFSArray layers = getParentRoom().getVariable(LAYERS_DATA_VAR).getSFSArrayValue();
		ISFSObject l;
		for(int i=0;i<layers.size();i++)
		{
			l=layers.getSFSObject(i);
			if(l.getInt("id")==layerId)
			{
				l.putBool("enabled", !l.getBool("enabled"));
				changed = true;
			}
		}
		
		if (changed)
		{
			setVar(LAYERS_DATA_VAR,layers);
		}
	}
	
	//
	// Private methods
	//
	
	private void lockSession(boolean locked)
	{
		setVar(LOCK_DATA_VAR,locked);
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
	
	private void loadSession(ISFSObject mapObj) throws SQLException
	{
		int mapId = mapObj.getInt("id");
		List<RoomVariable> varsArr = new ArrayList<RoomVariable>();
	
		trace("Session id: " + mapId);
		
		try
		{
			dropTable(SESSIONS_DATA_TABLE);
	    	String sql = "INSERT INTO `geo`.`sessions` (`map_id`) VALUES (?)";
	    	Object[] params = new Object[]{mapId};
	    	db.executeInsert(sql, params);
			
			//reset cache tables
			varsArr.add(dropTable(SCAN_DATA_TABLE,SCAN_DATA_VAR));
			varsArr.add(dropTable(SCAN_REQUEST_DATA_TABLE,SCAN_REQUEST_DATA_VAR));
			varsArr.add(dropTable(STORAGE_DATA_TABLE,STORAGE_DATA_VAR));
			
			//restore map
			varsArr.add(setupLayers());
			varsArr.add(new SFSRoomVariable(MAP_INFO_DATA_VAR, mapObj));
			hashMap(mapObj.getInt("id"));
			
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
			varsArr.add(restoreTable(STORAGE_DATA_TABLE,STORAGE_DATA_VAR));
			
			//restore map
			varsArr.add(setupLayers());
			hashMap(sessionId);
			//push them all
			getApi().setRoomVariables(varsArr.get(0).getOwner(), getParentRoom(), varsArr);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw e;
		}
		
	}
	
	private void hashMap(int mapId) throws SQLException
	{
		mapHash = new HashMap<String, Integer>();
		
		try
		{
			String sql = "SELECT * FROM " + MAP_DATA_TABLE+" WHERE (map_id=?)";
			ISFSArray res  = db.executeQuery(sql, new Object[]{mapId});
			
			int cx,cy,lid,value;
			ISFSObject item;
			String key;
			
			for(int i=0; i<res.size(); i++)
			{
				item = res.getSFSObject(i);
				cx = item.getInt("cell_x");
				cy = item.getInt("cell_y");
				lid = item.getInt("layer_id");
				value = item.getInt("value_id");
				key = getMapHashKey(cx,cy,lid);
				mapHash.put(key, value);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	private String getMapHashKey(int cell_x, int cell_y, int layer_id)
	{
		return "x"+cell_x+"y"+cell_y+"l"+layer_id;
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
			SFSRoomVariable layers = restoreTable(LAYERS_DATA_TABLE,LAYERS_DATA_VAR);
			ISFSArray larr = layers.getSFSArrayValue();
			for(int i=0;i<larr.size();i++)
			{
				larr.getSFSObject(i).putBool("enabled", true);
			}
			
			return layers;
		}
		catch (SQLException e) 
		{
			trace(ExtensionLogLevel.WARN, "SQL Failed: " + e.toString());
		}
		
		return null;
	}
	
	private void setVar(String varName, Object value)
	{
		List<RoomVariable> varsArr = new ArrayList<RoomVariable>();
		RoomVariable roomVar = new SFSRoomVariable(varName,value,true,false,false);
		varsArr.add(roomVar);
		getApi().setRoomVariables(roomVar.getOwner(), getParentRoom(), varsArr);
	}
	
	private void defineMapListOptions() throws SQLException
	{
		try
		{
			String sql = "SELECT COUNT(id) FROM "+ MAPS_TABLE +" WHERE NOT is_epic";
			ISFSArray res = db.executeQuery(sql, null);
			RoomVariable commonCnt = new SFSRoomVariable(HAS_COMMON_MAPS_VAR, res.size()>0);
			
			sql = "SELECT * FROM "+ MAPS_TABLE +" WHERE is_epic";
			res = db.executeQuery(sql, null);
			RoomVariable epicList = new SFSRoomVariable(EPIC_MAPS_VAR, res);
			
			List<RoomVariable> varsArr = new ArrayList<RoomVariable>();
			varsArr.add(commonCnt);
			varsArr.add(epicList);
			getApi().setRoomVariables(commonCnt.getOwner(), getParentRoom(), varsArr);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	/** {@inheritDoc} */
	@Override
	public void destroy()
	{
		super.destroy();
	}
}
