package com.coolchoice.monumentphoto.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class GPSGrave extends GPS {
	
	public GPSGrave() {
	}
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Grave Grave;

}
