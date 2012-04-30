package edu.uw.cs.cse461.sp12.OS;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.cs.cse461.sp12.OS.RPCCallable.RPCCallableMethod;
import edu.uw.cs.cse461.sp12.util.TCPMessageHandler;

public class UserConnection implements Runnable {

	Socket user;
	TCPMessageHandler handler;
	private Map<String, RPCCallableMethod> callbacks;
	boolean listening;
	boolean handshook;
	
	public UserConnection(Socket user, Map<String, RPCCallableMethod> callbacks) throws IOException {
		this.user = user;
		handler = new TCPMessageHandler(user);
		listening = true;
		handshook = false;
		this.callbacks = callbacks;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(listening) {
			try {
				parseMessage(handler.readMessageAsJSONObject());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void parseMessage(JSONObject json){
		if(!handshook){
			try {
				if(json.getString("action").equals("connect")){
					
				}
			} catch (JSONException e) {
				// TODO didn't contain the key "action"
				//error message
			}
		}else {
			try {
				if(json.getString("type").equals("invoke")) {
					
				}
				
			} catch (JSONException e) {
				// TODO didn't contain the key "type"
				//error message
				
				
			}
		}
	}

}
