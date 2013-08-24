package com.coolchoice.monumentphoto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;


public class MonumentInfoActivity extends FragmentActivity implements LocationListener {
	
	private static final String TABNAME_MONUMENT_LIST = "monument_list";
	private static final String TABNAME_COMMON_INFO = "common_info";
	private static final String TABNAME_PHOTO_INFO = "photo_info";
	
	public interface TabPageListener{
		void onChangeTab(int fromPageNumber, int toPageNumber);
	}
	
	private ArrayList<TabPageListener> tabPageListeners = new ArrayList<MonumentInfoActivity.TabPageListener>();
	
	public void setOnTabPageListener(TabPageListener tabPageListener){
		Class<?> curClass = tabPageListener.getClass();
		int index = -1;
		for(int i = 0; i < this.tabPageListeners.size(); i++){
			if(this.tabPageListeners.get(i).getClass().equals(curClass)){
				index = i;
				break;
			}
		}
		if(index >=0){
			this.tabPageListeners.remove(index);
		}
		this.tabPageListeners.add(tabPageListener);
	}
	
	public void onChangeTabPage(int fromPageNumber, int toPageNumber){
		if(this.tabPageListeners.size() > 3 ){
			Toast.makeText(MonumentInfoActivity.this, Integer.toString(this.tabPageListeners.size()) , Toast.LENGTH_SHORT).show();
		}
		Class<?> currentClass = null;
		switch(fromPageNumber) {
		case MONUMENT_LIST_TABPAGE_NUMBER:
			currentClass = MonumentListTabPage.class;
			break;
		case COMMON_INFO_TABPAGE_NUMBER:
			currentClass = MonumentCommonInfoTabPage.class;
			break;
		case PHOTO_INFO_TABPAGE_NUMBER:
			currentClass = MonumentPhotoInfoTabPage.class;
			break;
		}
		int index = -1;
		int j = 0;
		for(TabPageListener tabPageListener : this.tabPageListeners){
			if(tabPageListener.getClass().equals(currentClass)){
				index = j;
				break;
			}			
			j++;
		}
		if(index >= 0 ){
			this.tabPageListeners.get(index).onChangeTab(fromPageNumber, toPageNumber);
		}
		for(int i = 0; i<this.tabPageListeners.size(); i++ ){
			if(i == index) continue;
			this.tabPageListeners.get(i).onChangeTab(fromPageNumber, toPageNumber);			
		}
	}
	
	public static final int MONUMENT_LIST_TABPAGE_NUMBER = 0;
	public static final int COMMON_INFO_TABPAGE_NUMBER = 1;
	public static final int PHOTO_INFO_TABPAGE_NUMBER = 2;
	
	private static int lastSelectedTabPageNumber = 0;

	private TabHost mTabHost;
	
	private ViewPager mViewPager;
	
