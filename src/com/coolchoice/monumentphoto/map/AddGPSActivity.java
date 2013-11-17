package com.coolchoice.monumentphoto.map;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.data.*;
import com.coolchoice.monumentphoto.map.PointBalloonItem.OnRemoveOverlayItem;



import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolchoice.monumentphoto.AddObjectActivity;
import com.coolchoice.monumentphoto.R;
import com.coolchoice.monumentphoto.Settings;

import ru.yandex.yandexmapkit.*;
import ru.yandex.yandexmapkit.overlay.Overlay;
import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.overlay.balloon.BalloonItem;
import ru.yandex.yandexmapkit.overlay.balloon.OnBalloonListener;
import ru.yandex.yandexmapkit.overlay.drag.DragAndDropItem;
import ru.yandex.yandexmapkit.overlay.drag.DragAndDropOverlay;
import ru.yandex.yandexmapkit.overlay.location.MyLocationItem;
import ru.yandex.yandexmapkit.overlay.location.OnMyLocationListener;
import ru.yandex.yandexmapkit.utils.GeoPoint;



public class AddGPSActivity extends Activity implements OnBalloonListener, OnMyLocationListener, OnRemoveOverlayItem {
    
	private MapView mMapView;
    private MapController mMapController;
    private OverlayManager mOverlayManager;
    private DragAndDropOverlay mOverlay;
    private ListView mGPSListView;
    private GPSListAdapter mGPSListAdapter;
    private ImageButton mAddGPSButton;
    
    private static List<GPS> mGPSList = null;
    private static WaitGPSHandler mWaitGPSHandler;
    private static final float MAX_ZOOM = 17;
    
    private int mType, mId;
        
    int offsetX = 0;
    int offsetY = 0;
    
