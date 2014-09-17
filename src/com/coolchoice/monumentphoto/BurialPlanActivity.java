package com.coolchoice.monumentphoto;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.coolchoice.monumentphoto.SyncTaskHandler.OperationType;
import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.dal.MonumentDB;
import com.coolchoice.monumentphoto.dal.UserDB;
import com.coolchoice.monumentphoto.data.BaseDTO;
import com.coolchoice.monumentphoto.data.Burial;
import com.coolchoice.monumentphoto.data.Cemetery;
import com.coolchoice.monumentphoto.data.ComplexGrave;
import com.coolchoice.monumentphoto.data.Grave;
import com.coolchoice.monumentphoto.data.ResponsibleUser;
import com.coolchoice.monumentphoto.data.SettingsData;
import com.coolchoice.monumentphoto.data.ILogable.LogOperation;
import com.coolchoice.monumentphoto.data.User;
import com.coolchoice.monumentphoto.task.TaskResult;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

public class BurialPlanActivity extends Activity implements SyncTaskHandler.SyncCompleteListener {
    
    public static final String EXTRA_GRAVE_ID = "graveId";
    
    public static final String EXTRA_IS_DOWNLOADED = "isDownloaded";

    private ListView lvBurialPlan;
    
    private TextView tvTitle;
    
    private LinearLayout llLayout;

    private static SyncTaskHandler mSyncTaskHandler;

    private Menu mOptionsMenu;
    
    private int mGraveId;
    
    private static Date mLastSyncDate = null;
    
    protected final Logger mFileLog = Logger.getLogger(BurialPlanActivity.class);

    private void updateOptionsMenu() {
        if (this.mOptionsMenu == null)
            return;
        MenuItem actionGetMenuItem = this.mOptionsMenu.findItem(R.id.action_get);
        if (Settings.IsAutoDownloadData(this)) {
            actionGetMenuItem.setIcon(R.drawable.load_data_enable);
        } else {
            actionGetMenuItem.setIcon(R.drawable.load_data_disable);
        }
        MenuItem actionBurialPlanMenuItem = this.mOptionsMenu.findItem(R.id.action_burial_plan);
        actionBurialPlanMenuItem.setIcon(R.drawable.burial_plan_enable);
    }
    
