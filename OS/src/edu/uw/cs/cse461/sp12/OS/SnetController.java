package edu.uw.cs.cse461.sp12.OS;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.cs.cse461.sp12.DB461.DB461.DB461Exception;
import edu.uw.cs.cse461.sp12.OS.SNetDB461.CommunityRecord;
import edu.uw.cs.cse461.sp12.OS.SNetDB461.PhotoRecord;



public class SnetController extends RPCCallable {
	
	//TODO friends
	private SNetDB461 db;
	
	public SnetController() {
		try {
			db = new SNetDB461();
		} catch (DB461Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public String servicename() {
		return "snet";
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
	public JSONObject _fetchUpdates(JSONObject args){
		return null;
	}
	public JSONObject _fetchPhoto(JSONObject args){
		return null;
	}
	
	public JSONArray fetchUpdates(String name) {
		//TODO Make neededphotos
		
		try {
			DDNSRRecord rr = ((DDNSResolverService)OS.getService("ddnsresolver")).resolve(name);
			RPCCallerSocket sock = new RPCCallerSocket(rr.host, rr.host, rr.port);
			JSONObject request = new JSONObject();
			//TODO fill out request: {community:{MemberField,...}, needphotos:[int, ...]}
			//Put community table into JSON, Make neededphotos
			JSONObject response = sock.invoke("snet", "fetchUpdates", request);
			JSONObject commUpdates = response.getJSONObject("communityupdates");
			
			for(Iterator<String> it = commUpdates.keys(); it.hasNext();) {
				String key = it.next();
				JSONObject memberUpdate = commUpdates.getJSONObject(key);
				CommunityRecord cr = db.COMMUNITYTABLE.readOne(key);
				if(cr.generation < memberUpdate.getInt("generation")) {
					changeRef(cr.myPhotoHash, -1);
					changeRef(cr.chosenPhotoHash, -1);
					int myHash = memberUpdate.getInt("myphotohash");
					int chosenHash = memberUpdate.getInt("chosenphotohash");
					cr.generation = memberUpdate.getInt("generation");
					cr.myPhotoHash = myHash;
					cr.myPhotoHash = chosenHash;
					newPhoto(myHash);
					newPhoto(chosenHash);
					db.COMMUNITYTABLE.write(cr);
				}
			}
			
			return response.getJSONArray("photoupdates");
		} catch (DDNSException e) {
			// TODO failed ddns for whatever reason
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DB461Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private void changeRef(int hash, int dif) throws DB461Exception {
		PhotoRecord pr = db.PHOTOTABLE.readOne(hash);
		pr.refCount += dif;
		db.PHOTOTABLE.write(pr);
	}
	
	private void newPhoto(int hash) throws DB461Exception {
		PhotoRecord pr = db.PHOTOTABLE.readOne(hash);
		if(pr == null) {
			pr = db.PHOTOTABLE.createRecord();
			pr.hash = hash;
			pr.refCount = 1;
			pr.file = null;
		} else {
			changeRef(hash, 1);
		}
	}
	
	public List<File> getUnusedPhotos(){
		try{
			List<File> result = new LinkedList<File>();
			for(PhotoRecord pr : db.PHOTOTABLE.readAll()) {
				if(pr.refCount == 0) {
					result.add(pr.file);
				}
				db.PHOTOTABLE.delete(pr.hash);
			}
			return result;
		} catch (DB461Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] fetchPhoto(String name, int photoHash) {
		DDNSRRecord rr;
		try {
			rr = ((DDNSResolverService)OS.getService("ddnsresolver")).resolve(name);
			RPCCallerSocket sock = new RPCCallerSocket(rr.host, rr.host, rr.port);
			JSONObject request = new JSONObject();
		} catch (DDNSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void setPhotoLocation(int hash, File location) {
		try {
			PhotoRecord pr = db.PHOTOTABLE.readOne(hash);
			pr.file = location;
			db.PHOTOTABLE.write(pr);
		} catch (DB461Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	public List<String> getOnlineUsers() {
//		List<String> names = new LinkedList<String>();
//		
//		return names;
//	}
	
}
