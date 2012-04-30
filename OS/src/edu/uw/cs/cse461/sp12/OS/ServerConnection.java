package edu.uw.cs.cse461.sp12.OS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import edu.uw.cs.cse461.sp12.OS.RPCCallable.RPCCallableMethod;


/**
 * Manages a single user's connection to the server / back end logic
 */
public class ServerConnection implements Runnable {

	private ServerSocket connection;
	private Map<String, RPCCallableMethod> callbacks;
	
	public ServerConnection(ServerSocket connection, Map<String, RPCCallableMethod> callbacks) {
		this.connection = connection;
		this.callbacks = callbacks;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(!connection.isClosed()) {
			try {
				Socket newUser = connection.accept();
				UserConnection thread = new UserConnection(newUser, callbacks);
				thread.run();
			} catch (IOException e) {}
			
		}
	}

}