	private TabsAdapter mTabsAdapter;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.monument_info_activity);
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
		
		View view = LayoutInflater.from(mTabHost.getContext()).inflate(R.layout.tabs_bg, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText("C����� �����");
        mTabsAdapter.addTab(mTabHost.newTabSpec(TABNAME_MONUMENT_LIST).setIndicator(view), MonumentListTabPage.class, getIntent().getExtras());
        
		
        view = LayoutInflater.from(mTabHost.getContext()).inflate(R.layout.tabs_bg, null);
        tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText("��������");
        mTabsAdapter.addTab(mTabHost.newTabSpec(TABNAME_COMMON_INFO).setIndicator(view), MonumentCommonInfoTabPage.class, getIntent().getExtras());
        
        view = LayoutInflater.from(mTabHost.getContext()).inflate(R.layout.tabs_bg, null);
        tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText("����");        
        mTabsAdapter.addTab(mTabHost.newTabSpec(TABNAME_PHOTO_INFO).setIndicator(view), MonumentPhotoInfoTabPage.class, getIntent().getExtras());
        
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			
			@Override
			public void onTabChanged(String tabId) {
				int currentTabId = mTabHost.getCurrentTab();
				mTabsAdapter.getItem(currentTabId).isVisible();
				mTabHost.setCurrentTab(currentTabId);
				mViewPager.setCurrentItem(currentTabId);
				
				//Toast.makeText(MonumentInfoActivity.this, String.format("cur=%d;tabId=%s", currentTabId, tabId), Toast.LENGTH_LONG).show();
				if(lastSelectedTabPageNumber != currentTabId) {
					MonumentInfoActivity.this.onChangeTabPage(lastSelectedTabPageNumber, currentTabId);					
					lastSelectedTabPageNumber = currentTabId;
				}
				
				
			}
		});
        
        /*mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
						
			
			@Override
			public void onPageSelected(int pageNumber) {
				mTabsAdapter.getItem(pageNumber).isVisible();
				mTabHost.setCurrentTab(pageNumber);
				if(lastSelectedTabPageNumber != pageNumber) {
					MonumentInfoActivity.this.onChangeTabPage(lastSelectedTabPageNumber, pageNumber);
					updateAccessebilityTabs(pageNumber);
					lastSelectedTabPageNumber = pageNumber;
				}				
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
								
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {				
				
			}
		});
		updateAccessebilityTabs(mTabHost.getCurrentTab());*/		
	}
	
	@Override
	public void onResume(){
		super.onResume();
			
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		int gpsInterval = Settings.getGPSInterval(this);
        Log.i("GPSTimeUpdate", Integer.toString(gpsInterval) + " second");
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsInterval * 1000, 0, this);
		if(Settings.getCurrentLocation() == null){			
			Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		    Settings.setCurrentLocation(lastKnownLocation);			
		}
	}
			
	@Override
	public void onLocationChanged(Location location) {
		if(Settings.isBetterLocation(location, Settings.getCurrentLocation())){
    		Settings.setCurrentLocation(location);
    	}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}	
	
	public void setNewIdInExtras(int newId){
		getIntent().removeExtra("Id");
		getIntent().putExtra("Id", newId);
	}
	
	public void setActiveTabPage(int tabId){
		this.mTabHost.setCurrentTab(tabId);
	}
	
	public void updateAccessebilityTabs(int currentTabId){
		/*if(currentTabId == MONUMENT_LIST_TABPAGE_NUMBER){
			mTabHost.getTabWidget().getChildTabViewAt(COMMON_INFO_TABPAGE_NUMBER).setEnabled(false);
			mTabHost.getTabWidget().getChildTabViewAt(PHOTO_INFO_TABPAGE_NUMBER).setEnabled(false);			
		} else {
			mTabHost.getTabWidget().getChildTabViewAt(COMMON_INFO_TABPAGE_NUMBER).setEnabled(true);
			mTabHost.getTabWidget().getChildTabViewAt(PHOTO_INFO_TABPAGE_NUMBER).setEnabled(true);			
		}*/
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("tab", mTabHost.getCurrentTabTag());		
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
			//actionSettings();
			break;
		}	    
	    return true;
	}
	
	
	

    /**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between pages.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct paged in the ViewPager whenever the selected
     * tab changes.
     */
	public static class TabsAdapter extends FragmentPagerAdapter implements
			TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final TabHost mTabHost;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        /**
         * ����� ��� �������� ���������� � ����������� �������������, ������� ���������� ���������� �� ��������
         *
         */
		static final class TabInfo {
			private final String tag;
			private final Class<?> clss;
			private final Bundle args;

			TabInfo(String _tag, Class<?> _class, Bundle _args) {
				tag = _tag;
				clss = _class;
				args = _args;
			}
		}

        /**
         * ������� ��� ������������ View ������������� � TabsAdapter
         *
         */
		static class DummyTabFactory implements TabHost.TabContentFactory {
			private final Context mContext;

			public DummyTabFactory(Context context) {
				mContext = context;
			}

			@Override
			public View createTabContent(String tag) {
				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}

		public TabsAdapter(FragmentActivity activity, TabHost tabHost,
				ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mTabHost = tabHost;
			mViewPager = pager;
			mTabHost.setOnTabChangedListener(this);
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
			tabSpec.setContent(new DummyTabFactory(mContext));
			String tag = tabSpec.getTag();

			TabInfo info = new TabInfo(tag, clss, args);
			mTabs.add(info);
			mTabHost.addTab(tabSpec);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			return Fragment.instantiate(mContext, info.clss.getName(),
					info.args);
		}

		@Override
		public void onTabChanged(String tabId) {
			int position = mTabHost.getCurrentTab();
			mViewPager.setCurrentItem(position);			
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			// Unfortunately when TabHost changes the current tab, it kindly
			// also takes care of putting focus on it when not in touch mode.
			// The jerk.
			// This hack tries to prevent this from pulling focus out of our
			// ViewPager.
			TabWidget widget = mTabHost.getTabWidget();
			int oldFocusability = widget.getDescendantFocusability();
			widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			mTabHost.setCurrentTab(position);
			widget.setDescendantFocusability(oldFocusability);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}
	}

}
