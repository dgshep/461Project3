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
    	readUserInputs();
    	TextView output = (TextView) findViewById(R.id.output);
    	output.setText("");
    	int runs = 5;
		long time;
		long newTime;
		long overall = 0;
		RPCCallerSocket socket;
		try {
			socket = new RPCCallerSocket(mServerHost, mServerHost, mServerPort);
		} catch(UnknownHostException e){
			output.append("Unknown Host!");
			return;
		} catch(SocketException e){
			output.append(e.getMessage());
			return;
		} catch(NumberFormatException e){
			output.append("Not a valid port number!");
			return;
		} catch(Exception e){
			output.append(e.getMessage());
			return;
		}
		for(int i = 0; i < runs; i++){
			time = System.currentTimeMillis();
			socket.invoke("echo", "echo", new JSONObject().put("msg", ""));
			newTime = System.currentTimeMillis();
			long diff = newTime - time;
			overall += diff;
			output.append("Run #" + i + " (msec): " + diff + "\n");
			if(!socket.isPersistent() && i < runs){
				socket.close();
				socket = new RPCCallerSocket(mServerHost, mServerHost, mServerPort);
			}
		}
		output.append("Average (msec): " + ((double)overall) / runs + "\n");
		if (!socket.isClosed()) socket.close();
		output.append("Socket Closed.");
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
	                if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
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
    
    /**
     * Helper class to get onChar activity onto UI thread
     * <p>
     * The UI can be updated only by the UI (main) thread.  Characters are read by 
     * a background thread.  The background thread needs to get data to the UI thread
     * to update the display when characters arrive.  This class is useful for establishing
     * that inter-thread communication.
     */
    private class PingRunnerClass implements Runnable {

		public void run() {
			// TODO Auto-generated method stub
			
		}
    	
    }
//    private class OnCharClass implements Runnable {
//    	private static final int MAXCHARS = 25;
//    	int type;
//    	char c;
////    	Client mClient;
//    	public OnCharClass(int t, char ch, Client client) {
//    		type = t;
//    		c = ch;
////    		mClient = client;
//    	}
//		public void run() {
//			int textviewId = 0;
//			if ( type == Client.TYPE_SYNC ) textviewId = R.id.syncedText;
//			else if ( type == Client.TYPE_ASYNC ) textviewId = R.id.asyncText;
//			else throw new RuntimeException("Unknown type in ConsoleClient.onChar: " + type);
//			
//			TextView textView = (TextView)findViewById(textviewId);
//			//int textLength = textView.getText().length();
//			int textLength = textView.length();
//			int start = textLength > MAXCHARS ? textLength-MAXCHARS : 0;
//			String text = textView.getText().toString().substring(start) + c;
//			textView.setText(text);
//			
//			textView = (TextView)findViewById(R.id.charsreadText);
//			textView.setText(new Integer(mClient.getNumMatchingChars()).toString());
//		}
//    }
//    
//    /**
//     * Callback from Client object when a character is read, synchronously or asynchronously.
//     */
//    //@Override
//	public void onChar(int type, char c) {
//		runOnUiThread( new OnCharClass(type, c, this.mClient) );
//	}
}