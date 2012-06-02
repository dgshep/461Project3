package edu.uw.cs.cse461.sp12.OS;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import edu.uw.cs.cse461.sp12.DB461.DB461.DB461Exception;
import edu.uw.cs.cse461.sp12.DB461.DB461.Record;
import edu.uw.cs.cse461.sp12.DB461.DB461.RecordSet;
import edu.uw.cs.cse461.sp12.OS.RPCCallable.RPCCallableMethod;
import edu.uw.cs.cse461.sp12.OS.SNetDB461.CommunityRecord;
import edu.uw.cs.cse461.sp12.OS.SNetDB461.Photo;
import edu.uw.cs.cse461.sp12.OS.SNetDB461.PhotoRecord;
import edu.uw.cs.cse461.sp12.util.Base64;



public class SNetController extends RPCCallable {
	private SNetDB461 db;
	
	private RPCCallableMethod<SNetController> fetchUpdates;
	private RPCCallableMethod<SNetController> fetchPhoto;
	
	public SNetController() throws Exception {
		fetchUpdates = new RPCCallableMethod<SNetController>(this, "_fetchUpdates");
		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "fetchUpdates", fetchUpdates);
		fetchPhoto = new RPCCallableMethod<SNetController>(this, "_fetchPhoto");
		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "unregister", fetchPhoto);
		db = new SNetDB461();
		if(!db.dbExists()) db.openOrCreateDatabase();
		db.discard();
		
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
		try {
			db = new SNetDB461();
			JSONObject community;
			JSONArray needPhotos;
			needPhotos = args.getJSONArray("needPhotos");
			community = args.getJSONObject("community");
			JSONObject out = new JSONObject();
			RecordSet<CommunityRecord> oRecVec;
			oRecVec = toRecordSet(community);

			/** Process Community Updates **/
			JSONObject communityUpdates = new JSONObject();
			for(CommunityRecord oR: oRecVec){
				CommunityRecord mR = db.COMMUNITYTABLE.readOne(oR.name);
				if(mR != null){
					if(mR.generation > oR.generation){ // Checks to see if my data is more up to date
						JSONObject newRec = toJSONResponse(mR);
						communityUpdates.put(oR.name, newRec);
					} else if(mR.generation < oR.generation){
						//oR.isFriend = mR.isFriend;
						//db.COMMUNITYTABLE.write(oR); //Updates own table with more up to date data;
					}
				}
			}
			
			/**Process Photo Updates **/
			JSONArray photoUpdates = new JSONArray();
			for(int i = 0; i < needPhotos.length(); i++){
				PhotoRecord pr = db.PHOTOTABLE.readOne(needPhotos.getInt(i));
				if(pr != null) photoUpdates.put(pr.hash);
			}
			out.put("communityupdates", communityUpdates);
			out.put("photoupdates", photoUpdates);
			return out;
		} catch (DB461Exception e1) {
			e1.printStackTrace();
		} catch (JSONException je) {
			je.printStackTrace();
		} finally {
			   if ( db != null ) db.discard();
		}
		return new JSONObject();

	}
	public JSONObject _fetchPhoto(JSONObject args){
		try {
			db.openOrCreateDatabase();
			int photoHash = args.getInt("photohash");
			PhotoRecord pr = db.PHOTOTABLE.readOne(args.getInt("photohash"));
			if(pr != null){
				JSONObject out = new JSONObject();
				out.put("photohash", photoHash);
				out.put("photodata", Base64.encodeFromFile(pr.file.getAbsolutePath()));
				return out;
			} else {
				//TODO: Return Exception
				return null;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;

		} catch (DB461Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;

		} finally {
			if(db != null) db.discard();
		}
		
	}
	
	private RecordSet<CommunityRecord> toRecordSet(JSONObject o) throws JSONException{
		Iterator<String> names = o.keys();
		RecordSet<CommunityRecord> out = new RecordSet<CommunityRecord>();
		while(names.hasNext()){
			String name = names.next();
			JSONObject rec = o.getJSONObject(name);
			CommunityRecord r = db.createCommunityRecord();
			r.name = name;           
			r.generation = rec.getInt("generation");     
			r.myPhotoHash = rec.getInt("myphotohash");    
			r.chosenPhotoHash = rec.getInt("chosenphotohash");
			out.add(r);
		}
		return out;
	}
	private JSONObject toJSONResponse(CommunityRecord r) throws JSONException{
		JSONObject newRec = new JSONObject();
		newRec.put("generation", r.generation);
		newRec.put("chosenphotohash", r.chosenPhotoHash);
		newRec.put("myphotohash", r.myPhotoHash);
		return newRec;
	}
	
	
	
	

}