    public void setTitleActivity(){
    	User currentUser = UserDB.getCurrentUser();
        String currentUserFIO = null;
        if(currentUser != null){
        	currentUserFIO = currentUser.toString();
        } else {
        	currentUserFIO = getString(R.string.unauthorize_user);
        }
        String title = String.format("%s - %s", getString(R.string.burial_plan_activity_title), currentUserFIO);
        setTitle(title);   
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getBooleanExtra(SettingsActivity.EXTRA_EXIT, false)) {
            finish();
        }             
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
            ComplexGrave complexGrave = new ComplexGrave();
            complexGrave.loadByGraveId(this.mGraveId);
            tvTitle.setText(complexGrave.toString());
        }
        if (mSyncTaskHandler == null) {
            mSyncTaskHandler = new SyncTaskHandler();
        }
        mSyncTaskHandler.setContext(this);
        mSyncTaskHandler.checkResumeDataOperation(this);
        mSyncTaskHandler.setOnSyncCompleteListener(this);
    }
    
    private void updateBurialPlanListView(){
    	User currentUser = UserDB.getCurrentUser();
        RuntimeExceptionDao<Burial, Integer> burialDao = DB.dao(Burial.class);
        RuntimeExceptionDao<ResponsibleUser, Integer> responsibleDao = DB.dao(ResponsibleUser.class);
        QueryBuilder<Cemetery, Integer> qbCemetery = DB.dao(Cemetery.class).queryBuilder();
        List<Burial> burials = null;
        try {
        	if(currentUser != null){
        		qbCemetery.where().eq(Cemetery.ORG_ID_COLUMN, currentUser.OrgId).or().isNull(Cemetery.ORG_ID_COLUMN).or().eq(Cemetery.ORG_ID_COLUMN, BaseDTO.INT_NULL_VALUE);
        	} else {
        		qbCemetery.where().isNull(Cemetery.ORG_ID_COLUMN).or().eq(Cemetery.ORG_ID_COLUMN, BaseDTO.INT_NULL_VALUE);
        	}
            burials = burialDao.queryBuilder().join(qbCemetery).orderBy(Burial.PLANDATE_COLUMN_NAME, true).where().eq(Burial.STATUS_COLUMN_NAME, Burial.StatusEnum.APPROVED).query();
        } catch (SQLException e) {            
            e.printStackTrace();
        }
        ArrayList<BurialItem> items = new ArrayList<BurialPlanActivity.BurialItem>();
        Date currentDate = null;
        if(burials.size() > 0){
            for(int i = 0; i < burials.size(); i++){
                Burial burial = burials.get(i);
                if(burial.ResponsibleUser != null){
                    responsibleDao.refresh(burial.ResponsibleUser);
                }
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
        if(!taskResult.isError()){
            mLastSyncDate = new Date();
        }
        this.updateBurialPlanListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitleActivity();
        this.updateBurialPlanListView();
        updateOptionsMenu();
        autoGetData();
    }
    
    private void autoGetData(){
        /*Date curDate = new Date();
        if(mLastSyncDate == null || (curDate.getTime() - mLastSyncDate.getTime()) > (3 * 60 * 60 * 1000)){            
            actionGet();
        }*/
    	if(Settings.IsAutoDownloadData(this) && !getIntent().getBooleanExtra(EXTRA_IS_DOWNLOADED, false)){
    		actionGet();
    		getIntent().putExtra(EXTRA_IS_DOWNLOADED, true);
    	}
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
        case R.id.action_burial_plan:
            finish();
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

    public class BurialListAdapter extends BaseAdapter implements View.OnClickListener {
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
                Burial burial = item.getBurial();
                TextView tvFIO = (TextView) convertView.findViewById(R.id.tvFIO);
                TextView tvBurialPlanDate = (TextView) convertView.findViewById(R.id.tvBurialPlanDate);
                TextView tvResponsibleUser = (TextView) convertView.findViewById(R.id.tvResponsibleUser);
                TextView tvCurrentBinding = (TextView) convertView.findViewById(R.id.tvCurrentBinding);
                Button btnBind = (Button) convertView.findViewById(R.id.btnBind);
                Button btnUnbind = (Button) convertView.findViewById(R.id.btnUnbind);
                Button btnClose = (Button) convertView.findViewById(R.id.btnClose);
                btnBind.setTag(position);
                btnUnbind.setTag(position);
                btnClose.setTag(position);
                btnBind.setOnClickListener(this);
                btnUnbind.setOnClickListener(this);
                btnClose.setOnClickListener(this);
                burial.toUpperFirstCharacterInFIO();
                tvFIO.setText(burial.getFIO());
                tvBurialPlanDate.setText(mItemDateFormat.format(burial.PlanDate));
                if(burial.ResponsibleUser != null){
                    tvResponsibleUser.setText(String.format("Ответственный: %s", burial.ResponsibleUser.getFIO()));
                } else {
                    tvResponsibleUser.setText("Нет ответственного лица");
                }
                tvCurrentBinding.setText(MonumentDB.getCurrentAddress(burial));
                if(!BaseDTO.isNullValue(mGraveId)){
                    if(burial.Status == Burial.StatusEnum.APPROVED){                        
                        if(burial.Grave != null){
                            if(burial.Grave.Id == mGraveId){
                                btnBind.setEnabled(false);
                                btnUnbind.setEnabled(true);
                                btnClose.setEnabled(true);
                            } else {
                                btnBind.setEnabled(false);
                                btnUnbind.setEnabled(false);
                                btnClose.setEnabled(false);
                            }
                                                
                        } else {                            
                            btnBind.setEnabled(true);
                            btnUnbind.setEnabled(false);
                            btnClose.setEnabled(false);   
                        }
                    } else {
                        btnBind.setEnabled(false);
                        btnUnbind.setEnabled(false);
                        btnClose.setEnabled(false); 
                    }
                } else {
                    btnBind.setEnabled(false);
                    btnUnbind.setEnabled(false);
                    btnClose.setEnabled(false); 
                }
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

        @Override
        public void onClick(View btn) {
            int position = (Integer) btn.getTag();
            BurialItem item = (BurialItem) getItem(position);
            Burial dbBurial = DB.dao(Burial.class).queryForId(item.getBurial().Id);
            if(dbBurial.ResponsibleUser != null){
                DB.dao(ResponsibleUser.class).queryForId(dbBurial.ResponsibleUser.Id);
            }
            Grave dbGrave = DB.dao(Grave.class).queryForId(mGraveId);            
            switch (btn.getId()) {
            case R.id.btnBind:                
                dbBurial.Grave = dbGrave;
                DB.dao(Burial.class).update(dbBurial);
                item.setBurial(dbBurial);
                break;
            case R.id.btnUnbind: 
                dbBurial.Grave = null;
                DB.dao(Burial.class).update(dbBurial);
                item.setBurial(dbBurial);
                break;
            case R.id.btnClose: 
                Calendar c = Calendar.getInstance(TimeZone.getDefault());
                c.set(Calendar.HOUR, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);                
                dbBurial.FactDate =  c.getTime();                
                dbBurial.Status = Burial.StatusEnum.CLOSED;
                dbBurial.IsChanged = 1;
                DB.dao(Burial.class).update(dbBurial);
                item.setBurial(dbBurial);
                break;
            }
            dbBurial.toLog(mFileLog, LogOperation.UPDATE);
            notifyDataSetChanged();            
        }
    }

}
