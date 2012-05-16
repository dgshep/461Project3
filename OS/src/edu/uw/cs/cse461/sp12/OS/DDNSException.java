package edu.uw.cs.cse461.sp12.OS;

public class DDNSException extends Exception {

	public DDNSException(String string) {
		super(string);
		// TODO Auto-generated constructor stub
	}
	public class DDNSNoSuchNameException extends DDNSException {

		public DDNSNoSuchNameException(String string) {
			super(string);
			// TODO Auto-generated constructor stub
		}

	}
	public class DDNSNoAddressException extends DDNSException {

		public DDNSNoAddressException(String string) {
			super(string);
			// TODO Auto-generated constructor stub
		}
		
	}

}
