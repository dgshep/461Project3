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
		register = new RPCCallableMethod<DDNSService>(this, "_register");
		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "register", register);
		unregister = new RPCCallableMethod<DDNSService>(this, "_unregister");
		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "unregister", unregister);
		resolve = new RPCCallableMethod<DDNSService>(this, "_resolve");
		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "resolve", resolve);
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
		JSONObject result = new JSONObject();
		
		return result;
	}
	
	public JSONObject _unregister(JSONObject args) throws JSONException, IOException {
		JSONObject result = new JSONObject();
		return result;
	}
	
	public JSONObject _resolve(JSONObject args) throws JSONException, IOException {
		JSONObject result = new JSONObject();
		return result;
	}

}
