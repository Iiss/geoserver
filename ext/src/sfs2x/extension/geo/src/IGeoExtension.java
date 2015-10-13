package sfs2x.extension.geo.src;

import java.sql.SQLException;

public interface IGeoExtension
{
	void initRoomVars();
	void nextSession() throws SQLException;
	void addScanRequest(int x, int y, int layerId) throws SQLException;
}
