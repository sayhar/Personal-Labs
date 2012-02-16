package com.saharmassachi.labs.newfellow;

import static com.saharmassachi.labs.newfellow.Constants.MYID;
import static com.saharmassachi.labs.newfellow.Constants.PREFSNAME;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class FriendsMap extends MapActivity {

	protected MapView map; 
	int zoomLevel;
	GeoPoint center;
	MyLocationOverlay userLocationOverlay;
	List<Overlay> mapOverlays;
	ItemizedContactOverlay itemizedoverlay;
	private Drawable drawable;
	protected MapController controller;
	int streetView;
	boolean goToMyLocation;
	private DataHelper datahelper;
	private EditText etSearch;
	//ZoomPanListener zpl;
	//protected Handler handler = new Handler();
	
	
	/**
	 * Initializes Activity
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		etSearch = (EditText) findViewById(R.id.searchMap);
		//instantiates HappyData and creates an arraylist of all the bottles
		//HappyData datahelper = new HappyData(this);
		//ArrayList<HappyBottle> plottables = datahelper.getMyHistory();
		//initialize and display map view and user location
		initMapView();
		initOverlays();
		initMyLocation();
		goToMyLocation();
		streetView = 0;
		center = new GeoPoint(-1,-1);
		zoomLevel = map.getZoomLevel();
		datahelper = new DataHelper(this);
		makeDownloadThread();
		//check onResume for the place where we add more plots on the map.
		mapOverlays.add(itemizedoverlay);
		
		
		
		//temporary:
		/*GeoPoint point = new GeoPoint(19240000,-99120000);
		ContactOverlayItem overlayitem = new ContactOverlayItem(point, "Hola, Mundo!", "I'm in Mexico City!");
		itemizedoverlay.addToOverlay(overlayitem);
		mapOverlays.add(itemizedoverlay);*/
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	private void initOverlays(){
		mapOverlays = map.getOverlays();
		drawable = this.getResources().getDrawable(R.drawable.androidmarker);
		itemizedoverlay = new ItemizedContactOverlay(drawable, this);
	}
	
	//Starts tracking the users position on the map. 
	protected void initMyLocation() {
		userLocationOverlay = new MyLocationOverlay(this, map);
		userLocationOverlay.enableMyLocation();
		map.getOverlays().add(userLocationOverlay);  //adds the users location overlay to the overlays being displayed
	}
	
	//Finds and initializes the map view.
	protected void initMapView() {
		map = (MapView) findViewById(R.id.themap);
		controller = map.getController();
		map.setBuiltInZoomControls(false); //hides the default map zoom buttons so they don't interfere with the app buttons
		//checks streetView
		if (streetView == 1) {
			map.setStreetView(true);
			map.setSatellite(false);
		} else {
			map.setStreetView(false);
			map.setSatellite(true);	
		}
		map.invalidate();	

		/*//adds the sad and happy overlays to the map
		if (checkSad == 1)
			map.getOverlays().add(sadOverlay);
		if (checkHappy == 1) 
			map.getOverlays().add(happyOverlay);
		*/
	}
	
	
	protected boolean isMoved() {
		//Did the user move the map? 
		GeoPoint trueCenter =map.getMapCenter();
		int trueZoom = map.getZoomLevel();
		if(!((trueCenter.equals(center)) && (trueZoom == zoomLevel))){	
			return true;
		}else{
			return false;
		}}
	
	
	
	protected synchronized void overlayAdder(Contact[] toshow, ItemizedContactOverlay overlay){ 
		if (toshow == null) {return; }///THIS IS A PROBLEM AND SHOULD NEVER HAPPEN
		
		for(Contact contact : toshow) {
			
			String locationString = contact.getBase(); 
			int latitude = contact.getLat();
			int longitude = contact.getLong();
			GeoPoint point = new GeoPoint(latitude,longitude);
			String S = (contact.getName());
			long cid = contact.getCid(); 
			//I was worried a while about some contacts not having a cid. But that's SILLY! 
			//if you're on the map, then you must have a cid.
			overlay.addToOverlay(new ContactOverlayItem(point, S, locationString, cid));
		}
	}
	
	
	
	
	
	protected void goToMyLocation() {
		if (goToMyLocation == true) {
			userLocationOverlay.runOnFirstFix(new Runnable() {
				public void run() {
					// Zoom in to current location
					controller.animateTo(userLocationOverlay.getMyLocation());
					controller.setZoom(15); //sets the map zoom level to 15
				}
			});
		}
		map.getOverlays().add(userLocationOverlay); //adds the users location overlay to the overlays being displayed
	}

	/*
	//Our version of a listener - checks to see if the user moved.
	private class ZoomPanListener extends AsyncTask<Void, Void, Void>{
		@Override
		protected Void doInBackground(Void... params) {
			while(true){
				if(zoomLevel != map.getZoomLevel()) {
					handler.post(new Runnable(){
						@Override
						public void run(){
							mapClear();
							zoomLevel = map.getZoomLevel();}
								
					});	}
				
				if(isMoved() ){
					drawRecentLocal();
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}}}}
	
	*/
	
	public void mapSearch(View v){
		mapClear();
		String searchString = etSearch.getText().toString();
		Contact[] plottables;
		if (searchString.length() == 0){
			plottables = datahelper.getBasicContacts();
			overlayAdder(plottables, itemizedoverlay); //get rid of this when you uncomment the stuff below.
		}
		else{
			//TODO
			plottables = datahelper.search(searchString);
		}
		overlayAdder(plottables, itemizedoverlay);
		
		
	}
	
	protected void mapClear(){
		//stub - to be filled in later
		
		itemizedoverlay.emptyOverlay();
		/*happyOverlay.emptyOverlay();
		sadOverlay.emptyOverlay();
		filter.clear();*/
	}
	
	
	
	private void makeDownloadThread() {
		// TODO in the future this will not call h.getAllAttendees but a different method.
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				//datahelper.downPublic();
			}
		};
		new Thread(r).start();
	}
	
	
	
	//Disables MyLocation
	@Override
	protected void onPause() {
		super.onPause();
		userLocationOverlay.disableMyLocation();  
//		synchronized (zpl){
//			zpl.cancel(true);}
		super.onPause();
	}

	//Enables MyLocation
	@Override
	protected void onResume() {
		super.onResume();
		
		userLocationOverlay.enableMyLocation();
		
		SharedPreferences settings = getSharedPreferences(PREFSNAME, 0);
		if (!settings.contains(MYID)) {
		//	Intent i = new Intent(this, Login.class);
			//startActivity(i);
		}
		
		Contact[] plottables = datahelper.getBasicContacts();
		overlayAdder(plottables, itemizedoverlay);
		
	}

	public void goAddNew(View v){
    	//Intent i = new Intent(this, AddName.class);
		Intent i = new Intent(this, FilterAttendees.class);
    	startActivity(i);
    }

	public void seeContacts(View v){
		Intent i = new Intent(this, ViewContacts.class);
    	startActivity(i);
	}
	
	public void seeSched(View v){
		String url = "https://docs.google.com/spreadsheet/ccc?key=0ArpxecvCGBoMdDJXZEc5WGowNGJWaDRzQXJJa0d6bFE#gid=0";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}
}