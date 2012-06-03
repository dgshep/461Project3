package cse461.snet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import edu.uw.cs.cse461.sp12.OS.OS;
import edu.uw.cs.cse461.sp12.OS.SnetController;

public class CommunityManagementActivity extends Activity {
	private SnetController snet;
	private Spinner mContacts;
	private ArrayAdapter<String> mContactList;
	private Button mContact;
	private Button mContactAll;
	private Button mFriend;
	private Button mUnfriend;
	private TextView mConsole;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.control);
		mConsole = (TextView) findViewById(R.id.console);
		snet = (SnetController) OS.getService("snet");
		mContacts = (Spinner) findViewById(R.id.members);
		mContactList = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_dropdown_item_1line, snet.community());
		mContacts.setAdapter(mContactList);
		mContact = (Button) findViewById(R.id.contact);
		mContact.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				String name = (String) mContacts.getSelectedItem();
				snet.fetchUpdates(name, true);
				sendBroadcast(new Intent(
				        Intent.ACTION_MEDIA_MOUNTED,
				        Uri.parse("file://" + Environment.getExternalStorageDirectory())));
				Log.i("FetchUpdates", snet.toString());
			}
			
		});
		mContactAll = (Button) findViewById(R.id.updateAll);
		mContactAll.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					System.out = mConsole;
					snet.allUpdates();
					sendBroadcast(new Intent(
					        Intent.ACTION_MEDIA_MOUNTED,
					        Uri.parse("file://" + Environment.getExternalStorageDirectory())));
					Log.i("FetchUpdates", snet.toString());
				}
		});
		mFriend = (Button) findViewById(R.id.friend);
		mFriend.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				String name = (String) mContacts.getSelectedItem();
				if(snet.addFriend(name)){
					mConsole.setText("Added " + name + " to friends.");

				}
			}
		});

		mUnfriend = (Button) findViewById(R.id.unfriend);
		mUnfriend.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				String name = (String) mContacts.getSelectedItem();
				if(snet.removeFriend(name)){
					mConsole.setText("Removed " + name + " from friends.");
				}
				
			}
		});
		
		
		
		
		
		
	}

}
