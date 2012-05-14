package edu.uw.cs.cse461.sp12.OS;


public class DDNSResolverService extends RPCCallable {

	@Override
	public String servicename() {
		return "ddnsresolver";
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}
	public DDNSRRecord resolve(String targetStr) throws DDNSException{
		return null;
		//TODO find the relevant record;
	}

	public void register(DDNSFullName ddnsFullName, int port) {
		// TODO Auto-generated method stub
		
	}

	public void unregister(DDNSFullName ddnsFullName) {
		// TODO Auto-generated method stub
		
	}
	


}
