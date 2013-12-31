package com.coolchoice.monumentphoto;

import java.util.ArrayList;
import java.util.List;

import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.dal.MonumentDB;
import com.coolchoice.monumentphoto.data.Burial;
import com.coolchoice.monumentphoto.data.ComplexGrave;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class PlaceSearchActivity extends Activity {
	
	private ListView lvFIO;
	
	private Button btnOk, btnCancel;
	
	private EditText etFIO;
	
	public static final String EXTRA_CEMETERY_ID = "cemetery_id";
	
	public static final String EDITTEXT_FIO = "FIO";
	
	private int mCemeteryId;
	
	private static List<ComplexGrave.PlaceWithFIO> mListPlaceWithFIO;
	private static String mFIO;
	
	private MonumentDB mMonumentDB = new MonumentDB();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place_search_activity);
		this.mCemeteryId = getIntent().getIntExtra(EXTRA_CEMETERY_ID, 0);
		this.lvFIO = (ListView) findViewById(R.id.lvFIO);
		this.etFIO = (EditText) findViewById(R.id.etLastName);
		this.btnCancel = (Button) findViewById(R.id.btnCancel);
		this.btnOk = (Button) findViewById(R.id.btnOk);
		this.btnCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setResult(Activity.RESULT_CANCELED);				
			}
		});
		this.btnOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_OK);				
			}
		});
		
		this.etFIO.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				//Toast.makeText(PlaceSearchActivity.this, s, Toast.LENGTH_SHORT).show();
				mFIO = s.toString();
				if(mFIO != null && mFIO.length() >=3){
					mListPlaceWithFIO = mMonumentDB.getPlaceWithFIO(1, mFIO + "%");
					Toast.makeText(PlaceSearchActivity.this, Integer.toString(mListPlaceWithFIO.size()), Toast.LENGTH_SHORT).show();					
				}
				
			}
		});
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    super.onSaveInstanceState(savedInstanceState);	  
	    savedInstanceState.putString(EDITTEXT_FIO, this.etFIO.getText().toString());	  
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);  
	    String fio = savedInstanceState.getString(EDITTEXT_FIO);
	    this.etFIO.setText(fio);
	}

}
