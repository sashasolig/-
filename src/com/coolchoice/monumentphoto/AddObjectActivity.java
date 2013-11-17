package com.coolchoice.monumentphoto;

import java.sql.SQLException;
import java.util.List;
import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.data.BaseDTO;
import com.coolchoice.monumentphoto.data.Cemetery;
import com.coolchoice.monumentphoto.data.ComplexGrave;
import com.coolchoice.monumentphoto.data.Grave;
import com.coolchoice.monumentphoto.data.Place;
import com.coolchoice.monumentphoto.data.Region;
import com.coolchoice.monumentphoto.data.Row;

import com.coolchoice.monumentphoto.map.AddGPSActivity;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

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
		
	private EditText etCemetery, etRegion, etRow, etPlace, etGrave, etOldPlace; 
	
	private CheckBox cbOwnerLess;
	
	private LinearLayout llCemetery, llRegion, llRow, llPlace, llGrave;
	
	private Button btnNewToOldPlace;
	
	private LinearLayout llOldPlace;
		
	private Button btnAddGPS;
			
	private Button btnCancel, btnSave;
	
	private int mType, mId, mParentId;
	
	private boolean mIsEdit;
          	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		this.cbOwnerLess = (CheckBox) findViewById(R.id.cbIsOwnerLess);
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
		} else {
			this.llOldPlace.setVisibility(View.GONE);
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
				boolean isSave = saveObjectToDB();
				if(isSave){
					Intent intent = new Intent(AddObjectActivity.this, AddGPSActivity.class);
					intent.putExtra(EXTRA_ID, mId);
					intent.putExtra(EXTRA_TYPE, mType);
					startActivity(intent);
				} else {
					Toast.makeText(AddObjectActivity.this, "Недопустимое значение", Toast.LENGTH_LONG).show();
				}
				
				
				
			}
		});
		
	}
	
	private void setNextValueForPlace(){
		String nextPlaceName = null;
		if((this.mType == ADD_PLACE_WITHOUTROW || this.mType == ADD_PLACE_WITHROW) &&
			(this.mId < 0 && this.mParentId > 0 && this.etPlace.getText().length() == 0)){
			if(this.mType == ADD_PLACE_WITHOUTROW){				
				try {
					PreparedQuery<Place> query = DB.dao(Place.class).queryBuilder().orderBy(BaseDTO.COLUMN_NAME, false).where().eq("Region_id", this.mParentId).prepare();
					List<Place> list = DB.dao(Place.class).query(query);
					if(list.size() > 0){
						nextPlaceName = list.get(0).Name;
					}
				} catch (SQLException e) {
					
				}
			}
			if(this.mType == ADD_PLACE_WITHROW){
				try {
					PreparedQuery<Place> query = DB.dao(Place.class).queryBuilder().orderBy(BaseDTO.COLUMN_NAME, false).where().eq("Row_id", this.mParentId).prepare();
					List<Place> list = DB.dao(Place.class).query(query);
					if(list.size() > 0){
						nextPlaceName = list.get(0).Name;
					}
				} catch (SQLException e) {
					
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
		} else {
			this.etCemetery.setText(null);
		}
		if(complexGrave.Region != null){
			this.etRegion.setText(complexGrave.Region.Name);
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
			this.cbOwnerLess.setChecked(complexGrave.Place.IsOwnerLess);
		} else {
			this.etPlace.setText(null);
		}
		if(complexGrave.Grave != null){
			this.etGrave.setText(complexGrave.Grave.Name);
		} else {
			this.etGrave.setText(null);
		}		
		
		
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
		this.cbOwnerLess.setEnabled(true);
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
			e.printStackTrace();
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
			if(oldCemeteryName != newCemeteryName) {
				cemetery.Name = newCemeteryName;
				cemetery.IsChanged = 1;
				DB.dao(Cemetery.class).update(cemetery);
				ComplexGrave.renameCemetery(cemetery, oldCemeteryName);				
			}
		} else {
			//create
			Cemetery cemetery = new Cemetery();
			cemetery.Name = etCemetery.getText().toString();
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
			e.printStackTrace();
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
			if(oldRegionName != newRegionName){
				region.Name = newRegionName;
				region.IsChanged = 1;
				DB.dao(Region.class).update(region);
				ComplexGrave.renameRegion(region, oldRegionName);
			}
		} else {
			//create
			Region region = new Region();
			region.Cemetery = cemetery;
			region.Name = etRegion.getText().toString();
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
			e.printStackTrace();
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
			e.printStackTrace();
		}
		return true;
	}
	
	private boolean savePlaceWithRow(){
		Row row = null;
		if(mId >= 0){
			Place place = DB.dao(Place.class).queryForId(mId);
			row = place.Row;
		} else {
			row = DB.dao(Row.class).queryForId(this.mParentId);
		}		
		boolean isCheck = checkPlaceName(null, row, etPlace.getText().toString(), this.mId);
		if(!isCheck){
			return false;
		}
		if(mId >= 0){
			// update
			Place place = DB.dao(Place.class).queryForId(mId);
			String dbPlaceName = place.Name;
			String dbOldPlaceName = place.OldName;
			if(dbOldPlaceName == null) {
				dbOldPlaceName = "";
			}
			boolean dbIsOwnerLess = place.IsOwnerLess;
			String placeName = etPlace.getText().toString();
			String oldPlaceName = etOldPlace.getText().toString();
			boolean isOwnerLess = cbOwnerLess.isChecked();
			if(dbPlaceName != placeName || dbIsOwnerLess != isOwnerLess || dbOldPlaceName != oldPlaceName){
				place.Name = placeName;
				place.OldName = oldPlaceName;
				place.IsOwnerLess = isOwnerLess;
				place.IsChanged = 1;
				DB.dao(Place.class).update(place);
				ComplexGrave.renamePlace(place, dbPlaceName);
			}			
			
		} else {
			Place place = new Place();
			place.Row = row;
			place.Region = null;
			place.Name = etPlace.getText().toString();
			place.OldName = etOldPlace.getText().toString();
			place.IsOwnerLess = cbOwnerLess.isChecked();
			place.IsChanged = 1;
			DB.dao(Place.class).create(place);
			this.mId = place.Id;
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
		boolean isCheck = checkPlaceName(region, null, etPlace.getText().toString(), this.mId);
		if(!isCheck){
			return false;
		}
		if(mId >= 0){
			// update
			Place place = DB.dao(Place.class).queryForId(mId);
			String dbPlaceName = place.Name;
			String dbOldPlaceName = place.OldName;
			if(dbOldPlaceName == null){
				dbOldPlaceName = "";
			}
			boolean dbIsOwnerLess = place.IsOwnerLess;
			String placeName = etPlace.getText().toString();
			String oldPlaceName = etOldPlace.getText().toString();
			boolean isOwnerLess = cbOwnerLess.isChecked();
			if(dbPlaceName != placeName || dbIsOwnerLess != isOwnerLess || dbOldPlaceName != oldPlaceName){
				place.Name = placeName;
				place.OldName = oldPlaceName;
				place.IsOwnerLess = isOwnerLess;
				place.IsChanged = 1;
				DB.dao(Place.class).update(place);
				ComplexGrave.renamePlace(place, dbPlaceName);
			}		
			
		} else {
			Place place = new Place();
			place.Row = null;
			place.Region = region;
			place.Name = etPlace.getText().toString();
			place.OldName = etOldPlace.getText().toString();
			place.IsOwnerLess = cbOwnerLess.isChecked();
			place.IsChanged = 1;
			DB.dao(Place.class).create(place);
			this.mId = place.Id;
			setNewIdInExtras(EXTRA_ID, mId);
		}
		return true;
	}
	
	private boolean checkGraveName(Place place, String newGraveName, int curGraveId){
		if(newGraveName == null || newGraveName.equals("")){
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
			e.printStackTrace();
		}
		return true;
	}
	
	private boolean saveGrave(){
		Place place = null;
		if(mId >=0){
			Grave grave = DB.dao(Grave.class).queryForId(mId);
			place = grave.Place;
		} else {
			place = DB.dao(Place.class).queryForId(this.mParentId);
		}		
		String newGraveName = etGrave.getText().toString();
		boolean isCheck = checkGraveName(place, newGraveName, this.mId);
		if(!isCheck){
			return false;
		}
		if(mId >= 0){
			// update
			Grave grave = DB.dao(Grave.class).queryForId(mId);
			String oldGraveName = grave.Name;
			if(oldGraveName != newGraveName){
				grave.Name = newGraveName;
				grave.IsChanged = 1;
				DB.dao(Grave.class).update(grave);
				ComplexGrave.renameGrave(grave, oldGraveName);
			}
			
			place = DB.dao(Place.class).queryForId(grave.Place.Id);
			if(place.IsOwnerLess != this.cbOwnerLess.isChecked()){
				place.IsOwnerLess = this.cbOwnerLess.isChecked();
				place.IsChanged = 1;
				DB.dao(Place.class).createOrUpdate(place);
			}			
		} else {			
			if(place.IsOwnerLess != this.cbOwnerLess.isChecked() ){
				place.IsOwnerLess = this.cbOwnerLess.isChecked();
				place.IsChanged = 1;
				DB.dao(Place.class).createOrUpdate(place);				
			}
			
			Grave grave = new Grave();
			grave.Place = place;
			grave.Name = etGrave.getText().toString();
			grave.IsChanged = 1;
			DB.dao(Grave.class).create(grave);
			this.mId = grave.Id;
			setNewIdInExtras(EXTRA_ID, mId);
		}
		return true;
	}
	
	@Override
    protected void onPause() {
		super.onPause();
    }

}
