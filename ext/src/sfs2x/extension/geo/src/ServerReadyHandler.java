package sfs2x.extension.geo.src;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import com.smartfoxserver.v2.extensions.ISFSExtension;

public class ServerReadyHandler extends BaseServerEventHandler {

	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException {
		// TODO Auto-generated method stub
		initRoomVars();
	}

	private void initRoomVars() {
		ISFSExtension ext = getParentExtension();
		List<RoomVariable> varsArr = new ArrayList<RoomVariable>();
		
		SFSRoomVariable testVar = new SFSRoomVariable("test","I'm very happy to be here!");
		varsArr.add(testVar);

		trace("*** get db vars ***");
		
		Connection connection = null;
		PreparedStatement stmt = null;
		
		try
        {
            String sql = "SELECT * FROM test.messages";
            connection = ext.getParentZone().getDBManager().getConnection();
            stmt = connection.prepareStatement(sql);
            
            ResultSet resultSet = stmt.executeQuery();
            SFSArray msgArr= SFSArray.newFromResultSet(resultSet);
            SFSRoomVariable msgVar = new SFSRoomVariable("messages",msgArr);
            varsArr.add(msgVar);
        }
        
        // Not mandatory
        catch (SQLException se)
        {
           trace(se.getMessage());
        }
        // Mandatory! Close connection before leaving this method
        finally
        {
        	try {
        		if (stmt != null) stmt.close();
				if (connection != null) connection.close();
			} 
        	catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		
		getApi().setRoomVariables(testVar.getOwner(), ext.getParentRoom(), varsArr);
		trace("Let's play...");
	}
}