    private void initializeOffsetForDrag(){
    	Resources res = getResources();
        float density = getResources().getDisplayMetrics().density;
        int offsetX = (int)(-7 * density);
        int offsetY = (int)(20 * density); 
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	this.mType = getIntent().getIntExtra(AddObjectActivity.EXTRA_TYPE, 0);
    	this.mId = getIntent().getIntExtra(AddObjectActivity.EXTRA_ID, 0);
        initializeOffsetForDrag();
        setContentView(R.layout.map_activity);
        mGPSListView = (ListView) findViewById(R.id.lvGPS);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.showZoomButtons(true);
        mAddGPSButton = (ImageButton) findViewById(R.id.btnAddLocation);
        mMapController = mMapView.getMapController();
        mOverlayManager = mMapController.getOverlayManager();
        mOverlayManager.getMyLocation().setEnabled(true);
        
        if (mGPSList == null){
        	loadGPSListFromDB();        	
        }
        this.mGPSListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        	    mGPSListAdapter.setSelect(position);
        	    GPS gps = mGPSList.get(position);
        	    mMapController.setPositionAnimationTo(new GeoPoint(gps.Latitude, gps.Longitude));
        	    
        	}
		});
        this.mGPSListAdapter = new GPSListAdapter();
        this.mGPSListView.setAdapter(this.mGPSListAdapter);
        
        this.mAddGPSButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!Settings.checkWorkOfGPS(AddGPSActivity.this, null)){
					return;
				}
				mWaitGPSHandler.startWaitGPS();
			}
		});
        
        showObject();
        mOverlayManager.getMyLocation().addMyLocationListener(this);
        if(mWaitGPSHandler == null){
			mWaitGPSHandler = new WaitGPSHandler(this); 
		}        
		mWaitGPSHandler.checkWaitGPS(this);
		setMapCenterToLastPoint();    	
    }
    
    private void loadGPSListFromDB(){
		List<GPS> dbGPSList = new ArrayList<GPS>();		
		switch(mType){
		case AddObjectActivity.ADD_CEMETERY:
			List<GPSCemetery> tempGPSCemeteryList = DB.dao(GPSCemetery.class).queryForEq("Cemetery_id", mId);
			for(GPSCemetery gpsCemetery: tempGPSCemeteryList){
				dbGPSList.add(gpsCemetery);
			}
			break;
		case AddObjectActivity.ADD_REGION:
			List<GPSRegion> tempGPSRegionList = DB.dao(GPSRegion.class).queryForEq("Region_id", mId);
			for(GPSRegion gpsRegion: tempGPSRegionList){
				dbGPSList.add(gpsRegion);
			}
			break;		
		case AddObjectActivity.ADD_ROW:
			List<GPSRow> tempGPSRowList = DB.dao(GPSRow.class).queryForEq("Row_id", mId);
			for(GPSRow gpsRow: tempGPSRowList){
				dbGPSList.add(gpsRow);
			}
			break;			
		case AddObjectActivity.ADD_PLACE_WITHOUTROW:
			List<GPSPlace> tempGPSPlaceList1 = DB.dao(GPSPlace.class).queryForEq("Place_id", mId);
			for(GPSPlace gpsPlace: tempGPSPlaceList1){
				dbGPSList.add(gpsPlace);
			}				
			break;
		case AddObjectActivity.ADD_PLACE_WITHROW:
			List<GPSPlace> tempGPSPlaceList2 = DB.dao(GPSPlace.class).queryForEq("Place_id", mId);
			for(GPSPlace gpsPlace: tempGPSPlaceList2){
				dbGPSList.add(gpsPlace);
			}	
			break;
		case AddObjectActivity.ADD_GRAVE_WITHOUTROW:
			List<GPSGrave> tempGPSGraveList1 = DB.dao(GPSGrave.class).queryForEq("Grave_id", mId);
			for(GPSGrave gpsGrave: tempGPSGraveList1){
				dbGPSList.add(gpsGrave);
			}				
			break;
		case AddObjectActivity.ADD_GRAVE_WITHROW:
			List<GPSGrave> tempGPSGraveList2 = DB.dao(GPSGrave.class).queryForEq("Grave_id", mId);
			for(GPSGrave gpsGrave: tempGPSGraveList2){
				dbGPSList.add(gpsGrave);
			}				
			break;
		default:
			break;
		}		
		mGPSList  = new ArrayList<GPS>();
		for(GPS dbGPS : dbGPSList){
			mGPSList.add(dbGPS);
		}			
	}
    
    private void setMapCenterToLastPoint(){
        List<OverlayItem> list = mOverlay.getOverlayItems();
        if(list.size() > 0){
        	GeoPoint geoPoint = list.get(list.size()-1).getGeoPoint();
        	mMapController.setPositionAnimationTo(geoPoint, MAX_ZOOM);
        }        
    }
    
    @Override
    public void onMyLocationChange(final MyLocationItem myLocationItem) {
    	if(myLocationItem.getType() == MyLocationItem.GPS){
    		runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	            	if(mWaitGPSHandler.isFindNewGPS()){
	            		GPS gps = new GPS();
						gps.Latitude = myLocationItem.getGeoPoint().getLat();
						gps.Longitude = myLocationItem.getGeoPoint().getLon();
						boolean isExistSuchGPS = false;
						for(GPS g : mGPSList){
							if(g.Latitude == gps.Latitude && g.Longitude == gps.Longitude){
								isExistSuchGPS = true;
							}
						}
						if(!isExistSuchGPS){
							addGPS(gps);
							mMapController.setPositionAnimationTo(myLocationItem.getGeoPoint());
						} else {
							Toast.makeText(AddGPSActivity.this, "Полученная GPS координата уже была добавлена", Toast.LENGTH_LONG ).show();
						}						
	        		}	            	                
	            }			
				
	        });
    	}
        
    }
    
    @Override
    public void onResume(){
    	super.onResume();    	
    }
    
    @Override
	protected void onDestroy(){
		super.onDestroy();
		if(isFinishing()){
			mGPSList.clear();
			mGPSList = null;
		}
	}
    
    public void addGPS(GPS gps){
    	int count = this.mGPSList.size();
    	this.mGPSList.add(gps);
    	this.mGPSListAdapter.notifyDataSetChanged();
    	showObject();
    }
    
    public void removeGPS(int position, OverlayItem removedOverlayItem){
    	if(position < 0){
    		position = getPositionByOverlayItem(removedOverlayItem);    	    
    	}
    	this.mGPSListAdapter.removeItem(position);
		showObject();
    }
    
    private int getPositionByOverlayItem(OverlayItem overlayItem){
    	int position = -1;
    	GeoPoint geoPoint = overlayItem.getGeoPoint();
		for(int i = 0; i < this.mGPSList.size(); i++){
			GPS gps = this.mGPSList.get(i);
			if(geoPoint.getLat() == gps.Latitude && geoPoint.getLon() == gps.Longitude){
				position = i;
				break;
			}
		}
		return position;
    }

    public void showObject(){
    	DragAndDropOverlay prevOverlay = this.mOverlay;
    	Resources res = getResources();
        this.mOverlay = new DragAndDropOverlay(mMapController);
        int i = 1;
        for(GPS  gps : mGPSList){
	        DragAndDropItem overlayItem = new DragAndDropItem(new GeoPoint(gps.Latitude, gps.Longitude), res.getDrawable(R.drawable.map_point));
	        overlayItem.setOffsetX(offsetX);
	        overlayItem.setOffsetY(offsetY);
	        overlayItem.setDragable(false);
	        BitmapDrawable marker = writeOnDrawable(R.drawable.map_point, i++);
	        overlayItem.setDrawable(marker);
	        
	        
	        PointBalloonItem balloonItem = new PointBalloonItem(this, overlayItem.getGeoPoint());
	        balloonItem.setOffsetX(offsetX);
	        balloonItem.setOnBalloonListener(this);
	        balloonItem.setVisible(true);
	        balloonItem.setOnRemoveOverlayItem(this);
	        overlayItem.setBalloonItem(balloonItem);	          
	        this.mOverlay.addOverlayItem(overlayItem);
        }
        if(prevOverlay != null){
        	mOverlayManager.removeOverlay(prevOverlay);
        }
        mOverlayManager.addOverlay(mOverlay);
        mMapController.notifyRepaint();

    }
    
    public BitmapDrawable writeOnDrawable(int drawableId, int index){
        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);
        Paint paint = new Paint(); 
        paint.setStyle(Style.FILL); 
        paint.setColor(Color.BLACK); 
        paint.setTextSize(20);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        Canvas canvas = new Canvas(bm);
        String text = Integer.toString(index);
        int countChar = text.length();
        canvas.drawText(text, bm.getWidth()/2 - (int)(5.5 * countChar) , bm.getHeight()/2, paint);
        return new BitmapDrawable(bm);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_map, menu);		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    super.onOptionsItemSelected(item);
	    switch (item.getItemId()) {
		case R.id.action_map_list:
			int visibility  = this.mGPSListView.getVisibility();
			if(visibility == View.VISIBLE){
				this.mGPSListView.setVisibility(View.GONE);
			} else {
				this.mGPSListView.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.action_map_save:
			saveGPSToDB();
			finish();
			break;
		case R.id.action_map_cancel:
			finish();
			break;
		}	    
	    return true;
	}
	
	@Override
	public void onBalloonViewClick(BalloonItem arg0, View arg1) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void onBalloonShow(BalloonItem balloonItem) {
    	OverlayItem overlayItem = balloonItem.getOverlayItem();
    	int position = getPositionByOverlayItem(overlayItem);
    	((GPSListAdapter)this.mGPSListView.getAdapter()).setSelect(position);    	  	   	
    }

    @Override
    public void onBalloonHide(BalloonItem balloonItem) {
    	// TODO Auto-generated method stub
    }

    @Override
    public void onBalloonAnimationStart(BalloonItem balloonItem) {
    	// TODO Auto-generated method stub
    }

    @Override
    public void onBalloonAnimationEnd(BalloonItem balloonItem) {
        // TODO Auto-generated method stub
    }
    
    @Override
	public void onRemoveOverlayItem(PointBalloonItem balloonItem) {
    	OverlayItem overlayItem = balloonItem.getOverlayItem();
    	GeoPoint geoPoint = overlayItem.getGeoPoint();
    	removeGPS(-1, balloonItem);    			
	}
    
    private void saveGPSToDB(){
    	switch (mType) {
		case AddObjectActivity.ADD_CEMETERY:
			Cemetery cemetery = DB.dao(Cemetery.class).queryForId(mId);
			saveGPSCemetery(cemetery);
			break;
		case AddObjectActivity.ADD_REGION:
			Region region = DB.dao(Region.class).queryForId(mId);
			saveGPSRegion(region);
			break;		
		case AddObjectActivity.ADD_ROW:
			Row row = DB.dao(Row.class).queryForId(mId);
			saveGPSRow(row);
			break;			
		case AddObjectActivity.ADD_PLACE_WITHOUTROW:
		case AddObjectActivity.ADD_PLACE_WITHROW:
			Place place = DB.dao(Place.class).queryForId(mId);
			saveGPSPlace(place);
			break;		
		case AddObjectActivity.ADD_GRAVE_WITHOUTROW:
		case AddObjectActivity.ADD_GRAVE_WITHROW:
			Grave grave = DB.dao(Grave.class).queryForId(mId);
			saveGPSGrave(grave);
			break;
		default:
			break;
		}
    }
    
    private void saveGPSCemetery(Cemetery cemetery){
    	List<GPSCemetery> deletedGPS = DB.dao(GPSCemetery.class).queryForEq("Cemetery_id", cemetery.Id);
		DB.dao(GPSCemetery.class).delete(deletedGPS);
		for(GPS gps : mGPSList){
			GPSCemetery gpsCemetery = new GPSCemetery();
			gpsCemetery.Latitude = gps.Latitude;
			gpsCemetery.Longitude = gps.Longitude;
			gpsCemetery.Cemetery = cemetery;
			DB.dao(GPSCemetery.class).create(gpsCemetery);
		}
	}
	private void saveGPSRegion(Region region){
		List<GPSRegion> deletedGPS = DB.dao(GPSRegion.class).queryForEq("Region_id", region.Id);
		DB.dao(GPSRegion.class).delete(deletedGPS);
		for(GPS gps : mGPSList){
			GPSRegion gpsRegion = new GPSRegion();
			gpsRegion.Latitude = gps.Latitude;
			gpsRegion.Longitude = gps.Longitude;
			gpsRegion.Region = region;
			DB.dao(GPSRegion.class).create(gpsRegion);
		}
	}
	
	private void saveGPSRow(Row row){
		List<GPSRow> deletedGPS = DB.dao(GPSRow.class).queryForEq("Row_id", row.Id);
		DB.dao(GPSRow.class).delete(deletedGPS);
		for(GPS gps : mGPSList){
			GPSRow gpsRow = new GPSRow();
			gpsRow.Latitude = gps.Latitude;
			gpsRow.Longitude = gps.Longitude;
			gpsRow.Row = row;
			DB.dao(GPSRow.class).create(gpsRow);
		}
	}
	
	private void saveGPSPlace(Place place){
		List<GPSPlace> deletedGPS = DB.dao(GPSPlace.class).queryForEq("Place_id", place.Id);
		DB.dao(GPSPlace.class).delete(deletedGPS);
		for(GPS gps : mGPSList){
			GPSPlace gpsPlace = new GPSPlace();
			gpsPlace.Latitude = gps.Latitude;
			gpsPlace.Longitude = gps.Longitude;
			gpsPlace.Place = place;
			DB.dao(GPSPlace.class).create(gpsPlace);
		}
	}
	
	private void saveGPSGrave(Grave grave){
		List<GPSGrave> deletedGPS = DB.dao(GPSGrave.class).queryForEq("Grave_id", grave.Id);
		DB.dao(GPSGrave.class).delete(deletedGPS);
		for(GPS gps : mGPSList){
			GPSGrave gpsGrave = new GPSGrave();
			gpsGrave.Latitude = gps.Latitude;
			gpsGrave.Longitude = gps.Longitude;
			gpsGrave.Grave = grave;
			DB.dao(GPSGrave.class).create(gpsGrave);
		}
	}
    
    public class GPSListAdapter extends BaseAdapter {
		
    	private int mSelectedPosition = -1;
    	
        public GPSListAdapter() {
        	
        }
        
        public void setSelect(int pos){
        	this.mSelectedPosition = pos;
        	this.notifyDataSetChanged();
        }
        
        public void removeItem(int pos){
        	if(pos == mSelectedPosition){
        		mSelectedPosition = -1;
        	} else {
        		if(pos < mSelectedPosition){
        			mSelectedPosition--;
        		}
        	}
        	mGPSList.remove(pos);
        	this.notifyDataSetChanged();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
            	LayoutInflater inflater = (LayoutInflater) AddGPSActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.map_gps_list_item, parent, false);
            }
            Log.i("pos=", Integer.toString(position));
            if(position == this.mSelectedPosition){
            	convertView.setSelected(true);
            	convertView.setBackgroundResource(R.drawable.list_selector);
            } else {
            	convertView.setSelected(false);
            	convertView.setBackgroundDrawable(null);
            }
            TextView tvIndex = (TextView) convertView.findViewById(R.id.tvIndex);
            TextView tvGPS = (TextView) convertView.findViewById(R.id.tvGPS);
            ImageButton btnRemoveGPS = (ImageButton) convertView.findViewById(R.id.btnRemoveGPS);
            tvIndex.setText(String.format("%d.", position + 1));
            btnRemoveGPS.setTag(position);
            double lat = mGPSList.get(position).Latitude;
        	double lng = mGPSList.get(position).Longitude;
        	
        	String gpsString = String.format("%s, %s", Location.convert(lat, Location.FORMAT_SECONDS), Location.convert(lng, Location.FORMAT_SECONDS) );
        	tvGPS.setText(gpsString);
        	btnRemoveGPS.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					for(Object obj : mOverlay.getOverlayItems()){
						OverlayItem overlayItem = (OverlayItem) obj;
						if(overlayItem.getBalloonItem() != null){
							overlayItem.getBalloonItem().setVisible(false);
						}
					}
					int pos = (Integer) v.getTag();
					removeGPS(pos, null);					
				}
			});            
            return convertView;
        }

		@Override
		public int getCount() {
			return mGPSList.size();
		}

		@Override
		public Object getItem(int position) {
			return mGPSList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}	
		
    }
    
    class WaitGPSHandler {

		private Context mContext;
		private ProgressDialog mProgressDialogWaitGPS;
		private boolean isWaitGPS = false;
		private String mProgressDialogTitle;
		private String mProgressDialogMessage;
		
		public WaitGPSHandler(Context context){
			setContext(context);
		}
		
		public void setContext(Context context){
			this.mContext = context;
		}
		
		public void startWaitGPS(){
			if(isWaitGPS) return;
			isWaitGPS = true;
			mProgressDialogTitle = "Получение GPS координаты";
			mProgressDialogMessage = "Подождите...";
			createAndShowProgressDialog();			
		}
		
		public boolean isFindNewGPS(){
			if(isWaitGPS){
				this.isWaitGPS = false;
				mProgressDialogWaitGPS.dismiss();
				return true;
			} else {
				return false;
			}
		}
		
		
		public void checkWaitGPS(Context context){
			setContext(context);
			if(isWaitGPS){				
				createAndShowProgressDialog();
			}
		}
		
		private void createAndShowProgressDialog(){
			mProgressDialogWaitGPS =  new ProgressDialog(this.mContext);
			mProgressDialogWaitGPS.setTitle(mProgressDialogTitle);
			mProgressDialogWaitGPS.setMessage(mProgressDialogMessage);
			mProgressDialogWaitGPS.setCancelable(false);
			mProgressDialogWaitGPS.setButton(DialogInterface.BUTTON_NEGATIVE, "Отменить", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			    	isWaitGPS = false;
			        dialog.dismiss();
			    }
			});
			mProgressDialogWaitGPS.show();
		}
		
				
	}	
}