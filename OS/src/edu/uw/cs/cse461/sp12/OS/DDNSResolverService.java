package edu.uw.cs.cse461.sp12.OS;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;


public class DDNSResolverService extends RPCCallable {
	
	public static final String rootDNSServer = "cse461.cs.washington.edu";
	public static final String rootDNSPort = "46130";
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
		DDNSRRecord ret = null;
		String recordType = "";
		String name = targetStr;
		String ip = "";
		int port = 0;
		try {
			while (!recordType.equals("A")) {
				RPCCallerSocket socket = new RPCCallerSocket(rootDNSServer,
						rootDNSServer, rootDNSPort);
				JSONObject response = socket.invoke("ddns", "resolve",
						new JSONObject().put("name", targetStr));
				recordType = response.getString("type");
			}
		} catch (IOException e) {
			throw new DDNSException("Cannot connect to root DNS server!");
		} catch (JSONException e) {
			throw new DDNSException("Communication with name server encountered an error.");
		}
		return ret;

	}
	
	public void register(DDNSFullName ddnsFullName, int port) {
		// TODO Auto-generated method stub
		
	}

	public void unregister(DDNSFullName ddnsFullName) {
		// TODO Auto-generated method stub
		
	}
	private enum RecordType{
		
	}

}
