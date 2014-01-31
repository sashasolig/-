package com.coolchoice.monumentphoto.data;

import java.util.Collection;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Region extends BaseDTO {
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Cemetery Cemetery;
	
	@ForeignCollectionField
	public Collection<GPSRegion> GPSRegionList;
	
	@DatabaseField
	public Date PlaceSyncDate;
	
	@DatabaseField
	public Date GraveSyncDate;
	
	@DatabaseField
	public Date BurialSyncDate;
	
	@DatabaseField
	public byte IsGPSChanged;

}
