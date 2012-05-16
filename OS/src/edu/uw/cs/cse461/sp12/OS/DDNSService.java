package edu.uw.cs.cse461.sp12.OS;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.cs.cse461.sp12.OS.RPCCallable.RPCCallableMethod;

public class DDNSService extends RPCCallable {

	private RPCCallableMethod<DDNSService> register;
	private RPCCallableMethod<DDNSService> unregister;
	private RPCCallableMethod<DDNSService> resolve;
	
	
	
	private static final String PASSWORD = "champ";
	
	public DDNSService() throws Exception {
		//Read in config file for tree
		//Fill out storage tree
		
		register = new RPCCallableMethod<DDNSService>(this, "_register");
		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "register", register);
		unregister = new RPCCallableMethod<DDNSService>(this, "_unregister");
		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "unregister", unregister);
		resolve = new RPCCallableMethod<DDNSService>(this, "_resolve");
		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "resolve", resolve);
		
		RPCCallerSocket register = new RPCCallerSocket(OS.config().getProperty("ddns.rootserver"), 
				OS.config().getProperty("ddns.rootserver"), OS.config().getProperty("ddns.rootport"));	
		JSONObject request = new JSONObject();
		
		register.invoke("ddns", "register", request);
		
	}
	
	@Override
	public String servicename() {
		return "ddns";
	}

	@Override
	public void shutdown() {
		// Nothing to do here
	}
	
	public JSONObject _register(JSONObject args) throws JSONException, IOException {
//		{port:34562, name:"jz.cse461.","password":"jzpassword","ip":"192.168.0.77"}
		//Store this information into memory
		//Start keep alive timer
		//Send response message
		JSONObject result = new JSONObject();
		
		return result;
//		{node:[node representation described next], lifetime:600, resulttype:"registerresult", "done":true}
	}
	
	public JSONObject _unregister(JSONObject args) throws JSONException, IOException {
//		unregister() works exactly like register(), except for two things. First, it marks 
//		the node as having no current address rather than updating its address. Second, 
//		if done is true, no node is returned.
		//Remove info from storage
		JSONObject result = new JSONObject();
		return result;
	}
	
	public JSONObject _resolve(JSONObject args) throws JSONException, IOException {
		//Look up name in storage
		//Send result
		JSONObject result = new JSONObject();
		return result;
	}

}
