package com.coolchoice.monumentphoto;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.coolchoice.monumentphoto.SyncTaskHandler.OperationType;
import com.coolchoice.monumentphoto.SyncTaskHandler.SyncCompleteListener;
import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.data.BaseDTO;
import com.coolchoice.monumentphoto.data.Burial;
import com.coolchoice.monumentphoto.data.Cemetery;
import com.coolchoice.monumentphoto.data.SettingsData;
import com.coolchoice.monumentphoto.task.TaskResult;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

public class BurialPlanActivity extends Activity implements SyncTaskHandler.SyncCompleteListener {
    
    public static final String EXTRA_GRAVE_ID = "graveId";

    private ListView lvBurialPlan;
    
    private TextView tvTitle;
    
    private LinearLayout llLayout;

    private static SyncTaskHandler mSyncTaskHandler;

    private Menu mOptionsMenu;
    
    private int mGraveId;

    private void updateOptionsMenu() {
        if (this.mOptionsMenu == null)
            return;
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
        String title = getTitle().toString();
        title = title + String.format("(%s)", getString(R.string.action_burial_plan));
        setTitle(title);        
        this.mGraveId = getIntent().getIntExtra(EXTRA_GRAVE_ID, BaseDTO.INT_NULL_VALUE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.burial_plan_activity);
        this.lvBurialPlan = (ListView) findViewById(R.id.lvBurials);
        this.tvTitle = (TextView) findViewById(R.id.tvTitle);
        this.llLayout = (LinearLayout) findViewById(R.id.llTitle);
        if(BaseDTO.isNullValue(this.mGraveId)){
            this.llLayout.setVisibility(View.GONE);
        } else {
            this.llLayout.setVisibility(View.VISIBLE);
        }
        if (mSyncTaskHandler == null) {
            mSyncTaskHandler = new SyncTaskHandler();
        }
        mSyncTaskHandler.setContext(this);
        mSyncTaskHandler.checkResumeDataOperation(this);
        mSyncTaskHandler.setOnSyncCompleteListener(this);
    }
    
    private void updateBurialPlanListView(){
        /*ArrayList<BurialItem> items = new ArrayList<BurialPlanActivity.BurialItem>();
        for(int i = 0; i < 15; i++){
            BurialItem item = new BurialItem();
            if(i % 4 == 0){                
                item.setSectionDate(new Date());
            } else {                
                item.setBurial(new Burial());
                item.setSectionDate(new Date());
            }
            items.add(item);
        }*/        
        RuntimeExceptionDao<Burial, Integer> burialDao = DB.dao(Burial.class); 
        List<Burial> burials = null;
        try {
            burials = burialDao.queryBuilder().orderBy(Burial.PLANDATE_COLUMN_NAME, true).where().eq(Burial.STATUS_COLUMN_NAME, Burial.StatusEnum.APPROVED).query();
        } catch (SQLException e) {            
            e.printStackTrace();
        }
        ArrayList<BurialItem> items = new ArrayList<BurialPlanActivity.BurialItem>();
        Date currentDate = null;
        if(burials.size() > 0){
            for(int i = 0; i < burials.size(); i++){
                Burial burial = burials.get(i);
                if((currentDate == null) || (burial.PlanDate.getTime() / (86400  * 1000)) != (currentDate.getTime() / (86400  * 1000))){
                    currentDate = burial.PlanDate;
                    BurialItem item = new BurialItem();
                    item.setBurial(null);
                    item.setSectionDate(currentDate);
                    items.add(item);
                }
                BurialItem item = new BurialItem();
                item.setBurial(burial);
                item.setSectionDate(null);
                items.add(item);
            }
        }
        BurialListAdapter adapter = new BurialListAdapter(items);
        this.lvBurialPlan.setAdapter(adapter);
        
    }

    @Override
    public void onComplete(OperationType operationType, TaskResult taskResult) {
        this.updateBurialPlanListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.updateBurialPlanListView();        
        registerForContextMenu(this.lvBurialPlan);
        updateOptionsMenu();
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
            if (!settingsData.IsAutoDownloadData) {
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

    private void actionSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void actionGet() {
        mSyncTaskHandler.startGetApprovedBurial();
        mSyncTaskHandler.setOnSyncCompleteListener(this);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {        
        if (v.getId() == R.id.lvCemetery) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Cemetery cemetery = (Cemetery) this.lvBurialPlan.getAdapter().getItem(info.position);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.edit_context_menu, menu);
            menu.setHeaderTitle(cemetery.Name);           
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
        case R.id.action_edit:            
            return true;
        case R.id.action_remove:            
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }    
    
    public class BurialItem {
        
        private Burial mBurial;
        
        private Date mSectionDate;
        
        public Burial getBurial() {
            return mBurial;
        }
        public void setBurial(Burial burial) {
            this.mBurial = burial;
        }
        
        public Date getSectionDate() {
            return mSectionDate;
        }
        public void setSectionDate(Date sectionDate) {
            this.mSectionDate = sectionDate;
        }
        
        public boolean isSectionItem(){
            return this.mBurial == null;
        }
        
        
        
    }

    public class BurialListAdapter extends BaseAdapter {
        SimpleDateFormat mSectionDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat mItemDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        private List<BurialItem> mItems;

        public BurialListAdapter(List<BurialItem> items) {
            this.mItems = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) BurialPlanActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            BurialItem item = this.mItems.get(position);
            if(item.isSectionItem()){
                convertView = inflater.inflate(R.layout.burial_section_list_item, parent, false);
                TextView tvSectionTitle = (TextView) convertView.findViewById(R.id.tvSectionTitle);
                tvSectionTitle.setText(" * " + mSectionDateFormat.format(item.getSectionDate()));
            } else {
                convertView = inflater.inflate(R.layout.burial_list_item, parent, false);
                if(position % 2 == 0){
                    convertView.setBackgroundColor(getResources().getColor(R.color.burial_list_item_color2));
                }
                TextView tvFIO = (TextView) convertView.findViewById(R.id.tvFIO);
                TextView tvBurialPlanDate = (TextView) convertView.findViewById(R.id.tvBurialPlanDate);
                Button btnBind = (Button) convertView.findViewById(R.id.btnBind);
                Button btnUnbind = (Button) convertView.findViewById(R.id.btnUnbind);
                Button btnClose = (Button) convertView.findViewById(R.id.btnClose);
                item.getBurial().toUpperFirstCharacterInFIO();
                tvFIO.setText(item.getBurial().getFIO());
                tvBurialPlanDate.setText(mItemDateFormat.format(item.getBurial().PlanDate));
            }
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
