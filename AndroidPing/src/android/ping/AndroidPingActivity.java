package android.ping;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import edu.uw.cs.cse461.sp12.OS.OS;
import edu.uw.cs.cse461.sp12.OS.RPCCallerSocket;
import edu.uw.cs.cse461.sp12.OS.RPCService;
import edu.uw.cs.cse461.sp12.util.Log;



public class AndroidPingActivity extends Activity {
	
	public static final String TAG = "AndroidPingActivity";
    public static final String PREFS_NAME = "CSE461";

    private String mServerHost;
    private String mServerPort;
	
	/** Called when the activity is first created.  Establishes the UI.  Reads state information saved by previous runs. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        Properties config = new Properties();
        config.put("host.name", "foo.bar.");
        config.put("rpc.timeout", "10000");
		// boot the OS and load RPC services
		try {
			OS.boot(config);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OS.startServices(OS.rpcServiceClasses);
    }

    /**
     * Called when activity is stopped.  Save user preferences for next execution.
     */
    @Override
    protected void onStop() {
    	super.onStop();
    	
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putString("serverhost", mServerHost);
    	editor.putString("serverport", mServerPort);
    	editor.commit();
    }
    
    /**
     * Fetch the data entered through the UI by the user.
     * @return
     */
    private boolean readUserInputs() {
        mServerHost = ((TextView)findViewById(R.id.host_ip)).getText().toString();
        mServerPort = ((TextView)findViewById(R.id.app_port)).getText().toString();
    	return true;
    }
    
    /**
     * Start/stop toggle button click handler.  (The association of a button click with the 
     * invocation of this routine is made in the layout object.) This runs the Ping protocol
     * @throws JSONException 
     * @throws IOException 
     */
    public void onStart(View v) throws IOException, JSONException {
    	TextView output = (TextView) findViewById(R.id.output);
    	output.setText("");
    	Thread pingThread = new Thread(){
    		public void run(){
    			readUserInputs();
    	    	int runs = 5;
    			long time;
    			long newTime;
    			long overall = 0;
    			RPCCallerSocket socket;
    			try {
    				socket = new RPCCallerSocket(mServerHost, mServerHost, mServerPort);
    			} catch(UnknownHostException e){
    				runOnUiThread(new outputUpdater("Unknown Host!"));
    				return;
    			} catch(SocketException e){
    				runOnUiThread(new outputUpdater(e.getMessage()));
    				return;
    			} catch(NumberFormatException e){
    				runOnUiThread(new outputUpdater("Not a valid port number!"));
    				return;
    			} catch(Exception e){
    				runOnUiThread(new outputUpdater(e.getMessage()));
    				return;
    			}
    			try {
    				for(int i = 0; i < runs; i++){
    					time = System.currentTimeMillis();
    					socket.invoke("echo", "echo", new JSONObject().put("msg", ""));
    					newTime = System.currentTimeMillis();
    					long diff = newTime - time;
    					overall += diff;
    					runOnUiThread(new outputUpdater("Run #" + i + " (msec): " + diff + "\n"));
    					if(!socket.isPersistent() && i < runs){
    						socket.close();
    						socket = new RPCCallerSocket(mServerHost, mServerHost, mServerPort);
    					}
    				}
    				runOnUiThread(new outputUpdater("Average (msec): " + ((double)overall) / runs + "\n"));
    				if (!socket.isClosed()) socket.close();
    				runOnUiThread(new outputUpdater("Socket Closed."));
    			} catch (Exception e) {
    				runOnUiThread(new outputUpdater(e.getMessage()));
    				return;
    			}
    	    }
    	};
    	pingThread.start();
    }
    public void whoami(View v) throws IOException, JSONException {
    	TextView output = (TextView) findViewById(R.id.output);
    	output.setText("");
		StringBuilder IFCONFIG=new StringBuilder();
		try {
		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()){ //&& !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
	                IFCONFIG.append(inetAddress.getHostAddress().toString());
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        Log.e("LOG_TAG", ex.toString());
	    }
	    try {
			RPCService rpcService = (RPCService)OS.getService("rpc");
			IFCONFIG.append("  Port: " + rpcService.localPort());
		} catch (Exception e) {}
		output.setText(IFCONFIG);
    }
    private class outputUpdater implements Runnable {
    	String output;
    	TextView v;
    	public outputUpdater(String output){
    		this.output = output;
    		v = (TextView)findViewById(R.id.output);
    	}
		@Override
		public void run() {
			v.append(output);
		}
    	
    }
}