package com.coolchoice.monumentphoto;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coolchoice.monumentphoto.dal.MonumentDB;
import com.coolchoice.monumentphoto.data.ComplexGrave;

public class PlaceSearchActivity extends Activity {
	
	private ListView lvFIO;
	
	private Button btnOk, btnCancel;
	
	private EditText etFIO;
	
	private ProgressBar pbSearch;
	
	private TextView tvResult;
	
	public static final String EXTRA_CEMETERY_ID = "cemetery_id";
	
	public static final String EDITTEXT_FIO = "FIO";
	
	public static final String EXTRA_PLACE_ID = "place_id";
	
	public static final String EXTRA_PLACE_NAME = "place_name";
	
	public static final String EXTRA_PLACE_OLDNAME = "place_old_name";
	
	public static final int PLACE_SEARCH_REQUESTCODE = 789;
	
	private int mCemeteryId;
	
	private static List<ComplexGrave.PlaceWithFIO> mListPlaceWithFIO;
	
	private String mLastSearchFIO;
	
	private MonumentDB mMonumentDB = new MonumentDB();
	
	private PlaceWithFIOAdapter mPlaceWithFIOAdapter = null;
	
	private Thread placeSearchThread;
	
	private static final int NUMBER_LETTER_FOR_SEARCH = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place_search_activity);
		this.mCemeteryId = getIntent().getIntExtra(EXTRA_CEMETERY_ID, 0);
		this.lvFIO = (ListView) findViewById(R.id.lvFIO);
		this.etFIO = (EditText) findViewById(R.id.etLastName);
		this.btnCancel = (Button) findViewById(R.id.btnCancel);
		this.btnOk = (Button) findViewById(R.id.btnOk);
		this.tvResult = (TextView) findViewById(R.id.tvResult);
		this.pbSearch = (ProgressBar) findViewById(R.id.pbSearch);
		this.pbSearch.setVisibility(View.GONE);
		this.btnCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});
		this.btnOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_OK);
				finish();
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
				if(!s.toString().equalsIgnoreCase(mLastSearchFIO)){
					findPlace();
				}				
			}
		});
		String result = String.format("Для поиска введите не менее %d букв", NUMBER_LETTER_FOR_SEARCH);	 
	    tvResult.setText(result);
	    this.lvFIO.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	public void onItemClick (AdapterView<?> parent, View view, int position, long id){
	    		try{
	    			ComplexGrave.PlaceWithFIO placeWithFIO = mListPlaceWithFIO.get(position);
	    			Intent data = new Intent();
	    			data.putExtra(EXTRA_PLACE_ID, placeWithFIO.PlaceId);
	    			data.putExtra(EXTRA_PLACE_NAME, placeWithFIO.PlaceName);
	    			data.putExtra(EXTRA_PLACE_OLDNAME, placeWithFIO.OldPlaceName);
	    			setResult(Activity.RESULT_OK, data);
	    			finish();
	    		}catch(Exception exc){
	    			exc.printStackTrace();
	    		}	    		
	    	}
		});
	}
	
	public void showResult(){
		PlaceSearchActivity.this.runOnUiThread(new Runnable() {		                              
	         @Override
	         public void run() {		             
	             if(!isFinishing()){	            	 
            		 lvFIO.setAdapter(mPlaceWithFIOAdapter);
            		 mPlaceWithFIOAdapter.notifyDataSetChanged();
            		 String result = null;
            		 if(mLastSearchFIO != null && mLastSearchFIO.length() >= NUMBER_LETTER_FOR_SEARCH){
					     result = String.format("Найдено %s человек(a)", Integer.toString(mListPlaceWithFIO.size()));
	            	 } else {
	            		 result = String.format("Для поиска введите не менее %d букв", NUMBER_LETTER_FOR_SEARCH);
	            	 }
					 tvResult.setText(result);
	             }
	                                             
	         }
        });
	}
	
	
	public void findPlace(){
		if(placeSearchThread == null || !placeSearchThread.isAlive()){
			synchronized (this) {				
				placeSearchThread = (new Thread(new Runnable() {
	
					 @Override
					 public void run() {
						 String value = etFIO.getText().toString().toLowerCase();
						 while(!value.equals(mLastSearchFIO)){							 
							 PlaceSearchActivity.this.runOnUiThread(new Runnable() {		                              
						         @Override
						         public void run() {		             
						             if(!isFinishing()){
						            	 pbSearch.setVisibility(View.VISIBLE);
						             }
						                                             
						         }
					         });
							 
						     try {
						    	 if(value.length() >= NUMBER_LETTER_FOR_SEARCH && (mLastSearchFIO == null || value.length() >= mLastSearchFIO.length())){
							    	 mLastSearchFIO = value;			
									 mListPlaceWithFIO = mMonumentDB.getPlaceWithFIO(mCemeteryId, mLastSearchFIO);					 
									 mPlaceWithFIOAdapter = new PlaceWithFIOAdapter(mListPlaceWithFIO);
									 showResult();
						        }else {
						        	if(mLastSearchFIO != null){
						        		if(!mLastSearchFIO.startsWith(value)){
						        			mListPlaceWithFIO = new ArrayList<ComplexGrave.PlaceWithFIO>();
						        			mPlaceWithFIOAdapter = new PlaceWithFIOAdapter(mListPlaceWithFIO);
											showResult();
						        		}
						        	}
						        	mLastSearchFIO = value;
						        }
						                                
						    } catch (Exception e) {
						        e.printStackTrace();
						    }
						    value = etFIO.getText().toString().toLowerCase();
						}
					    PlaceSearchActivity.this.runOnUiThread(new Runnable() {		                              
				            @Override
				            public void run() {		             
				            	if(!isFinishing()){
				            		pbSearch.setVisibility(View.GONE);
				            	}
				                                             
				            }
					    });
					}
			    }));
				placeSearchThread.start();
			}
		}		
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    super.onSaveInstanceState(savedInstanceState);	  
	    savedInstanceState.putString(EDITTEXT_FIO, this.etFIO.getText().toString());
	    savedInstanceState.putString("mLastSearchFIO", mLastSearchFIO);
	    
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);  
	    String fio = savedInstanceState.getString(EDITTEXT_FIO);
	    mLastSearchFIO = savedInstanceState.getString("mLastSearchFIO");
	    this.etFIO.setText(fio);
	}
	
    public class PlaceWithFIOAdapter extends BaseAdapter {
		
		private List<ComplexGrave.PlaceWithFIO> mItems;
		
        public PlaceWithFIOAdapter(List<ComplexGrave.PlaceWithFIO> items) {
        	this.mItems = items;
        }        

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
            	LayoutInflater inflater = (LayoutInflater) PlaceSearchActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.place_search_item, parent, false);
            }
            TextView tvIndex = (TextView) convertView.findViewById(R.id.tvIndex);
            TextView tvPlace = (TextView) convertView.findViewById(R.id.tvPlace);
            ComplexGrave.PlaceWithFIO place = mItems.get(position);
            place.toUpperFirstCharacterInFIO();
            String placeName = String.format("%s %s %s Место:%s %s", place.LName, place.FName, place.MName, (place.PlaceName != null) ? place.PlaceName : "",
            		(place.OldPlaceName != null) ? "Старое место:" + place.OldPlaceName : "");
            tvPlace.setText(placeName);
            tvIndex.setText(Integer.toString(position + 1));            
            return convertView;
        }
        
        public final int getCount() {
            return mItems.size();
        }

        public final Object getItem(int position) {
            return mItems.get(position);
        }

        public final long getItemId(int position) {
            return position;
        }
    }	

}
