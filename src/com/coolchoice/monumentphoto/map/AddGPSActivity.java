package com.coolchoice.monumentphoto.map;

import java.util.List;

import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.OverlayManager;
import ru.yandex.yandexmapkit.map.MapLayer;
import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.overlay.balloon.BalloonItem;
import ru.yandex.yandexmapkit.overlay.balloon.OnBalloonListener;
import ru.yandex.yandexmapkit.overlay.drag.DragAndDropItem;
import ru.yandex.yandexmapkit.overlay.drag.DragAndDropOverlay;
import ru.yandex.yandexmapkit.overlay.location.MyLocationItem;
import ru.yandex.yandexmapkit.overlay.location.OnMyLocationListener;
import ru.yandex.yandexmapkit.utils.GeoPoint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.coolchoice.monumentphoto.AddObjectActivity;
import com.coolchoice.monumentphoto.R;
import com.coolchoice.monumentphoto.Settings;
import com.coolchoice.monumentphoto.data.GPS;
import com.coolchoice.monumentphoto.map.PointBalloonItem.OnRemoveOverlayItem;



public class AddGPSActivity extends Activity implements OnBalloonListener, OnMyLocationListener, OnRemoveOverlayItem, LocationListener {
    
	private MapView mMapView;
    private MapController mMapController;
    private OverlayManager mOverlayManager;
    private DragAndDropOverlay mOverlay;
    private ListView mGPSListView;
    private GPSListAdapter mGPSListAdapter;
    private ImageButton mAddGPSButton;
    private SlidingDrawer mSlidingDrawer;
    private ImageView mSligingImageView;
    private LinearLayout mLayersLayout;
    
    private static List<GPS> mGPSList = null;
    private static WaitGPSHandler mWaitGPSHandler;
    private static final float MAX_ZOOM = 17;
    
    private int mType, mId;
        
    int offsetX = 0;
    int offsetY = 0;
    
    private LocationManager locationManager;
    
    public static final String SLIDING_DRAWER_KEY = "sliding_drawer_key";
    public static final String GPS_LIST_KEY = "gps_list_key";
    public static boolean mIsSlidingDrawerOpen = false;
	private static int mIndexSelectedMapLayer = 0;
    
    private void initializeOffsetForDrag(){
    	Resources res = getResources();
        float density = getResources().getDisplayMetrics().density;
        int offsetX = (int)(-7 * density);
        int offsetY = (int)(20 * density); 
    }
    
    @Override
    public void onResume(){
        super.onResume();
        /*this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);*/
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        this.locationManager.removeUpdates(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);    	
    	this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);    	
    	this.mType = getIntent().getIntExtra(AddObjectActivity.EXTRA_TYPE, 0);
    	this.mId = getIntent().getIntExtra(AddObjectActivity.EXTRA_ID, 0);
        initializeOffsetForDrag();
        setContentView(R.layout.map_activity);
        mSlidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
        mSligingImageView = (ImageView) findViewById(R.id.handle);
        mGPSListView = (ListView) findViewById(R.id.lvGPS);
        mMapView = (MapView) findViewById(R.id.map);
        mLayersLayout = (LinearLayout) findViewById(R.id.llLayers);
        mMapView.showZoomButtons(true);
        mAddGPSButton = (ImageButton) findViewById(R.id.btnAddLocation);
        mMapController = mMapView.getMapController();
        mOverlayManager = mMapController.getOverlayManager();
        mOverlayManager.getMyLocation().setEnabled(true);
        
        if (mGPSList == null){
        	String gpsListString = getIntent().getStringExtra(GPS_LIST_KEY);
        	mGPSList = AddObjectActivity.parseGPSListString(gpsListString);
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
		
		mSlidingDrawer.setOnDrawerScrollListener(new  SlidingDrawer.OnDrawerScrollListener(){

			@Override
			public void onScrollEnded() {
				if(mSlidingDrawer.isOpened()){
					mSligingImageView.setImageDrawable(getResources().getDrawable(R.drawable.slider_left));
				} else {
					mSligingImageView.setImageDrawable(getResources().getDrawable(R.drawable.slider_right));
				}
			}

			@Override
			public void onScrollStarted() {
				
			}
		});	
		if(mIsSlidingDrawerOpen){
			mSlidingDrawer.open();
		}
		showLayerButtons();
    }
    
