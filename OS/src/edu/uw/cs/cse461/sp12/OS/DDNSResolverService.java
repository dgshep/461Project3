package edu.uw.cs.cse461.sp12.OS;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.cs.cse461.sp12.OS.DDNSException.DDNSNoAddressException;
import edu.uw.cs.cse461.sp12.OS.DDNSException.DDNSNoSuchNameException;
import edu.uw.cs.cse461.sp12.OS.DDNSException.DDNSAuthorizationException;
import edu.uw.cs.cse461.sp12.OS.DDNSException.DDNSRuntimeException;
import edu.uw.cs.cse461.sp12.OS.DDNSException.DDNSTTLExpiredException;
import edu.uw.cs.cse461.sp12.OS.DDNSException.DDNSZoneException;




public class DDNSResolverService extends RPCCallable {
	
	private final String rootDNSServer = OS.config().getProperty("ddns.rootserver");
	private final String rootDNSPort = OS.config().getProperty("ddns.rootport");
	private final String password = OS.config().getProperty("ddns.password");
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
			request.put("name", targetStr);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		DDNSRRecord ret = process("resolve", request);
		return new DDNSRRecord(ret.type, targetStr, ret.host, ret.port);

	}
	
	public void register(DDNSFullName ddnsFullName, int port) {
		// TODO Auto-generated method stub
		
	}
	private DDNSRRecord process(String method, JSONObject request) throws DDNSException{
		DDNSRRecord ret = null;
		String resultType = "";
		String recordType = "";
		boolean done = false;
		int port = Integer.parseInt(rootDNSPort);
		JSONObject response = null;
		JSONObject node = null;
		String server = rootDNSServer;
		RPCCallerSocket caller;
		try{
			while(!done){
				caller = new RPCCallerSocket(server, server, port);
				response = caller.invoke("ddns", method, request);
				resultType = response.getString("resulttype");
				if(resultType.equals("ddnsexception")){
					int failType = response.getInt("exceptionnum");
					if(failType == 1) throw new DDNSNoSuchNameException();
					else if(failType == 2) throw new DDNSNoAddressException();
					else if(failType == 3) throw new DDNSAuthorizationException();
					else if(failType == 4) throw new DDNSRuntimeException();
					else if(failType == 5) throw new DDNSTTLExpiredException();
					else if(failType == 6) throw new DDNSZoneException();
					else throw new DDNSException(response.getString("message"));
				} else {
					node = response.getJSONObject("node");
					done = node.getBoolean("done");
					server = node.getString("ip");
					port = node.getInt("port");
				}
				return new DDNSRRecord(node.getString("name"), node.getString("type"), server, port);
			}
		} catch(IOException ioe){
			throw new DDNSException("Cannot connect to name server: " + server);
		} catch(JSONException je){
			throw new DDNSException("JSON related communication error");
		}
		return ret;
		
	}

	public void unregister(DDNSFullName ddnsFullName) {
		// TODO Auto-generated method stub
		
	}
	private enum RecordType{
		
	}

}
