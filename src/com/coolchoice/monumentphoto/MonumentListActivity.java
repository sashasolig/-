package com.coolchoice.monumentphoto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.coolchoice.monumentphoto.dal.MonumentDB;
import com.coolchoice.monumentphoto.data.Monument;
import com.coolchoice.monumentphoto.data.GravePhoto;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MonumentListActivity extends Activity {

	private ListView lvMonuments;
	
	private MonumentsArrayAdapter mAdapter;
	
	private Button btnSettings, btnNextPoint;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.monument_list_activity);
		this.lvMonuments = (ListView) findViewById(R.id.listViewMonuments);
		this.btnSettings = (Button) findViewById(R.id.btnSettings);
		this.btnNextPoint = (Button) findViewById(R.id.btnNextPoint);		
		
		this.btnNextPoint.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				actionNextPoint();				
			}
		});
		
		this.btnSettings.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				actionSettings();				
			}
		});
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		List<Monument> monuments = MonumentDB.getMonuments();
		this.mAdapter = new MonumentsArrayAdapter(this, monuments);
		this.lvMonuments.setAdapter(this.mAdapter);
		this.lvMonuments.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Monument monument = mAdapter.getItem(position);
				Intent intent = new Intent(MonumentListActivity.this,  MonumentInfoActivity.class);
				intent.putExtra("Id", monument.Id);
				startActivity(intent);				
			}
		});
	}
	
	private void actionSettings(){
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
	private void actionNextPoint(){
		Monument monument = new Monument();
		monument.CreateDate = new Date();
		monument.Photos = new ArrayList<GravePhoto>();
		monument = MonumentDB.saveMonument(monument);
		Intent intent = new Intent(this,  MonumentInfoActivity.class);
		intent.putExtra("Id", monument.Id);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_monument_list, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    super.onOptionsItemSelected(item);
	    switch (item.getItemId()) {
		case R.id.action_settings:
			actionSettings();
			break;
		}	    
	    return true;
	}
	
	class MonumentsArrayAdapter extends ArrayAdapter<Monument> {
	    private final Context context;
	    private final List<Monument> monuments;

	    public MonumentsArrayAdapter(Context context, List<Monument> monuments) {
	        super(context, R.layout.monument_list_activity, monuments);
	        this.context = context;
	        this.monuments = monuments;
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        View rowView = inflater.inflate(R.layout.monument_list_item, parent, false);
	        ImageView ivStatus = (ImageView) rowView.findViewById(R.id.ivStatus);
	        TextView tvRegion = (TextView) rowView.findViewById(R.id.tvRegion);
	        TextView tvRow = (TextView) rowView.findViewById(R.id.tvRow);
	        TextView tvPlace = (TextView) rowView.findViewById(R.id.tvPlace);
	        TextView tvGrave = (TextView) rowView.findViewById(R.id.tvGrave);
	        TextView tvPhotoCount = (TextView) rowView.findViewById(R.id.tvPhotoCount);
	        Monument monument = getItem(position);
	        tvRegion.setText(String.format("Участок: %s", monument.Region));
	        tvRow.setText(String.format("Ряд: %s", monument.Row));
	        tvPlace.setText(String.format("Место: %s", monument.Place));
	        tvGrave.setText(String.format("Могила: %s", monument.Grave));
	        tvPhotoCount.setText(String.format("Фото: %d", monument.Photos.size()));
	        ivStatus.getDrawable().setLevel(monument.Status);
	        return rowView;
	    }
	}


}
