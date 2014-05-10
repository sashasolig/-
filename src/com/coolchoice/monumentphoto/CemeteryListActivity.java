package com.coolchoice.monumentphoto;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.coolchoice.monumentphoto.SyncTaskHandler.OperationType;
import com.coolchoice.monumentphoto.SyncTaskHandler.SyncCompleteListener;
import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.dal.MonumentDB;
import com.coolchoice.monumentphoto.data.BaseDTO;
import com.coolchoice.monumentphoto.data.Cemetery;
import com.coolchoice.monumentphoto.data.SettingsData;
import com.coolchoice.monumentphoto.task.BaseTask;
import com.coolchoice.monumentphoto.task.TaskResult;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CemeteryListActivity extends Activity implements SyncTaskHandler.SyncCompleteListener {
	
	private ListView lvCemetery;
	
	private Button btnAddCemetery;
	
	private static Cemetery mChoosedCemetery;
	
	private static SyncTaskHandler mSyncTaskHandler;
	
	private Menu mOptionsMenu;	
	
	private void updateOptionsMenu() {
		if(this.mOptionsMenu == null) return;
		MenuItem actionGetMenuItem = this.mOptionsMenu.findItem(R.id.action_get);
        if (Settings.IsAutoDownloadData(this)) {
            actionGetMenuItem.setIcon(R.drawable.load_data_enable);           
        } else {
        	actionGetMenuItem.setIcon(R.drawable.load_data_disable); 
        }
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (getIntent().getBooleanExtra(SettingsActivity.EXTRA_EXIT, false)) {
		    finish();
		}
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.cemetery_list_activity);
		this.lvCemetery = (ListView) findViewById(R.id.lvCemetery);
		this.btnAddCemetery = (Button) findViewById(R.id.btnAddCemetery);
		
		if(mSyncTaskHandler == null){
			mSyncTaskHandler = new SyncTaskHandler();
		}
		mSyncTaskHandler.setContext(this);
		mSyncTaskHandler.checkResumeDataOperation(this);
		mSyncTaskHandler.setOnSyncCompleteListener(this);
	}
	
	@Override
	public void onComplete(OperationType operationType, TaskResult taskResult) {
		this.updateCemeteryList();		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		this.updateCemeteryList();
		this.btnAddCemetery.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CemeteryListActivity.this, AddObjectActivity.class);
				intent.putExtra(AddObjectActivity.EXTRA_TYPE, AddObjectActivity.ADD_CEMETERY);
				startActivity(intent);				
			}
		});
		registerForContextMenu(this.lvCemetery);
		updateOptionsMenu();
	}
	
	private void updateCemeteryList(){
		RuntimeExceptionDao<Cemetery, Integer> cemeteryDAO = DB.dao(Cemetery.class);
    	QueryBuilder<Cemetery, Integer> cemeteryBuilder = cemeteryDAO.queryBuilder();
		cemeteryBuilder.orderByRaw(BaseDTO.ORDER_BY_COLUMN_NAME);
		List<Cemetery> cemeteryList = new ArrayList<Cemetery>();
		try {
			cemeteryList = cemeteryDAO.query(cemeteryBuilder.prepare());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		CemeteryGridAdapter adapter = new CemeteryGridAdapter(cemeteryList);
		this.lvCemetery.setAdapter(adapter);		
		this.lvCemetery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				Cemetery cemetery = (Cemetery) lvCemetery.getAdapter().getItem(pos);
				Intent intent = new Intent(CemeteryListActivity.this, BrowserCemeteryActivity.class);
				intent.putExtra(BrowserCemeteryActivity.EXTRA_CEMETERY_ID, cemetery.Id);
				intent.putExtra(BrowserCemeteryActivity.EXTRA_TYPE, AddObjectActivity.ADD_CEMETERY);
				startActivity(intent);
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_monument_list, menu);
		this.mOptionsMenu = menu;
		this.updateOptionsMenu();
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    super.onOptionsItemSelected(item);
	    switch (item.getItemId()) {
		case R.id.action_settings:
			actionSettings();
			break;
		case R.id.action_get:
			SettingsData settingsData = Settings.getSettingData(this);
			if (!settingsData.IsAutoDownloadData){
				settingsData.IsAutoDownloadData = true;
				actionGet();
			} else {
				settingsData.IsAutoDownloadData = false;
			}
			Settings.saveSettingsData(this, settingsData);
			updateOptionsMenu();
			break;
		case R.id.action_upload:
			break;
		}	    
	    return true;
	}
	
	private void actionSettings(){
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
	private void actionGet(){
		mSyncTaskHandler.startGetCemetery();
		mSyncTaskHandler.setOnSyncCompleteListener(new SyncCompleteListener() {				
			@Override
			public void onComplete(OperationType operationType, TaskResult taskResult) {
				updateCemeteryList();					
			}
		});
	}
	
	private void actionGetAllDataByCemetery(int cemeteryServerId){
		mSyncTaskHandler.startGetOnlyChangedData(cemeteryServerId);
		mSyncTaskHandler.setOnSyncCompleteListener(new SyncCompleteListener() {				
			@Override
			public void onComplete(OperationType operationType, TaskResult taskResult) {
				updateCemeteryList();					
			}
		});
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    //super.onCreateContextMenu(menu, v, menuInfo);
	    //MenuInflater inflater = getMenuInflater();
	    //inflater.inflate(R.menu.edit_context_menu, menu);
		if (v.getId() == R.id.lvCemetery) {
		    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		    Cemetery cemetery = (Cemetery) this.lvCemetery.getAdapter().getItem(info.position);
		    MenuInflater inflater = getMenuInflater();
		    inflater.inflate(R.menu.edit_context_menu, menu);
		    menu.setHeaderTitle(cemetery.Name);
		    mChoosedCemetery = cemetery;
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();	        
	    switch (item.getItemId()) {
	        case R.id.action_edit:
	            actionCemeteryEdit(mChoosedCemetery);
	            return true;
	        case R.id.action_remove:
	        	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    	    @Override
		    	    public void onClick(DialogInterface dialog, int which) {
		    	        switch (which){
		    	        case DialogInterface.BUTTON_POSITIVE:
		    	        	MonumentDB.deleteCemetery(mChoosedCemetery.Id);
		    	        	onResume();
		    	            break;
		    	        case DialogInterface.BUTTON_NEGATIVE:
		    	            //do nothing
		    	            break;
		    	        }
		    	    }
		    	};	
		    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    	String titleConfirmDeleteDialog = String.format(getString(R.string.deleteItemQuestion), mChoosedCemetery.Name);
		    	builder.setMessage(titleConfirmDeleteDialog).setPositiveButton(getString(R.string.yes), dialogClickListener)
		    	    .setNegativeButton(getString(R.string.no), dialogClickListener).show();
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
	private void actionCemeteryEdit(Cemetery cemetery){
		Intent intent = new Intent(this, AddObjectActivity.class);
		intent.putExtra(AddObjectActivity.EXTRA_TYPE, AddObjectActivity.ADD_CEMETERY);
		intent.putExtra(AddObjectActivity.EXTRA_EDIT, true);
		intent.putExtra(AddObjectActivity.EXTRA_ID, cemetery.Id);		
		startActivity(intent);
	}
	
	
	public class CemeteryGridAdapter extends BaseAdapter {
		
		private List<Cemetery> mItems;
		
        public CemeteryGridAdapter(List<Cemetery> items) {
        	this.mItems = items;
        }        

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
            	LayoutInflater inflater = (LayoutInflater) CemeteryListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.cemetery_item, parent, false);
            }
            TextView tvIndex = (TextView) convertView.findViewById(R.id.tvIndex);
            TextView tvCemetery = (TextView) convertView.findViewById(R.id.tvCemetery);
            TextView tvCemeterySquare = (TextView) convertView.findViewById(R.id.tvCemeterySquare);
            Button btnGetDataByCemetery = (Button) convertView.findViewById(R.id.btnGetDataByCemetery);
            Cemetery cemetery = mItems.get(position); 
            btnGetDataByCemetery.setTag(cemetery.ServerId);
            if(cemetery.ServerId > 0 ){
            	btnGetDataByCemetery.setVisibility(View.VISIBLE);
            } else {
            	btnGetDataByCemetery.setVisibility(View.GONE);
            }
            if(cemetery.Square != null){
            	tvCemeterySquare.setText(String.format(getString(R.string.square), Double.toString(cemetery.Square)));
            } else {
            	tvCemeterySquare.setText(null);
            }
            String value = cemetery.Name;
            tvCemetery.setText(value);
            tvIndex.setText(Integer.toString(position + 1));
            btnGetDataByCemetery.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					int cemeteryServerId = Integer.parseInt(v.getTag().toString());
					if(cemeteryServerId > 0) {
						actionGetAllDataByCemetery(cemeteryServerId);
					}					
				}
			});
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
