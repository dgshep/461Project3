package edu.uw.cs.cse461.sp12.OS;

@SuppressWarnings("serial")
public class DDNSException extends Exception {

	public DDNSException(String string) {
		super(string);
		// TODO Auto-generated constructor stub
	}
	public static class DDNSNoSuchNameException extends DDNSException {

		public DDNSNoSuchNameException(String string) {
			super(string);
			// TODO Auto-generated constructor stub
		}
		public DDNSNoSuchNameException() {
			super("");
			// TODO Auto-generated constructor stub
		}

	}
	public static class DDNSNoAddressException extends DDNSException {

		public DDNSNoAddressException(String string) {
			super(string);
			// TODO Auto-generated constructor stub
		}
		public DDNSNoAddressException() {
			super("");
			// TODO Auto-generated constructor stub
		}
		
	}
	public static class DDNSAuthorizationException extends DDNSException {

		public DDNSAuthorizationException(String string) {
			super(string);
			// TODO Auto-generated constructor stub
		}
		public DDNSAuthorizationException() {
			super("");
			// TODO Auto-generated constructor stub
		}
		
	}
	public static class DDNSRuntimeException extends DDNSException{
		
		public DDNSRuntimeException(String string){
			super(string);
		}
		public DDNSRuntimeException(){
			super("");
		}
	}
	public static class DDNSTTLExpiredException extends DDNSException{
		public DDNSTTLExpiredException(String string){
			super(string);
		}
		public DDNSTTLExpiredException(){
			super("");
		}
		
	}
	public static class DDNSZoneException extends DDNSException{
		public DDNSZoneException(String string){
			super(string);
		}
		public DDNSZoneException(){
			super("");
		}
	}

}
