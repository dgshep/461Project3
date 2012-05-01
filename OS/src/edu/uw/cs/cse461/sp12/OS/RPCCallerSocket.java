package edu.uw.cs.cse461.sp12.OS;

import java.io.IOException;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.cs.cse461.sp12.util.TCPMessageHandler;

/**
 * Implements a Socket to use in sending remote RPC invocations.  (It must engage
 * in the RPC handshake before sending the invocation request.)
 * @author zahorjan
 *
 */
public class RPCCallerSocket extends Socket {
	// This variable is part of the android Log.x idiom, as in Log.v(TAG, "some debugging log message")
	// You can use Log.x in console apps as well.
	private static final String TAG = "RPCCallerSocket";
	private TCPMessageHandler tcpHandler;
	private String mRemoteHost;
	private int id = 1;
	private boolean persistent;
	private boolean expired;
	
	/**
	 * Create a socket for sending RPC invocations, connecting it to the specified remote ip and port.
	 * @param hostname In Project 4, it's intended to be the string name of the remote system.  In Project 3, it's not terribly meaningful - repeat the ip.
	 * @param ip  Remote system IP address.
	 * @param port Remote RPC service's port.
	 * @throws IOException
	 * @throws JSONException 
	 */
	public RPCCallerSocket(String hostname, String ip, String port) throws IOException, JSONException {
		super(ip, Integer.parseInt(port));
		mRemoteHost = hostname;
		String time = OS.config().getProperty("rpc.timeout");
		int rpcTimeout = Integer.parseInt(time);  
		this.setSoTimeout(rpcTimeout);
		expired = false;
		tcpHandler = null;
		tcpHandler = new TCPMessageHandler(this);
		handShake();
	}
	
	/**
	 * Close this socket.
	 */
	@Override
	public void close() throws IOException {
		super.close();
	}
	public boolean isPersistent() {
		return persistent;
	}
	
	private void handShake() throws JSONException, IOException {
		JSONObject handshake = new JSONObject();
		handshake.put("id", id);
		id++;
		handshake.put("host", mRemoteHost);
		handshake.put("action", "connect");
		handshake.put("connection", "keep-alive");
		handshake.put("type", "control");
		tcpHandler.sendMessage(handshake);
		JSONObject reply = tcpHandler.readMessageAsJSONObject();
		if (reply.get("type").equals("ERROR")){
			throw new IOException(TAG + ": Handshake failed!");
		}
		try{
			String contype = reply.getString("connection");
			persistent = contype.equals("keep-alive");
			//System.out.println("Persistent Connection!");
		} catch(JSONException e) {
			persistent = false;
		}
		
	}
	/**
	 * Returns the name of the remote host to which this socket is connected (as specified in the constructor call).
	 * Useful in Project 4.
	 */
	public String remotehost() {
		return mRemoteHost;
	}

	/**
	 * Causes a remote call to the service/method names by the arguments.
	 * @param service Name of the remote service (or application)
	 * @param method Method of that service to invoke
	 * @param userRequest Call arguments
	 * @return
	 * @throws JSONException 
	 * @throws IOException 
	 */
	public JSONObject invoke(String service, String method, JSONObject userRequest) throws JSONException, IOException {
		if(expired) {
			throw new IOException("This is not a persistent connection!");
		}
		JSONObject invokation = new JSONObject();
		invokation.put("args", userRequest);
		invokation.put("id", id);
		//System.out.println(id);
		invokation.put("app", service);
		invokation.put("host", remotehost());
		invokation.put("method", method);
		invokation.put("type", "invoke");
		JSONObject output = new JSONObject();
		tcpHandler.sendMessage(invokation);
		id++;
		JSONObject out = tcpHandler.readMessageAsJSONObject();
		if (out.get("type").equals("OK")) {
			output = out.getJSONObject("value");
		} else {
			throw new IOException("Invocation Error: "
					+ out.getString("message"));
		}
		if(!persistent){
			expired = true;
		}
		return output;

	}
	
}
