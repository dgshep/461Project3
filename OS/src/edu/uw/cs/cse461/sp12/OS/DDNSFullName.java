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
	public int hashCode(){
		return name.hashCode();
	}
	public boolean equals(Object o){
		if(o == null) return false;
		if(!(o instanceof DDNSFullName)) return false; 
		DDNSFullName cmp = (DDNSFullName) o;
		return cmp.name.equals(this.name);
	}

}
