package com.coolchoice.monumentphoto.data;

import java.util.Collection;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Grave extends BaseDTO {
	
	@DatabaseField
	public boolean IsMilitary;

	@DatabaseField
	public boolean IsWrongFIO;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Place Place;
	
	@ForeignCollectionField
	public Collection<GravePhoto> Photos;
	
	@ForeignCollectionField
	public Collection<GPSGrave> GPSGraveList;
	
	@ForeignCollectionField
	public Collection<Burial> BurialList;	

}
