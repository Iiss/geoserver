package sfs2x.extension.geo.src;

import java.sql.SQLException;

public interface IGeoExtension
{
	void initRoomVars();
	void nextSession() throws SQLException;
	void addScanRequest(int x, int y, int layerId) throws SQLException;
	void scan(int x, int y, int layerId) throws SQLException;
	void deliverProbe(int x, int y, int layerId) throws SQLException;
	void assignProbe(int probeId, int kernId) throws SQLException;
}
