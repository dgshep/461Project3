package edu.uw.cs.cse461.sp12.OS;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
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
	private Map<String, Node> nodes;
	
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
		
		nodes = new HashMap<String, Node>();
		String[] namespace = OS.config().getProperty("ddns.namespace").split(",");
		for(String s : namespace) {
			nodes.put(s, new Node(s, OS.config().getProperty(s)));
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
		JSONObject pw = checkPW(args);
		JSONObject check = checkArgs(args);
		if(check != null)
			return check;
		else if(pw != null)
			return pw;
		else{
			Node location = search(args.getString("name"));
			JSONObject result = new JSONObject();
			if(location == null){
				noNameExep();
			}else if(location.type.equals("NS") || location.type.equals("CNAME")) {
				if(location.dirty)
					return noAddressExep();
				result.put("done", false);
			} else {
				result.put("done", true);
				result.put("lifetime", Integer.parseInt(OS.config().getProperty("lifetime")));
				try{
					nodes.get(location.name).dirty = false;
					nodes.get(location.name).ip = args.getString("ip");
					nodes.get(location.name).port = args.getInt("port");
				}catch(Exception e) {
					return runtimeExep();
				}
			}
			result.put("node", location.toJSON());
			result.put("resulttype", "registerresult");
			return result;
		}
//		{node:[node representation described next], lifetime:600, resulttype:"registerresult", "done":true}
	}
	
	private JSONObject checkPW(JSONObject args) {
		try {
			if(args.getString("password").equals(OS.config().getProperty("ddns.password")))
				return authorizationExep();
			return null;
		} catch (JSONException e) {
			return runtimeExep();
		}
	}
	
	private JSONObject checkArgs(JSONObject args) {
		try {
			String hostname = OS.config().getProperty("ddns.hostname");
			String requestName = args.getString("name");
			if(!requestName.substring(requestName.length() - 1 - hostname.length()).equals(hostname))
				return zoneExep();
			return null;
		} catch (JSONException e) {
			return runtimeExep();
		}
	}
	
	private Node search(String name) {
		String[] tokens = name.split("\\.");
		String host = OS.config().getProperty("ddns.hostname");
		int index = host.split("\\.").length;
		for(int i = tokens.length - 1 - index; i >= 0; i--){
			host = tokens[i] + "." + host;
			Node current = nodes.get(host);
			if(current == null || current.type.equals("NS") || current.type.equals("CNAME")){
				return current;	
			}
		}
		return nodes.get(host);
	}
	
	private JSONObject noAddressExep(String name) {
		try{
		JSONObject result = new JSONObject();
		result.put("resulttype", "ddnsexception");
		result.put("exceptionnum", 2);
		result.put("name", name);
		result.put("message", "");
		return result;
		}catch(Exception e) {return null;}
	}
	
	private JSONObject authorizationExep(String name) {
		try{
			JSONObject result = new JSONObject();
			result.put("resulttype", "ddnsexception");
			result.put("exceptionnum", 3);
			result.put("name", name);
			result.put("message", "");
			return result;
			}catch(Exception e) {return null;}
	}
	
	private JSONObject noNameExep(String name) {
		try{
			JSONObject result = new JSONObject();
			result.put("resulttype", "ddnsexception");
			result.put("exceptionnum", 1);
			result.put("name", name);
			result.put("message", "");
			return result;
			}catch(Exception e) {return null;}
	}
	
	private JSONObject runtimeExep(String name) {
		try{
			JSONObject result = new JSONObject();
			result.put("resulttype", "ddnsexception");
			result.put("exceptionnum", 4);
			result.put("name", name);
			result.put("message", "");
			return result;
			}catch(Exception e) {return null;}
	}
	
	private JSONObject expiredExep(String name) {
		try{
			JSONObject result = new JSONObject();
			result.put("resulttype", "ddnsexception");
			result.put("exceptionnum", 5);
			result.put("name", name);
			result.put("message", "");
			return result;
			}catch(Exception e) {return null;}
	}
	
	private JSONObject zoneExep(String name) {
		try{
			JSONObject result = new JSONObject();
			result.put("resulttype", "ddnsexception");
			result.put("exceptionnum", 6);
			result.put("name", name);
			result.put("message", "");
			return result;
			}catch(Exception e) {return null;}
	}
	
	public JSONObject _unregister(JSONObject args) throws JSONException, IOException {
//		unregister() works exactly like register(), except for two things. First, it marks 
//		the node as having no current address rather than updating its address. Second, 
//		if done is true, no node is returned.
		//Remove info from storage
		JSONObject pw = checkPW(args);
		JSONObject check = checkArgs(args);
		if(check != null)
			return check;
		else if(pw != null)
			return pw;
		else{
			Node location = search(args.getString("name"));
			JSONObject result = new JSONObject();
			if(location == null){
				noNameExep();
			}else if(location.type.equals("NS") || location.type.equals("CNAME")) {
				if(location.dirty)
					return noAddressExep();
				result.put("done", false);
				result.put("node", location.toJSON());
			} else {
				result.put("done", true);
				try{
					nodes.get(location.name).dirty = true;
				}catch(Exception e) {
					return runtimeExep();
				}
			}
			result.put("resulttype", "unregisterresult");
			return result;
		}
	}
	
	public JSONObject _resolve(JSONObject args) throws JSONException, IOException {
		//Look up name in storage
		//Send result
		JSONObject check = checkArgs(args);
		if(check != null)
			return check;
		else{
			Node location = search(args.getString("name"));
			JSONObject result = new JSONObject();
			if(location == null){
				noNameExep();
			}else if(location.type.equals("NS") || location.type.equals("CNAME")) {
				if(location.dirty)
					return noAddressExep();
				result.put("done", false);
			} else {
				result.put("done", true);
			}
			result.put("node", location.toJSON());
			result.put("resulttype", "resolveresult");
			return result;
		}
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
	
	private class Node {
		
		private String ip;
		private int port;
		private String name;
		private String type;
		private String alias;
		private boolean dirty;
		
		public Node(String name, String type){
			this.name = name;
			this.type = type;
			dirty = true;
			port = 0;
			ip = "";
		}
		
		public JSONObject toJSON() throws JSONException {
			JSONObject result = new JSONObject();
			result.put("name", name);
			result.put("type", type);
			if(type.equals("NS"))
				result.put("type", type);
			else {
				result.put("ip", ip);
				result.put("port", port);
			}
			return result;
		}
	}
}
