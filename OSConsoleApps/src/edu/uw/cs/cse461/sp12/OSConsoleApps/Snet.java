package edu.uw.cs.cse461.sp12.OSConsoleApps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONArray;


import edu.uw.cs.cse461.sp12.OS.DDNSFullName;
import edu.uw.cs.cse461.sp12.OS.OS;
import edu.uw.cs.cse461.sp12.OS.SNetDB461.CommunityRecord;
import edu.uw.cs.cse461.sp12.OS.SNetDB461.Photo;
import edu.uw.cs.cse461.sp12.OS.SnetController;
import edu.uw.cs.cse461.sp12.util.Log;

public class Snet implements OSConsoleApp {
	private static String TAG = "Snet";
	@Override
	public String appname() {
		return "snet";
	}

	@Override
	public void run() throws Exception {
		//fetchUpdates from root
		//fetchUpdates from all online
		//Find all photos I don't have
		//all fetchUpdates again with neededPhotos
		//fetchPhotos
		//
		
		try {
			// Eclipse doesn't support System.console()
			SnetController snet = (SnetController) OS.getService("snet");
			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("This is console Snet at your service!");
			String[] photoList = null;
			File photos = null;
			while ( photoList == null ){
				System.out.print("Please enter a base photo directory(relative): ");
				String dir = console.readLine();
				photos = new File(dir.toLowerCase());
				snet.setPhotoDirectory(photos);
				photoList = photos.list();
				if(photoList == null){
					System.out.println("Invalid directory");
					continue;
				}
				System.out.println("Photos found:");
				for(String s : photoList) System.out.println(s);
			}
			while ( true ) {
				try {
					System.out.print("Enter a command or exit to quit: ");
					String command = console.readLine();
					if	(command.toLowerCase().equals("exit")) break;
					if (command.toLowerCase().equals("fetchupdates")){
						System.out.print("Enter all to fetch updates from all users or a specific name: ");
						command = console.readLine();
						if(command.toLowerCase().equals("all"))
							snet.allUpdates();
						else
							snet.fetchUpdates(command, false);
						System.out.println(snet.toString());
						continue;
					}
					if (command.toLowerCase().equals("community")) {
						System.out.println(snet.toString());
						continue;
					}
					if(command.toLowerCase().equals("setmyphoto")){
						snet.setMyPhoto(getPhoto(console));
					}
					if(command.toLowerCase().equals("setchosenphoto")){
						snet.setChosenPhoto(getPhoto(console));
					}
					if(command.toLowerCase().equals("fix")){
						snet.fix();
					}
					if(command.toLowerCase().equals("fetchphoto")){
						System.out.print("Enter name: ");
						command = console.readLine();
						CommunityRecord r = snet.getRecord(new DDNSFullName(command).toString());
						if(r == null) continue;
						int myChosenHash = r.chosenPhotoHash;
						snet.fetchPhoto(command, myChosenHash);
					}
					if(command.toLowerCase().equals("addfriend")){
						System.out.print("Enter name: ");
						command = console.readLine();
						snet.addFriend(new DDNSFullName(command).toString());
					}
					
				} catch (Exception e) {
					System.out.println("Exception: " + e.getMessage());
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "SnetConsole.run() caught exception: " +e.getMessage());
		}
	}

	@Override
	public void shutdown() throws Exception {
		// TODO Auto-generated method stub
		
	}
	public class OnlyExt implements FilenameFilter { 
		String ext; 
		public OnlyExt(String ext) { 
			this.ext = "." + ext; 
		} 
		public boolean accept(File dir, String name) { 
			return name.endsWith(ext); 
		} 
	}
	private Photo getPhoto(BufferedReader console) throws IOException{
		System.out.print("Enter filename: ");
		String command = console.readLine();
		File p = new File(command);
		return new Photo(p);
	}

}