    private void showLayerButtons(){
    	List<MapLayer> mapLayers = mMapController.getListMapLayer();
    	int index = 0;
        for(MapLayer mapLayer : mapLayers)
        {
        	final MapLayer finalMapLayer = mapLayer;        	          
            Button btn = new Button(this);
            btn.setTag(index);
            btn.setText(mapLayer.name);
            btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            btn.setGravity(Gravity.CENTER);            
            btn.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent event) {					
					mMapController.setCurrentMapLayer(finalMapLayer);                                        
                    mIndexSelectedMapLayer = (Integer) view.getTag();
					return true;
				}
            });
            mLayersLayout.addView(btn);
            if(index == mIndexSelectedMapLayer){            	
        		mMapController.setCurrentMapLayer(finalMapLayer);        		
        	}  
            index++;
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState){
    	outState.putBoolean(SLIDING_DRAWER_KEY, mSlidingDrawer.isOpened());
    	mIsSlidingDrawerOpen = mSlidingDrawer.isOpened();
    	syncGPSCoordinatesWithOveralayItems();
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
            handleNewLocation(myLocationItem);
        }
    }
    
    @Override
    public void onLocationChanged(Location location) {
        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());        
        MyLocationItem myLocationItem = new MyLocationItem(geoPoint, this.getResources().getDrawable(R.drawable.ymk_find_me_drawable));        
        handleNewLocation(myLocationItem);
    }
    
    private void handleNewLocation(final MyLocationItem myLocationItem){        
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mWaitGPSHandler.isFindNewGPS()){
                    GPS gps = new GPS();
                    gps.Latitude = myLocationItem.getGeoPoint().getLat();
                    gps.Longitude = myLocationItem.getGeoPoint().getLon();
                    int nextOrdinalNumberGPS = mGPSList.size() + 1;
                    boolean isExistSuchGPS = false;
                    for(GPS g : mGPSList){
                        if(g.Latitude == gps.Latitude && g.Longitude == gps.Longitude){
                            isExistSuchGPS = true;
                        }
                        if(g.OrdinalNumber >= nextOrdinalNumberGPS){
                            nextOrdinalNumberGPS = g.OrdinalNumber + 1;
                        }
                    }
                    gps.OrdinalNumber = nextOrdinalNumberGPS;
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
    
    public void removeGPS(int position, CustomDragAndDropItem removedOverlayItem){
    	if(position < 0){
    		position = getPositionByOverlayItem(removedOverlayItem);    	    
    	}
    	this.mGPSListAdapter.removeItem(position);
		showObject();
    }
    
    private int getPositionByOverlayItem(CustomDragAndDropItem overlayItem){
    	int position = -1;    	
		for(int i = 0; i < this.mGPSList.size(); i++){
			GPS gps = this.mGPSList.get(i);
			if(gps == overlayItem.getGps()){
				position = i;
				break;
			}
		}
		return position;
    }

    public void showObject(){
    	syncGPSCoordinatesWithOveralayItems();
    	DragAndDropOverlay prevOverlay = this.mOverlay;    	
    	Resources res = getResources();
        this.mOverlay = new DragAndDropOverlay(mMapController);      
        for(GPS  gps : mGPSList){
        	BitmapDrawable marker = writeOnDrawable(R.drawable.map_point, gps.OrdinalNumber);
        	offsetY = marker.getBitmap().getHeight()/2;
	        CustomDragAndDropItem overlayItem = new CustomDragAndDropItem(new GeoPoint(gps.Latitude, gps.Longitude), res.getDrawable(R.drawable.map_point), gps);
	        overlayItem.setOffsetX(offsetX);
	        overlayItem.setOffsetY(offsetY);
	        overlayItem.setDragable(true);	        
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
    
    private void syncGPSCoordinatesWithOveralayItems(){
    	if(this.mOverlay != null){
    		for(Object obj : mOverlay.getOverlayItems()){
    			CustomDragAndDropItem overlayItem = (CustomDragAndDropItem) obj;
    			overlayItem.syncGPSWithGeoPoint();
    		}    		
    		
    	}
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
			syncGPSCoordinatesWithOveralayItems();
			Intent resultData = new Intent();
			resultData.putExtra(GPS_LIST_KEY, AddObjectActivity.GPSListToString(mGPSList));
			setResult(Activity.RESULT_OK, resultData);
			finish();
			break;
		case R.id.action_map_cancel:
			setResult(Activity.RESULT_CANCELED);
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
    	CustomDragAndDropItem overlayItem = (CustomDragAndDropItem) balloonItem.getOverlayItem();
    	overlayItem.syncGPSWithGeoPoint();
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
    	CustomDragAndDropItem overlayItem = (CustomDragAndDropItem) balloonItem.getOverlayItem();
    	removeGPS(-1, overlayItem);    			
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
            tvIndex.setText(String.format("%d.", mGPSList.get(position).OrdinalNumber));
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
			mProgressDialogWaitGPS.setOwnerActivity(AddGPSActivity.this);
			mProgressDialogWaitGPS.show();			
		}
		
				
	}

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
        
    }	
}