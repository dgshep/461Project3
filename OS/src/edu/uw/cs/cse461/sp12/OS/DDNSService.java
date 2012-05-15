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
		// TODO Auto-generated method stub
		
	}
	
	public JSONObject _register(JSONObject args) throws JSONException, IOException {
		// We can't assume the underlying implementation won't modify args in some way that is
		// incompatible with return value, so have to make a copy of the args.
		
		// ANDROID INCOMPATIBILITY
		//JSONObject result = new JSONObject(args, JSONObject.getNames(args));
		
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
