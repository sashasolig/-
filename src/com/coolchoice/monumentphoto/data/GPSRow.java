package com.coolchoice.monumentphoto.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class GPSRow extends GPS {
	
	public GPSRow() {
	}
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, index = true)
	public Row Row;

}
