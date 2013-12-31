package com.coolchoice.monumentphoto.data;

import java.util.Collection;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Place extends BaseDTO {
	
	public static final String IsOwnerLessColumnName = "IsOwnerLess";
	
	@DatabaseField
	public Double Width;
	
	@DatabaseField
	public Double Length;
	
	@DatabaseField
	public boolean IsOwnerLess;
	
	@DatabaseField
	public String OldName;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Row Row;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Region Region;
	
	@ForeignCollectionField
	public Collection<GPSPlace> GPSPlaceList;
	

}
