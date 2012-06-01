package edu.uw.cs.cse461.sp12.OS;

import java.io.IOException;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.cs.cse461.sp12.DB461.DB461.DB461Exception;



public class SnetController extends RPCCallable {
	
	private SNetDB461 db;
	
	public SnetController() {
		try {
			db = new SNetDB461();
		} catch (DB461Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public String servicename() {
		return "snet";
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
	public JSONObject _fetchUpdates(JSONObject args){
		return null;
	}
	public JSONObject _fetchPhoto(JSONObject args){
		return null;
	}
	
	public JSONArray fetchUpdates(String name) {
		try {
			DDNSRRecord rr = ((DDNSResolverService)OS.getService("ddnsresolver")).resolve(name);
			RPCCallerSocket sock = new RPCCallerSocket(rr.host, rr.host, rr.port);
			JSONObject request = new JSONObject();
			JSONObject response = sock.invoke("snet", "fetchUpdates", request);
			JSONObject commUpdates = response.getJSONObject("communityupdates");
			for(Iterator<String> it = commUpdates.keys(); it.hasNext();) {
				JSONObject memberUpdate = commUpdates.getJSONObject(it.next());
				
			}
			return response.getJSONArray("photoupdates");
		} catch (DDNSException e) {
			// TODO failed ddns for whatever reason
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] fetchPhoto(String name, int photoHash) {
		return null;
	}
	

}
