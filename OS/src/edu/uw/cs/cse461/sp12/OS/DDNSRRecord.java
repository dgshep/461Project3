package edu.uw.cs.cse461.sp12.OS;

public class DDNSRRecord {
	public final String type;
	public final String name;
	public final String host;
	public final int port;
	
	public DDNSRRecord(String type, String name, String host, int port) {
		this.type = type;
		this.name = name;
		this.host = host;
		this.port = port;
	}
	public String toString(){
		return "Type: " + this.type +
			   "; Name: " + this.name + 
			   "; Host: " + this.host +
			   "; Port: " + this.port;
	}

}
