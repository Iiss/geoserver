package sfs2x.extension.geo.src;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import com.smartfoxserver.v2.extensions.ISFSExtension;

public class ServerReadyHandler extends BaseServerEventHandler {

	private IDBManager db;
	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException {
		db = getParentExtension().getParentZone().getDBManager();
		initRoomVars();
	}

	private void initRoomVars() {
		trace("*** Init game session ***");
		db = getParentExtension().getParentZone().getDBManager();
		
		try 
		{
			restoreSession();
		} 
		catch (SQLException e)
		{
			trace(ExtensionLogLevel.WARN, "SQL Failed: " + e.toString());
		}
		
		trace("*** Session ready ***");
	}
	//
	// Load new Session
	//
	private void loadSession(int sessionId) throws SQLException
	{
		trace("Session id: " + sessionId);
		List<RoomVariable> varsArr = new ArrayList<RoomVariable>();
		ISFSExtension ext = getParentExtension();
		varsArr.add(new SFSRoomVariable("sessionId",sessionId));
		
		try
		{
			//reset scan data
			dropTable("geo.scan_data");
			varsArr.add(new SFSRoomVariable("scanData", new SFSArray()));
			
			//reset scan requests data
			dropTable("geo.scan_requests");
			varsArr.add(new SFSRoomVariable("scanRequests", new SFSArray()));
			
			//restore map
			SFSRoomVariable mapVar = new SFSRoomVariable("mapData", generateMap(sessionId));
			mapVar.setPrivate(true);
			mapVar.setHidden(false);
			varsArr.add(mapVar);
			
			//push them all
			getApi().setRoomVariables(mapVar.getOwner(), ext.getParentRoom(), varsArr);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	private void dropTable(String tableName) throws SQLException
	{
		String sql = "DELETE FROM "+tableName;
		db.executeUpdate(sql, new Object[] {});
	}
	
	private void restoreSession() throws SQLException
	{
		int sessionId;
		String sql = "SELECT session_id FROM geo.sessions LIMIT 1";
        ISFSArray activeSessions = db.executeQuery(sql, new Object[] {});
        
		if(activeSessions != null  && activeSessions.size()>0)
        {
			sessionId = activeSessions.getInt(0);
			loadSession(sessionId);
        }
		else
		{
			nextSession();
		}
	}
	
	private void nextSession() throws SQLException
	{
		int sessonId;
		//if no active session add random to active_sessoins table
    	String sql = "SELECT id FROM geo.sessions ORDER BY RAND() LIMIT 1";
    	sessonId = db.executeQuery(sql, new Object[] {}).getInt(0);
    	
    	sql = "INSERT INTO `geo`.`sessions` (`map_id`) VALUES (?)";
    	Object[] params = new Object[]{sessonId};
    	db.executeInsert(sql, params);
    	
    	loadSession(sessonId);
	}
	
	
	
	private SFSObject generateMap(int mapId)
	{
		int w = 31;
		int h = 17;

		String sql = "SELECT * FROM geo.map_data WHERE(map_id=?)";
		
		SFSObject mapData = new SFSObject();
		
		try 
		{
			ISFSArray cells = db.executeQuery(sql, new Object[] {mapId});
			for(int i=0; i<cells.size(); i++)
			{
				 ISFSObject cell = cells.getSFSObject(i);
				 String key = cell.getInt("layer_id").toString();
				 
				 ArrayList<Double> cellArr;
				 if(!mapData.containsKey(key))
				 {
					 cellArr = new ArrayList<Double>();
					 for(int j = 0; j < w*h; j++)
					 {
						 cellArr.add((double) 0);
					 }
					 
					 mapData.putDoubleArray(key, cellArr);
				 }
				 else
				 {
					 cellArr = (ArrayList<Double>) mapData.getDoubleArray(key);
				 }
				 
				 cellArr.add(cell.getInt("cell_x")*w+cell.getInt("cell_y"), cell.getDouble("value"));	
			}
		}
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			trace(ExtensionLogLevel.WARN, "SQL Failed: " + e.toString());
		}
		
		return mapData;
	}
	
}
