package edu.uw.cs.cse461.sp12.OS;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;

/**
 * Implements the side of RPC that receives remote invocation requests.
 * 
 * @author zahorjan
 *
 */
public class RPCService extends RPCCallable {
	// used with the android idiom Log.x, as in Log.v(TAG, "some debug log message")
	private static final String TAG="RPCService";
	
	private ServerSocket mServerSocket;
	private Thread connectionListener;
	private Map<String, RPCCallableMethod> callbacks;
	
	/**
	 * This method must be implemented by RPCCallable's.  
	 * "rpc" is the well-known name of this service.
	 */
	@Override
	public String servicename() {
		return "rpc";
	}
	
	/**
	 * Constructor.  Creates the Java ServerSocket and binds it to a port.
	 * If the config file specifies an rpc.serverport value, it should be bound to that port.
	 * Otherwise, you should specify port 0, meaning the operating system should choose a currently unused port.
	 * (The config file settings are available via the OS object.)
	 * <p>
	 * Once the port is created, a thread needs to be created to listen for connections on it.
	 * 
	 * @throws Exception
	 */
	RPCService() throws Exception {

		// Set some socket options.  
		// setReuseAddress lets you reuse a server port immediately after terminating
		// an application that has used it.  (Normally that port is unavailable for a while, for reasons we'll see
		// later in the course.
		// setSoTimeout causes a thread waiting for connections to timeout, instead of waiting forever, if no connection
		// is made before the timeout interval expires.  (You don't have to use 1/2 sec. for this value - choose your own.)
		String port = OS.config().getProperty("rpc.serverport");
		if(port != null)
			mServerSocket = new ServerSocket(Integer.parseInt(port));
		else
			mServerSocket = new ServerSocket();
		mServerSocket.setReuseAddress(true); // allow port number to be reused immediately after close of this socket
		mServerSocket.setSoTimeout(500); // well, we have to wake up every once and a while to check for program termination
		
		ServerConnection newConnection = new ServerConnection(mServerSocket, callbacks);
		connectionListener = new Thread(newConnection);
		connectionListener.start();
		//TODO: implement
	}
	
	/**
	 * System is shutting down imminently.  Do any cleanup required.
	 */
	public void shutdown() {
		try {
			mServerSocket.close();
		} catch (IOException e) {}
	}
	
	public Map<String, RPCCallableMethod> getHandlers() {
		return Collections.unmodifiableMap(callbacks);
	}
	
	/**
	 * Services and applications with RPC callable methods register them with the RPC service using this routine.
	 * Those methods are then invoked as callbacks when an remote RPC request for them arrives.
	 * @param serviceName  The name of the service.
	 * @param methodName  The external, well-known name of the service's method to call
	 * @param method The descriptor allowing invocation of the Java method implementing the call
	 * @throws Exception
	 */
	public synchronized void registerHandler(String serviceName, String methodName, RPCCallableMethod method) throws Exception {
		//TODO: implement
		callbacks.put(serviceName + methodName, method);
	}
	
	/**
	 * Returns the local IP address.
	 * @return
	 * @throws UnknownHostException
	 */
	public String localIP() throws UnknownHostException {
		//TODO might have to remove the port from the end of the string
		return mServerSocket.toString();
	}

	/**
	 * Returns the port to which the RPC ServerSocket is bound.
	 * @return
	 */
	public int localPort() {
		return mServerSocket.getLocalPort();
	}
}
