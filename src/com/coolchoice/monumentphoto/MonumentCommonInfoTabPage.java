package com.coolchoice.monumentphoto;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.coolchoice.monumentphoto.data.*;
import com.coolchoice.monumentphoto.dal.*;

public class MonumentCommonInfoTabPage extends Fragment implements View.OnClickListener, MonumentInfoActivity.TabPageListener {	
	
	private EditText etCemetery, etRegion, etRow, etPlace, etGrave;
	
	private CheckBox cbIsOwnerLess;
	
	private Button btnAddRegion, btnAddRow, btnAddPlace, btnAddGrave;
	
	@Override
	public void onChangeTab(int fromPageNumber, int toPageNumber) {
		if(toPageNumber == MonumentInfoActivity.COMMON_INFO_TABPAGE_NUMBER){
			Monument monument = MonumentDB.getMonumentById(getActivity().getIntent().getIntExtra("Id", 0));
	        updateUI(monument);
		}
		if(fromPageNumber == MonumentInfoActivity.COMMON_INFO_TABPAGE_NUMBER){
			saveMonumentCommonInfo();			
		}		
	}
		
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
        View result = inflater.inflate(R.layout.monument_common_info_tabpage, null);
        etCemetery = (EditText) result.findViewById(R.id.etCemetery);
        etRegion = (EditText) result.findViewById(R.id.etRegion);
        etRow = (EditText) result.findViewById(R.id.etRow);
        etPlace = (EditText) result.findViewById(R.id.etPlace);
        etGrave = (EditText) result.findViewById(R.id.etGrave);
        cbIsOwnerLess = (CheckBox) result.findViewById(R.id.cbIsOwnerLess);
        
        this.btnAddRegion = (Button) result.findViewById(R.id.btnAddRegion);
        this.btnAddRow = (Button) result.findViewById(R.id.btnAddRow);
        this.btnAddPlace = (Button) result.findViewById(R.id.btnAddPlace);
        this.btnAddGrave = (Button) result.findViewById(R.id.btnAddGrave);
        this.btnAddRegion.setOnClickListener(this);
        this.btnAddRow.setOnClickListener(this);
        this.btnAddPlace.setOnClickListener(this);
        this.btnAddGrave.setOnClickListener(this);
                
        
        Monument monument = MonumentDB.getMonumentById(getActivity().getIntent().getIntExtra("Id", 0));
        updateUI(monument);
        ((MonumentInfoActivity)getActivity()).setOnTabPageListener(this);                
        return result;		
	}
	
	private void updateUI(Monument monument){
		if(monument != null){
        	etCemetery.setText(null);
        	etCemetery.setText(monument.CemeteryName);
        	etRegion.setText(monument.Region);
        	etRow.setText(monument.Row);
        	etPlace.setText(monument.Place);
        	etGrave.setText(monument.Grave);
        	cbIsOwnerLess.setChecked(monument.IsOwnerLess);
        }
	}
	
	@Override
	public void onPause(){
		super.onPause();
		saveMonumentCommonInfo();
	}
	
	public void saveMonumentCommonInfo(){
		Monument monument = MonumentDB.getMonumentById(getActivity().getIntent().getIntExtra("Id", 0));
        if(monument != null){
        	monument.CemeteryName = etCemetery.getText().toString();
        	monument.Region  = etRegion.getText().toString();
        	monument.Row = etRow.getText().toString();
        	monument.Place = etPlace.getText().toString();
        	monument.Grave = etGrave.getText().toString();
        	monument.IsOwnerLess = cbIsOwnerLess.isChecked();
        	MonumentDB.saveMonument(monument);
        }        
        
	}
	
	@Override
	public void onClick(View v) {
		saveMonumentCommonInfo();
		Monument monument = MonumentDB.getMonumentById(getActivity().getIntent().getIntExtra("Id", 0));
		boolean isAddNewMonument = false;
		if(monument.Photos.size() == 0){
			//update this record
			isAddNewMonument = false;
		} else {
			//create new
			isAddNewMonument = true;
		}		
		String newRegion = null, newRow = null, newPlace = null, newGrave = null;
		switch (v.getId()) {
		case R.id.btnAddRegion:
			if(isAddNewMonument){
				newRegion = nextString(monument.Region);
			} else{
				monument.Region = nextString(monument.Region);
			}
			break;
		case R.id.btnAddRow:
			if(isAddNewMonument){
				newRow = nextString(monument.Row);
			}else{
				monument.Row = nextString(monument.Row);
			}
			break;
		case R.id.btnAddPlace:
			if(isAddNewMonument){
				newPlace = nextString(monument.Place);
			} else {
				monument.Place = nextString(monument.Place);
			}
			break;
		case R.id.btnAddGrave:
			if(isAddNewMonument){
				newGrave = nextString(monument.Grave);
			} else{
				monument.Grave = nextString(monument.Grave);
			}			
			break;
		default:
			break;
		}
		
		if(isAddNewMonument){
			Monument newMonument = new Monument();
			newMonument.CemeteryName = monument.CemeteryName;
			newMonument.IsOwnerLess = monument.IsOwnerLess;
			newMonument.CreateDate = new Date();
			newMonument.Status = Monument.STATUS_FORMATE;
			newMonument.Photos = new ArrayList<GravePhoto>();
			if(newRegion != null){
				newMonument.Region = newRegion;
			} else{
				newMonument.Region = monument.Region;
			}
			if(newRow != null){
				newMonument.Row = newRow;
			} else {
				newMonument.Row = monument.Row;
			}
			if(newPlace != null){
				newMonument.Place = newPlace;
			} else {
				newMonument.Place = monument.Place;
			}
			if(newGrave != null){
				newMonument.Grave = newGrave;
			} else {
				newMonument.Grave = monument.Grave;
			}
			
			newMonument = MonumentDB.saveMonument(newMonument);
			((MonumentInfoActivity) getActivity()).setNewIdInExtras(newMonument.Id);
			updateUI(newMonument);
		} else {
			monument = MonumentDB.saveMonument(monument);
			updateUI(monument);
		}	
		
	}
	
	private String nextString(String s){
		String result = null;
		if(s == null || s == ""){
			result = "1";
		} else {
			char lastChar = s.charAt(s.length() - 1);
			if(Character.isDigit(lastChar)){
				int value = Integer.parseInt(Character.toString(lastChar));
				value++;
				result = s.substring(0, s.length() - 1) + Integer.toString(value);
			}else{
				result = s + "1";
			}		
		}
		return result;
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}	
	
	@Override
	public void onSaveInstanceState(Bundle bundle) {
		bundle.putString("1", etCemetery.getText().toString());
		bundle.putString("2", etRegion.getText().toString());
		bundle.putString("3", etRow.getText().toString());
		bundle.putString("4", etPlace.getText().toString());
		bundle.putString("5", etGrave.getText().toString());
		bundle.putBoolean("6", cbIsOwnerLess.isChecked());
	}
	
	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);
		if(bundle != null){
			etCemetery.setText(bundle.getString("1"));
			etRegion.setText(bundle.getString("2"));
			etRow.setText(bundle.getString("3"));
			etPlace.setText(bundle.getString("4"));
			etGrave.setText(bundle.getString("5"));
			cbIsOwnerLess.setChecked(bundle.getBoolean("6"));
		}
	}	
	
}
