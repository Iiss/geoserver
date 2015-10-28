package sfs2x.extension.geo.src;

import java.util.HashMap;
import java.util.Map;

import com.smartfoxserver.v2.entities.data.ISFSArray;

public class CellBlankModel
{
	public Boolean scanRequest = false;
	public Map<Integer,Double> layers;
	
	public CellBlankModel(ISFSArray layersArr)
	{
		layers = new HashMap<Integer,Double>();
		int i, id;
		for(i=0; i<layersArr.size(); i++)
		{
			id = layersArr.getSFSObject(i).getInt("id");
			layers.put(id,null);
		}
	}
}
