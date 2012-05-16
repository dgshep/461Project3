package edu.uw.cs.cse461.sp12.OS;

public class DDNSRRecord {
	public final String name;
	public final String host;
	public final int port;
	
	public DDNSRRecord(String name, String host, int port) {
		this.name = name;
		this.host = host;
		this.port = port;
	}

}
