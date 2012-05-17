package edu.uw.cs.cse461.sp12.OS;

public class DDNSFullName {
	
	private String name;

	public DDNSFullName(String name) {
		if(!name.endsWith(".")) {
			this.name = name.trim().concat(".");
		} else {
			this.name = name.trim();
		}
	}
	public String toString(){
		return name;
	}

}
