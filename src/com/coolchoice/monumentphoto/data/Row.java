package com.coolchoice.monumentphoto.data;

import java.util.Collection;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Row extends BaseDTO {
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Region Region;
	
	@ForeignCollectionField
	public Collection<GPSRow> GPSRowList;

}
