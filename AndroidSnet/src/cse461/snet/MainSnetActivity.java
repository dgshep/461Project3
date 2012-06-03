package cse461.snet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import edu.uw.cs.cse461.sp12.DB461.ContextManager;
import edu.uw.cs.cse461.sp12.OS.DDNSException;
import edu.uw.cs.cse461.sp12.OS.DDNSFullName;
import edu.uw.cs.cse461.sp12.OS.DDNSResolverService;
import edu.uw.cs.cse461.sp12.OS.OS;
import edu.uw.cs.cse461.sp12.OS.RPCService;
import edu.uw.cs.cse461.sp12.OS.SNetDB461.Photo;
import edu.uw.cs.cse461.sp12.OS.SnetController;
import edu.uw.cs.cse461.sp12.util.Log;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainSnetActivity extends Activity {
	
	private Button mCam;
	
	private Button mChoose;
	
	private Button mManage;
	
	private ImageView myPhoto;
	
	private ImageView chosenPhoto;
	
	private File externalStorage;
	
	private SnetController snet;
	
	private final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
	
	
	
    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		externalStorage = Environment.getExternalStorageDirectory();
		mCam = (Button) findViewById(R.id.camera);
		mCam.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
				
			}
			
		});
	}

	private void bootOS(){
		Properties config = new Properties();
		config.put("host.name", "foo.bar.");
		config.put("rpc.timeout", "2000");
		config.put("ddns.cachettl", "60");
		config.put("ddns.rootserver", "cse461.cs.washington.edu");
		config.put("ddns.rootport", "46150");
		config.put("ddns.password", "champ");
		config.put("rpc.serverport", "46150");
		config.put("host.name", "hallshep.cse461.");
		config.put("ddns.resolvesteps","10");
		config.put("ddns.namespace", "hallshep.cse461.");
		config.put("hallshep.cse461.", "SOA");
		
		// boot the OS and load RPC services
		ContextManager.setContext(getApplicationContext());
		try {
			OS.boot(config);
			OS.startServices(OS.androidServiceClasses);
			DDNSFullName ddnsName = new DDNSFullName(OS.hostname());
			int port = ((RPCService) OS.getService("rpc")).localPort();
			((DDNSResolverService) OS.getService("ddnsresolver")).register(ddnsName, port);
		} catch (RuntimeException r){
		} catch (DDNSException e){
			Log.i("Boot", "This device cannot register");
		} catch (Exception e) {
			throw new IllegalStateException("OS Hasn't booted!");
		}
		snet = (SnetController) OS.getService("snet");
		snet.setPhotoDirectory(externalStorage);
		
		
		
		myPhoto = (ImageView) findViewById(R.id.myPhoto);
		File myPhotoFile = new File(externalStorage, "myPhoto.png");
		if(myPhotoFile.exists()) {
			try {
				myPhoto.setImageBitmap(BitmapLoader.loadBitmap(myPhotoFile.getCanonicalPath(), 500, 700));
			} catch (IOException e) {
				
			}
		}
		
		
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		bootOS();
		if(requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK){
			Bitmap photoBmp = (Bitmap)data.getExtras().get("data");
			myPhoto.setImageBitmap(photoBmp);
			OutputStream outStream = null;
			File file = new File(externalStorage, "myPhoto.png");
			try {
			    outStream = new FileOutputStream(file);
			    photoBmp.compress(Bitmap.CompressFormat.PNG, 75, outStream);
			    outStream.flush();
			    outStream.close();
			    snet.setMyPhoto(new Photo(file));
			    Log.i("Community", snet.toString());
			} catch(IOException e) {
				Toast.makeText(MainSnetActivity.this, "Failed to Save Image", Toast.LENGTH_SHORT).show();
			}
		}
	}
	protected void onStart(){
		super.onStart();
		bootOS();
	}
    

    protected void onStop(){
    	super.onStop();
    	OS.shutdown();
    }


}