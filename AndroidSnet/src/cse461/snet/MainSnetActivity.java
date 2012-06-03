package cse461.snet;

import java.io.File;
import java.io.FileNotFoundException;
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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
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
	private final int CHOOSE_PICTURE_ACTIVITY_REQUEST_CODE = 2;
	
	
	
    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bootOS();
		setContentView(R.layout.main);
		mCam = (Button) findViewById(R.id.camera);
		mCam.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
				
			}
			
		});
		mChoose = (Button) findViewById(R.id.choose);
		mChoose.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_PICK,
				          android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				intent.setType("image/*");
				startActivityForResult(intent, CHOOSE_PICTURE_ACTIVITY_REQUEST_CODE);
				
			}
			
		});
		mManage = (Button) findViewById(R.id.community);
		mManage.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				Intent intent = new Intent(MainSnetActivity.this, CommunityManagementActivity.class);
				startActivity(intent);
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
		config.put("ddns.edgesteps", "10");
		
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
		externalStorage = new File(Environment.getExternalStorageDirectory(), "/gallery/");
		externalStorage.mkdir();
		snet = (SnetController) OS.getService("snet");
		snet.setPhotoDirectory(externalStorage);
		
		
		
		
		
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		bootOS();
		if(resultCode == Activity.RESULT_OK) {
			if(requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE){
				Bitmap photoBmp = (Bitmap)data.getExtras().get("data");
				myPhoto.setImageBitmap(photoBmp);
				OutputStream outStream = null;
				File file = new File(externalStorage, (snet.getMe().generation + 1) + "myPhoto.png");
				try {
				    outStream = new FileOutputStream(file);
				    photoBmp.compress(Bitmap.CompressFormat.PNG, 75, outStream);
				    outStream.flush();
				    outStream.close();
				    sendBroadcast(new Intent(
				            Intent.ACTION_MEDIA_MOUNTED,
				            Uri.parse("file://" + Environment.getExternalStorageDirectory())));
				    snet.setMyPhoto(new Photo(file));
				    Log.i("Community", snet.toString());
				} catch(IOException e) {
					Toast.makeText(MainSnetActivity.this, "Failed to Save Image", Toast.LENGTH_SHORT).show();
				}
			} else if(requestCode == CHOOSE_PICTURE_ACTIVITY_REQUEST_CODE){
				Uri selectedImage = data.getData();
				String[] filePathColumn = {MediaStore.Images.Media.DATA};

				Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();
				File photoFile = new File(filePath);
				try {
					snet.setChosenPhoto(new Photo(photoFile));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}
	protected void onStart(){
		super.onStart();
		updatePhoto();
	}
    protected void onResume(){
    	super.onResume();
    	updatePhoto();
    }

    protected void onDestroy(){
    	super.onDestroy();
    	OS.shutdown();
    }
    private void updatePhoto(){
    	myPhoto = (ImageView) findViewById(R.id.myPhoto);
    	chosenPhoto = (ImageView) findViewById(R.id.chosenPhoto);
    	Photo mp = snet.getMyPhoto();
    	Photo cp = snet.getChosenPhoto();
    	if(mp != null) {
    		File myPhotoFile = new File(externalStorage, mp.file().getName());
    		if(myPhotoFile.exists()) {
    			try {
    				myPhoto.setImageBitmap(BitmapLoader.loadBitmap(myPhotoFile.getCanonicalPath(), 100, 100));
    			} catch (IOException e) {
    				
    			}
    		}
    	}
    	if(cp != null){
			File chosenPhotoFile = new File(externalStorage, cp.file().getName());
			
			if(chosenPhotoFile.exists()){
				try {
					chosenPhoto.setImageBitmap(BitmapLoader.loadBitmap(chosenPhotoFile.getCanonicalPath(), 100, 100));
				} catch (IOException e) {
					
				}
			}
    	}
			
    }


}