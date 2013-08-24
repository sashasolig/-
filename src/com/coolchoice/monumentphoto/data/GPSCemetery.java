package com.coolchoice.monumentphoto.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class GPSCemetery extends GPS {
	
	public GPSCemetery() {
	}

	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Cemetery Cemetery;
	

}
