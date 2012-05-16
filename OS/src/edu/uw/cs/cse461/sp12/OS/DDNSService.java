package edu.uw.cs.cse461.sp12.OS;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.cs.cse461.sp12.OS.RPCCallable.RPCCallableMethod;

public class DDNSService extends RPCCallable {

	private RPCCallableMethod<DDNSService> register;
	private RPCCallableMethod<DDNSService> unregister;
	private RPCCallableMethod<DDNSService> resolve;
	
	private RPCCallerSocket root;
	private Thread regThread;
	private Map<String, node> nodes;
	
	public DDNSService() throws Exception {
		//Read in config file for tree
		//Fill out storage tree
		
		register = new RPCCallableMethod<DDNSService>(this, "_register");
		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "register", register);
		unregister = new RPCCallableMethod<DDNSService>(this, "_unregister");
		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "unregister", unregister);
		resolve = new RPCCallableMethod<DDNSService>(this, "_resolve");
		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "resolve", resolve);
		
		root = new RPCCallerSocket(OS.config().getProperty("ddns.rootserver"), 
				OS.config().getProperty("ddns.rootserver"), OS.config().getProperty("ddns.rootport"));
		regThread = new Thread(new Registration());
		regThread.start();
		
		String[] namespace = OS.config().getProperty("ddns.namespace").split(",");
		for(String s : namespace) {
			
		}
		
	}
	
	@Override
	public String servicename() {
		return "ddns";
	}

	@Override
	public void shutdown() {
		try {
			JSONObject unregister = new JSONObject();
			unregister.put("name", OS.config().getProperty("ddns.hostname"));
			unregister.put("password", OS.config().getProperty("ddns.password"));
			root.invoke("ddns", "unregister", unregister);
			root.close();
		} catch (Exception e) {e.printStackTrace();}
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
	
	private node search(String name) {
		String[] tokens = name.split("\\.");
		String host = OS.config().getProperty("ddns.hostname");
		int index = host.split("\\.").length;
		for(int i = tokens.length - 1; i > 0; i--){
			
		}
		return null;
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

	private class Registration implements Runnable {

		@Override
		public void run() {
			try {
				while(!root.isClosed()){
					JSONObject register = new JSONObject();
					register.put("name", OS.config().getProperty("ddns.hostname"));
					register.put("port", ((RPCService)OS.getService("rpc")).localPort());
					register.put("password", OS.config().getProperty("ddns.password"));
					register.put("ip", ((RPCService)OS.getService("rpc")).localIP());
					JSONObject ttl = root.invoke("ddns", "register", register);
					Timer t = new Timer();
					t.schedule(new wakeup(), ttl.getInt("lifetime") - 5000);
					wait();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private class wakeup extends TimerTask {
		@Override
		public void run() {
			regThread.notify();
		}
	}
	
	private class node {
		
		private String ip;
		private int port;
		private String name;
		private boolean dirty;
		
		public node(String name){
			this.name = name;
			dirty = true;
			port = 0;
			ip = "";
		}
		
		
	}
}
