package com.coolchoice.monumentphoto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.coolchoice.monumentphoto.MonumentListActivity.MonumentsArrayAdapter;
import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.dal.MonumentDB;
import com.coolchoice.monumentphoto.data.Monument;
import com.coolchoice.monumentphoto.data.GravePhoto;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MonumentListTabPage extends Fragment implements MonumentInfoActivity.TabPageListener {
	
	private ListView lvMonuments;
	
	private MonumentsArrayAdapter mAdapter;
	
	private Button btnSettings, btnNextPoint;
	
	@Override
	public void onChangeTab(int fromPageNumber, int toPageNumber) {
		if(fromPageNumber == MonumentInfoActivity.MONUMENT_LIST_TABPAGE_NUMBER){
			int monumentId = getActivity().getIntent().getIntExtra("Id", -1);
			Monument monument = DB.dao(Monument.class).queryForId(monumentId);
			if(monument == null){
				monument = new Monument();
				monument.CreateDate = new Date();
				monument.Photos = new ArrayList<GravePhoto>();
				monument = MonumentDB.saveMonument(monument);
				((MonumentInfoActivity)getActivity()).setNewIdInExtras(monument.Id);
			}
		}
		if(toPageNumber == MonumentInfoActivity.MONUMENT_LIST_TABPAGE_NUMBER){
			Monument monument = MonumentDB.getMonumentById(getActivity().getIntent().getIntExtra("Id", 0));
			if(monument != null){
				if(isNullOrEmpty(monument.CemeteryName) && isNullOrEmpty(monument.Region) &&
						isNullOrEmpty(monument.Row) && isNullOrEmpty(monument.Place) &&
						isNullOrEmpty(monument.Grave) && monument.Photos.size() == 0){
					DB.dao(Monument.class).deleteById(monument.Id);
				}
			}
			updateMonumentList();
		}		
	}
	
	public static boolean isNullOrEmpty(String s) {
		return s == null || s == "";		
	} 
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.monument_list_activity, null);
		this.lvMonuments = (ListView) result.findViewById(R.id.listViewMonuments);
		this.btnSettings = (Button) result.findViewById(R.id.btnSettings);
		this.btnNextPoint = (Button) result.findViewById(R.id.btnNextPoint);		
		
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
		((MonumentInfoActivity)getActivity()).setOnTabPageListener(this);
		return result;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		updateMonumentList();
	}
	
	private void updateMonumentList(){
		List<Monument> monuments = MonumentDB.getMonuments();
		this.mAdapter = new MonumentsArrayAdapter(getActivity(), monuments);
		this.lvMonuments.setAdapter(this.mAdapter);
		this.lvMonuments.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Monument monument = mAdapter.getItem(position);
				((MonumentInfoActivity) getActivity()).setNewIdInExtras(monument.Id);
				((MonumentInfoActivity) getActivity()).setActiveTabPage(MonumentInfoActivity.COMMON_INFO_TABPAGE_NUMBER);
				
			}
		});

	}
	
	private void actionSettings(){
		Intent intent = new Intent(getActivity(), SettingsActivity.class);
		startActivity(intent);
	}
	
	private void actionNextPoint(){
		Monument monument = new Monument();
		monument.CreateDate = new Date();
		monument.Photos = new ArrayList<GravePhoto>();
		monument = MonumentDB.saveMonument(monument);
		
		((MonumentInfoActivity) getActivity()).setNewIdInExtras(monument.Id);
		((MonumentInfoActivity) getActivity()).setActiveTabPage(MonumentInfoActivity.COMMON_INFO_TABPAGE_NUMBER);		
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
	        TextView tvCemeteryName = (TextView) rowView.findViewById(R.id.tvCemeteryName);
	        TextView tvCreateDate = (TextView) rowView.findViewById(R.id.tvCreateDate);
	        TextView tvRegion = (TextView) rowView.findViewById(R.id.tvRegion);
	        TextView tvRow = (TextView) rowView.findViewById(R.id.tvRow);
	        TextView tvPlace = (TextView) rowView.findViewById(R.id.tvPlace);
	        TextView tvGrave = (TextView) rowView.findViewById(R.id.tvGrave);
	        TextView tvPhotoCount = (TextView) rowView.findViewById(R.id.tvPhotoCount);
	        TextView tvIsOwnerLess = (TextView) rowView.findViewById(R.id.tvOwnerLess);
	        Monument monument = getItem(position);
	        tvCemeteryName.setText(String.format("��������: %s", monument.CemeteryName));
	        if(monument.CreateDate != null){
	        	SimpleDateFormat formatterDate = new SimpleDateFormat("HH:mm dd.MM.yyyy");
	        	tvCreateDate.setText(formatterDate.format(monument.CreateDate));
	        }	        
	        tvRegion.setText(String.format("�������: %s", monument.Region));
	        tvRow.setText(String.format("���: %s", monument.Row));
	        tvPlace.setText(String.format("�����: %s", monument.Place));
	        tvGrave.setText(String.format("������: %s", monument.Grave));
	        tvPhotoCount.setText(String.format("����: %d", monument.Photos.size()));
	        ivStatus.getDrawable().setLevel(monument.Status);
	        if(monument.IsOwnerLess){
	        	tvIsOwnerLess.setVisibility(View.VISIBLE);
	        } else {
	        	tvIsOwnerLess.setVisibility(View.GONE);
	        }
	        return rowView;
	    }
	}

}
