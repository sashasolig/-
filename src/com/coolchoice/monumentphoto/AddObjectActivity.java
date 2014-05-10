package com.coolchoice.monumentphoto;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ru.yandex.yandexmapkit.utils.Utils;

import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.data.BaseDTO;
import com.coolchoice.monumentphoto.data.Cemetery;
import com.coolchoice.monumentphoto.data.ComplexGrave;
import com.coolchoice.monumentphoto.data.GPS;
import com.coolchoice.monumentphoto.data.GPSCemetery;
import com.coolchoice.monumentphoto.data.GPSGrave;
import com.coolchoice.monumentphoto.data.GPSPlace;
import com.coolchoice.monumentphoto.data.GPSRegion;
import com.coolchoice.monumentphoto.data.GPSRow;
import com.coolchoice.monumentphoto.data.Grave;
import com.coolchoice.monumentphoto.data.Place;
import com.coolchoice.monumentphoto.data.Region;
import com.coolchoice.monumentphoto.data.Row;
import com.coolchoice.monumentphoto.map.AddGPSActivity;
import com.coolchoice.monumentphoto.task.BaseTask;
import com.coolchoice.monumentphoto.util.FindingSquare;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AddObjectActivity extends Activity {
	
	public static final String EXTRA_TYPE = "extra_type";
	public static final String EXTRA_ID = "extra_id";
	public static final String EXTRA_PARENT_ID = "extra_parent_id";
	public static final String EXTRA_EDIT = "extra_edit";
	
	public static final int MASK_CEMETERY = 1;
	public static final int MASK_REGION = 2;
	public static final int MASK_ROW = 4;
	public static final int MASK_PLACE = 8;
	public static final int MASK_GRAVE = 16;
	
	public static final int ADD_CEMETERY = MASK_CEMETERY;
	public static final int ADD_REGION = MASK_CEMETERY | MASK_REGION;
	public static final int ADD_ROW = MASK_CEMETERY | MASK_REGION | MASK_ROW;
	public static final int ADD_PLACE_WITHROW = MASK_CEMETERY | MASK_REGION | MASK_ROW | MASK_PLACE;
	public static final int ADD_PLACE_WITHOUTROW = MASK_CEMETERY | MASK_REGION | MASK_PLACE;
	public static final int ADD_GRAVE_WITHROW = MASK_CEMETERY | MASK_REGION | MASK_ROW | MASK_PLACE | MASK_GRAVE;
	public static final int ADD_GRAVE_WITHOUTROW = MASK_CEMETERY | MASK_REGION | MASK_PLACE | MASK_GRAVE;
	public static final String CEMETERY_SQUARE_KEY = "CEMETERY_SQUARE_KEY";
	public static final String REGION_SQUARE_KEY = "REGION_SQUARE_KEY";
		
	private EditText etCemetery, etRegion, etRow, etPlace, etGrave, etOldPlace, etPlaceLength, etPlaceWidth;
	
	private CheckBox cbPlaceUnowned, cbPlaceSizeVioleted, cbPlaceUnindentified, cbPlaceWrongFIO, cbPlaceMilitary;
	
	private TextView tvCemeterySquare, tvRegionSquare;
	private Double mCemeterySquare = null, mRegionSquare = null;
		
	//private CheckBox cbIsGraveWrongFIO, cbIsGraveMilitary;
	
	private LinearLayout llCemetery, llRegion, llRow, llPlace, llGrave;
	
	private Button btnNewToOldPlace, btnFindOldPlace;
	
	private LinearLayout llOldPlace;
		
	private Button btnAddGPS;
			
	private Button btnCancel, btnSave;
	
	private int mType, mId, mParentId;
	
	private boolean mIsEdit;
	
	public static final int ADD_GPS_ACTIVITY_REQUEST_CODE = 1;
	
	private static String mGPSListString = "";
	private static boolean mIsStoreGPS = false;
	
	protected final Logger mFileLog = Logger.getLogger(AddObjectActivity.class);
          	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null){
			if(savedInstanceState.containsKey(CEMETERY_SQUARE_KEY)){
				this.mCemeterySquare = savedInstanceState.getDouble(CEMETERY_SQUARE_KEY);
			}
			if(savedInstanceState.containsKey(REGION_SQUARE_KEY)){
				this.mRegionSquare = savedInstanceState.getDouble(REGION_SQUARE_KEY);
			}
		}
		setContentView(R.layout.add_object_activity);
		this.mType = getIntent().getExtras().getInt(EXTRA_TYPE);
		this.mId = getIntent().getExtras().getInt(EXTRA_ID, -1);
		this.mParentId = getIntent().getExtras().getInt(EXTRA_PARENT_ID, -1);
		this.mIsEdit = getIntent().getExtras().getBoolean(EXTRA_EDIT, false);
		this.etCemetery = (EditText) findViewById(R.id.etCemetery);
		this.etRegion = (EditText) findViewById(R.id.etRegion);
		this.etRow = (EditText) findViewById(R.id.etRow);
		this.etPlace = (EditText) findViewById(R.id.etPlace);
		this.etOldPlace = (EditText) findViewById(R.id.etOldPlace);
		this.etPlaceLength = (EditText) findViewById(R.id.etPlaceLength);
		this.etPlaceWidth = (EditText) findViewById(R.id.etPlaceWidth);
		this.btnFindOldPlace = (Button) findViewById(R.id.btnFindOldPlace);
		
		this.cbPlaceUnowned = (CheckBox) findViewById(R.id.cb_place_is_unowner);
		this.cbPlaceMilitary = (CheckBox) findViewById(R.id.cb_place_is_military);
		this.cbPlaceWrongFIO = (CheckBox) findViewById(R.id.cb_place_is_wrong_fio);
		this.cbPlaceSizeVioleted = (CheckBox) findViewById(R.id.cb_place_is_size_violated);
		this.cbPlaceUnindentified = (CheckBox) findViewById(R.id.cb_place_is_unindentified);
		
		this.etGrave = (EditText) findViewById(R.id.etGrave);
		this.llCemetery = (LinearLayout) findViewById(R.id.llCemetery);
		this.llRegion = (LinearLayout) findViewById(R.id.llRegion);
		this.llRow = (LinearLayout) findViewById(R.id.llRow);
		this.llPlace = (LinearLayout) findViewById(R.id.llPlace);
		this.llGrave = (LinearLayout) findViewById(R.id.llGrave);
		this.btnSave = (Button) findViewById(R.id.btnSave);
		this.btnCancel = (Button) findViewById(R.id.btnCancel);
		
		this.llOldPlace = (LinearLayout) findViewById(R.id.llOldPlace);
		this.btnNewToOldPlace = (Button) findViewById(R.id.btnNewToOldPlace);				
		this.btnAddGPS = (Button) findViewById(R.id.btnAddGPS);
		
		this.tvCemeterySquare = (TextView) findViewById(R.id.tvCemeterySquare);
		this.tvRegionSquare = (TextView) findViewById(R.id.tvRegionSquare);
		
		updateAccessibilityUI(this.mType);
		this.btnCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();				
			}
		});
		
		this.btnSave.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isSave = saveObjectToDB();
				if(isSave){
					setResult(Activity.RESULT_OK);
					finish();	
				} else {
					Toast.makeText(AddObjectActivity.this, "Недопустимое значение", Toast.LENGTH_LONG).show();
				}
			}
		});
		
		loadDataFromDB(mType, mId, mParentId, mIsEdit);
		setNextValueForPlace();
				
		if(Settings.IsOldPlaceNameOption(this)){
			this.llOldPlace.setVisibility(View.VISIBLE);
			if(this.etOldPlace.getText().toString() != ""){
				this.btnNewToOldPlace.setEnabled(true);
			} else {
				this.btnNewToOldPlace.setEnabled(false);
			}
			this.btnFindOldPlace.setVisibility(View.VISIBLE);
		} else {
			this.llOldPlace.setVisibility(View.GONE);
			this.btnFindOldPlace.setVisibility(View.GONE);
		}
		this.btnNewToOldPlace.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String placeName = etPlace.getText().toString();
				String oldPlaceName = etOldPlace.getText().toString();
				etOldPlace.setText(placeName);
				etPlace.setText(oldPlaceName);				
			}
		});
		
		this.btnAddGPS.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AddObjectActivity.this, AddGPSActivity.class);
				intent.putExtra(EXTRA_ID, mId);
				intent.putExtra(EXTRA_TYPE, mType);				
				if(TextUtils.isEmpty(mGPSListString)){
					List<GPS> gpsList = getGPSListFromDB();
					mGPSListString = GPSListToString(gpsList);
				}						
				intent.putExtra(AddGPSActivity.GPS_LIST_KEY, mGPSListString);
				startActivityForResult(intent, ADD_GPS_ACTIVITY_REQUEST_CODE);				
			}
		});
		
		this.btnFindOldPlace.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int cemeteryId = (Integer) v.getTag();
				Intent intent = new Intent(AddObjectActivity.this, PlaceSearchActivity.class);
				intent.putExtra(PlaceSearchActivity.EXTRA_CEMETERY_ID, cemeteryId);
				startActivityForResult(intent, PlaceSearchActivity.PLACE_SEARCH_REQUESTCODE);
			}
		});
		
	}
	
	protected void onSaveInstanceState (Bundle outState){
		super.onSaveInstanceState(outState);
		if(mCemeterySquare != null){
			outState.putDouble(CEMETERY_SQUARE_KEY, this.mCemeterySquare);
		}
		if(mRegionSquare != null){
			outState.putDouble(REGION_SQUARE_KEY, this.mRegionSquare);
		}
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		if(isFinishing()){
			mGPSListString = "";
			mIsStoreGPS = false;		
		}
	}
	
	private void setNextValueForPlace(){
		String nextPlaceName = null;
		if((this.mType == ADD_PLACE_WITHOUTROW || this.mType == ADD_PLACE_WITHROW) &&
			(this.mId < 0 && this.mParentId > 0 && this.etPlace.getText().length() == 0)){
			if(this.mType == ADD_PLACE_WITHOUTROW){				
				try {
					PreparedQuery<Place> query = DB.dao(Place.class).queryBuilder().orderByRaw(BaseDTO.ORDER_BY_DESC_COLUMN_NAME).where().eq("Region_id", this.mParentId).prepare();
					List<Place> list = DB.dao(Place.class).query(query);
					if(list.size() > 0){
						nextPlaceName = list.get(0).Name;
					}
				} catch (SQLException e) {
					this.mFileLog.error(Settings.UNEXPECTED_ERROR_MESSAGE, e);
				}
			}
			if(this.mType == ADD_PLACE_WITHROW){
				try {
					PreparedQuery<Place> query = DB.dao(Place.class).queryBuilder().orderByRaw(BaseDTO.ORDER_BY_DESC_COLUMN_NAME).where().eq("Row_id", this.mParentId).prepare();
					List<Place> list = DB.dao(Place.class).query(query);
					if(list.size() > 0){
						nextPlaceName = list.get(0).Name;
					}
				} catch (SQLException e) {
					this.mFileLog.error(Settings.UNEXPECTED_ERROR_MESSAGE, e);
				}
			}
			if(nextPlaceName != null){
				nextPlaceName = BrowserCemeteryActivity.nextString(nextPlaceName);
				this.etPlace.setText(nextPlaceName);
			}
		}
	}
	
	private boolean saveObjectToDB(){
		boolean isSave = false;
		switch(mType){
			case ADD_CEMETERY:
				isSave = saveCemetery();
				break;
			case ADD_REGION:
				isSave = saveRegion();
				break;		
			case ADD_ROW:
				isSave = saveRow();
				break;			
			case ADD_PLACE_WITHOUTROW:
				isSave = savePlaceWithoutRow();		
				break;
			case ADD_PLACE_WITHROW:
				isSave = savePlaceWithRow();
				break;
			case ADD_GRAVE_WITHOUTROW:
				isSave = saveGrave();
				break;
			case ADD_GRAVE_WITHROW:
				isSave = saveGrave();
				break;
			default:
				break;
		}
		if(isSave && mIsStoreGPS){            
            saveGPSToDB(mGPSListString);
		}
		return isSave;		
	}
	
	private void loadDataFromDB(int type, int id, int parentId, boolean isEdit){
		String title = getTitle().toString();
		String action = null;
		if(isEdit){
			action = "Редактировать";
		} else {
			action = "Добавить";
		}
		String objectName = null;
		ComplexGrave complexGrave = new ComplexGrave();		
		switch (type) {
		case ADD_CEMETERY:
			objectName = "кладбище";
			if(id >= 0){
				complexGrave.loadByCemeteryId(id);
			}
			break;
		case ADD_REGION:
			objectName = "участок";
			if(id >= 0){
				complexGrave.loadByRegionId(id);
			} else {
				complexGrave.loadByCemeteryId(parentId);
			}
			break;		
		case ADD_ROW:
			objectName = "ряд";
			if(id >= 0){
				complexGrave.loadByRowId(id);
			} else {
				complexGrave.loadByRegionId(parentId);
			}
			break;			
		case ADD_PLACE_WITHOUTROW:
			objectName = "место";
			if(id >= 0){
				complexGrave.loadByPlaceId(id);
			} else {				
				complexGrave.loadByRegionId(parentId);
			}
			break;
		case ADD_PLACE_WITHROW:
			objectName = "место";
			if(id >= 0){
				complexGrave.loadByPlaceId(id);
			} else {
				complexGrave.loadByRowId(parentId);
			}
			break;
		case ADD_GRAVE_WITHOUTROW:
			objectName = "могилу";
			if(id >= 0){
				complexGrave.loadByGraveId(id);
			} else {
				complexGrave.loadByPlaceId(parentId);
			}
			break;
		case ADD_GRAVE_WITHROW:
			objectName = "могилу";
			if(id >=0){
				complexGrave.loadByGraveId(id);
			} else {
				complexGrave.loadByPlaceId(parentId);
			}
			break;
		default:
			break;
		}		
		title = String.format("%s (%s %s)", title, action, objectName);
		setTitle(title);
		
		if(complexGrave.Cemetery != null){
			this.etCemetery.setText(complexGrave.Cemetery.Name);
			if(mCemeterySquare == null){
				mCemeterySquare = complexGrave.Cemetery.Square;
			}			
		} else {
			this.etCemetery.setText(null);			
		}
		if(complexGrave.Region != null){
			this.etRegion.setText(complexGrave.Region.Name);
			if(mRegionSquare == null){
				mRegionSquare = complexGrave.Region.Square;
			}			
		} else {
			this.etRegion.setText(null);			
		}
		if(complexGrave.Row != null){
			this.etRow.setText(complexGrave.Row.Name);
		} else {
			this.etRow.setText(null);			
		}
		if(complexGrave.Place != null){
			this.etPlace.setText(complexGrave.Place.Name);
			this.etOldPlace.setText(complexGrave.Place.OldName);
			
			this.cbPlaceUnowned.setChecked(complexGrave.Place.isUnowned());
			this.cbPlaceMilitary.setChecked(complexGrave.Place.isMilitary());
            this.cbPlaceWrongFIO.setChecked(complexGrave.Place.isWrongFIO());
            this.cbPlaceUnindentified.setChecked(complexGrave.Place.isUnindentified());
            this.cbPlaceSizeVioleted.setChecked(complexGrave.Place.isSizeViolated());
			
			if(complexGrave.Place.Length != null){ 
				this.etPlaceLength.setText(Double.toString(complexGrave.Place.Length));
			} else {
				this.etPlaceLength.setText(null);
			}
			if(complexGrave.Place.Width != null){ 
				this.etPlaceWidth.setText(Double.toString(complexGrave.Place.Width));
			} else {
				this.etPlaceWidth.setText(null);
			}
		} else {
			this.etPlace.setText(null);
			this.etPlaceWidth.setText(null);
			this.etPlaceLength.setText(null);
		}
		if(complexGrave.Grave != null){
			this.etGrave.setText(complexGrave.Grave.Name);			
		} else {
			this.etGrave.setText(null);
		}
		
		if(complexGrave.Cemetery != null){
			this.btnFindOldPlace.setTag(complexGrave.Cemetery.Id);
		}
		updateSquareInUI();
	}
	
	private void updateSquareInUI(){
		if(mCemeterySquare != null){
			this.tvCemeterySquare.setText(String.format(getString(R.string.square), Double.toString(mCemeterySquare)));
		} else{
			this.tvCemeterySquare.setText(String.format(getString(R.string.square), ""));
		}
		if(mRegionSquare != null){
			this.tvRegionSquare.setText(String.format(getString(R.string.square), Double.toString(mRegionSquare)));
		} else {
			this.tvRegionSquare.setText(String.format(getString(R.string.square), ""));
		}
	}
	
	private void calculateSquare(){
		Double square = null;
		if((this.mType == AddObjectActivity.ADD_CEMETERY) || (this.mType == AddObjectActivity.ADD_REGION)){			
			if(mIsStoreGPS){
				if(!TextUtils.isEmpty(mGPSListString)){
					List<GPS> gpsList = parseGPSListString(mGPSListString);
					if(gpsList.size() >= 3) {						
						Comparator<GPS> comparator = new Comparator<GPS>() {
						    public int compare(GPS gps1, GPS gps2) {
						        return gps2.OrdinalNumber - gps1.OrdinalNumber; 
						    }
						};
						Collections.sort(gpsList, comparator);
						square = FindingSquare.getSquare(gpsList);
					}
				}
			}
			if(this.mType == AddObjectActivity.ADD_CEMETERY){
				mCemeterySquare = square;
			}
			if(this.mType == AddObjectActivity.ADD_REGION){
				mRegionSquare = square;
			}
		}
		updateSquareInUI();
	}
	
	public void setNewIdInExtras(String extraName, int id){
		getIntent().removeExtra(extraName);
		getIntent().putExtra(extraName, id);
	}
	
	public void updateAccessibilityUI(int type){
		this.llCemetery.setEnabled(false);
		setEnabled(this.llCemetery, false);
		this.llCemetery.setVisibility(View.GONE);
		this.llRegion.setEnabled(false);
		setEnabled(this.llRegion, false);
		this.llRegion.setVisibility(View.GONE);
		this.llRow.setVisibility(View.GONE);
		this.llRow.setEnabled(false);
		setEnabled(this.llRow, false);
		this.llPlace.setEnabled(false);
		setEnabled(this.llPlace, false);
		this.llPlace.setVisibility(View.GONE);
		this.llGrave.setEnabled(false);
		setEnabled(this.llGrave, false);
		this.llGrave.setVisibility(View.GONE);
		switch (type) {
		case ADD_CEMETERY:
			this.llCemetery.setEnabled(true);
			setEnabled(this.llCemetery, true);
			this.llCemetery.setVisibility(View.VISIBLE);			
			break;
		case ADD_REGION:
			this.llCemetery.setVisibility(View.VISIBLE);
			this.llRegion.setEnabled(true);
			setEnabled(this.llRegion, true);
			this.llRegion.setVisibility(View.VISIBLE);
			break;		
		case ADD_ROW:
			this.llCemetery.setVisibility(View.VISIBLE);
			this.llRegion.setVisibility(View.VISIBLE);
			this.llRow.setVisibility(View.VISIBLE);
			this.llRow.setEnabled(true);
			setEnabled(this.llRow, true);
			break;			
		case ADD_PLACE_WITHOUTROW:
			this.llCemetery.setVisibility(View.VISIBLE);
			this.llRegion.setVisibility(View.VISIBLE);
			this.llRow.setVisibility(View.GONE);
			this.llPlace.setEnabled(true);
			setEnabled(this.llPlace, true);
			this.llPlace.setVisibility(View.VISIBLE);			
			break;
		case ADD_PLACE_WITHROW:
			this.llCemetery.setVisibility(View.VISIBLE);
			this.llRegion.setVisibility(View.VISIBLE);
			this.llRow.setVisibility(View.VISIBLE);
			this.llPlace.setEnabled(true);
			setEnabled(this.llPlace, true);
			this.llPlace.setVisibility(View.VISIBLE);
			break;
		case ADD_GRAVE_WITHOUTROW:
			this.llCemetery.setVisibility(View.VISIBLE);
			this.llRegion.setVisibility(View.VISIBLE);
			this.llRow.setVisibility(View.GONE);
			this.llPlace.setVisibility(View.VISIBLE);
			this.llGrave.setEnabled(true);
			setEnabled(this.llGrave, true);
			this.llGrave.setVisibility(View.VISIBLE);
			break;
		case ADD_GRAVE_WITHROW:
			this.llCemetery.setVisibility(View.VISIBLE);
			this.llRegion.setVisibility(View.VISIBLE);
			this.llRow.setVisibility(View.VISIBLE);
			this.llPlace.setVisibility(View.VISIBLE);
			this.llGrave.setEnabled(true);
			setEnabled(this.llGrave, true);
			this.llGrave.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
		this.cbPlaceUnowned.setEnabled(true);
		this.cbPlaceMilitary.setEnabled(true);
		this.cbPlaceWrongFIO.setEnabled(true);
		this.cbPlaceSizeVioleted.setEnabled(true);
		this.cbPlaceUnindentified.setEnabled(true);
		this.etPlaceLength.setEnabled(true);
		this.etPlaceWidth.setEnabled(true);
	}
	
	public static void setEnabled(ViewGroup viewGroup, boolean enabled) {
		int childCount = viewGroup.getChildCount();
		for (int i = 0; i < childCount; i++) {
		    View view = viewGroup.getChildAt(i);
		    view.setEnabled(enabled);
		    if (view instanceof ViewGroup) {
		        setEnabled((ViewGroup) view, enabled);
		    } else if (view instanceof ListView) {
		    	view.setEnabled(enabled);
		    	ListView listView = (ListView) view;
		    	int listChildCount = listView.getChildCount();
		    	for (int j = 0; j < listChildCount; j++) {
		    		listView.getChildAt(j).setEnabled(enabled);
		    	}
		    }
		}
	}

	private boolean checkCemeteryName(String newCemeteryName, int curCemeteryId){
		if(newCemeteryName == null || newCemeteryName.equals("")){
			return false;
		}
		QueryBuilder<Cemetery, Integer> builder = DB.dao(Cemetery.class).queryBuilder();
		try {
			builder.where().eq("Name", newCemeteryName).and().ne("Id", curCemeteryId);
			List<Cemetery> findedCemeteries = DB.dao(Cemetery.class).query(builder.prepare());
			if(findedCemeteries.size() > 0){
				return false;
			}
		} catch (SQLException e) {					
			this.mFileLog.error(Settings.UNEXPECTED_ERROR_MESSAGE, e);
		}
		return true;
	}
	
	private boolean saveCemetery(){
		boolean isCheck = checkCemeteryName(etCemetery.getText().toString(), this.mId);
		if(!isCheck){
			return false;
		}
		if(mId >= 0){
			// update
			Cemetery cemetery = DB.dao(Cemetery.class).queryForId(mId);
			String oldCemeteryName = cemetery.Name;
			String newCemeteryName = etCemetery.getText().toString();
			if(oldCemeteryName != newCemeteryName || cemetery.Square != mCemeterySquare) {
				cemetery.Name = newCemeteryName;
				cemetery.Square = mCemeterySquare;
				cemetery.IsChanged = 1;
				DB.dao(Cemetery.class).update(cemetery);
				ComplexGrave.renameCemetery(cemetery, oldCemeteryName);				
			}
		} else {
			//create
			Cemetery cemetery = new Cemetery();
			cemetery.Name = etCemetery.getText().toString();
			cemetery.Square = mCemeterySquare;
			cemetery.IsChanged = 1;
			DB.dao(Cemetery.class).create(cemetery);
			this.mId = cemetery.Id;
			setNewIdInExtras(EXTRA_ID, mId);
		}
		return true;
	}
		
	private boolean checkRegionName(Cemetery cemetery, String newRegionName, int curRegionId){
		if(newRegionName == null || newRegionName.equals("")){
			return false;
		}
		QueryBuilder<Region, Integer> builder = DB.dao(Region.class).queryBuilder();
		try {
			builder.where().eq("Cemetery_id", cemetery.Id).and().eq("Name", newRegionName).and().ne("Id", curRegionId);
			List<Region> findedRegions = DB.dao(Region.class).query(builder.prepare());
			if(findedRegions.size() > 0){
				return false;
			}
		} catch (SQLException e) {					
			this.mFileLog.error(Settings.UNEXPECTED_ERROR_MESSAGE, e);
		}
		return true;
	}
	
	private boolean saveRegion(){
		Cemetery cemetery = null;
		if(mId >= 0){
			Region region = DB.dao(Region.class).queryForId(mId);
			cemetery = region.Cemetery;
		} else {
			cemetery = DB.dao(Cemetery.class).queryForId(this.mParentId);
		}		
		boolean isCheck = checkRegionName(cemetery, etRegion.getText().toString(), this.mId);
		if(!isCheck){
			return false;
		}
		if(mId >= 0){
			// update
			Region region = DB.dao(Region.class).queryForId(mId);
			String oldRegionName = region.Name;
			String newRegionName = etRegion.getText().toString();
			if(oldRegionName != newRegionName || region.Square != mRegionSquare){
				region.Name = newRegionName;
				region.Square = mRegionSquare;
				region.IsChanged = 1;
				DB.dao(Region.class).update(region);
				ComplexGrave.renameRegion(region, oldRegionName);
			}
		} else {
			//create
			Region region = new Region();
			region.Cemetery = cemetery;
			region.Name = etRegion.getText().toString();
			region.Square = mRegionSquare;
			region.IsChanged = 1;
			DB.dao(Region.class).create(region);
			this.mId = region.Id;
			setNewIdInExtras(EXTRA_ID, mId);
		}
		return true;
	}
	
	private boolean checkRowName(Region region, String newRowName, int curRowId){
		if(newRowName == null || newRowName.equals("")){
			return false;
		}
		QueryBuilder<Row, Integer> builder = DB.dao(Row.class).queryBuilder();
		try {
			builder.where().eq("Region_id", region.Id).and().eq("Name", newRowName).and().ne("Id", curRowId);
			List<Row> findedRows = DB.dao(Row.class).query(builder.prepare());
			if(findedRows.size() > 0){
				return false;
			}
		} catch (SQLException e) {					
			this.mFileLog.error(Settings.UNEXPECTED_ERROR_MESSAGE, e);
		}
		return true;
	}
	
	private boolean saveRow(){
		Region region = null;
		if(mId >= 0){
			Row row = DB.dao(Row.class).queryForId(mId);
			region = row.Region;
		} else {
			region = DB.dao(Region.class).queryForId(this.mParentId);
		}		
		boolean isCheck = checkRowName(region, etRow.getText().toString(), this.mId);
		if(!isCheck){
			return false;
		}
		if(mId >= 0){
			// update
			Row row = DB.dao(Row.class).queryForId(mId);
			String oldRowName = row.Name;
			String newRowName = etRow.getText().toString();
			if(oldRowName != newRowName){
				row.Name = newRowName;
				row.IsChanged = 1;
				DB.dao(Row.class).update(row);
				ComplexGrave.renameRow(row, oldRowName);
			}
		} else {
			//create
			Row row = new Row();
			row.Region = region;
			row.Name = etRow.getText().toString();
			row.IsChanged = 1;
			DB.dao(Row.class).create(row);
			this.mId = row.Id;
			setNewIdInExtras(EXTRA_ID, mId);
		}
		return true;
	}	
	
	private boolean checkPlaceName(Region region, Row row, String newPlaceName, int curPlaceId){
		if(newPlaceName == null || newPlaceName.equals("")){
			return false;
		}
		QueryBuilder<Place, Integer> builder = DB.dao(Place.class).queryBuilder();
		try {
			if(row != null){
				builder.where().eq("Row_id", row.Id).and().eq("Name", newPlaceName).and().ne("Id", curPlaceId);
			} else {
				builder.where().eq("Region_id", region.Id).and().eq("Name", newPlaceName).and().ne("Id", curPlaceId);
			}
			List<Place> findedPlaces = DB.dao(Place.class).query(builder.prepare());
			if(findedPlaces.size() > 0){
				return false;
			}
		} catch (SQLException e) {					
			this.mFileLog.error(Settings.UNEXPECTED_ERROR_MESSAGE, e);
		}
		return true;
	}
	
	private boolean checkPlaceWidthAndLength(Double[] placeSizes){
		boolean result = true;
		String placeWidth = this.etPlaceWidth.getText().toString();
		String placeLength = this.etPlaceLength.getText().toString();
		Double length = null;
		Double width = null;
		try{
			if(!(placeLength == null || placeLength.length() == 0)){
				length = Double.parseDouble(placeLength);
			}
			if(!(placeWidth == null || placeWidth.length() == 0)){
				width = Double.parseDouble(placeWidth);
			}
			placeSizes[0] = length;
			placeSizes[1] = width;
		}catch(NumberFormatException ex){
			length = null;
			width = null;
			result = false;
		}
		return result;
	}
	
	private boolean savePlaceWithRow(){
		Row row = null;
		if(mId >= 0){
			Place place = DB.dao(Place.class).queryForId(mId);
			row = place.Row;
		} else {
			row = DB.dao(Row.class).queryForId(this.mParentId);
		}
		Double[] placeSizes = new Double[2];
		boolean isCheck = checkPlaceName(null, row, etPlace.getText().toString(), this.mId);
		boolean isCheck2 = checkPlaceWidthAndLength(placeSizes);
		if(!isCheck || !isCheck2){
			return false;
		}
		Date wrongFIODate = null, unownedDate = null, sizeViolatedDate = null, unindentifiedDate = null, militaryDate = null;     
        if(cbPlaceUnowned.isChecked()){
            unownedDate = new Date();
        }
        if(cbPlaceWrongFIO.isChecked()){
            wrongFIODate = new Date();
        }
        if(cbPlaceSizeVioleted.isChecked()){
            sizeViolatedDate = new Date();
        }
        if(cbPlaceUnindentified.isChecked()){
            unindentifiedDate = new Date();
        }
        if(cbPlaceMilitary.isChecked()){
            militaryDate = new Date();
        }
		if(mId >= 0){
			// update
			Place dbPlace = DB.dao(Place.class).queryForId(mId);
			String previousPlaceName = dbPlace.Name;
			String newPlaceName = (!TextUtils.isEmpty(etPlace.getText().toString())) ? etPlace.getText().toString() : null;
            boolean isRenamePlace = !previousPlaceName.equals(newPlaceName);
			
            dbPlace.Name = newPlaceName;
            dbPlace.OldName = (!TextUtils.isEmpty(etOldPlace.getText().toString())) ? etOldPlace.getText().toString() : null;
            dbPlace.IsOwnerLess = cbPlaceUnowned.isChecked();
            dbPlace.UnownedDate = unownedDate;
            dbPlace.SizeViolatedDate = sizeViolatedDate;
            dbPlace.UnindentifiedDate = unindentifiedDate;
            dbPlace.WrongFIODate = wrongFIODate;
            dbPlace.MilitaryDate = militaryDate;
            dbPlace.Length = placeSizes[0];
            dbPlace.Width = placeSizes[1];
            DB.dao(Place.class).update(dbPlace);
            if(isRenamePlace){
                ComplexGrave.renamePlace(dbPlace, previousPlaceName);
            }		
			
		} else {
			Place newPlace = new Place();						
			newPlace.Region = null;
			newPlace.Row = row;
			newPlace.Name = (!TextUtils.isEmpty(etPlace.getText().toString())) ? etPlace.getText().toString() : null;
			newPlace.OldName = (!TextUtils.isEmpty(etOldPlace.getText().toString())) ? etOldPlace.getText().toString() : null;
			newPlace.IsOwnerLess = cbPlaceUnowned.isChecked();
			newPlace.UnownedDate = unownedDate;
			newPlace.SizeViolatedDate = sizeViolatedDate;
			newPlace.UnindentifiedDate = unindentifiedDate;
			newPlace.WrongFIODate = wrongFIODate;
			newPlace.MilitaryDate = militaryDate;
			newPlace.Length = placeSizes[0];
			newPlace.Width = placeSizes[1];			
			newPlace.IsChanged = 1;
			DB.dao(Place.class).create(newPlace);
			this.mId = newPlace.Id;
			setNewIdInExtras(EXTRA_ID, mId);
		}
		return true;
	}
	
	private boolean savePlaceWithoutRow(){
		Region region = null;
		if(mId >= 0){
			Place place = DB.dao(Place.class).queryForId(mId);
			region = place.Region;
		} else {
			region = DB.dao(Region.class).queryForId(this.mParentId);
		}
		Double[] placeSizes = new Double[2];
		boolean isCheck = checkPlaceName(region, null, etPlace.getText().toString(), this.mId);
		boolean isCheck2 = checkPlaceWidthAndLength(placeSizes);
		if(!isCheck || !isCheck2){
			return false;
		}
		Date wrongFIODate = null, unownedDate = null, sizeViolatedDate = null, unindentifiedDate = null, militaryDate = null;     
        if(cbPlaceUnowned.isChecked()){
            unownedDate = new Date();
        }
        if(cbPlaceWrongFIO.isChecked()){
            wrongFIODate = new Date();
        }
        if(cbPlaceSizeVioleted.isChecked()){
            sizeViolatedDate = new Date();
        }
        if(cbPlaceUnindentified.isChecked()){
            unindentifiedDate = new Date();
        }
        if(cbPlaceMilitary.isChecked()){
            militaryDate = new Date();
        }
		if(mId >= 0){
			// update			
			Place dbPlace = DB.dao(Place.class).queryForId(mId);
			String previousPlaceName = dbPlace.Name;
			String newPlaceName = (!TextUtils.isEmpty(etPlace.getText().toString())) ? etPlace.getText().toString() : null;
			boolean isRenamePlace = !previousPlaceName.equals(newPlaceName);
			
			dbPlace.Name = newPlaceName;
			dbPlace.OldName = (!TextUtils.isEmpty(etOldPlace.getText().toString())) ? etOldPlace.getText().toString() : null;
			dbPlace.IsOwnerLess = cbPlaceUnowned.isChecked();
			dbPlace.UnownedDate = unownedDate;
            dbPlace.SizeViolatedDate = sizeViolatedDate;
            dbPlace.UnindentifiedDate = unindentifiedDate;
            dbPlace.WrongFIODate = wrongFIODate;
            dbPlace.MilitaryDate = militaryDate;
			dbPlace.Length = placeSizes[0];
			dbPlace.Width = placeSizes[1];
			dbPlace.IsChanged = 1;
            DB.dao(Place.class).update(dbPlace);
            if(isRenamePlace){
                ComplexGrave.renamePlace(dbPlace, previousPlaceName);
            }						
			
		} else {
			Place newPlace = new Place();
			newPlace.Row = null;
			newPlace.Region = region;			
			newPlace.Name = (!TextUtils.isEmpty(etPlace.getText().toString())) ? etPlace.getText().toString() : null;
			newPlace.OldName = (!TextUtils.isEmpty(etOldPlace.getText().toString())) ? etOldPlace.getText().toString() : null;
			newPlace.IsOwnerLess = cbPlaceUnowned.isChecked();
			newPlace.UnownedDate = unownedDate;
			newPlace.SizeViolatedDate = sizeViolatedDate;
			newPlace.UnindentifiedDate = unindentifiedDate;
			newPlace.WrongFIODate = wrongFIODate;
			newPlace.MilitaryDate = militaryDate;
			newPlace.Length = placeSizes[0];
			newPlace.Width = placeSizes[1];			
			newPlace.IsChanged = 1;			
			DB.dao(Place.class).create(newPlace);
			this.mId = newPlace.Id;
			setNewIdInExtras(EXTRA_ID, mId);
		}
		return true;
	}
	
	private boolean checkGraveName(Place place, String newGraveName, int curGraveId){
		if(TextUtils.isEmpty(newGraveName)){
			return false;
		}
		try{
			int value = Integer.parseInt(newGraveName);
			if(value < 0 || value > 32767){
				return false;
			}			
		}catch(Exception exc){
			return false;
		}
		QueryBuilder<Grave, Integer> builder = DB.dao(Grave.class).queryBuilder();
		try {
			builder.where().eq("Place_id", place.Id).and().eq("Name", newGraveName).and().ne("Id", curGraveId);
			List<Grave> findedGraves = DB.dao(Grave.class).query(builder.prepare());
			if(findedGraves.size() > 0){
				return false;
			}
		} catch (SQLException e) {					
			this.mFileLog.error(Settings.UNEXPECTED_ERROR_MESSAGE, e);
		}
		return true;
	}
	
	private boolean saveGrave(){
		Place dbPlace = null;
		Grave dbGrave = null;
		if(mId >=0){
			dbGrave = DB.dao(Grave.class).queryForId(mId);
			DB.dao(Place.class).refresh(dbGrave.Place);
			dbPlace = dbGrave.Place;
		} else {
			dbPlace = DB.dao(Place.class).queryForId(this.mParentId);
		}		
		String newGraveName = (!TextUtils.isEmpty(etGrave.getText().toString())) ? etGrave.getText().toString() : null;
		boolean isCheck = checkGraveName(dbPlace, newGraveName, this.mId);
		Double[] placeSizes = new Double[2];
		boolean isCheck2 = checkPlaceWidthAndLength(placeSizes);		
		if(!isCheck || !isCheck2){
			return false;
		}
		Date wrongFIODate = null, unownedDate = null, sizeViolatedDate = null, unindentifiedDate = null, militaryDate = null;		
		if(cbPlaceUnowned.isChecked()){
		    unownedDate = new Date();
		}
		if(cbPlaceWrongFIO.isChecked()){
		    wrongFIODate = new Date();
		}
		if(cbPlaceSizeVioleted.isChecked()){
		    sizeViolatedDate = new Date();
		}
		if(cbPlaceUnindentified.isChecked()){
		    unindentifiedDate = new Date();
		}
		if(cbPlaceMilitary.isChecked()){
		    militaryDate = new Date();
		}
		if(mId >= 0){
			// update
		    String previousGraveName = dbGrave.Name;
		    boolean isRenameGrave = !previousGraveName.equals(newGraveName);
		    dbGrave.IsChanged = 1;
            dbGrave.Name = newGraveName;
            dbGrave.IsMilitary = cbPlaceMilitary.isChecked();
            dbGrave.IsWrongFIO = cbPlaceWrongFIO.isChecked();
            DB.dao(Grave.class).update(dbGrave);
            if(isRenameGrave){
                ComplexGrave.renameGrave(dbGrave, previousGraveName);
            }
			
			dbPlace.IsOwnerLess = this.cbPlaceUnowned.isChecked();
			dbPlace.UnownedDate = unownedDate;
			dbPlace.SizeViolatedDate = sizeViolatedDate;
			dbPlace.UnindentifiedDate = unindentifiedDate;
			dbPlace.WrongFIODate = wrongFIODate;
			dbPlace.MilitaryDate = militaryDate;
			dbPlace.Length = placeSizes[0];
			dbPlace.Width = placeSizes[1];
			dbPlace.IsChanged = 1;
			DB.dao(Place.class).createOrUpdate(dbPlace);						
					
		} else {			
			
		    dbPlace.IsOwnerLess = this.cbPlaceUnowned.isChecked();
            dbPlace.UnownedDate = unownedDate;
            dbPlace.SizeViolatedDate = sizeViolatedDate;
            dbPlace.UnindentifiedDate = unindentifiedDate;
            dbPlace.WrongFIODate = wrongFIODate;
            dbPlace.MilitaryDate = militaryDate;
            dbPlace.Length = placeSizes[0];
            dbPlace.Width = placeSizes[1];
            dbPlace.IsChanged = 1;
            DB.dao(Place.class).createOrUpdate(dbPlace);
			
			Grave newGrave = new Grave();
			newGrave.Place = dbPlace;
			newGrave.Name = newGraveName;
			newGrave.IsMilitary = cbPlaceMilitary.isChecked();
			newGrave.IsWrongFIO = cbPlaceWrongFIO.isChecked();
			newGrave.IsChanged = 1;
			DB.dao(Grave.class).create(newGrave);
			this.mId = newGrave.Id;
			setNewIdInExtras(EXTRA_ID, mId);
		}
		return true;
	}
	
	private List<GPS> getGPSListFromDB(){
		List<GPS> dbGPSList = new ArrayList<GPS>();		
		switch(mType){
		case AddObjectActivity.ADD_CEMETERY:
			try {
				QueryBuilder<GPSCemetery, Integer> qbGPSCemetery = DB.dao(GPSCemetery.class).queryBuilder();
				qbGPSCemetery.orderBy(GPS.ORDINAL_COLUMN, true).where().eq("Cemetery_id", mId);
				List<GPSCemetery> tempGPSCemeteryList = DB.dao(GPSCemetery.class).query(qbGPSCemetery.prepare());
				for(GPSCemetery gpsCemetery: tempGPSCemeteryList){
					dbGPSList.add(gpsCemetery);
				}
			} catch (SQLException e) {
				this.mFileLog.error(Settings.UNEXPECTED_ERROR_MESSAGE, e);
			}
			break;
		case AddObjectActivity.ADD_REGION:
			try {
				QueryBuilder<GPSRegion, Integer> qbGPSRegion = DB.dao(GPSRegion.class).queryBuilder();
				qbGPSRegion.orderBy(GPS.ORDINAL_COLUMN, true).where().eq("Region_id", mId);
				List<GPSRegion> tempGPSRegionList = DB.dao(GPSRegion.class).query(qbGPSRegion.prepare());
				for(GPSRegion gpsRegion: tempGPSRegionList){
					dbGPSList.add(gpsRegion);
				}
			} catch (SQLException e) {
				this.mFileLog.error(Settings.UNEXPECTED_ERROR_MESSAGE, e);
			}
			break;	
		case AddObjectActivity.ADD_ROW:
			try {
				QueryBuilder<GPSRow, Integer> qbGPSRow = DB.dao(GPSRow.class).queryBuilder();
				qbGPSRow.orderBy(GPS.ORDINAL_COLUMN, true).where().eq("Row_id", mId);
				List<GPSRow> tempGPSRowList = DB.dao(GPSRow.class).query(qbGPSRow.prepare());
				for(GPSRow gpsRow: tempGPSRowList){
					dbGPSList.add(gpsRow);
				}
			} catch (SQLException e) {
				this.mFileLog.error(Settings.UNEXPECTED_ERROR_MESSAGE, e);
			}
			break;		
		case AddObjectActivity.ADD_PLACE_WITHOUTROW :
		case AddObjectActivity.ADD_PLACE_WITHROW :
			try {
				QueryBuilder<GPSPlace, Integer> qbGPSPlace = DB.dao(GPSPlace.class).queryBuilder();
				qbGPSPlace.orderBy(GPS.ORDINAL_COLUMN, true).where().eq("Place_id", mId);
				List<GPSPlace> tempGPSPlaceList = DB.dao(GPSPlace.class).query(qbGPSPlace.prepare());
				for(GPSPlace gpsPlace : tempGPSPlaceList){
					dbGPSList.add(gpsPlace);
				}
			} catch (SQLException e) {
				this.mFileLog.error(Settings.UNEXPECTED_ERROR_MESSAGE, e);
			}
			break;		
		case AddObjectActivity.ADD_GRAVE_WITHOUTROW :
		case AddObjectActivity.ADD_GRAVE_WITHROW :
			try {
				QueryBuilder<GPSGrave, Integer> qbGPSGrave = DB.dao(GPSGrave.class).queryBuilder();
				qbGPSGrave.orderBy(GPS.ORDINAL_COLUMN, true).where().eq("Grave_id", mId);
				List<GPSGrave> tempGPSGraveList = DB.dao(GPSGrave.class).query(qbGPSGrave.prepare());
				for(GPSGrave gpsGrave : tempGPSGraveList){
					dbGPSList.add(gpsGrave);
				}
			} catch (SQLException e) {
				this.mFileLog.error(Settings.UNEXPECTED_ERROR_MESSAGE, e);
			}
			break;	
		default:
			break;
		}		
		return dbGPSList;			
	}
	
	private void saveGPSToDB(String gpsListString){
		List<GPS> gpsList = parseGPSListString(gpsListString);
		List<GPS> gpsListInDB = getGPSListFromDB();
		String gpsListStringInDB = GPSListToString(gpsListInDB);
		if(!gpsListString.equals(gpsListStringInDB)){
	    	switch (mType) {
			case AddObjectActivity.ADD_CEMETERY:
				Cemetery cemetery = DB.dao(Cemetery.class).queryForId(mId);
				saveGPSCemetery(cemetery, gpsList);
				cemetery.IsChanged = 1;
				cemetery.IsGPSChanged = 1;
				DB.dao(Cemetery.class).update(cemetery);
				break;
			case AddObjectActivity.ADD_REGION:
				Region region = DB.dao(Region.class).queryForId(mId);
				saveGPSRegion(region, gpsList);
				region.IsChanged = 1;
				region.IsGPSChanged = 1;
				DB.dao(Region.class).update(region);
				break;		
			case AddObjectActivity.ADD_ROW:
				Row row = DB.dao(Row.class).queryForId(mId);
				saveGPSRow(row, gpsList);				
				break;			
			case AddObjectActivity.ADD_PLACE_WITHOUTROW:
			case AddObjectActivity.ADD_PLACE_WITHROW:
				Place place = DB.dao(Place.class).queryForId(mId);
				saveGPSPlace(place, gpsList);
				break;		
			case AddObjectActivity.ADD_GRAVE_WITHOUTROW:
			case AddObjectActivity.ADD_GRAVE_WITHROW:
				Grave grave = DB.dao(Grave.class).queryForId(mId);
				saveGPSGrave(grave, gpsList);
				break;
			default:
				break;
			}
		}
    }
    
    private void saveGPSCemetery(Cemetery cemetery, List<GPS> gpsList){
    	List<GPSCemetery> deletedGPS = DB.dao(GPSCemetery.class).queryForEq("Cemetery_id", cemetery.Id);
		DB.dao(GPSCemetery.class).delete(deletedGPS);
		for(GPS gps : gpsList){
			GPSCemetery gpsCemetery = new GPSCemetery();
			if(gps.Id > 0) {
				gpsCemetery.Id = gps.Id;
			}
			gpsCemetery.Latitude = gps.Latitude;
			gpsCemetery.Longitude = gps.Longitude;
			gpsCemetery.OrdinalNumber = gps.OrdinalNumber;
			gpsCemetery.Cemetery = cemetery;
			DB.dao(GPSCemetery.class).create(gpsCemetery);
		}
	}
	
    private void saveGPSRegion(Region region, List<GPS> gpsList){
		List<GPSRegion> deletedGPS = DB.dao(GPSRegion.class).queryForEq("Region_id", region.Id);
		DB.dao(GPSRegion.class).delete(deletedGPS);
		for(GPS gps : gpsList){
			GPSRegion gpsRegion = new GPSRegion();
			if(gps.Id > 0) {
				gpsRegion.Id = gps.Id;
			}
			gpsRegion.Latitude = gps.Latitude;
			gpsRegion.Longitude = gps.Longitude;
			gpsRegion.OrdinalNumber = gps.OrdinalNumber;
			gpsRegion.Region = region;
			DB.dao(GPSRegion.class).create(gpsRegion);
		}
	}
	
	private void saveGPSRow(Row row, List<GPS> gpsList){
		List<GPSRow> deletedGPS = DB.dao(GPSRow.class).queryForEq("Row_id", row.Id);
		DB.dao(GPSRow.class).delete(deletedGPS);
		for(GPS gps : gpsList){			
			GPSRow gpsRow = new GPSRow();
			if(gps.Id > 0) {
				gpsRow.Id = gps.Id;
			}
			gpsRow.Latitude = gps.Latitude;
			gpsRow.Longitude = gps.Longitude;
			gpsRow.Row = row;
			DB.dao(GPSRow.class).create(gpsRow);
		}
	}
	
	private void saveGPSPlace(Place place, List<GPS> gpsList){
		List<GPSPlace> deletedGPS = DB.dao(GPSPlace.class).queryForEq("Place_id", place.Id);
		DB.dao(GPSPlace.class).delete(deletedGPS);
		for(GPS gps : gpsList){
			GPSPlace gpsPlace = new GPSPlace();
			if(gps.Id > 0) {
				gpsPlace.Id = gps.Id;
			}
			gpsPlace.Latitude = gps.Latitude;
			gpsPlace.Longitude = gps.Longitude;
			gpsPlace.Place = place;
			DB.dao(GPSPlace.class).create(gpsPlace);
		}
	}
	
	private void saveGPSGrave(Grave grave, List<GPS> gpsList){
		List<GPSGrave> deletedGPS = DB.dao(GPSGrave.class).queryForEq("Grave_id", grave.Id);
		DB.dao(GPSGrave.class).delete(deletedGPS);
		for(GPS gps : gpsList){
			GPSGrave gpsGrave = new GPSGrave();
			if(gps.Id > 0) {
				gpsGrave.Id = gps.Id;
			}
			gpsGrave.Latitude = gps.Latitude;
			gpsGrave.Longitude = gps.Longitude;
			gpsGrave.Grave = grave;
			DB.dao(GPSGrave.class).create(gpsGrave);
		}
	}
	
	public static String GPSListToString(List<GPS> gpsList){
		StringBuilder sb = new StringBuilder();
		DecimalFormat df = new DecimalFormat("##0.000000000000");
		for(GPS gps: gpsList){
			sb.append(String.format("%d~%d~%s~%s~", gps.Id, gps.OrdinalNumber, df.format(gps.Longitude), df.format(gps.Latitude)));			
		}
		if(sb.lastIndexOf("~") == (sb.length() - 1) && sb.length() > 0){
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();		
	}
	
	public static List<GPS> parseGPSListString(String gpsListString){
		ArrayList<GPS> gpsList = new ArrayList<GPS>();
		DecimalFormat df = new DecimalFormat("##0.000000000000");
		if(gpsListString != null && gpsListString.length() > 0){
			String[] arr = gpsListString.split("~");
			for(int i = 0; i < arr.length; i+=4){
				GPS gps = new GPS();
				gps.Id = Integer.parseInt(arr[i]);
				gps.OrdinalNumber = Integer.parseInt(arr[i+1]);
				try {
					gps.Longitude = df.parse(arr[i+2]).doubleValue();
					gps.Latitude = df.parse(arr[i+3]).doubleValue();
				} catch (ParseException e) {					
					e.printStackTrace();
				}				
				gpsList.add(gps);
			}			
		}
		return gpsList;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if(resultCode != RESULT_OK && requestCode == ADD_GPS_ACTIVITY_REQUEST_CODE){
	        mIsStoreGPS = false;
	    }
	    if (resultCode == RESULT_OK) {
	        switch (requestCode) {
	        case ADD_GPS_ACTIVITY_REQUEST_CODE:
	            mGPSListString = data.getStringExtra(AddGPSActivity.GPS_LIST_KEY);
	            mIsStoreGPS = true;
	            calculateSquare();
	            break;	
	        case PlaceSearchActivity.PLACE_SEARCH_REQUESTCODE:
	        	String oldPlaceName = data.getStringExtra(PlaceSearchActivity.EXTRA_PLACE_OLDNAME);
	        	if(oldPlaceName == null){
	        		oldPlaceName = data.getStringExtra(PlaceSearchActivity.EXTRA_PLACE_NAME);
	        	}
	        	this.etOldPlace.setText(oldPlaceName);
	        	break;
	        }
	    }
	}
	
	@Override
	public void onBackPressed() {
	    if(mIsStoreGPS){
	        saveGPSToDB(mGPSListString);
	    }
	    super.onBackPressed();
	}

}
