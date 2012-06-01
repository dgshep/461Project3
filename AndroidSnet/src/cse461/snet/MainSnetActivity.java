package cse461.snet;

import java.util.Properties;

import edu.uw.cs.cse461.sp12.OS.DDNSException;
import edu.uw.cs.cse461.sp12.OS.DDNSFullName;
import edu.uw.cs.cse461.sp12.OS.DDNSResolverService;
import edu.uw.cs.cse461.sp12.OS.OS;
import edu.uw.cs.cse461.sp12.OS.RPCService;
import edu.uw.cs.cse461.sp12.util.Log;
import android.app.Activity;
import android.os.Bundle;

public class MainSnetActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        bootOS();
    }
    
    private void bootOS(){
 	   Properties config = new Properties();
        config.put("host.name", "foo.bar.");
        config.put("rpc.timeout", "2000");
        config.put("ddns.cachettl", "60");
        config.put("ddns.rootserver", "cse461.cs.washington.edu");
        config.put("ddns.rootport", "46130");
        config.put("ddns.password", "champ");
        config.put("rpc.serverport", "46120");
        config.put("host.name", "galaxy.hallshep.cse461");
        config.put("ddns.resolvesteps","10");
 		// boot the OS and load RPC services
 		try {
 			OS.boot(config);
 		} catch (RuntimeException r){
 		} catch (Exception e) {
 			throw new IllegalStateException("OS Hasn't booted!");
 		}
 		OS.startServices(OS.androidServiceClasses);
 		DDNSFullName ddnsName = new DDNSFullName(OS.hostname());
 		int port = ((RPCService) OS.getService("rpc")).localPort();
 		try {
 			((DDNSResolverService) OS.getService("ddnsresolver")).register(ddnsName, port);
 		} catch (DDNSException e) {
 			Log.e("DDNS Register", "Couldnt Register this device!: " + e.getMessage());
 		}
    }

}