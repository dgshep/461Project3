package android.ping;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;



public class AndroidPingActivity extends Activity {
	public static final String TAG = "AndroidPingActivity";
    public static final String PREFS_NAME = "CSE461";
    private final String configFilename = "foo.bar.config.ini";

//    private Client mClient;
    private Thread mClientThread;
	private String mServerHost;
	private int mServerPort;
	
	/** Called when the activity is first created.  Establishes the UI.  Reads state information saved by previous runs. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
//        mClient = new Client();
//        mClient.addListener(this);
//        
//        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
//        mServerHost = settings.getString("serverhost", Properties.SERVER_HOST);
//        int defaultPort = Properties.SERVER_PORT_NEGOTIATE + Properties.SERVER_PORT_INTERSYMBOL_TIME_VEC.length;
//        mServerPort = settings.getInt("serverport", defaultPort );
//        
//        ((TextView)findViewById(R.id.hostText)).setText(mServerHost);
//        ((TextView)findViewById(R.id.portText)).setText(new Integer(mServerPort).toString());
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
    	editor.putInt("serverport", mServerPort);
    	editor.commit();
    }
    
    /**
     * Fetch the data entered through the UI by the user.
     * @return
     */
    private boolean readUserInputs() {
        mServerHost = ((TextView)findViewById(R.id.host_ip)).getText().toString();
        mServerPort = Integer.parseInt(((TextView)findViewById(R.id.app_port)).getText().toString());
//        if(mServerPort != 46100){
//	        try {
//	        	mServerInterSymbolTime = mClient.portToIntersymbolTime(mServerPort, Properties.SERVER_INTER_SYMBOL_TIME);
//	        } catch (Exception e) {
//	        	// display a fleeting error message
//	    		Toast toast = Toast.makeText(getApplicationContext(), "Valid port numbers are " + Properties.SERVER_PORT_NEGOTIATE + "-" + 
//	    							(Properties.SERVER_PORT_NEGOTIATE + Properties.SERVER_PORT_INTERSYMBOL_TIME_VEC.length), Toast.LENGTH_LONG);
//	    		toast.show();
//	    		return false;
//	    	}
//        } else{
//        	mServerInterSymbolTime = Integer.parseInt(((TextView)findViewById(R.id.interText)).getText().toString());
//        }
    	return true;
    }
    
    /**
     * Start/stop toggle button click handler.  (The association of a button click with the 
     * invocation of this routine is made in the layout object.)
     * @throws JSONException 
     * @throws IOException 
     */
    public void onStart(View v) throws IOException, JSONException {
    	EditText hostIP = ((EditText) findViewById(R.id.host_ip));
    	EditText appPort = ((EditText) findViewById(R.id.app_port));
    	TextView output = (TextView) findViewById(R.id.output);
    	output.setText("");
    	String targetIP = hostIP.getText().toString();
    	String targetPort = appPort.getText().toString();
    	int runs = 5;
		long time;
		long newTime;
		long overall = 0;
		AndroidRPCCallerSocket socket = new AndroidRPCCallerSocket(targetIP, targetIP, targetPort);
		for(int i = 0; i < runs; i++){
			time = System.currentTimeMillis();
			socket.invoke("echo", "echo", new JSONObject().put("msg", ""));
			newTime = System.currentTimeMillis();
			long diff = newTime - time;
			overall += diff;
			output.append("Run #" + i + " (msec): " + diff + "\n");
			if(!socket.isPersistent() && i < runs){
				socket.close();
				socket = new AndroidRPCCallerSocket(targetIP, targetIP, targetPort);
			}
		}
		output.append("Average (msec): " + ((double)overall) / runs);
		if (!socket.isClosed()) socket.close();
    }

    /**
     * Helper class to get onChar activity onto UI thread
     * <p>
     * The UI can be updated only by the UI (main) thread.  Characters are read by 
     * a background thread.  The background thread needs to get data to the UI thread
     * to update the display when characters arrive.  This class is useful for establishing
     * that inter-thread communication.
     */
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