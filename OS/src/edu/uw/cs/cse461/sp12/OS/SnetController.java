package edu.uw.cs.cse461.sp12.OS;

import org.json.JSONObject;



public class SnetController extends RPCCallable {
	private SNetDB461 db;
	
	public SnetController() {
		//TODO Init DB
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
	
	
	

}
