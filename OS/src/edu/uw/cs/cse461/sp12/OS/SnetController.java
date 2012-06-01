package edu.uw.cs.cse461.sp12.OS;

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
	
	public void fetchUpdates(String name) {
		
	}
	
	public byte[] fetchPhoto(int photoHash) {
		return null;
	}
	

}
