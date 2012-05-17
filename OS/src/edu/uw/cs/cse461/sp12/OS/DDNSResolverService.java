package edu.uw.cs.cse461.sp12.OS;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.cs.cse461.sp12.OS.DDNSException.DDNSNoAddressException;
import edu.uw.cs.cse461.sp12.OS.DDNSException.DDNSNoSuchNameException;
import edu.uw.cs.cse461.sp12.OS.DDNSException.DDNSAuthorizationException;
import edu.uw.cs.cse461.sp12.OS.DDNSException.DDNSRuntimeException;
import edu.uw.cs.cse461.sp12.OS.DDNSException.DDNSTTLExpiredException;
import edu.uw.cs.cse461.sp12.OS.DDNSException.DDNSZoneException;
import edu.uw.cs.cse461.sp12.util.Log;




public class DDNSResolverService extends RPCCallable {
	
	private final String rootDNSServer = OS.config().getProperty("ddns.rootserver");
	private final String rootDNSPort = OS.config().getProperty("ddns.rootport");
	private final String password = OS.config().getProperty("ddns.password");
	private Map<DDNSFullName, RegThread> regThreads = new HashMap<DDNSFullName, RegThread>();
	/**
	 * The constructor registers RPC-callable methods with the RPCService.
	 * @throws IOException
	 * @throws NoSuchMethodException
	 */
	DDNSResolverService() throws Exception {
		// Set up the method descriptor variable to refer to this->_()
		// Register the method with the RPC service as externally invocable method "echo"
	}
	@Override
	public String servicename() {
		return "ddnsresolver";
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}
	public DDNSRRecord resolve(String targetStr) throws DDNSException{
		JSONObject request = new JSONObject();
		try {
			request.put("name", new DDNSFullName(targetStr).toString());
		} catch (JSONException e) {
			throw new IllegalArgumentException("Target String: " + targetStr + " is invalid!");
		}
		try {
			JSONObject ret = process("resolve", request).getJSONObject("node");
			return new DDNSRRecord(ret.getString("type"), targetStr, ret.getString("ip"), ret.getInt("port"));
		} catch (JSONException e) {
			Log.e("Resolve", "Return message is garbled...");
			return null;
		}
	}
	
	public void register(DDNSFullName ddnsFullName, int port) throws DDNSException {
		String ip = getIp();
		JSONObject request = new JSONObject();
		RegThread r;
		try {
			request.put("name", ddnsFullName.toString());
			request.put("ip", ip);
			request.put("port", port);
			request.put("password", password);
			r = new RegThread(request);
		} catch (JSONException e) {
			throw new IllegalArgumentException("Illegal arguments: " + ddnsFullName + " ; " + password);
		}
		Thread t = new Thread(r);
		t.start();
		regThreads.put(ddnsFullName, r);
//		JSONObject ret = process("register", request);
//		try{
//			return ret.getInt("lifetime");
//		} catch(JSONException e){
//			throw new IllegalStateException("Unknown lifetime!");
//		}
		
	}
	public void unregister(DDNSFullName ddnsFullName) throws DDNSException {
		JSONObject request = new JSONObject();
		try{
			request.put("name", ddnsFullName.toString());
			request.put("password", password);
		} catch(JSONException e){
			throw new IllegalArgumentException("Illegal arguments: " + ddnsFullName + " ; " + password);
		}
		RegThread t = regThreads.get(ddnsFullName);
		t.stopUpdate();
		try{
			process("unregister", request);
		}catch (DDNSNoAddressException e){};
	}
	private String getIp(){
		RPCService rpc = (RPCService) OS.getService("rpc");
		String ip = "";
		try {
			ip = rpc.localIP();
		} catch (UnknownHostException e) {
			throw new IllegalStateException("This host doesn't have an ip address!");
		}
		return ip;
	}
	private JSONObject process(String method, JSONObject request) throws DDNSException{
		String resultType = "";
		String recordType = "";
		boolean done = false;
		int serverPort = Integer.parseInt(rootDNSPort);
		String serverHost = rootDNSServer;
		String ip = "";
		int port = 0;
		JSONObject response = null;
		JSONObject node = null;
		RPCCallerSocket caller;
		try{
			while(true){
				caller = new RPCCallerSocket(serverHost, serverHost, serverPort);
				response = caller.invoke("ddns", method, request);
				resultType = response.getString("resulttype");
				if(resultType.equals("ddnsexception")){
					int failType = response.getInt("exceptionnum");
					String failMessage = response.getString("message");
					throwException(failType, failMessage);
					throw new DDNSException(failMessage);
				} else {
					done = response.getBoolean("done");
					if(done){ // || response.getJSONObject("node").getString("name").equals(request.getString("name")))  break; //
						caller.close();
						break;
					}
					node = response.getJSONObject("node");
					ip = node.getString("ip");
					port = node.getInt("port");
					recordType = node.getString("type");
					if(recordType.equals("CNAME")){
						request.put("name", node.get("alias"));
						serverHost = rootDNSServer;
						serverPort = Integer.parseInt(rootDNSPort);
					}
					if(recordType.equals("NS")) {
						serverHost = ip;
						serverPort = port;
					}
				}
				caller.close();
			}
			return response;
		} catch(IOException ioe){
			throw new DDNSException("Cannot connect to name server: " + serverHost);
		} catch(JSONException je){
			throw new DDNSException("JSON related communication error");
		}	
	}
	private void throwException(int failType, String failMessage) throws DDNSException{
		if(failType == 1) throw new DDNSNoSuchNameException(failMessage);
		else if(failType == 2) throw new DDNSNoAddressException(failMessage);
		else if(failType == 3) throw new DDNSAuthorizationException(failMessage);
		else if(failType == 4) throw new DDNSRuntimeException(failMessage);
		else if(failType == 5) throw new DDNSTTLExpiredException(failMessage);
		else if(failType == 6) throw new DDNSZoneException(failMessage);
	}
	public class RegThread implements Runnable{
		JSONObject request;
		boolean update;

		public RegThread(JSONObject request) throws JSONException{
			this.request = new JSONObject(request.toString());
			this.update = true;
		}
		public synchronized void stopUpdate() {
			this.update = false;
			
		}
		@Override
		public void run() {
			int ttl;
			boolean registered = false;
			while (update) {
				try {
					Log.i("Register Thread", request.getString("name"));
					JSONObject ret = process("register", this.request);
					ttl = ret.getInt("lifetime");
					if (!registered) {
						System.out.println("Registered "+ request.getString("name") + " with a lifetime of: " + ttl);
						registered = true;
					}
				} catch (DDNSException e) {
					e.printStackTrace();
					break;
				} catch (JSONException je) {
					je.printStackTrace();
					break;
				}
				
				int updateTime = Math.abs(ttl - (int) (ttl * .5));
				try {
					int sleepTime = 0;
					while((sleepTime < updateTime) && update) {//checks for unregister
						Thread.sleep(1000);
						sleepTime += 1;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
}
